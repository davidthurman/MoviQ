package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.dthurman.moviesaver.feature_movies.domain.use_cases.UpdateSeenStatusUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AcceptRecommendationAsSeenUseCaseTest {

    private lateinit var acceptRecommendationAsSeenUseCase: AcceptRecommendationAsSeenUseCase
    private lateinit var updateSeenStatusUseCase: UpdateSeenStatusUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker

    private val testMovie = Movie(
        id = 1,
        title = "Test Movie",
        posterUrl = "",
        backdropUrl = "",
        releaseDate = "2024-01-01",
        overview = "Test overview",
        aiReason = "This is a great recommendation because..."
    )

    @Before
    fun setUp() {
        fakeMovieRepository = FakeMovieRepository()
        fakeMovieRepository.movies.clear()
        fakeAnalyticsTracker = FakeAnalyticsTracker()

        updateSeenStatusUseCase = UpdateSeenStatusUseCase(
            movieRepository = fakeMovieRepository,
            analytics = fakeAnalyticsTracker
        )

        acceptRecommendationAsSeenUseCase = AcceptRecommendationAsSeenUseCase(
            updateSeenStatusUseCase = updateSeenStatusUseCase,
            analytics = fakeAnalyticsTracker
        )
    }

    @Test
    fun `accept recommendation as seen returns success`() {
        runBlocking {
            val result = acceptRecommendationAsSeenUseCase.invoke(testMovie)

            assertThat(result.isSuccess).isTrue()
        }
    }

    @Test
    fun `accept recommendation as seen marks movie as seen`() {
        runBlocking {
            acceptRecommendationAsSeenUseCase.invoke(testMovie)

            val seenMovies = fakeMovieRepository.getSeenMovies().first()
            assertThat(seenMovies).hasSize(1)
            assertThat(seenMovies[0].id).isEqualTo(testMovie.id)
            assertThat(seenMovies[0].isSeen).isTrue()
        }
    }

    @Test
    fun `accept recommendation as seen removes AI reason`() {
        runBlocking {
            acceptRecommendationAsSeenUseCase.invoke(testMovie)

            val seenMovies = fakeMovieRepository.getSeenMovies().first()
            assertThat(seenMovies[0].aiReason).isNull()
        }
    }

    @Test
    fun `accept recommendation as seen with rating saves rating`() {
        runBlocking {
            val rating = 4.5f
            acceptRecommendationAsSeenUseCase.invoke(testMovie, rating)

            val movie = fakeMovieRepository.getMovieById(testMovie.id)
            assertThat(movie).isNotNull()
            assertThat(movie?.rating).isEqualTo(rating)
        }
    }

    @Test
    fun `accept recommendation as seen without rating has no rating`() {
        runBlocking {
            acceptRecommendationAsSeenUseCase.invoke(testMovie, null)

            val movie = fakeMovieRepository.getMovieById(testMovie.id)
            assertThat(movie).isNotNull()
            assertThat(movie?.rating).isNull()
        }
    }

    @Test
    fun `accept recommendation as seen logs analytics event`() {
        runBlocking {
            acceptRecommendationAsSeenUseCase.invoke(testMovie)

            assertThat(fakeAnalyticsTracker.events).containsKey("ai_recommendation_accepted_seen")
            val eventParams = fakeAnalyticsTracker.events["ai_recommendation_accepted_seen"]
            assertThat(eventParams).isNotNull()
            assertThat(eventParams?.get("movie_id")).isEqualTo(1)
            assertThat(eventParams?.get("rated")).isEqualTo(false)
        }
    }

    @Test
    fun `accept recommendation as seen with rating logs rated as true`() {
        runBlocking {
            acceptRecommendationAsSeenUseCase.invoke(testMovie, 4.0f)

            val eventParams = fakeAnalyticsTracker.events["ai_recommendation_accepted_seen"]
            assertThat(eventParams?.get("rated")).isEqualTo(true)
        }
    }

    @Test
    fun `accept multiple recommendations as seen adds all movies`() {
        runBlocking {
            val movie1 = testMovie.copy(id = 1)
            val movie2 = testMovie.copy(id = 2)
            val movie3 = testMovie.copy(id = 3)

            acceptRecommendationAsSeenUseCase.invoke(movie1)
            acceptRecommendationAsSeenUseCase.invoke(movie2)
            acceptRecommendationAsSeenUseCase.invoke(movie3)

            val seenMovies = fakeMovieRepository.getSeenMovies().first()
            assertThat(seenMovies).hasSize(3)
        }
    }

    @Test
    fun `accept recommendation as seen with different ratings saves each correctly`() {
        runBlocking {
            val movie1 = testMovie.copy(id = 1)
            val movie2 = testMovie.copy(id = 2)
            val movie3 = testMovie.copy(id = 3)

            acceptRecommendationAsSeenUseCase.invoke(movie1, 5.0f)
            acceptRecommendationAsSeenUseCase.invoke(movie2, 3.5f)
            acceptRecommendationAsSeenUseCase.invoke(movie3, null)

            val savedMovie1 = fakeMovieRepository.getMovieById(1)
            val savedMovie2 = fakeMovieRepository.getMovieById(2)
            val savedMovie3 = fakeMovieRepository.getMovieById(3)

            assertThat(savedMovie1?.rating).isEqualTo(5.0f)
            assertThat(savedMovie2?.rating).isEqualTo(3.5f)
            assertThat(savedMovie3?.rating).isNull()
        }
    }

    @Test
    fun `accept recommendation as seen removes from watchlist if present`() {
        runBlocking {
            val movieInWatchlist = testMovie.copy(isWatchlist = true)
            fakeMovieRepository.updateWatchlistStatus(movieInWatchlist, true)

            acceptRecommendationAsSeenUseCase.invoke(movieInWatchlist)

            val watchlistMovies = fakeMovieRepository.getWatchlistMovies().first()
            assertThat(watchlistMovies).isEmpty()
            
            val seenMovies = fakeMovieRepository.getSeenMovies().first()
            assertThat(seenMovies).hasSize(1)
        }
    }

    @Test
    fun `accept recommendation as seen preserves other movie properties`() {
        runBlocking {
            acceptRecommendationAsSeenUseCase.invoke(testMovie, 4.5f)

            val seenMovies = fakeMovieRepository.getSeenMovies().first()
            assertThat(seenMovies[0].title).isEqualTo("Test Movie")
            assertThat(seenMovies[0].overview).isEqualTo("Test overview")
            assertThat(seenMovies[0].releaseDate).isEqualTo("2024-01-01")
        }
    }
}

