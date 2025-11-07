package com.dthurman.moviesaver.feature_movies.presentation.my_movies

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.dthurman.moviesaver.core.app.AppNavHost
import com.dthurman.moviesaver.core.app.Destination
import com.dthurman.moviesaver.core.app.MainActivity
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
class SeenScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

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
                    startDestination = Destination.SEEN.route,
                    onMovieClick = { },
                    onSettingsClick = { }
                )
            }
        }
    }

    @Test
    fun clickToggleSortButton_isVisible() {
        composeRule.onNodeWithTag(TestTags.MY_MOVIES_SORT_BUTTON).assertExists()
        composeRule.onNodeWithTag(TestTags.MY_MOVIES_SORT_DROPDOWN_SECTION).assertDoesNotExist()
        composeRule.onNodeWithTag(TestTags.MY_MOVIES_SORT_BUTTON).performClick()
        composeRule.onAllNodesWithTag(TestTags.MY_MOVIES_SORT_DROPDOWN_SECTION).onFirst().assertExists()
    }


}