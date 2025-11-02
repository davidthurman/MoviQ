package com.dthurman.moviesaver.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dthurman.moviesaver.data.remote.firebase.analytics.CrashlyticsService
import com.dthurman.moviesaver.data.remote.firebase.firestore.FirestoreSyncService
import com.dthurman.moviesaver.domain.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MovieSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreSyncService: FirestoreSyncService,
    private val authRepository: AuthRepository,
    private val crashlyticsService: CrashlyticsService
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "movie_sync_work"
    }

    override suspend fun doWork(): Result {
        return try {
            val userId = authRepository.getCurrentUser()?.id
            
            if (userId == null) {
                crashlyticsService.log("MovieSyncWorker: No user logged in, skipping sync")
                return Result.success()
            }
            
            val syncResult = firestoreSyncService.syncPendingChanges(userId)
            
            if (syncResult.isSuccess) {
                val result = syncResult.getOrNull()
                crashlyticsService.log("Sync completed: created=${result?.created}, updated=${result?.updated}, deleted=${result?.deleted}, failed=${result?.failed}")
                Result.success()
            } else {
                val error = syncResult.exceptionOrNull()
                crashlyticsService.log("Sync failed (attempt ${runAttemptCount + 1}): ${error?.message}")
                error?.let { crashlyticsService.recordException(it) }
                
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    crashlyticsService.log("Sync failed after 3 attempts, giving up")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            crashlyticsService.log("MovieSyncWorker exception (attempt ${runAttemptCount + 1}): ${e.message}")
            crashlyticsService.recordException(e)
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                crashlyticsService.log("MovieSyncWorker failed after 3 attempts")
                Result.failure()
            }
        }
    }
}

