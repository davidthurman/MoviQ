package com.dthurman.moviesaver.feature_movies.presentation.discover

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.app.AppNavHost
import com.dthurman.moviesaver.core.app.Destination
import com.dthurman.moviesaver.core.app.MainActivity
import com.dthurman.moviesaver.core.util.TestTags
import com.dthurman.moviesaver.di.AppBindingModule
import com.dthurman.moviesaver.di.AppModule
import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.MovieInformationDataSource
import com.dthurman.moviesaver.ui.theme.AppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, AppBindingModule::class)
class DiscoverScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var movieInformationDataSource: MovieInformationDataSource
    lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        composeRule.activity.setContent {
            val navController = rememberNavController()
            AppTheme {
                AppNavHost(
                    navController = navController,
                    startDestination = Destination.DISCOVER.route,
                    onMovieClick = { },
                    onSettingsClick = { }
                )
            }
        }
    }

    @Test
    fun discoverScreen_displaysPopularMovies() {
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.popular_movies)).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Interstellar")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("The Shawshank Redemption")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Holes")).assertExists()
    }

    @Test
    fun searchForMovie_displaysFilteredResults() {
        composeRule.waitForIdle()
        
        val searchContentDescription = context.getString(R.string.search_content_description)
        composeRule.onNodeWithContentDescription(searchContentDescription).assertExists()
        
        val searchPlaceholder = context.getString(R.string.search_for_movies)
        composeRule.onNodeWithText(searchPlaceholder).performClick()
        composeRule.onNodeWithText(searchPlaceholder).performTextInput("Interstellar")
        
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText("Results for", substring = true) and hasText("Interstellar", substring = true)).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Interstellar")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("The Shawshank Redemption")).assertDoesNotExist()
        composeRule.onNodeWithTag(TestTags.moviePreview("Holes")).assertDoesNotExist()
    }

    @Test
    fun searchForMovie_caseInsensitive() {
        composeRule.waitForIdle()
        
        val searchPlaceholder = context.getString(R.string.search_for_movies)
        composeRule.onNodeWithText(searchPlaceholder).performClick()
        composeRule.onNodeWithText(searchPlaceholder).performTextInput("shawshank")
        
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText("Results for", substring = true) and hasText("shawshank", substring = true)).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("The Shawshank Redemption")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Interstellar")).assertDoesNotExist()
    }

    @Test
    fun searchForMovie_noResults() {
        composeRule.waitForIdle()
        
        val searchPlaceholder = context.getString(R.string.search_for_movies)
        composeRule.onNodeWithText(searchPlaceholder).performClick()
        composeRule.onNodeWithText(searchPlaceholder).performTextInput("NonexistentMovie123")
        
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText("Results for", substring = true) and hasText("NonexistentMovie123", substring = true)).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Interstellar")).assertDoesNotExist()
        composeRule.onNodeWithTag(TestTags.moviePreview("The Shawshank Redemption")).assertDoesNotExist()
        composeRule.onNodeWithTag(TestTags.moviePreview("Holes")).assertDoesNotExist()
    }
}
