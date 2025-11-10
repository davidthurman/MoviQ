package com.dthurman.moviesaver.ui.components

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.app.MainActivity
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.util.TestTags
import com.dthurman.moviesaver.di.AppBindingModule
import com.dthurman.moviesaver.di.AppModule
import com.dthurman.moviesaver.ui.theme.AppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class, AppBindingModule::class)
class SettingsModalTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    lateinit var context: Context
    private var dismissCalled = false
    private var signOutCalled = false
    private var themeToggleCalled = false
    private var themeToggleValue: Boolean? = null

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        dismissCalled = false
        signOutCalled = false
        themeToggleCalled = false
        themeToggleValue = null
    }

    @Test
    fun settingsModal_withUser_displaysUserInfo() {
        val testUser = User(
            id = "test_user_123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null,
            credits = 10
        )
        
        composeRule.activity.setContent {
            AppTheme {
                SettingsModal(
                    onDismiss = { dismissCalled = true },
                    currentUser = testUser,
                    onSignOut = { signOutCalled = true },
                    isDarkMode = false,
                    onThemeToggle = { themeToggleCalled = true }
                )
            }
        }
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_MODAL).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings)).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_USER_NAME).assertIsDisplayed()
        composeRule.onNodeWithText("Test User").assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_USER_EMAIL).assertIsDisplayed()
        composeRule.onNodeWithText("test@example.com").assertIsDisplayed()
    }

    @Test
    fun settingsModal_withoutUser_doesNotDisplayUserInfo() {
        composeRule.activity.setContent {
            AppTheme {
                SettingsModal(
                    onDismiss = { dismissCalled = true },
                    currentUser = null,
                    onSignOut = { signOutCalled = true },
                    isDarkMode = false,
                    onThemeToggle = { themeToggleCalled = true }
                )
            }
        }
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_MODAL).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_USER_NAME).assertDoesNotExist()
        composeRule.onNodeWithTag(TestTags.SETTINGS_USER_EMAIL).assertDoesNotExist()
        composeRule.onNodeWithTag(TestTags.SETTINGS_SIGN_OUT_BUTTON).assertDoesNotExist()
    }

    @Test
    fun settingsModal_clickSignOut_callsCallback() {
        val testUser = User(
            id = "test_user_123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null,
            credits = 10
        )
        
        composeRule.activity.setContent {
            AppTheme {
                SettingsModal(
                    onDismiss = { dismissCalled = true },
                    currentUser = testUser,
                    onSignOut = { signOutCalled = true },
                    isDarkMode = false,
                    onThemeToggle = { themeToggleCalled = true }
                )
            }
        }
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_SIGN_OUT_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_SIGN_OUT_BUTTON).performClick()
        
        assert(signOutCalled)
    }

    @Test
    fun settingsModal_clickClose_callsDismiss() {
        composeRule.activity.setContent {
            AppTheme {
                SettingsModal(
                    onDismiss = { dismissCalled = true },
                    currentUser = null,
                    onSignOut = { signOutCalled = true },
                    isDarkMode = false,
                    onThemeToggle = { themeToggleCalled = true }
                )
            }
        }
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_CLOSE_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_CLOSE_BUTTON).performClick()
        
        assert(dismissCalled)
    }

    @Test
    fun settingsModal_themeSwitch_darkMode_isSelected() {
        composeRule.activity.setContent {
            AppTheme {
                SettingsModal(
                    onDismiss = { dismissCalled = true },
                    currentUser = null,
                    onSignOut = { signOutCalled = true },
                    isDarkMode = true,
                    onThemeToggle = { 
                        themeToggleCalled = true
                        themeToggleValue = it
                    }
                )
            }
        }
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_THEME_SWITCH).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_THEME_SWITCH).assertIsSelected()
        composeRule.onNodeWithText(context.getString(R.string.dark_mode)).assertIsDisplayed()
    }

    @Test
    fun settingsModal_themeSwitch_lightMode_isNotSelected() {
        composeRule.activity.setContent {
            AppTheme {
                SettingsModal(
                    onDismiss = { dismissCalled = true },
                    currentUser = null,
                    onSignOut = { signOutCalled = true },
                    isDarkMode = false,
                    onThemeToggle = { 
                        themeToggleCalled = true
                        themeToggleValue = it
                    }
                )
            }
        }
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_THEME_SWITCH).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_THEME_SWITCH).assertIsNotSelected()
        composeRule.onNodeWithText(context.getString(R.string.light_mode)).assertIsDisplayed()
    }

    @Test
    fun settingsModal_toggleTheme_callsCallbackWithCorrectValue() {
        composeRule.activity.setContent {
            AppTheme {
                SettingsModal(
                    onDismiss = { dismissCalled = true },
                    currentUser = null,
                    onSignOut = { signOutCalled = true },
                    isDarkMode = false,
                    onThemeToggle = { 
                        themeToggleCalled = true
                        themeToggleValue = it
                    }
                )
            }
        }
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_THEME_SWITCH).performClick()
        
        assert(themeToggleCalled)
        assert(themeToggleValue == true)
    }

    @Test
    fun settingsModal_userWithPhoto_displaysAllInfo() {
        val testUser = User(
            id = "test_user_123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg",
            credits = 10
        )
        
        composeRule.activity.setContent {
            AppTheme {
                SettingsModal(
                    onDismiss = { dismissCalled = true },
                    currentUser = testUser,
                    onSignOut = { signOutCalled = true },
                    isDarkMode = false,
                    onThemeToggle = { themeToggleCalled = true }
                )
            }
        }
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_USER_NAME).assertIsDisplayed()
        composeRule.onNodeWithText("Test User").assertIsDisplayed()
        composeRule.onNodeWithText("test@example.com").assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_SIGN_OUT_BUTTON).assertIsDisplayed()
    }

    @Test
    fun settingsModal_displaysThemeSection() {
        composeRule.activity.setContent {
            AppTheme {
                SettingsModal(
                    onDismiss = { dismissCalled = true },
                    currentUser = null,
                    onSignOut = { signOutCalled = true },
                    isDarkMode = false,
                    onThemeToggle = { themeToggleCalled = true }
                )
            }
        }
        
        composeRule.onNodeWithText(context.getString(R.string.theme)).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_THEME_SWITCH).assertIsDisplayed()
    }
}

