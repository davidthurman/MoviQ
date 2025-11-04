package com.dthurman.moviesaver.feature_movies.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.feature_movies.data.remote.data_source.MovieRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MovieSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val movieRemoteDataSource: MovieRemoteDataSource,
    private val movieDao: MovieDao,
    private val userRepository: UserRepository,
    private val errorLogger: ErrorLogger
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "movie_sync_work"
    }

    override suspend fun doWork(): Result {
        return try {
            val userId = userRepository.getCurrentUser()?.id
            
            if (userId == null) {
                errorLogger.log("MovieSyncWorker: No user logged in, skipping sync")
                return Result.success()
            }
            
            val pendingSyncMovies = movieDao.getPendingSyncMovies()
            val pendingDeleteMovies = movieDao.getPendingDeleteMovies()
            
            val syncResult = movieRemoteDataSource.syncPendingChanges(userId, pendingSyncMovies, pendingDeleteMovies)
            
            if (syncResult.isSuccess) {
                val result = syncResult.getOrNull()
                errorLogger.log("Sync completed: created=${result?.created}, updated=${result?.updated}, deleted=${result?.deleted}, failed=${result?.failed}")
                Result.success()
            } else {
                val error = syncResult.exceptionOrNull()
                errorLogger.log("Sync failed (attempt ${runAttemptCount + 1}): ${error?.message}")
                error?.let { errorLogger.recordException(it) }
                
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    errorLogger.log("Sync failed after 3 attempts, giving up")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            errorLogger.log("MovieSyncWorker exception (attempt ${runAttemptCount + 1}): ${e.message}")
            errorLogger.recordException(e)
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                errorLogger.log("MovieSyncWorker failed after 3 attempts")
                Result.failure()
            }
        }
    }
}

