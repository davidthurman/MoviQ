package com.dthurman.moviesaver.core.data.sync

import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_movies.data.remote.data_source.MovieRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieSyncService @Inject constructor(
    private val movieDao: MovieDao,
    private val movieRemoteDataSource: MovieRemoteDataSource,
    private val userRepository: UserRepository,
    private val errorLogger: ErrorLogger
) {
    @Volatile
    private var isSyncingFromCloud = false

    suspend fun syncFromFirestore() {
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
                        errorLogger.log("Sync from Firestore completed: updated=$updated, skipped=$skipped")
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
            throw e
        }
    }

    fun isSyncingFromCloud(): Boolean = isSyncingFromCloud
}

