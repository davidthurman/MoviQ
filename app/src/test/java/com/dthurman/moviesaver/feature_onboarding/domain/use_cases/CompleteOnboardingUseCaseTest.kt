package com.dthurman.moviesaver.feature_onboarding.domain.use_cases

import com.dthurman.moviesaver.feature_onboarding.data.repository.FakeOnboardingRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class CompleteOnboardingUseCaseTest {

    private lateinit var completeOnboardingUseCase: CompleteOnboardingUseCase
    private lateinit var fakeOnboardingRepository: FakeOnboardingRepository

    @Before
    fun setUp() {
        fakeOnboardingRepository = FakeOnboardingRepository()
        completeOnboardingUseCase = CompleteOnboardingUseCase(
            repository = fakeOnboardingRepository
        )
    }

    @Test
    fun `invoke sets onboarding as completed`() {
        runBlocking {
            assertThat(fakeOnboardingRepository.hasCompletedOnboarding()).isFalse()
            
            completeOnboardingUseCase.invoke()
            
            assertThat(fakeOnboardingRepository.hasCompletedOnboarding()).isTrue()
            assertThat(fakeOnboardingRepository.setCompletedCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `multiple invocations track call count`() {
        runBlocking {
            completeOnboardingUseCase.invoke()
            completeOnboardingUseCase.invoke()
            
            assertThat(fakeOnboardingRepository.setCompletedCallCount).isEqualTo(2)
        }
    }

    @Test
    fun `invoke persists completion state`() {
        runBlocking {
            completeOnboardingUseCase.invoke()
            
            val hasCompleted = fakeOnboardingRepository.hasCompletedOnboarding()
            assertThat(hasCompleted).isTrue()
        }
    }
}

