/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dthurman.moviesaver.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "movie")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val releaseDate: String,
    val overview: String,
    val isSeen: Boolean = false,
    val isWatchlist: Boolean = false,
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val rating: Float? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val aiReason: String? = null,
    val notInterested: Boolean = false
)

@Dao
interface MovieDao {
    @Query("SELECT * FROM movie WHERE isSeen = 1 ORDER BY addedAt DESC")
    fun getSeenMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE isWatchlist = 1 ORDER BY addedAt DESC")
    fun getWatchlistMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoriteMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE id IN (:movieIds)")
    suspend fun getMoviesByIds(movieIds: List<Int>): List<MovieEntity>

    @Query("SELECT * FROM movie WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMovie(movie: MovieEntity)

    @Query("UPDATE movie SET isSeen = :isSeen, lastModified = :timestamp WHERE id = :movieId")
    suspend fun updateSeenStatus(movieId: Int, isSeen: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE movie SET isWatchlist = :isWatchlist, lastModified = :timestamp WHERE id = :movieId")
    suspend fun updateWatchlistStatus(movieId: Int, isWatchlist: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE movie SET isFavorite = :isFavorite, lastModified = :timestamp WHERE id = :movieId")
    suspend fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE movie SET rating = :rating, lastModified = :timestamp WHERE id = :movieId")
    suspend fun updateRating(movieId: Int, rating: Float?, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM movie WHERE id = :movieId AND isSeen = 0 AND isWatchlist = 0 AND isFavorite = 0")
    suspend fun deleteMovieIfNotUsed(movieId: Int)
    
    @Query("SELECT * FROM movie WHERE isSeen = 1 OR isWatchlist = 1 OR isFavorite = 1 OR rating IS NOT NULL")
    suspend fun getAllModifiedMovies(): List<MovieEntity>
    
    @Query("SELECT * FROM movie WHERE aiReason IS NOT NULL ORDER BY addedAt DESC")
    fun getRecommendations(): Flow<List<MovieEntity>>
    
    @Query("UPDATE movie SET aiReason = NULL WHERE id = :movieId")
    suspend fun clearAiReason(movieId: Int)
    
    @Query("DELETE FROM movie WHERE id = :movieId AND aiReason IS NOT NULL")
    suspend fun deleteRecommendation(movieId: Int)
    
    @Query("SELECT * FROM movie WHERE notInterested = 1")
    suspend fun getNotInterestedMovies(): List<MovieEntity>
    
    @Query("UPDATE movie SET notInterested = :notInterested, aiReason = NULL, lastModified = :timestamp WHERE id = :movieId")
    suspend fun updateNotInterested(movieId: Int, notInterested: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM movie")
    suspend fun clearAllMovies()
}
