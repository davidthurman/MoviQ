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

package com.dthurman.moviesaver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import com.dthurman.moviesaver.data.local.MovieEntity
import com.dthurman.moviesaver.data.local.MovieDao

/**
 * Unit tests for [com.dthurman.moviesaver.domain.repository.DefaultMovieRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class DefaultMovieRepositoryTest {

    @Test
    fun movies_newItemSaved_itemIsReturned() = runTest {
        val repository = DefaultMovieRepository(FakeMovieDao())

        repository.add("Repository")

        assertEquals(repository.movies.first().size, 1)
    }

}

private class FakeMovieDao : MovieDao {

    private val data = mutableListOf<MovieEntity>()

    override fun getMovies(): Flow<List<MovieEntity>> = flow {
        emit(data)
    }

    override suspend fun insertMovie(item: MovieEntity) {
        data.add(0, item)
    }
}
