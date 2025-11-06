package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetMovieByIdUseCaseTest {

    private lateinit var getMovieByIdUseCaseTest: GetMovieByIdUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository

    @Before
    fun setup() {
        fakeMovieRepository = FakeMovieRepository()
        fakeMovieRepository.movies.add(
            Movie(
                1,
                "Movie 1",
                "PosterUrl",
                "BackDropUrl",
                "releaseDate",
                "overview"
            )
        )
        getMovieByIdUseCaseTest = GetMovieByIdUseCase(fakeMovieRepository)
    }

    @Test
    fun `get movie by id, movie in list`() {
        runBlocking {
            val movie = getMovieByIdUseCaseTest.invoke(1).getOrNull()
            assertThat(movie?.id).isEqualTo(1)
        }
    }

    @Test
    fun `get movie by id, movie not in list`() {
        runBlocking {
            val movie = getMovieByIdUseCaseTest.invoke(99999999).getOrNull()
            assertThat(movie).isNull()
        }
    }


}