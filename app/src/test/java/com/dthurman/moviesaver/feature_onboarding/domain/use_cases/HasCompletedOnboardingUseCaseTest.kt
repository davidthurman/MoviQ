package com.dthurman.moviesaver.feature_onboarding.domain.use_cases

import com.dthurman.moviesaver.feature_onboarding.data.repository.FakeOnboardingRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class HasCompletedOnboardingUseCaseTest {

    private lateinit var hasCompletedOnboardingUseCase: HasCompletedOnboardingUseCase
    private lateinit var fakeOnboardingRepository: FakeOnboardingRepository

    @Before
    fun setUp() {
        fakeOnboardingRepository = FakeOnboardingRepository()
        hasCompletedOnboardingUseCase = HasCompletedOnboardingUseCase(
            repository = fakeOnboardingRepository
        )
    }

    @Test
    fun `invoke returns false when onboarding not completed`() {
        runBlocking {
            fakeOnboardingRepository.setHasCompleted(false)
            
            val result = hasCompletedOnboardingUseCase.invoke()
            
            assertThat(result).isFalse()
            assertThat(fakeOnboardingRepository.hasCompletedCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `invoke returns true when onboarding completed`() {
        runBlocking {
            fakeOnboardingRepository.setHasCompleted(true)
            
            val result = hasCompletedOnboardingUseCase.invoke()
            
            assertThat(result).isTrue()
            assertThat(fakeOnboardingRepository.hasCompletedCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `multiple invocations track call count`() {
        runBlocking {
            hasCompletedOnboardingUseCase.invoke()
            hasCompletedOnboardingUseCase.invoke()
            hasCompletedOnboardingUseCase.invoke()
            
            assertThat(fakeOnboardingRepository.hasCompletedCallCount).isEqualTo(3)
        }
    }
}

