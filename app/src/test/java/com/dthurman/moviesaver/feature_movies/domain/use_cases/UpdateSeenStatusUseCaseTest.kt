package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.data.mockMovies
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UpdateSeenStatusUseCaseTest {

    private lateinit var updateSeenStatusUseCase: UpdateSeenStatusUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker

    @Before
    fun setUp() {
        fakeMovieRepository = FakeMovieRepository()
        fakeAnalyticsTracker = FakeAnalyticsTracker()
        updateSeenStatusUseCase = UpdateSeenStatusUseCase(
            movieRepository = fakeMovieRepository,
            analytics = fakeAnalyticsTracker
        )
    }

    @Test
    fun `mark new movie as seen without rating`() {
        runBlocking {
            val newMovie = Movie(
                999,
                "New Movie",
                "posterUrl",
                "backdropUrl",
                "",
                "Overview"
            )
            updateSeenStatusUseCase.invoke(newMovie, true)
            val myMovie = fakeMovieRepository.getMovieById(999)
            assertThat(myMovie?.isSeen).isTrue()
            assertThat(myMovie?.isWatchlist).isFalse()
        }
    }

    @Test
    fun `mark new movie as seen with rating`() {
        runBlocking {
            val newMovie = Movie(
                999,
                "New Movie",
                "posterUrl",
                "backdropUrl",
                "",
                "Overview"
            )
            updateSeenStatusUseCase.invoke(newMovie, true, 4.5f)
            val myMovie = fakeMovieRepository.getMovieById(999)
            assertThat(myMovie?.isSeen).isTrue()
            assertThat(myMovie?.rating).isEqualTo(4.5f)
            assertThat(myMovie?.isWatchlist).isFalse()
        }
    }

    @Test
    fun `mark existing movie as seen`() {
        runBlocking {
            val unseenMovie = mockMovies.first { !it.isSeen }
            updateSeenStatusUseCase.invoke(unseenMovie, true, 3.5f)
            val myMovie = fakeMovieRepository.getMovieById(unseenMovie.id)
            assertThat(myMovie?.isSeen).isTrue()
            assertThat(myMovie?.rating).isEqualTo(3.5f)
        }
    }

    @Test
    fun `unmark movie as seen clears rating`() {
        runBlocking {
            val seenMovie = mockMovies.first { it.isSeen }
            updateSeenStatusUseCase.invoke(seenMovie, false)
            val myMovie = fakeMovieRepository.getMovieById(seenMovie.id)
            assertThat(myMovie?.isSeen).isFalse()
            assertThat(myMovie?.rating).isNull()
        }
    }
}

