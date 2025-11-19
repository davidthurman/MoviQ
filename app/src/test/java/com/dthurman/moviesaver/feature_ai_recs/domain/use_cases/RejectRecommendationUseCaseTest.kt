package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_ai_recs.data.repository.FakeRecommendationRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class RejectRecommendationUseCaseTest {

    private lateinit var rejectRecommendationUseCase: RejectRecommendationUseCase
    private lateinit var fakeRecommendationRepository: FakeRecommendationRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker

    private val testRecommendations = listOf(
        Movie(
            id = 1,
            title = "Test Movie 1",
            posterUrl = "",
            backdropUrl = "",
            releaseDate = "2024-01-01",
            overview = "Test overview 1",
            aiReason = "Test reason 1"
        ),
        Movie(
            id = 2,
            title = "Test Movie 2",
            posterUrl = "",
            backdropUrl = "",
            releaseDate = "2024-01-02",
            overview = "Test overview 2",
            aiReason = "Test reason 2"
        ),
        Movie(
            id = 3,
            title = "Test Movie 3",
            posterUrl = "",
            backdropUrl = "",
            releaseDate = "2024-01-03",
            overview = "Test overview 3",
            aiReason = "Test reason 3"
        )
    )

    @Before
    fun setUp() {
        fakeRecommendationRepository = FakeRecommendationRepository()
        fakeAnalyticsTracker = FakeAnalyticsTracker()

        rejectRecommendationUseCase = RejectRecommendationUseCase(
            recommendationRepository = fakeRecommendationRepository,
            analytics = fakeAnalyticsTracker
        )
    }

    @Test
    fun `reject recommendation returns success`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            val result = rejectRecommendationUseCase.invoke(1)

            assertThat(result.isSuccess).isTrue()
        }
    }

    @Test
    fun `reject recommendation marks movie as not interested`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            rejectRecommendationUseCase.invoke(1)

            assertThat(fakeRecommendationRepository.markAsNotInterestedCallCount).isEqualTo(1)
            assertThat(fakeRecommendationRepository.lastMarkedNotInterestedId).isEqualTo(1)
        }
    }

    @Test
    fun `reject recommendation removes from saved recommendations`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            rejectRecommendationUseCase.invoke(1)

            val remainingRecommendations = fakeRecommendationRepository.getSavedRecommendations().first()
            assertThat(remainingRecommendations).hasSize(2)
            assertThat(remainingRecommendations.none { it.id == 1 }).isTrue()
        }
    }

    @Test
    fun `reject recommendation adds to not interested list`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            rejectRecommendationUseCase.invoke(1)

            val notInterestedMovies = fakeRecommendationRepository.getNotInterestedMovies()
            assertThat(notInterestedMovies).hasSize(1)
            assertThat(notInterestedMovies[0].id).isEqualTo(1)
            assertThat(notInterestedMovies[0].notInterested).isTrue()
        }
    }

    @Test
    fun `reject recommendation logs analytics event`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            rejectRecommendationUseCase.invoke(1)

            assertThat(fakeAnalyticsTracker.events).containsKey("ai_recommendation_rejected")
            val eventParams = fakeAnalyticsTracker.events["ai_recommendation_rejected"]
            assertThat(eventParams).isNotNull()
            assertThat(eventParams?.get("movie_id")).isEqualTo(1)
        }
    }

    @Test
    fun `reject multiple recommendations removes all from saved list`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            rejectRecommendationUseCase.invoke(1)
            rejectRecommendationUseCase.invoke(2)

            val remainingRecommendations = fakeRecommendationRepository.getSavedRecommendations().first()
            assertThat(remainingRecommendations).hasSize(1)
            assertThat(remainingRecommendations[0].id).isEqualTo(3)
        }
    }

    @Test
    fun `reject multiple recommendations adds all to not interested list`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            rejectRecommendationUseCase.invoke(1)
            rejectRecommendationUseCase.invoke(2)
            rejectRecommendationUseCase.invoke(3)

            val notInterestedMovies = fakeRecommendationRepository.getNotInterestedMovies()
            assertThat(notInterestedMovies).hasSize(3)
        }
    }

    @Test
    fun `reject recommendation with invalid id still returns success`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            val result = rejectRecommendationUseCase.invoke(999)

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeRecommendationRepository.markAsNotInterestedCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `reject all recommendations leaves empty list`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            rejectRecommendationUseCase.invoke(1)
            rejectRecommendationUseCase.invoke(2)
            rejectRecommendationUseCase.invoke(3)

            val remainingRecommendations = fakeRecommendationRepository.getSavedRecommendations().first()
            assertThat(remainingRecommendations).isEmpty()
        }
    }

    @Test
    fun `reject recommendation tracks each rejection separately`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            rejectRecommendationUseCase.invoke(1)
            assertThat(fakeRecommendationRepository.lastMarkedNotInterestedId).isEqualTo(1)

            rejectRecommendationUseCase.invoke(2)
            assertThat(fakeRecommendationRepository.lastMarkedNotInterestedId).isEqualTo(2)

            assertThat(fakeRecommendationRepository.markAsNotInterestedCallCount).isEqualTo(2)
        }
    }

    @Test
    fun `reject recommendation logs event for each rejection`() {
        runBlocking {
            fakeRecommendationRepository.setSavedRecommendations(testRecommendations)

            rejectRecommendationUseCase.invoke(1)
            rejectRecommendationUseCase.invoke(2)

            assertThat(fakeAnalyticsTracker.events["ai_recommendation_rejected"]).isNotNull()
        }
    }
}



