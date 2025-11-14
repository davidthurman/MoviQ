package com.dthurman.moviesaver.feature_onboarding.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dthurman.moviesaver.feature_onboarding.presentation.OnboardingSteps

@Composable
fun OnboardingPage(
    page: Int,
    modifier: Modifier = Modifier
) {
    val steps = OnboardingSteps.entries
    if (page in steps.indices) {
        OnboardingStepContent(
            step = steps[page],
            isFirstPage = page == 0,
            modifier = modifier
        )
    }
}

