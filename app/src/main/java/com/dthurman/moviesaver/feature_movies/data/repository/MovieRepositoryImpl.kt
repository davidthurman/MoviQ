package com.dthurman.moviesaver.feature_movies.data.repository

import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.data.sync.SyncManager
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.model.SyncState
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_movies.data.remote.data_source.MovieRemoteDataSource
import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.MovieInformationDataSource
import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.toDomain
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val movieRemoteDataSource: MovieRemoteDataSource,
    private val movieInformationDataSource: MovieInformationDataSource,
    private val userRepository: UserRepository,
    private val syncManager: SyncManager,
    private val errorLogger: ErrorLogger
) : MovieRepository {
    
    @Volatile
    private var isSyncingFromCloud = false

    override fun getSeenMovies(): Flow<List<Movie>> {
        return movieDao.getSeenMovies()
    }

    override fun getWatchlistMovies(): Flow<List<Movie>> {
        return movieDao.getWatchlistMovies()
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return movieDao.getFavoriteMovies()
    }

    override suspend fun getMovieById(movieId: Int): Movie? {
        return movieDao.getMovieById(movieId)
    }

    override suspend fun updateSeenStatus(movie: Movie, isSeen: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateSeenStatus(movie.id, isSeen)
        } else {
            movieDao.insertOrUpdateMovie(
                movie.copy(isSeen = isSeen, syncState = SyncState.PENDING_CREATE)
            )
        }
        triggerSync()
    }

    override suspend fun updateWatchlistStatus(movie: Movie, isWatchlist: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateWatchlistStatus(movie.id, isWatchlist)
        } else {
            movieDao.insertOrUpdateMovie(
                movie.copy(isWatchlist = isWatchlist, syncState = SyncState.PENDING_CREATE)
            )
        }
        triggerSync()
    }

    override suspend fun updateFavoriteStatus(movie: Movie, isFavorite: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateFavoriteStatus(movie.id, isFavorite)
        } else {
            movieDao.insertOrUpdateMovie(
                movie.copy(isFavorite = isFavorite, syncState = SyncState.PENDING_CREATE)
            )
        }
        triggerSync()
    }

    override suspend fun updateRating(movie: Movie, rating: Float?) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateRating(movie.id, rating)
        } else {
            movieDao.insertOrUpdateMovie(
                movie.copy(rating = rating, syncState = SyncState.PENDING_CREATE)
            )
        }
        triggerSync()
    }

    override suspend fun getPopularMovies(): List<Movie> {
        return try {
            val result = movieInformationDataSource.getPopularMovies()
            val movieDtos = result.getOrThrow()
            val movies = movieDtos.map { it.toDomain() }
            enrichMoviesWithLocalStatus(movies)
        } catch (e: Exception) {
            errorLogger.logNetworkError("getPopularMovies", e)
            throw e
        }
    }

    override suspend fun searchMovieByTitle(title: String): List<Movie> {
        return try {
            val result = movieInformationDataSource.searchMovies(title)
            val movieDtos = result.getOrThrow()
            val movies = movieDtos.map { it.toDomain() }
            enrichMoviesWithLocalStatus(movies)
        } catch (e: Exception) {
            errorLogger.logNetworkError("searchMovieByTitle", e)
            throw e
        }
    }

    private suspend fun enrichMoviesWithLocalStatus(apiMovies: List<Movie>): List<Movie> {
        if (apiMovies.isEmpty()) return emptyList()
        
        val movieIds = apiMovies.map { it.id }
        val localMovies = movieDao.getMoviesByIds(movieIds)
        val localMovieMap = localMovies.associateBy { it.id }
        
        return apiMovies.map { apiMovie ->
            val localMovie = localMovieMap[apiMovie.id]
            if (localMovie != null) {
                apiMovie.copy(
                    isSeen = localMovie.isSeen,
                    isWatchlist = localMovie.isWatchlist,
                    isFavorite = localMovie.isFavorite,
                    rating = localMovie.rating
                )
            } else {
                apiMovie
            }
        }
    }

    private fun triggerSync() {
        if (isSyncingFromCloud) {
            return
        }
        syncManager.triggerSync()
    }

    override suspend fun syncFromFirestore() {
        try {
            val userId = userRepository.getCurrentUser()?.id
            if (userId != null) {
                isSyncingFromCloud = true

                try {
                    val downloadResult = movieRemoteDataSource.fetchUserMovies(userId)
                    if (downloadResult.isSuccess) {
                        val cloudMovies = downloadResult.getOrNull() ?: emptyList()
                        var updated = 0
                        var skipped = 0

                        cloudMovies.forEach { cloudMovie ->
                            val localMovie = movieDao.getMovieById(cloudMovie.id)

                            if (localMovie == null) {
                                movieDao.insertOrUpdateMovie(cloudMovie)
                                updated++
                            } else {
                                if (cloudMovie.lastModified > localMovie.lastModified) {
                                    movieDao.insertOrUpdateMovie(cloudMovie)
                                    updated++
                                } else {
                                    skipped++
                                }
                            }
                        }
                    } else {
                        errorLogger.log("Failed to sync from Firestore: ${downloadResult.exceptionOrNull()?.message}")
                        downloadResult.exceptionOrNull()?.let { errorLogger.recordException(it) }
                    }
                } finally {
                    isSyncingFromCloud = false
                }
            }
        } catch (e: Exception) {
            errorLogger.logDatabaseError("syncFromFirestore", e)
            isSyncingFromCloud = false
        }
    }
    
    override suspend fun markAsNotInterested(movieId: Int) {
        movieDao.updateNotInterested(movieId, true)
        triggerSync()
    }
    
    override suspend fun getNotInterestedMovies(): List<Movie> {
        return movieDao.getNotInterestedMovies()
    }
    
    override suspend fun clearAllLocalData() {
        try {
            movieDao.clearAllMovies()
        } catch (e: Exception) {
            errorLogger.logDatabaseError("clearAllLocalData", e)
        }
    }
    
    override suspend fun saveRecommendations(recommendations: List<Movie>) {
        val moviesToSave = recommendations.map { movie ->
            movie.copy(syncState = SyncState.PENDING_CREATE)
        }
        moviesToSave.forEach { movieDao.insertOrUpdateMovie(it) }
        triggerSync()
    }

    override fun getAiRecommendations(): Flow<List<Movie>> {
        return movieDao.getRecommendations()
    }
}