package com.dthurman.moviesaver.feature_onboarding.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dthurman.moviesaver.R

enum class OnboardingSteps(
    @StringRes val headerRes: Int,
    @DrawableRes val imageRes: Int,
    @StringRes val imageDescriptionRes: Int,
) {
    HOME(
        headerRes = R.string.onboarding_home_title,
        imageRes = R.drawable.onboard_logo,
        imageDescriptionRes = R.string.onboarding_home_image_description,
    ),
    SEARCH(
        headerRes = R.string.onboarding_search_title,
        imageRes = R.drawable.onboard_seen,
        imageDescriptionRes = R.string.onboarding_search_image_description,
    ),
    RATE(
        headerRes = R.string.onboarding_rate_title,
        imageRes = R.drawable.onboard_rate,
        imageDescriptionRes = R.string.onboarding_rate_image_description,
    ),
    AI(
        headerRes = R.string.onboarding_ai_title,
        imageRes = R.drawable.onboard_ai,
        imageDescriptionRes = R.string.onboarding_ai_image_description,
    )
}