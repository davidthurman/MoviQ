package com.dthurman.moviesaver.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dthurman.moviesaver.data.remote.firebase.firestore.FirestoreSyncService
import com.dthurman.moviesaver.domain.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker that syncs pending local changes to Firestore
 * This runs in the background with guaranteed execution and automatic retries
 */
@HiltWorker
class MovieSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreSyncService: FirestoreSyncService,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "MovieSyncWorker"
        const val WORK_NAME = "movie_sync_work"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync work (attempt ${runAttemptCount + 1})")
        
        return try {
            val userId = authRepository.getCurrentUser()?.id
            
            if (userId == null) {
                Log.w(TAG, "User not logged in, skipping sync")
                // Don't retry if user is not logged in
                return Result.success()
            }
            
            val syncResult = firestoreSyncService.syncPendingChanges(userId)
            
            if (syncResult.isSuccess) {
                val result = syncResult.getOrNull()
                Log.d(TAG, "Sync completed successfully: $result")
                Result.success()
            } else {
                val error = syncResult.exceptionOrNull()
                Log.e(TAG, "Sync failed: ${error?.message}", error)
                
                // Retry with exponential backoff for network errors
                if (runAttemptCount < 3) {
                    Log.d(TAG, "Retrying sync (attempt ${runAttemptCount + 1})")
                    Result.retry()
                } else {
                    Log.e(TAG, "Max retries reached, giving up")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sync: ${e.message}", e)
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

