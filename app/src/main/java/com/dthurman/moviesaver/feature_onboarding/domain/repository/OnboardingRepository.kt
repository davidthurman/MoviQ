package com.dthurman.moviesaver.feature_onboarding.domain.repository

interface OnboardingRepository {
    suspend fun hasCompletedOnboarding(): Boolean
    suspend fun setOnboardingCompleted()
}

