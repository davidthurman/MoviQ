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

package com.dthurman.moviesaver.data.di

import com.dthurman.moviesaver.data.DefaultMovieRepository
import com.dthurman.moviesaver.data.local.database.MovieEntity
import com.dthurman.moviesaver.data.remote.toDomain
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsMovieRepository(
        movieRepository: DefaultMovieRepository
    ): MovieRepository
}

class FakeMovieRepository @Inject constructor() : MovieRepository {

    override fun getSeenMovies(): Flow<List<Movie>> {
        return flowOf(fakeMovie)
    }

    override fun getWatchlistMovies(): Flow<List<Movie>> {
        return flowOf(fakeMovie)
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return flowOf(fakeMovie)
    }

    override suspend fun getMovieById(movieId: Int): Movie? {
        return fakeMovie.firstOrNull()
    }

    override suspend fun updateSeenStatus(
        movie: Movie,
        isSeen: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateWatchlistStatus(
        movie: Movie,
        isWatchlist: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateFavoriteStatus(
        movie: Movie,
        isFavorite: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateRating(
        movie: Movie,
        rating: Float?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getPopularMovies(): List<Movie> {
        return fakeMovies.map { it.toDomain() }
    }

    override suspend fun searchMovieByTitle(title: String): List<Movie> {
        return fakeMovies.map { it.toDomain() }
    }
}

val fakeMovies = listOf(MovieEntity(0, "Title", "", "", "", ""))
val fakeMovie = listOf(Movie(0, "Title", "", "", "", ""))
