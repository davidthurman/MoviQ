package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.feature_movies.data.mockMovies
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UpdateRatingUseCaseTest {

    private lateinit var updateRatingUseCase: UpdateRatingUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker

    @Before
    fun setUp() {
        fakeMovieRepository = FakeMovieRepository()
        fakeAnalyticsTracker = FakeAnalyticsTracker()
        updateRatingUseCase = UpdateRatingUseCase(
            movieRepository = fakeMovieRepository,
            analytics = fakeAnalyticsTracker
        )
    }

    @Test
    fun `update rating for existing movie`() {
        runBlocking {
            val movie = mockMovies.first { it.isSeen }
            val result = updateRatingUseCase.invoke(movie, 4.5f)
            assertThat(result.isSuccess).isTrue()
            val updatedMovie = fakeMovieRepository.getMovieById(movie.id)
            assertThat(updatedMovie?.rating).isEqualTo(4.5f)
        }
    }

    @Test
    fun `rating validation - too low`() {
        runBlocking {
            val movie = mockMovies.first()
            val result = updateRatingUseCase.invoke(movie, -1f)
            assertThat(result.isFailure).isTrue()
        }
    }

    @Test
    fun `rating validation - too high`() {
        runBlocking {
            val movie = mockMovies.first()
            val result = updateRatingUseCase.invoke(movie, 6f)
            assertThat(result.isFailure).isTrue()
        }
    }

    @Test
    fun `rating validation - valid range`() {
        runBlocking {
            val movie = mockMovies.first()
            val result = updateRatingUseCase.invoke(movie, 3.5f)
            assertThat(result.isSuccess).isTrue()
        }
    }
}

