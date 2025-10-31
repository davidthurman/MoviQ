package com.dthurman.moviesaver.data.remote.firestore

import android.util.Log
import com.dthurman.moviesaver.data.local.database.MovieEntity
import com.dthurman.moviesaver.data.local.database.RecommendationEntity
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
        private const val MOVIES_COLLECTION = "movies"
        private const val RECOMMENDATIONS_COLLECTION = "recommendations"
        private const val TAG = "FirestoreSync"
    }

    suspend fun syncMovie(movie: MovieEntity, userId: String): Result<Unit> {
        return try {
            val firestoreMovie = movie.toFirestore(userId)
            firestore.collection(MOVIES_COLLECTION)
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
            val snapshot = firestore.collection(MOVIES_COLLECTION)
                .whereEqualTo("userId", userId)
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
            listenerRegistration = firestore.collection(MOVIES_COLLECTION)
                .whereEqualTo("userId", userId)
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

    suspend fun syncRecommendation(recommendation: RecommendationEntity, userId: String): Result<Unit> {
        return try {
            val firestoreRec = recommendation.toFirestore(userId)
            firestore.collection(RECOMMENDATIONS_COLLECTION)
                .document(firestoreRec.id)
                .set(firestoreRec)
                .await()
            Log.d(TAG, "Recommendation synced to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync recommendation: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUserRecommendations(userId: String): Result<List<RecommendationEntity>> {
        return try {
            val snapshot = firestore.collection(RECOMMENDATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("orderIndex")
                .get()
                .await()
            
            val recommendations = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreRecommendation::class.java)?.toEntity()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing recommendation: ${e.message}")
                    null
                }
            }
            
            Log.d(TAG, "Fetched ${recommendations.size} recommendations from Firestore")
            Result.success(recommendations)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch recommendations: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteMovie(movieId: Int, userId: String): Result<Unit> {
        return try {
            val docId = "${userId}_${movieId}"
            firestore.collection(MOVIES_COLLECTION)
                .document(docId)
                .delete()
                .await()
            Log.d(TAG, "Movie deleted from Firestore: $movieId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete movie: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun clearAllRecommendations(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(RECOMMENDATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            Log.d(TAG, "Cleared all recommendations from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear recommendations: ${e.message}", e)
            Result.failure(e)
        }
    }
}

