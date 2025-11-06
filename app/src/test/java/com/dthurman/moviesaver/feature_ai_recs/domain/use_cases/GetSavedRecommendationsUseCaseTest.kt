package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_ai_recs.data.repository.FakeAiRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetSavedRecommendationsUseCaseTest {

    private lateinit var getSavedRecommendationsUseCase: GetSavedRecommendationsUseCase
    private lateinit var fakeAiRepository: FakeAiRepository

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
        fakeAiRepository = FakeAiRepository()
        getSavedRecommendationsUseCase = GetSavedRecommendationsUseCase(
            aiRepository = fakeAiRepository
        )
    }

    @Test
    fun `get saved recommendations returns flow of recommendations`() {
        runBlocking {
            fakeAiRepository.setSavedRecommendations(testRecommendations)

            val result = getSavedRecommendationsUseCase.invoke().first()

            assertThat(result).isEqualTo(testRecommendations)
            assertThat(result).hasSize(3)
        }
    }

    @Test
    fun `get saved recommendations returns empty list when no recommendations`() {
        runBlocking {
            fakeAiRepository.setSavedRecommendations(emptyList())

            val result = getSavedRecommendationsUseCase.invoke().first()

            assertThat(result).isEmpty()
        }
    }

    @Test
    fun `get saved recommendations returns single recommendation`() {
        runBlocking {
            val singleRecommendation = listOf(testRecommendations[0])
            fakeAiRepository.setSavedRecommendations(singleRecommendation)

            val result = getSavedRecommendationsUseCase.invoke().first()

            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo(1)
            assertThat(result[0].title).isEqualTo("Test Movie 1")
        }
    }

    @Test
    fun `get saved recommendations preserves AI reasons`() {
        runBlocking {
            fakeAiRepository.setSavedRecommendations(testRecommendations)

            val result = getSavedRecommendationsUseCase.invoke().first()

            assertThat(result[0].aiReason).isEqualTo("Test reason 1")
            assertThat(result[1].aiReason).isEqualTo("Test reason 2")
            assertThat(result[2].aiReason).isEqualTo("Test reason 3")
        }
    }

    @Test
    fun `get saved recommendations updates when repository changes`() {
        runBlocking {
            fakeAiRepository.setSavedRecommendations(emptyList())
            val flow = getSavedRecommendationsUseCase.invoke()

            val initialResult = flow.first()
            assertThat(initialResult).isEmpty()

            fakeAiRepository.setSavedRecommendations(testRecommendations)
            val updatedResult = flow.first()
            assertThat(updatedResult).hasSize(3)
        }
    }

    @Test
    fun `get saved recommendations returns recommendations in order`() {
        runBlocking {
            fakeAiRepository.setSavedRecommendations(testRecommendations)

            val result = getSavedRecommendationsUseCase.invoke().first()

            assertThat(result[0].id).isEqualTo(1)
            assertThat(result[1].id).isEqualTo(2)
            assertThat(result[2].id).isEqualTo(3)
        }
    }
}

