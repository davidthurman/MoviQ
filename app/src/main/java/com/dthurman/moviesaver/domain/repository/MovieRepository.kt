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

package com.dthurman.moviesaver.domain.repository

import com.dthurman.moviesaver.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    // Get movies by status
    fun getSeenMovies(): Flow<List<Movie>>
    fun getWatchlistMovies(): Flow<List<Movie>>
    fun getFavoriteMovies(): Flow<List<Movie>>
    suspend fun getMovieById(movieId: Int): Movie?
    suspend fun updateSeenStatus(movie: Movie, isSeen: Boolean)
    suspend fun updateWatchlistStatus(movie: Movie, isWatchlist: Boolean)
    suspend fun updateFavoriteStatus(movie: Movie, isFavorite: Boolean)
    suspend fun updateRating(movie: Movie, rating: Float?)
    suspend fun getPopularMovies(): List<Movie>
    suspend fun searchMovieByTitle(title: String): List<Movie>
}


