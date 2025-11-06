package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AcceptRecommendationToWatchlistUseCaseTest {

    private lateinit var acceptRecommendationToWatchlistUseCase: AcceptRecommendationToWatchlistUseCase
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

        acceptRecommendationToWatchlistUseCase = AcceptRecommendationToWatchlistUseCase(
            movieRepository = fakeMovieRepository,
            analytics = fakeAnalyticsTracker
        )
    }

    @Test
    fun `accept recommendation to watchlist returns success`() {
        runBlocking {
            val result = acceptRecommendationToWatchlistUseCase.invoke(testMovie)

            assertThat(result.isSuccess).isTrue()
        }
    }

    @Test
    fun `accept recommendation to watchlist adds movie to watchlist`() {
        runBlocking {
            acceptRecommendationToWatchlistUseCase.invoke(testMovie)

            val watchlistMovies = fakeMovieRepository.getWatchlistMovies().first()
            assertThat(watchlistMovies).hasSize(1)
            assertThat(watchlistMovies[0].id).isEqualTo(testMovie.id)
            assertThat(watchlistMovies[0].isWatchlist).isTrue()
        }
    }

    @Test
    fun `accept recommendation to watchlist removes AI reason`() {
        runBlocking {
            acceptRecommendationToWatchlistUseCase.invoke(testMovie)

            val watchlistMovies = fakeMovieRepository.getWatchlistMovies().first()
            assertThat(watchlistMovies[0].aiReason).isNull()
        }
    }

    @Test
    fun `accept recommendation to watchlist logs analytics event`() {
        runBlocking {
            acceptRecommendationToWatchlistUseCase.invoke(testMovie)

            assertThat(fakeAnalyticsTracker.events).containsKey("ai_recommendation_accepted_watchlist")
            val eventParams = fakeAnalyticsTracker.events["ai_recommendation_accepted_watchlist"]
            assertThat(eventParams).isNotNull()
            assertThat(eventParams?.get("movie_id")).isEqualTo(1)
        }
    }

    @Test
    fun `accept multiple recommendations to watchlist adds all movies`() {
        runBlocking {
            val movie1 = testMovie.copy(id = 1)
            val movie2 = testMovie.copy(id = 2)
            val movie3 = testMovie.copy(id = 3)

            acceptRecommendationToWatchlistUseCase.invoke(movie1)
            acceptRecommendationToWatchlistUseCase.invoke(movie2)
            acceptRecommendationToWatchlistUseCase.invoke(movie3)

            val watchlistMovies = fakeMovieRepository.getWatchlistMovies().first()
            assertThat(watchlistMovies).hasSize(3)
        }
    }

    @Test
    fun `accept recommendation to watchlist preserves other movie properties`() {
        runBlocking {
            val movieWithProperties = testMovie.copy(
                rating = 4.5f,
                isSeen = false,
                isFavorite = false
            )

            acceptRecommendationToWatchlistUseCase.invoke(movieWithProperties)

            val watchlistMovies = fakeMovieRepository.getWatchlistMovies().first()
            assertThat(watchlistMovies[0].title).isEqualTo("Test Movie")
            assertThat(watchlistMovies[0].overview).isEqualTo("Test overview")
            assertThat(watchlistMovies[0].releaseDate).isEqualTo("2024-01-01")
        }
    }

    @Test
    fun `accept recommendation to watchlist logs event for each movie`() {
        runBlocking {
            val movie1 = testMovie.copy(id = 100)
            val movie2 = testMovie.copy(id = 200)

            acceptRecommendationToWatchlistUseCase.invoke(movie1)
            acceptRecommendationToWatchlistUseCase.invoke(movie2)

            assertThat(fakeAnalyticsTracker.events["ai_recommendation_accepted_watchlist"]).isNotNull()
        }
    }
}

