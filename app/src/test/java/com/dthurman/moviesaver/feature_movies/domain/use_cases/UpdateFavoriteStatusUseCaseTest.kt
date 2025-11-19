package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.data.mockMovies
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UpdateFavoriteStatusUseCaseTest {

    private lateinit var updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker

    @Before
    fun setUp() {
        fakeMovieRepository = FakeMovieRepository()
        fakeAnalyticsTracker = FakeAnalyticsTracker()
        updateFavoriteStatusUseCase = UpdateFavoriteStatusUseCase(
            movieRepository = fakeMovieRepository,
            analytics = fakeAnalyticsTracker
        )
    }

    @Test
    fun `mark seen movie as favorite`() {
        runBlocking {
            val seenMovie = mockMovies.first { it.isSeen && !it.isFavorite }
            val result = updateFavoriteStatusUseCase.invoke(seenMovie, true)
            assertThat(result.isSuccess).isTrue()
            val updatedMovie = fakeMovieRepository.getMovieById(seenMovie.id)
            assertThat(updatedMovie?.isFavorite).isTrue()
        }
    }

    @Test
    fun `unmark movie as favorite`() {
        runBlocking {
            val favoriteMovie = mockMovies.first { it.isFavorite }
            val result = updateFavoriteStatusUseCase.invoke(favoriteMovie, false)
            assertThat(result.isSuccess).isTrue()
            val updatedMovie = fakeMovieRepository.getMovieById(favoriteMovie.id)
            assertThat(updatedMovie?.isFavorite).isFalse()
        }
    }

    @Test
    fun `cannot favorite unseen movie`() {
        runBlocking {
            val unseenMovie = mockMovies.first { !it.isSeen }
            val result = updateFavoriteStatusUseCase.invoke(unseenMovie, true)
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        }
    }

    @Test
    fun `mark new seen movie as favorite`() {
        runBlocking {
            val newMovie = Movie(
                999,
                "New Movie",
                "posterUrl",
                "backdropUrl",
                "",
                "Overview",
                isSeen = true
            )
            val result = updateFavoriteStatusUseCase.invoke(newMovie, true)
            assertThat(result.isSuccess).isTrue()
            val updatedMovie = fakeMovieRepository.getMovieById(999)
            assertThat(updatedMovie?.isFavorite).isTrue()
        }
    }

    @Test
    fun `cannot favorite new unseen movie`() {
        runBlocking {
            val newMovie = Movie(
                999,
                "New Movie",
                "posterUrl",
                "backdropUrl",
                "",
                "Overview",
                isSeen = false
            )
            val result = updateFavoriteStatusUseCase.invoke(newMovie, true)
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        }
    }
}



