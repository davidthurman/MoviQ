package com.dthurman.moviesaver.feature_movies.data.remote.data_source

import com.dthurman.moviesaver.core.domain.model.Movie

interface MovieRemoteDataSource {
    suspend fun fetchUserMovies(userId: String): Result<List<Movie>>
    suspend fun syncMovie(userId: String, movie: Movie): Result<Unit>
    suspend fun deleteMovie(userId: String, movieId: Int): Result<Unit>
    suspend fun syncPendingChanges(userId: String, pendingMovies: List<Movie>, pendingDeletes: List<Movie>): Result<SyncResult>
}

data class SyncResult(
    val created: Int = 0,
    val updated: Int = 0,
    val deleted: Int = 0,
    val failed: Int = 0
)

