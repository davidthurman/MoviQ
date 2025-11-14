package com.dthurman.moviesaver.feature_onboarding.data.repository

import android.content.Context
import androidx.core.content.edit
import com.dthurman.moviesaver.feature_onboarding.domain.repository.OnboardingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OnboardingRepository {

    private val sharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun hasCompletedOnboarding(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override suspend fun setOnboardingCompleted() {
        sharedPreferences.edit {
            putBoolean(KEY_ONBOARDING_COMPLETED, true)
        }
    }

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}

