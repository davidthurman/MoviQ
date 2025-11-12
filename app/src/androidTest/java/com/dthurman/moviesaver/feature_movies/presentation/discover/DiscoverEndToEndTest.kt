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
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.app.AppScaffold
import com.dthurman.moviesaver.core.app.MainActivity
import com.dthurman.moviesaver.core.util.TestTags
import com.dthurman.moviesaver.di.AppBindingModule
import com.dthurman.moviesaver.di.AppModule
import com.dthurman.moviesaver.di.ObservabilityModule
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
@UninstallModules(AppModule::class, AppBindingModule::class, ObservabilityModule::class)
class DiscoverEndToEndTest {

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
            AppTheme {
                AppScaffold(
                    currentUser = null,
                    isDarkMode = false,
                    onThemeToggle = { },
                    onSignOut = { }
                )
            }
        }
    }

    @Test
    fun endToEnd_navigateToDiscover_clickMovie_opensDetailSheet_closeWithBack() {
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_search))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("The Shawshank Redemption")).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.MOVIE_DETAIL_IMAGE).assertExists()
        
        Espresso.pressBack()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("The Shawshank Redemption")).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.popular_movies)).assertExists()
    }

    @Test
    fun endToEnd_addMovieToWatchlist_navigateToWatchlist_verifyExists() {
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_search))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Holes")).performClick()
        composeRule.waitForIdle()
        
        val addToWatchlistDescription = context.getString(R.string.add_to_watchlist)
        composeRule.onNodeWithContentDescription(addToWatchlistDescription).assertExists()
        composeRule.onNodeWithContentDescription(addToWatchlistDescription).performClick()
        composeRule.waitForIdle()
        
        val inWatchlistDescription = context.getString(R.string.in_watchlist)
        composeRule.onNodeWithContentDescription(inWatchlistDescription).assertExists()
        
        Espresso.pressBack()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.watchlist)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Holes")).assertExists()
    }

    @Test
    fun endToEnd_addMovieToSeen_navigateToSeen_verifyExists() {
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_search))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Interstellar")).performClick()
        composeRule.waitForIdle()
        
        val addToSeenDescription = context.getString(R.string.add_to_seen)
        composeRule.onNodeWithContentDescription(addToSeenDescription).assertExists()
        composeRule.onNodeWithContentDescription(addToSeenDescription).performClick()
        composeRule.waitForIdle()
        
        val star5Description = context.getString(R.string.star_content_description, 5)
        composeRule.onNodeWithContentDescription(star5Description).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.save))).performClick()
        composeRule.waitForIdle()
        
        Espresso.pressBack()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Interstellar")).assertExists()
    }

    @Test
    fun endToEnd_searchAndAddToWatchlist_navigateToWatchlist_verifyExists() {
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_search))).performClick()
        composeRule.waitForIdle()
        
        val searchPlaceholder = context.getString(R.string.search_for_movies)
        composeRule.onNodeWithText(searchPlaceholder).performClick()
        composeRule.onNodeWithText(searchPlaceholder).performTextInput("Holes")
        
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Holes")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Holes")).performClick()
        composeRule.waitForIdle()
        
        val addToWatchlistDescription = context.getString(R.string.add_to_watchlist)
        composeRule.onNodeWithContentDescription(addToWatchlistDescription).performClick()
        composeRule.waitForIdle()
        
        Espresso.pressBack()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.watchlist)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Holes")).assertExists()
    }

    @Test
    fun endToEnd_navigateBetweenScreens_discoverStateResets() {
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_search))).performClick()
        composeRule.waitForIdle()
        
        val searchPlaceholder = context.getString(R.string.search_for_movies)
        composeRule.onNodeWithText(searchPlaceholder).performClick()
        composeRule.onNodeWithText(searchPlaceholder).performTextInput("Shawshank")
        
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("The Shawshank Redemption")).assertExists()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_search))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.popular_movies)).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Interstellar")).assertExists()
    }

    @Test
    fun endToEnd_markMovieAsFavorite_navigateToSeen_filterFavorites_verifyExists() {
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_search))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("The Shawshank Redemption")).performClick()
        composeRule.waitForIdle()
        
        val addToSeenDescription = context.getString(R.string.add_to_seen)
        composeRule.onNodeWithContentDescription(addToSeenDescription).performClick()
        composeRule.waitForIdle()
        
        val star4Description = context.getString(R.string.star_content_description, 4)
        composeRule.onNodeWithContentDescription(star4Description).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.save))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.SET_MOVIE_AS_FAVORITE_BUTTON).performClick()
        composeRule.waitForIdle()
        
        Espresso.pressBack()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.TOGGLE_FAVORITE_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("The Shawshank Redemption")).assertExists()
    }
}

