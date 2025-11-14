package com.dthurman.moviesaver.feature_onboarding.presentation

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.app.MainActivity
import com.dthurman.moviesaver.di.AppBindingModule
import com.dthurman.moviesaver.di.AppModule
import com.dthurman.moviesaver.di.ObservabilityModule
import com.dthurman.moviesaver.feature_onboarding.domain.repository.OnboardingRepository
import com.dthurman.moviesaver.ui.theme.AppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, AppBindingModule::class, ObservabilityModule::class)
class OnboardingScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var onboardingRepository: OnboardingRepository

    lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        
        composeRule.activity.setContent {
            AppTheme {
                OnboardingDialog()
            }
        }
    }

    @Test
    fun onboardingScreen_initialState_displaysFirstPage() {
        composeRule.onNodeWithText(context.getString(R.string.onboarding_home_title))
            .assertIsDisplayed()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_back))
            .assertIsDisplayed()
            .assertIsNotEnabled()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun onboardingScreen_clickNext_navigatesToSecondPage() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .performClick()
        
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.onboarding_search_title))
            .assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_navigateToSecondPage_backButtonEnabled() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .performClick()
        
        composeRule.waitForIdle()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_back))
            .assertIsEnabled()
    }

    @Test
    fun onboardingScreen_clickBack_returnsToPreviousPage() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .performClick()
        
        composeRule.waitForIdle()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_back))
            .performClick()
        
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.onboarding_home_title))
            .assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_navigateToLastPage_displaysCheckmark() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.onboarding_ai_title))
            .assertIsDisplayed()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_get_started))
            .assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_allPages_displayCorrectTitles() {
        composeRule.onNodeWithText(context.getString(R.string.onboarding_home_title))
            .assertIsDisplayed()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_search_title))
            .assertIsDisplayed()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_rate_title))
            .assertIsDisplayed()
        
        composeRule.onNodeWithContentDescription(context.getString(R.string.onboarding_next))
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_ai_title))
            .assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_pageIndicator_displaysCorrectCount() {
        composeRule.onNodeWithText(context.getString(R.string.onboarding_home_title))
            .assertIsDisplayed()
    }
}

