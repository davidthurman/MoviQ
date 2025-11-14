package com.dthurman.moviesaver.feature_onboarding.data.repository

import com.dthurman.moviesaver.feature_onboarding.domain.repository.OnboardingRepository

class FakeOnboardingRepository : OnboardingRepository {
    
    private var hasCompleted = false
    var hasCompletedCallCount = 0
    var setCompletedCallCount = 0
    
    override suspend fun hasCompletedOnboarding(): Boolean {
        hasCompletedCallCount++
        return hasCompleted
    }
    
    override suspend fun setOnboardingCompleted() {
        setCompletedCallCount++
        hasCompleted = true
    }
    
    fun reset() {
        hasCompleted = false
        hasCompletedCallCount = 0
        setCompletedCallCount = 0
    }
    
    fun setHasCompleted(completed: Boolean) {
        hasCompleted = completed
    }
}

