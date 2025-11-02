package com.dthurman.moviesaver.data.credits

import android.util.Log
import com.dthurman.moviesaver.data.local.database.UserCreditsDao
import com.dthurman.moviesaver.data.local.database.UserCreditsEntity
import com.dthurman.moviesaver.data.remote.firestore.FirestoreSyncService
import com.dthurman.moviesaver.domain.repository.CreditsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditsRepositoryImpl @Inject constructor(
    private val userCreditsDao: UserCreditsDao,
    private val firestoreSyncService: FirestoreSyncService
) : CreditsRepository {

    companion object {
        private const val TAG = "CreditsRepository"
    }

    override fun getCreditsFlow(userId: String): Flow<Int> {
        return userCreditsDao.getCreditsFlow(userId).map { it?.credits ?: 0 }
    }

    override suspend fun getCredits(userId: String): Int {
        return userCreditsDao.getCredits(userId)?.credits ?: 0
    }

    override suspend fun deductCredit(userId: String): Boolean {
        try {
            // Attempt to deduct credit locally
            val rowsAffected = userCreditsDao.deductCredit(userId)
            
            if (rowsAffected > 0) {
                // Successfully deducted, sync to Firestore
                val updatedCredits = userCreditsDao.getCredits(userId)
                if (updatedCredits != null) {
                    // Sync to both old subcollection and new user document field
                    firestoreSyncService.syncUserCredits(updatedCredits)
                    firestoreSyncService.updateUserCredits(userId, updatedCredits.credits)
                    Log.d(TAG, "Credit deducted successfully. Remaining: ${updatedCredits.credits}")
                }
                return true
            } else {
                Log.w(TAG, "No credits available to deduct")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deducting credit: ${e.message}", e)
            return false
        }
    }

    override suspend fun addCredits(userId: String, credits: Int) {
        try {
            val currentCredits = userCreditsDao.getCredits(userId)
            val newTotal = (currentCredits?.credits ?: 0) + credits
            
            val updatedCredits = UserCreditsEntity(
                userId = userId,
                credits = newTotal,
                lastUpdated = System.currentTimeMillis()
            )
            
            userCreditsDao.insertOrUpdate(updatedCredits)
            // Sync to both old subcollection and new user document field
            firestoreSyncService.syncUserCredits(updatedCredits)
            firestoreSyncService.updateUserCredits(userId, newTotal)
            
            Log.d(TAG, "Added $credits credits. New total: $newTotal")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding credits: ${e.message}", e)
        }
    }

    override suspend fun initializeCredits(userId: String, initialCredits: Int) {
        try {
            val existingCredits = userCreditsDao.getCredits(userId)
            
            if (existingCredits == null) {
                val userCredits = UserCreditsEntity(
                    userId = userId,
                    credits = initialCredits,
                    lastUpdated = System.currentTimeMillis()
                )
                
                userCreditsDao.insertOrUpdate(userCredits)
                // Sync to both old subcollection and new user document field
                firestoreSyncService.syncUserCredits(userCredits)
                firestoreSyncService.updateUserCredits(userId, initialCredits)
                
                Log.d(TAG, "Initialized credits for user $userId with $initialCredits credits")
            } else {
                Log.d(TAG, "User $userId already has credits: ${existingCredits.credits}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing credits: ${e.message}", e)
        }
    }

    override suspend fun syncFromFirestore(userId: String) {
        try {
            val result = firestoreSyncService.fetchUserCredits(userId)
            
            result.onSuccess { firestoreCredits ->
                if (firestoreCredits != null) {
                    val localCredits = userCreditsDao.getCredits(userId)
                    
                    // Use the most recently updated version
                    if (localCredits == null || firestoreCredits.lastUpdated > localCredits.lastUpdated) {
                        userCreditsDao.insertOrUpdate(firestoreCredits)
                        Log.d(TAG, "Synced credits from Firestore: ${firestoreCredits.credits}")
                    } else if (localCredits.lastUpdated > firestoreCredits.lastUpdated) {
                        // Local is newer, sync to Firestore
                        firestoreSyncService.syncUserCredits(localCredits)
                        Log.d(TAG, "Local credits newer, synced to Firestore: ${localCredits.credits}")
                    }
                } else {
                    // No credits in Firestore, initialize
                    initializeCredits(userId)
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed to sync from Firestore: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing credits: ${e.message}", e)
        }
    }
    
    override suspend fun clearAllLocalCredits() {
        try {
            userCreditsDao.clearAllCredits()
            Log.d(TAG, "Cleared all local credits data")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local credits: ${e.message}", e)
        }
    }
}

