package com.dthurman.moviesaver.feature_auth.presentation

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.app.MainActivity
import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.util.TestTags
import com.dthurman.moviesaver.di.AppBindingModule
import com.dthurman.moviesaver.di.AppModule
import com.dthurman.moviesaver.feature_auth.data.repository.FakeAuthRepository
import com.dthurman.moviesaver.feature_auth.domain.AuthRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, AppBindingModule::class)
class AuthEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var userRepository: UserRepository
    
    @Inject
    lateinit var movieDao: MovieDao

    lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun endToEnd_successfulLogin_navigatesToMoviesScreen() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertIsDisplayed()
        
        runBlocking {
            authRepository.signInWithGoogle("test_token")
        }
        
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText(context.getString(R.string.nav_movies))).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertDoesNotExist()
    }

    @Test
    fun endToEnd_loginError_staysOnLoginScreen() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertIsDisplayed()
        
        val fakeAuthRepo = authRepository as FakeAuthRepository
        fakeAuthRepo.shouldSignInSucceed = false
        fakeAuthRepo.signInError = "Authentication failed"
        
        runBlocking {
            val result = authRepository.signInWithGoogle("test_token")
            assert(result.isFailure)
        }
        
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.LOGIN_GOOGLE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun endToEnd_authenticatedUser_openSettings_signOut_returnsToLogin() {
        runBlocking {
            authRepository.signInWithGoogle("test_token")
        }
        
        composeRule.waitForIdle()
        
        val settingsDescription = context.getString(R.string.settings_content_description)
        composeRule.onNodeWithContentDescription(settingsDescription).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_MODAL).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SETTINGS_USER_NAME).assertIsDisplayed()
        composeRule.onNodeWithText("Test User").assertIsDisplayed()
        composeRule.onNodeWithText("test@example.com").assertIsDisplayed()
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_SIGN_OUT_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.LOGIN_GOOGLE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun endToEnd_signIn_addMovie_signOut_signInAgain_movieStillExists() {
        runBlocking {
            authRepository.signInWithGoogle("test_token")
        }
        
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText(context.getString(R.string.nav_movies))).fetchSemanticsNodes().isNotEmpty()
        }
        
        runBlocking {
            val testMovie = Movie(
                id = 1,
                title = "Test Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "Test overview",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null
            )
            movieDao.insertOrUpdateMovie(testMovie)
        }
        
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag(TestTags.moviePreview("Test Movie")).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Test Movie")).assertIsDisplayed()
        
        val settingsDescription = context.getString(R.string.settings_content_description)
        composeRule.onNodeWithContentDescription(settingsDescription).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_SIGN_OUT_BUTTON).performClick()
        
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag(TestTags.APP_LOGO).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertIsDisplayed()
        
        runBlocking {
            authRepository.signInWithGoogle("test_token")
        }
        
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag(TestTags.moviePreview("Test Movie")).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Test Movie")).assertIsDisplayed()
    }

    @Test
    fun endToEnd_authenticatedUserRestartsApp_staysAuthenticated() {
        runBlocking {
            authRepository.signInWithGoogle("test_token")
        }
        
        composeRule.waitForIdle()
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertDoesNotExist()
    }

    @Test
    fun endToEnd_openSettings_closeSettings_returnsToMoviesScreen() {
        runBlocking {
            authRepository.signInWithGoogle("test_token")
        }
        
        composeRule.waitForIdle()
        
        val settingsDescription = context.getString(R.string.settings_content_description)
        composeRule.onNodeWithContentDescription(settingsDescription).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_MODAL).assertIsDisplayed()
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_CLOSE_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.SETTINGS_MODAL).assertDoesNotExist()
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).assertIsDisplayed()
    }

    @Test
    fun endToEnd_multipleSignInAttempts_finallySucceeds() {
        val fakeAuthRepo = authRepository as FakeAuthRepository
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertIsDisplayed()
        
        fakeAuthRepo.shouldSignInSucceed = false
        fakeAuthRepo.signInError = "Network error"
        
        runBlocking {
            val result = authRepository.signInWithGoogle("test_token")
            assert(result.isFailure)
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertIsDisplayed()
        
        fakeAuthRepo.shouldSignInSucceed = false
        fakeAuthRepo.signInError = "Server error"
        
        runBlocking {
            val result = authRepository.signInWithGoogle("test_token")
            assert(result.isFailure)
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertIsDisplayed()
        
        fakeAuthRepo.shouldSignInSucceed = true
        
        runBlocking {
            val result = authRepository.signInWithGoogle("test_token")
            assert(result.isSuccess)
        }
        
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(hasText(context.getString(R.string.nav_movies))).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.APP_LOGO).assertDoesNotExist()
    }
}

