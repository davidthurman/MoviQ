package com.dthurman.moviesaver.feature_onboarding.domain.use_cases

data class OnboardingUseCases(
    val hasCompletedOnboarding: HasCompletedOnboardingUseCase,
    val completeOnboarding: CompleteOnboardingUseCase
)

