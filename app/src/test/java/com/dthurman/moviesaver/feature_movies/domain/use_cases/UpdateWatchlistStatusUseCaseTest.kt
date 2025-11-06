package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.data.mockMovies
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UpdateWatchlistStatusUseCaseTest {

    private lateinit var updateWatchlistStatusUseCase: UpdateWatchlistStatusUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker

    @Before
    fun setUp() {
        fakeMovieRepository = FakeMovieRepository()
        fakeAnalyticsTracker = FakeAnalyticsTracker()
        updateWatchlistStatusUseCase = UpdateWatchlistStatusUseCase(
            movieRepository = fakeMovieRepository,
            analytics = fakeAnalyticsTracker
        )
    }

    @Test
    fun `add new movie to watchlist`() {
        runBlocking {
            val newMovie = Movie(
                999,
                "New Movie",
                "posterUrl",
                "backdropUrl",
                "",
                "Overview"
            )
            updateWatchlistStatusUseCase.invoke(newMovie, true)
            val myMovie = fakeMovieRepository.getMovieById(999)
            assertThat(myMovie?.isWatchlist).isTrue()
        }
    }

    @Test
    fun `update existing movie to watchlist`() {
        runBlocking {
            val existingMovie = mockMovies.first { !it.isWatchlist }
            updateWatchlistStatusUseCase.invoke(existingMovie, true)
            val myMovie = fakeMovieRepository.getMovieById(existingMovie.id)
            assertThat(myMovie?.isWatchlist).isTrue()
        }
    }

    @Test
    fun `remove movie from watchlist`() {
        runBlocking {
            val watchlistMovie = mockMovies.first { it.isWatchlist }
            updateWatchlistStatusUseCase.invoke(watchlistMovie, false)
            val myMovie = fakeMovieRepository.getMovieById(watchlistMovie.id)
            assertThat(myMovie?.isWatchlist).isFalse()
        }
    }
}

