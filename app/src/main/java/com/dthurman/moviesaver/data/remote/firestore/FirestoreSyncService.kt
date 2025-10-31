package com.dthurman.moviesaver.data.remote.firestore

import android.util.Log
import com.dthurman.moviesaver.data.local.database.MovieEntity
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

}

