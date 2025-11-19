package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SearchMoviesUseCaseTest {

    private lateinit var searchMoviesUseCase: SearchMoviesUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker

    @Before
    fun setUp() {
        fakeMovieRepository = FakeMovieRepository()
        fakeAnalyticsTracker = FakeAnalyticsTracker()
        searchMoviesUseCase = SearchMoviesUseCase(
            movieRepository = fakeMovieRepository,
            analytics = fakeAnalyticsTracker
        )
    }

    @Test
    fun `search movies with valid query returns results`() {
        runBlocking {
            val result = searchMoviesUseCase.invoke("Interstellar")
            assertThat(result.isSuccess).isTrue()
            val movies = result.getOrNull()
            assertThat(movies).isNotNull()
            assertThat(movies?.size).isGreaterThan(0)
            assertThat(movies?.first()?.title).contains("Interstellar")
        }
    }

    @Test
    fun `search movies with partial query returns results`() {
        runBlocking {
            val result = searchMoviesUseCase.invoke("Ti")
            assertThat(result.isSuccess).isTrue()
            val movies = result.getOrNull()
            assertThat(movies).isNotNull()
            assertThat(movies?.any { it.title.contains("Titanic", ignoreCase = true) }).isTrue()
        }
    }

    @Test
    fun `search movies with case insensitive query returns results`() {
        runBlocking {
            val result = searchMoviesUseCase.invoke("titanic")
            assertThat(result.isSuccess).isTrue()
            val movies = result.getOrNull()
            assertThat(movies).isNotNull()
            assertThat(movies?.any { it.title.contains("Titanic", ignoreCase = true) }).isTrue()
        }
    }

    @Test
    fun `search movies with query too short fails`() {
        runBlocking {
            val result = searchMoviesUseCase.invoke("T")
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun `search movies with empty query fails`() {
        runBlocking {
            val result = searchMoviesUseCase.invoke("")
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun `search movies with whitespace only query fails`() {
        runBlocking {
            val result = searchMoviesUseCase.invoke("   ")
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun `search movies trims whitespace from query`() {
        runBlocking {
            val result = searchMoviesUseCase.invoke("  Titanic  ")
            assertThat(result.isSuccess).isTrue()
            val movies = result.getOrNull()
            assertThat(movies).isNotNull()
            assertThat(movies?.any { it.title.contains("Titanic", ignoreCase = true) }).isTrue()
        }
    }

    @Test
    fun `search movies with no matches returns empty list`() {
        runBlocking {
            val result = searchMoviesUseCase.invoke("NonExistentMovie12345")
            assertThat(result.isSuccess).isTrue()
            val movies = result.getOrNull()
            assertThat(movies).isNotNull()
            assertThat(movies?.size).isEqualTo(0)
        }
    }
}



