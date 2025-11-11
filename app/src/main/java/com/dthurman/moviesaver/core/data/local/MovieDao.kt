package com.dthurman.moviesaver.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.model.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movie WHERE isSeen = 1 ORDER BY addedAt DESC")
    fun getSeenMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movie WHERE isWatchlist = 1 ORDER BY addedAt DESC")
    fun getWatchlistMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movie WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoriteMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movie WHERE id IN (:movieIds)")
    suspend fun getMoviesByIds(movieIds: List<Int>): List<Movie>

    @Query("SELECT * FROM movie WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): Movie?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertOrUpdateMovie(movie: Movie)

    @Query("UPDATE movie SET isSeen = :isSeen, aiReason = NULL, lastModified = :timestamp, syncState = 'PENDING_UPDATE' WHERE id = :movieId")
    suspend fun updateSeenStatus(movieId: Int, isSeen: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE movie SET isWatchlist = :isWatchlist, aiReason = NULL, lastModified = :timestamp, syncState = 'PENDING_UPDATE' WHERE id = :movieId")
    suspend fun updateWatchlistStatus(movieId: Int, isWatchlist: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE movie SET isFavorite = :isFavorite, lastModified = :timestamp, syncState = 'PENDING_UPDATE' WHERE id = :movieId")
    suspend fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE movie SET rating = :rating, lastModified = :timestamp, syncState = 'PENDING_UPDATE' WHERE id = :movieId")
    suspend fun updateRating(movieId: Int, rating: Float?, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM movie WHERE isSeen = 1 OR isWatchlist = 1 OR isFavorite = 1 OR rating IS NOT NULL")
    suspend fun getAllModifiedMovies(): List<Movie>

    @Query("SELECT * FROM movie WHERE aiReason IS NOT NULL ORDER BY addedAt DESC")
    fun getRecommendations(): Flow<List<Movie>>

    @Query("SELECT * FROM movie WHERE notInterested = 1")
    suspend fun getNotInterestedMovies(): List<Movie>

    @Query("UPDATE movie SET notInterested = :notInterested, aiReason = NULL, lastModified = :timestamp, syncState = 'PENDING_UPDATE' WHERE id = :movieId")
    suspend fun updateNotInterested(movieId: Int, notInterested: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM movie")
    suspend fun clearAllMovies()

    @Query("SELECT * FROM movie WHERE syncState != 'SYNCED' AND syncState != 'PENDING_DELETE'")
    suspend fun getPendingSyncMovies(): List<Movie>

    @Query("SELECT * FROM movie WHERE syncState = 'PENDING_DELETE'")
    suspend fun getPendingDeleteMovies(): List<Movie>

    @Query("UPDATE movie SET syncState = :syncState WHERE id = :movieId")
    suspend fun updateSyncState(movieId: Int, syncState: SyncState)

    @Query("DELETE FROM movie WHERE id = :movieId")
    suspend fun deleteMovie(movieId: Int)
}