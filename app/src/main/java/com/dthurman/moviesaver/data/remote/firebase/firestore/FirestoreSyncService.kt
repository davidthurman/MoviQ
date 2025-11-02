package com.dthurman.moviesaver.data.remote.firebase.firestore

import android.util.Log
import com.dthurman.moviesaver.data.local.MovieDao
import com.dthurman.moviesaver.data.local.MovieEntity
import com.dthurman.moviesaver.data.local.SyncState
import com.dthurman.moviesaver.domain.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val movieDao: MovieDao
) {
    
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MOVIES_COLLECTION = "movies"
        private const val CREDITS_DOCUMENT = "credits"
        private const val TAG = "FirestoreSync"
    }

    suspend fun fetchUserMovies(userId: String): Result<List<MovieEntity>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOVIES_COLLECTION)
                .get()
                .await()
            
            val movies = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreMovie::class.java)?.toEntity()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing movie document: ${e.message}")
                    null
                }
            }
            
            Log.d(TAG, "Fetched ${movies.size} movies from Firestore")
            Result.success(movies)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch movies from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Create or update user profile document in Firestore
     * This should be called when a user signs up or signs in
     */
    suspend fun createOrUpdateUserProfile(user: User): Result<Unit> {
        return try {
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .get()
                .await()
            
            if (!userDoc.exists()) {
                val firestoreUser = user.toFirestoreUser()
                firestore.collection(USERS_COLLECTION)
                    .document(user.id)
                    .set(firestoreUser)
                    .await()
                Log.d(TAG, "Created new user profile for ${user.id} with ${user.credits} credits")
            } else {
                val updates = hashMapOf<String, Any?>(
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl,
                    "lastUpdated" to FieldValue.serverTimestamp()
                )
                firestore.collection(USERS_COLLECTION)
                    .document(user.id)
                    .update(updates)
                    .await()
                Log.d(TAG, "Updated user profile for ${user.id}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create/update user profile: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Fetch complete user profile from Firestore including credits
     */
    suspend fun fetchUserProfile(userId: String): Result<User?> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val user = snapshot.toObject(FirestoreUser::class.java)?.toUser()
            Log.d(TAG, "Fetched user profile: ${user?.email}, credits: ${user?.credits}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update user credits in Firestore
     * This updates the credits field in the main user document
     */
    suspend fun updateUserCredits(userId: String, credits: Int): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "credits" to credits,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            Log.d(TAG, "Updated user credits to $credits for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user credits: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync all pending changes to Firestore
     * This processes movies marked as PENDING_CREATE, PENDING_UPDATE, and PENDING_DELETE
     */
    suspend fun syncPendingChanges(userId: String): Result<SyncResult> {
        return try {
            var created = 0
            var updated = 0
            var deleted = 0
            var failed = 0
            
            val pendingSyncMovies = movieDao.getPendingSyncMovies()
            pendingSyncMovies.forEach { movie ->
                try {
                    val firestoreMovie = movie.toFirestore()
                    firestore.collection(USERS_COLLECTION)
                        .document(userId)
                        .collection(MOVIES_COLLECTION)
                        .document(firestoreMovie.id)
                        .set(firestoreMovie)
                        .await()

                    movieDao.updateSyncState(movie.id, SyncState.SYNCED)
                    
                    when (movie.syncState) {
                        SyncState.PENDING_CREATE -> created++
                        SyncState.PENDING_UPDATE -> updated++
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync movie ${movie.title}: ${e.message}", e)
                    movieDao.updateSyncState(movie.id, SyncState.FAILED)
                    failed++
                }
            }
            
            val pendingDeleteMovies = movieDao.getPendingDeleteMovies()
            pendingDeleteMovies.forEach { movie ->
                try {
                    firestore.collection(USERS_COLLECTION)
                        .document(userId)
                        .collection(MOVIES_COLLECTION)
                        .document(movie.id.toString())
                        .delete()
                        .await()
                    
                    movieDao.deleteMovie(movie.id)
                    deleted++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete movie ${movie.title}: ${e.message}", e)
                    movieDao.updateSyncState(movie.id, SyncState.FAILED)
                    failed++
                }
            }
            
            val result = SyncResult(
                created = created,
                updated = updated,
                deleted = deleted,
                failed = failed
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error during batch sync: ${e.message}", e)
            Result.failure(e)
        }
    }

}

data class SyncResult(
    val created: Int = 0,
    val updated: Int = 0,
    val deleted: Int = 0,
    val failed: Int = 0
)


