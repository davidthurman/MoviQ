package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetPopularMoviesUseCaseTest {

    private lateinit var getPopularMoviesUseCase: GetPopularMoviesUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker

    @Before
    fun setUp() {
        fakeMovieRepository = FakeMovieRepository()
        fakeAnalyticsTracker = FakeAnalyticsTracker()
        getPopularMoviesUseCase = GetPopularMoviesUseCase(
            movieRepository = fakeMovieRepository,
            analytics = fakeAnalyticsTracker
        )
    }

    @Test
    fun `get popular movies returns success with movies`() {
        runBlocking {
            val result = getPopularMoviesUseCase.invoke()
            assertThat(result.isSuccess).isTrue()
            val movies = result.getOrNull()
            assertThat(movies).isNotNull()
            assertThat(movies?.size).isGreaterThan(0)
        }
    }

    @Test
    fun `get popular movies logs analytics event on success`() {
        runBlocking {
            val result = getPopularMoviesUseCase.invoke()
            assertThat(result.isSuccess).isTrue()
            val movies = result.getOrNull()
            assertThat(fakeAnalyticsTracker.events).containsKey("popular_movies_fetched")
            assertThat(fakeAnalyticsTracker.events["popular_movies_fetched"]?.get("count")).isEqualTo(movies?.size)
        }
    }

    @Test
    fun `get popular movies returns all movies from repository`() {
        runBlocking {
            val result = getPopularMoviesUseCase.invoke()
            assertThat(result.isSuccess).isTrue()
            val movies = result.getOrNull()
            assertThat(movies?.size).isEqualTo(fakeMovieRepository.movies.size)
        }
    }
}

