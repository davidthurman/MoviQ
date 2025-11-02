package com.dthurman.moviesaver.data

import android.util.Log
import com.dthurman.moviesaver.data.local.MovieDao
import com.dthurman.moviesaver.data.remote.firebase.firestore.FirestoreSyncService
import com.dthurman.moviesaver.data.remote.themovieapi.TheMovieApi.theMovieApi
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultMovieRepository @Inject constructor(
    private val movieDao: MovieDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val authRepository: AuthRepository,
) : MovieRepository {
    
    private val TAG = "MovieRepository"
    private val syncScope = CoroutineScope(Dispatchers.IO)
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
            movieDao.insertOrUpdateMovie(movie.copy(isSeen = isSeen).toEntity())
        }
        syncToFirestore(movie.id)
    }

    override suspend fun updateWatchlistStatus(movie: Movie, isWatchlist: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateWatchlistStatus(movie.id, isWatchlist)
        } else {
            movieDao.insertOrUpdateMovie(movie.copy(isWatchlist = isWatchlist).toEntity())
        }
        syncToFirestore(movie.id)
    }

    override suspend fun updateFavoriteStatus(movie: Movie, isFavorite: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateFavoriteStatus(movie.id, isFavorite)
        } else {
            movieDao.insertOrUpdateMovie(movie.copy(isFavorite = isFavorite).toEntity())
        }
        syncToFirestore(movie.id)
    }

    override suspend fun updateRating(movie: Movie, rating: Float?) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateRating(movie.id, rating)
        } else {
            movieDao.insertOrUpdateMovie(movie.copy(rating = rating).toEntity())
        }
        syncToFirestore(movie.id)
    }

    override suspend fun getPopularMovies(): List<Movie> {
        val response = theMovieApi.getPopularMovies()
        if (response.isSuccessful) {
            val apiMovies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
            return enrichMoviesWithLocalStatus(apiMovies)
        } else {
            throw Exception("Failed to fetch movies: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun searchMovieByTitle(title: String): List<Movie> {
        val response = theMovieApi.searchMovies(query = title)
        if (response.isSuccessful) {
            val apiMovies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
            return enrichMoviesWithLocalStatus(apiMovies)
        } else {
            throw Exception("Failed to search movies: ${response.code()} ${response.message()}")
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
    
    private fun syncToFirestore(movieId: Int) {
        if (isSyncingFromCloud) {
            Log.d(TAG, "Skipping sync for movie $movieId - currently syncing from cloud")
            return
        }
        
        syncScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.id
                if (userId != null) {
                    val movieEntity = movieDao.getMovieById(movieId)
                    if (movieEntity != null) {
                        firestoreSyncService.syncMovie(movieEntity, userId)
                    }
                } else {
                    Log.w(TAG, "Cannot sync to Firestore: user not logged in")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing movie to Firestore: ${e.message}", e)
            }
        }
    }
    
    override suspend fun syncFromFirestore() {
        try {
            val userId = authRepository.getCurrentUser()?.id
            if (userId != null) {
                Log.d(TAG, "Syncing from Firestore for user: $userId")
                
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
                        
                        Log.d(TAG, "Sync complete: $updated updated, $skipped skipped (local newer)")
                    } else {
                        Log.e(TAG, "Failed to download from Firestore: ${downloadResult.exceptionOrNull()?.message}")
                    }
                } finally {
                    isSyncingFromCloud = false
                }
            }
        } catch (e: Exception) {
            isSyncingFromCloud = false
            Log.e(TAG, "Error during sync: ${e.message}", e)
        }
    }
    
    override suspend fun deleteRecommendation(movieId: Int) {
        movieDao.deleteRecommendation(movieId)
        val userId = authRepository.getCurrentUser()?.id
        if (userId != null) {
            firestoreSyncService.deleteMovie(movieId, userId)
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
        syncToFirestore(movieId)
    }
    
    override suspend fun getNotInterestedMovies(): List<Movie> {
        return movieDao.getNotInterestedMovies().map { it.toDomain() }
    }
    
    override suspend fun clearAllLocalData() {
        try {
            movieDao.clearAllMovies()
            Log.d(TAG, "Cleared all local movie data")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local movie data: ${e.message}", e)
        }
    }
}