package com.dthurman.moviesaver.feature_onboarding.domain.use_cases

import com.dthurman.moviesaver.feature_onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke() {
        repository.setOnboardingCompleted()
    }
}

