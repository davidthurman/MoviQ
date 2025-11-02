package com.dthurman.moviesaver.data

import com.dthurman.moviesaver.data.local.MovieDao
import com.dthurman.moviesaver.data.local.SyncState
import com.dthurman.moviesaver.data.remote.firebase.analytics.AnalyticsService
import com.dthurman.moviesaver.data.remote.firebase.analytics.CrashlyticsService
import com.dthurman.moviesaver.data.remote.firebase.firestore.FirestoreSyncService
import com.dthurman.moviesaver.data.remote.themovieapi.TheMovieApi.theMovieApi
import com.dthurman.moviesaver.data.sync.SyncManager
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultMovieRepository @Inject constructor(
    private val movieDao: MovieDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
    private val analyticsService: AnalyticsService,
    private val crashlyticsService: CrashlyticsService
) : MovieRepository {
    
    @Volatile
    private var isSyncingFromCloud = false

    override fun getSeenMovies(): Flow<List<Movie>> {
        return movieDao.getSeenMovies().map { items -> items.map { it.toDomain() } }
    }

    override fun getWatchlistMovies(): Flow<List<Movie>> {
        return movieDao.getWatchlistMovies().map { items -> items.map { it.toDomain() } }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return movieDao.getFavoriteMovies().map { items -> items.map { it.toDomain() } }
    }

    override suspend fun getMovieById(movieId: Int): Movie? {
        return movieDao.getMovieById(movieId)?.toDomain()
    }

    override suspend fun updateSeenStatus(movie: Movie, isSeen: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateSeenStatus(movie.id, isSeen)
            movieDao.updateWatchlistStatus(movie.id, false)
        } else {
            movieDao.insertOrUpdateMovie(
                movie.copy(isSeen = isSeen).toEntity().copy(syncState = SyncState.PENDING_CREATE)
            )
        }
        
        if (isSeen) {
            analyticsService.logMovieSaved(movie.id, movie.title)
        }
        
        triggerSync()
    }

    override suspend fun updateWatchlistStatus(movie: Movie, isWatchlist: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateWatchlistStatus(movie.id, isWatchlist)
        } else {
            movieDao.insertOrUpdateMovie(
                movie.copy(isWatchlist = isWatchlist).toEntity().copy(syncState = SyncState.PENDING_CREATE)
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
                movie.copy(isFavorite = isFavorite).toEntity().copy(syncState = SyncState.PENDING_CREATE)
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
                movie.copy(rating = rating).toEntity().copy(syncState = SyncState.PENDING_CREATE)
            )
        }
        
        if (rating != null) {
            analyticsService.logMovieRated(movie.id, movie.title, rating)
        }
        
        triggerSync()
    }

    override suspend fun getPopularMovies(): List<Movie> {
        return try {
            val response = theMovieApi.getPopularMovies()
            if (response.isSuccessful) {
                val apiMovies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                enrichMoviesWithLocalStatus(apiMovies)
            } else {
                val error = Exception("Failed to fetch movies: ${response.code()} ${response.message()}")
                crashlyticsService.logNetworkError("getPopularMovies", error)
                throw error
            }
        } catch (e: Exception) {
            crashlyticsService.logNetworkError("getPopularMovies", e)
            throw e
        }
    }

    override suspend fun searchMovieByTitle(title: String): List<Movie> {
        return try {
            if (title.isNotBlank()) {
                analyticsService.logMovieSearched(title)
            }
            
            val response = theMovieApi.searchMovies(query = title)
            if (response.isSuccessful) {
                val apiMovies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                enrichMoviesWithLocalStatus(apiMovies)
            } else {
                val error = Exception("Failed to search movies: ${response.code()} ${response.message()}")
                crashlyticsService.logNetworkError("searchMovieByTitle", error)
                throw error
            }
        } catch (e: Exception) {
            crashlyticsService.logNetworkError("searchMovieByTitle", e)
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
            val userId = authRepository.getCurrentUser()?.id
            if (userId != null) {
                isSyncingFromCloud = true

                try {
                    val downloadResult = firestoreSyncService.fetchUserMovies(userId)
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
                        crashlyticsService.log("Failed to sync from Firestore: ${downloadResult.exceptionOrNull()?.message}")
                        downloadResult.exceptionOrNull()?.let { crashlyticsService.recordException(it) }
                    }
                } finally {
                    isSyncingFromCloud = false
                }
            }
        } catch (e: Exception) {
            crashlyticsService.logDatabaseError("syncFromFirestore", e)
            isSyncingFromCloud = false
        }
    }
    
    override suspend fun clearAiReasonAndUpdateWatchlist(movie: Movie) {
        movieDao.clearAiReason(movie.id)
        updateWatchlistStatus(movie, true)
    }
    
    override suspend fun clearAiReasonAndMarkSeen(movie: Movie, rating: Float?) {
        movieDao.clearAiReason(movie.id)
        updateSeenStatus(movie, true)
        if (rating != null) {
            updateRating(movie, rating)
        }
    }
    
    override suspend fun markAsNotInterested(movieId: Int) {
        movieDao.updateNotInterested(movieId, true)
        triggerSync()
    }
    
    override suspend fun getNotInterestedMovies(): List<Movie> {
        return movieDao.getNotInterestedMovies().map { it.toDomain() }
    }
    
    override suspend fun clearAllLocalData() {
        try {
            movieDao.clearAllMovies()
        } catch (e: Exception) {
            crashlyticsService.logDatabaseError("clearAllLocalData", e)
        }
    }
    
    override suspend fun saveRecommendations(recommendations: List<Movie>) {
        val movieEntities = recommendations.map { movie ->
            movie.toEntity().copy(syncState = SyncState.PENDING_CREATE)
        }
        movieEntities.forEach { movieDao.insertOrUpdateMovie(it) }
        triggerSync()
    }
}