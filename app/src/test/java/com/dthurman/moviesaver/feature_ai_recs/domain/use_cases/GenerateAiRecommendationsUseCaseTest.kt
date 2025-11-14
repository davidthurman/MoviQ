package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.data.observability.FakeErrorLogger
import com.dthurman.moviesaver.core.data.repository.FakeCreditsRepository
import com.dthurman.moviesaver.core.data.repository.FakeUserRepository
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.feature_ai_recs.data.repository.FakeAiRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GenerateAiRecommendationsUseCaseTest {

    private lateinit var generateAiRecommendationsUseCase: GenerateAiRecommendationsUseCase
    private lateinit var fakeAiRepository: FakeAiRepository
    private lateinit var fakeCreditsRepository: FakeCreditsRepository
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker
    private lateinit var fakeErrorLogger: FakeErrorLogger

    private val testUser = User(
        id = "user_123",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
        credits = 10
    )

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
        )
    )

    @Before
    fun setUp() {
        fakeAiRepository = FakeAiRepository()
        fakeCreditsRepository = FakeCreditsRepository()
        fakeUserRepository = FakeUserRepository()
        fakeAnalyticsTracker = FakeAnalyticsTracker()
        fakeErrorLogger = FakeErrorLogger()

        generateAiRecommendationsUseCase = GenerateAiRecommendationsUseCase(
            aiRepository = fakeAiRepository,
            creditsRepository = fakeCreditsRepository,
            userRepository = fakeUserRepository,
            analytics = fakeAnalyticsTracker,
            errorLogger = fakeErrorLogger
        )
    }

    @Test
    fun `generate recommendations with valid user and credits returns success`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(testUser)
            fakeCreditsRepository.setCredits(10)
            fakeAiRepository.recommendationsToGenerate = testRecommendations

            val result = generateAiRecommendationsUseCase.invoke()

            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEqualTo(testRecommendations)
            assertThat(fakeCreditsRepository.deductCreditsCallCount).isEqualTo(1)
            assertThat(fakeCreditsRepository.lastDeductedAmount).isEqualTo(1)
            assertThat(fakeCreditsRepository.getCredits()).isEqualTo(9)
        }
    }

    @Test
    fun `generate recommendations logs analytics event`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(testUser)
            fakeCreditsRepository.setCredits(10)
            fakeAiRepository.recommendationsToGenerate = testRecommendations

            generateAiRecommendationsUseCase.invoke()

            assertThat(fakeAnalyticsTracker.events).containsKey("ai_recommendations_generated")
            val eventParams = fakeAnalyticsTracker.events["ai_recommendations_generated"]
            assertThat(eventParams).isNotNull()
            assertThat(eventParams?.get("user_id")).isEqualTo("user_123")
            assertThat(eventParams?.get("count")).isEqualTo(2)
            assertThat(eventParams?.get("credits_remaining")).isEqualTo(9)
        }
    }

    @Test
    fun `generate recommendations without user returns failure`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(null)
            fakeCreditsRepository.setCredits(10)

            val result = generateAiRecommendationsUseCase.invoke()

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            assertThat(result.exceptionOrNull()?.message).isEqualTo("User not logged in")
            assertThat(fakeCreditsRepository.deductCreditsCallCount).isEqualTo(0)
        }
    }

    @Test
    fun `generate recommendations with zero credits returns failure`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(testUser)
            fakeCreditsRepository.setCredits(0)

            val result = generateAiRecommendationsUseCase.invoke()

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(InsufficientCreditsException::class.java)
            val exception = result.exceptionOrNull() as InsufficientCreditsException
            assertThat(exception.currentCredits).isEqualTo(0)
            assertThat(fakeCreditsRepository.deductCreditsCallCount).isEqualTo(0)
        }
    }

    @Test
    fun `generate recommendations with negative credits returns failure`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(testUser)
            fakeCreditsRepository.setCredits(-5)

            val result = generateAiRecommendationsUseCase.invoke()

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(InsufficientCreditsException::class.java)
            assertThat(fakeCreditsRepository.deductCreditsCallCount).isEqualTo(0)
        }
    }

    @Test
    fun `generate recommendations when deduct credits fails returns failure`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(testUser)
            fakeCreditsRepository.setCredits(10)
            fakeCreditsRepository.shouldDeductSucceed = false

            val result = generateAiRecommendationsUseCase.invoke()

            assertThat(result.isFailure).isTrue()
            assertThat(fakeCreditsRepository.deductCreditsCallCount).isEqualTo(1)
            assertThat(fakeAiRepository.generateCallCount).isEqualTo(0)
        }
    }

    @Test
    fun `generate recommendations when AI generation fails logs error and returns failure`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(testUser)
            fakeCreditsRepository.setCredits(10)
            fakeAiRepository.shouldGenerateSucceed = false

            val result = generateAiRecommendationsUseCase.invoke()

            assertThat(result.isFailure).isTrue()
            assertThat(fakeErrorLogger.exceptions).hasSize(1)
            assertThat(fakeErrorLogger.customKeys["error_context"]).isEqualTo("ai_error")
        }
    }

    @Test
    fun `generate recommendations deducts exactly one credit`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(testUser)
            fakeCreditsRepository.setCredits(5)
            fakeAiRepository.recommendationsToGenerate = testRecommendations

            generateAiRecommendationsUseCase.invoke()

            assertThat(fakeCreditsRepository.getCredits()).isEqualTo(4)
            assertThat(fakeCreditsRepository.lastDeductedAmount).isEqualTo(1)
        }
    }

    @Test
    fun `generate recommendations with empty result returns success`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(testUser)
            fakeCreditsRepository.setCredits(10)
            fakeAiRepository.recommendationsToGenerate = emptyList()

            val result = generateAiRecommendationsUseCase.invoke()

            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEmpty()
            assertThat(fakeCreditsRepository.getCredits()).isEqualTo(9)
        }
    }

    @Test
    fun `generate recommendations does not log analytics on failure`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(testUser)
            fakeCreditsRepository.setCredits(10)
            fakeAiRepository.shouldGenerateSucceed = false

            generateAiRecommendationsUseCase.invoke()

            assertThat(fakeAnalyticsTracker.events).doesNotContainKey("ai_recommendations_generated")
        }
    }
}


