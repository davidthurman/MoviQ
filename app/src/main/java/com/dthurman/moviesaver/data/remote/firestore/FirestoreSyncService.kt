package com.dthurman.moviesaver.data.remote.firestore

import android.util.Log
import com.dthurman.moviesaver.data.local.database.MovieEntity
import com.dthurman.moviesaver.data.local.database.UserCreditsEntity
import com.dthurman.moviesaver.domain.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MOVIES_COLLECTION = "movies"
        private const val CREDITS_DOCUMENT = "credits"
        private const val TAG = "FirestoreSync"
    }

    suspend fun syncMovie(movie: MovieEntity, userId: String): Result<Unit> {
        return try {
            val firestoreMovie = movie.toFirestore()
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOVIES_COLLECTION)
                .document(firestoreMovie.id)
                .set(firestoreMovie)
                .await()
            Log.d(TAG, "Movie synced to Firestore: ${movie.title}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync movie to Firestore: ${e.message}", e)
            Result.failure(e)
        }
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

    fun listenToMovieChanges(userId: String): Flow<List<MovieEntity>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null
        
        try {
            listenerRegistration = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOVIES_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to movies: ${error.message}", error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val movies = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(FirestoreMovie::class.java)?.toEntity()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing movie: ${e.message}")
                                null
                            }
                        }
                        trySend(movies)
                    }
                }
                
            awaitClose { listenerRegistration?.remove() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up movie listener: ${e.message}", e)
            close(e)
        }
    }

    suspend fun deleteMovie(movieId: Int, userId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOVIES_COLLECTION)
                .document(movieId.toString())
                .delete()
                .await()
            Log.d(TAG, "Movie deleted from Firestore: $movieId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete movie: ${e.message}", e)
            Result.failure(e)
        }
    }

    // User Credits Sync
    suspend fun syncUserCredits(userCredits: UserCreditsEntity): Result<Unit> {
        return try {
            val firestoreCredits = userCredits.toFirestore()
            firestore.collection(USERS_COLLECTION)
                .document(userCredits.userId)
                .collection("data")
                .document(CREDITS_DOCUMENT)
                .set(firestoreCredits)
                .await()
            Log.d(TAG, "User credits synced to Firestore: ${userCredits.credits}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync credits to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUserCredits(userId: String): Result<UserCreditsEntity?> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("data")
                .document(CREDITS_DOCUMENT)
                .get()
                .await()
            
            val credits = snapshot.toObject(FirestoreUserCredits::class.java)?.toEntity()
            Log.d(TAG, "Fetched user credits from Firestore: ${credits?.credits ?: "null"}")
            Result.success(credits)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch credits from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun listenToUserCredits(userId: String): Flow<UserCreditsEntity?> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null
        
        try {
            listenerRegistration = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("data")
                .document(CREDITS_DOCUMENT)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to credits: ${error.message}", error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val credits = snapshot.toObject(FirestoreUserCredits::class.java)?.toEntity()
                            trySend(credits)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing credits: ${e.message}")
                        }
                    } else {
                        trySend(null)
                    }
                }
                
            awaitClose { listenerRegistration?.remove() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up credits listener: ${e.message}", e)
            close(e)
        }
    }

    // User Profile Management
    /**
     * Create or update user profile document in Firestore
     * This should be called when a user signs up or signs in
     */
    suspend fun createOrUpdateUserProfile(user: User, credits: Int = 10): Result<Unit> {
        return try {
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .get()
                .await()
            
            if (!userDoc.exists()) {
                // New user - create document with initial credits
                val firestoreUser = user.toFirestoreUser(credits)
                firestore.collection(USERS_COLLECTION)
                    .document(user.id)
                    .set(firestoreUser)
                    .await()
                Log.d(TAG, "Created new user profile for ${user.id} with $credits credits")
            } else {
                // Existing user - update profile info only (preserve credits)
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
     * Fetch user credits from the main user document
     */
    suspend fun fetchUserCreditsFromProfile(userId: String): Result<Int?> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val credits = snapshot.getLong("credits")?.toInt()
            Log.d(TAG, "Fetched user credits from profile: ${credits ?: "null"}")
            Result.success(credits)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user credits from profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Listen to user credits changes from the main user document
     */
    fun listenToUserCreditsFromProfile(userId: String): Flow<Int?> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null
        
        try {
            listenerRegistration = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to user credits: ${error.message}", error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val credits = snapshot.getLong("credits")?.toInt()
                            trySend(credits)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing user credits: ${e.message}")
                            trySend(null)
                        }
                    } else {
                        trySend(null)
                    }
                }
                
            awaitClose { listenerRegistration?.remove() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up user credits listener: ${e.message}", e)
            close(e)
        }
    }

}


