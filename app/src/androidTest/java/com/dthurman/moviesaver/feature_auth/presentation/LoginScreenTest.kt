package com.dthurman.moviesaver.feature_auth.presentation

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.app.MainActivity
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.util.TestTags
import com.dthurman.moviesaver.di.AppBindingModule
import com.dthurman.moviesaver.di.AppModule
import com.dthurman.moviesaver.di.ObservabilityModule
import com.dthurman.moviesaver.feature_auth.domain.AuthRepository
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
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var userRepository: UserRepository

    lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        
        composeRule.activity.setContent {
            AppTheme {
                LoginScreen()
            }
        }
    }

    @Test
    fun loginScreen_initialState_displaysAllElements() {
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertIsDisplayed()
        
        composeRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        
        composeRule.onNodeWithText(context.getString(R.string.track_and_discover)).assertIsDisplayed()
        
        composeRule.onNodeWithTag(TestTags.LOGIN_GOOGLE_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.LOGIN_GOOGLE_BUTTON).assertIsEnabled()
        
        composeRule.onNodeWithText(context.getString(R.string.sign_in_with_google)).assertIsDisplayed()
    }

}

