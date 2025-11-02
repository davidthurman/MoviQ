package com.dthurman.moviesaver.data.sync

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages sync operations using WorkManager
 * Provides optimistic sync with guaranteed execution
 */
@Singleton
class SyncManager @Inject constructor(
    private val workManager: WorkManager
) {
    
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_WORK_NAME = "movie_sync_work"
        private const val PERIODIC_SYNC_WORK_NAME = "movie_periodic_sync"
        private const val PERIODIC_SYNC_INTERVAL_HOURS = 6L
    }

    fun triggerSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWorkRequest = OneTimeWorkRequestBuilder<MovieSyncWorker>()
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
        Log.d(TAG, "Sync work enqueued")
    }

    fun startPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val periodicSyncRequest = PeriodicWorkRequestBuilder<MovieSyncWorker>(
            PERIODIC_SYNC_INTERVAL_HOURS,
            TimeUnit.HOURS,
            30,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            periodicSyncRequest
        )
        
        Log.d(TAG, "Periodic sync scheduled")
    }

    fun stopPeriodicSync() {
        Log.d(TAG, "Stopping periodic sync")
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
    }
}

