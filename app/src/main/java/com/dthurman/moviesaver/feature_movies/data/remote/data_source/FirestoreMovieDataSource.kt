package com.dthurman.moviesaver.feature_movies.data.remote.data_source

import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.model.SyncState
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore implementation of MovieRemoteDataSource.
 * Handles all movie-related Firestore operations.
 * Uses the unified Movie model directly - no DTO mapping needed!
 */
@Singleton
class FirestoreMovieDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val movieDao: MovieDao,
    private val errorLogger: ErrorLogger
) : MovieRemoteDataSource {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MOVIES_COLLECTION = "movies"
    }

    override suspend fun fetchUserMovies(userId: String): Result<List<Movie>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOVIES_COLLECTION)
                .get()
                .await()
            
            val movies = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Movie::class.java)?.copy(syncState = SyncState.SYNCED)
                } catch (e: Exception) {
                    errorLogger.log("Error parsing movie document: ${doc.id}")
                    errorLogger.recordException(e)
                    null
                }
            }
            
            Result.success(movies)
        } catch (e: Exception) {
            errorLogger.logNetworkError("fetchUserMovies", e)
            Result.failure(e)
        }
    }

    override suspend fun syncMovie(userId: String, movie: Movie): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOVIES_COLLECTION)
                .document(movie.id.toString())
                .set(movie)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            errorLogger.logNetworkError("syncMovie", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteMovie(userId: String, movieId: Int): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOVIES_COLLECTION)
                .document(movieId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            errorLogger.logNetworkError("deleteMovie", e)
            Result.failure(e)
        }
    }

    override suspend fun syncPendingChanges(
        userId: String,
        pendingMovies: List<Movie>,
        pendingDeletes: List<Movie>
    ): Result<SyncResult> {
        return try {
            var created = 0
            var updated = 0
            var deleted = 0
            var failed = 0
            
            // Sync creates/updates
            pendingMovies.forEach { movie ->
                try {
                    syncMovie(userId, movie).getOrThrow()
                    movieDao.updateSyncState(movie.id, SyncState.SYNCED)
                    
                    when (movie.syncState) {
                        SyncState.PENDING_CREATE -> created++
                        SyncState.PENDING_UPDATE -> updated++
                        else -> {}
                    }
                } catch (e: Exception) {
                    errorLogger.log("Failed to sync movie ${movie.id}: ${e.message}")
                    errorLogger.recordException(e)
                    movieDao.updateSyncState(movie.id, SyncState.FAILED)
                    failed++
                }
            }
            
            // Sync deletes
            pendingDeletes.forEach { movie ->
                try {
                    deleteMovie(userId, movie.id).getOrThrow()
                    movieDao.deleteMovie(movie.id)
                    deleted++
                } catch (e: Exception) {
                    errorLogger.log("Failed to delete movie ${movie.id}: ${e.message}")
                    errorLogger.recordException(e)
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
            errorLogger.logNetworkError("syncPendingChanges", e)
            Result.failure(e)
        }
    }
}

