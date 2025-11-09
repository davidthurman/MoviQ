package com.dthurman.moviesaver.feature_movies.presentation.my_movies

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.app.AppNavHost
import com.dthurman.moviesaver.core.app.Destination
import com.dthurman.moviesaver.core.app.MainActivity
import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.util.TestTags
import com.dthurman.moviesaver.di.AppBindingModule
import com.dthurman.moviesaver.di.AppModule
import com.dthurman.moviesaver.ui.theme.AppTheme
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
class SeenScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var movieDao: MovieDao

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

    @Test
    fun clickSortOption_dismissesMenu() {
        composeRule.onNodeWithTag(TestTags.MY_MOVIES_SORT_BUTTON).performClick()
        composeRule.onAllNodesWithTag(TestTags.MY_MOVIES_SORT_DROPDOWN_SECTION).onFirst().assertExists()
        
        composeRule.onAllNodesWithTag(TestTags.MY_MOVIES_SORT_DROPDOWN_SECTION).onFirst().performClick()
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.MY_MOVIES_SORT_DROPDOWN_SECTION).assertDoesNotExist()
    }

    @Test
    fun clickFavoriteButton_togglesState() {
        composeRule.onNodeWithTag(TestTags.TOGGLE_FAVORITE_BUTTON).assertExists()
        composeRule.onNodeWithTag(TestTags.TOGGLE_FAVORITE_BUTTON).assertIsNotSelected()
        
        composeRule.onNodeWithTag(TestTags.TOGGLE_FAVORITE_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.TOGGLE_FAVORITE_BUTTON).assertIsSelected()
    }

    @Test
    fun segmentedButtons_seenAndWatchlist_exist() {
        val seenText = context.getString(R.string.seen)
        val watchlistText = context.getString(R.string.watchlist)
        
        composeRule.onNodeWithText(seenText).assertExists()
        composeRule.onNodeWithText(watchlistText).assertExists()
    }

    @Test
    fun seenButton_isSelectedByDefault() {
        val seenText = context.getString(R.string.seen)
        composeRule.onNodeWithText(seenText).assertIsSelected()
    }

    @Test
    fun clickWatchlistButton_switchesFilter() {
        val seenText = context.getString(R.string.seen)
        val watchlistText = context.getString(R.string.watchlist)
        
        composeRule.onNodeWithText(seenText).assertIsSelected()
        composeRule.onNodeWithText(watchlistText).assertIsNotSelected()
        
        composeRule.onNodeWithText(watchlistText).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(watchlistText).assertIsSelected()
        composeRule.onNodeWithText(seenText).assertIsNotSelected()
    }

    @Test
    fun clickSeenButton_afterWatchlist_switchesBackToSeen() {
        val seenText = context.getString(R.string.seen)
        val watchlistText = context.getString(R.string.watchlist)
        
        composeRule.onNodeWithText(watchlistText).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(watchlistText).assertIsSelected()
        
        composeRule.onNodeWithText(seenText).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(seenText).assertIsSelected()
        composeRule.onNodeWithText(watchlistText).assertIsNotSelected()
    }

    @Test
    fun sortButton_displaysCurrentSortOrder() {
        val sortLabel = context.getString(R.string.sort_label, context.getString(R.string.sort_date_added_desc))
        composeRule.onNode(hasText(sortLabel, substring = true)).assertExists()
    }

    @Test
    fun allSortOptions_areDisplayedInMenu() {
        composeRule.onNodeWithTag(TestTags.MY_MOVIES_SORT_BUTTON).performClick()
        
        composeRule.onNodeWithText(context.getString(R.string.sort_title_asc)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.sort_title_desc)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.sort_date_added_asc)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.sort_date_added_desc)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.sort_release_date_asc)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.sort_release_date_desc)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.sort_rating_asc)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.sort_rating_desc)).assertExists()
    }

    @Test
    fun withSeenMovies_displaysMovieList() {
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
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.moviePreview("Test Movie")).assertExists()
    }

    @Test
    fun withFavoriteMovie_favoriteFilterOn_displaysMovie() {
        runBlocking {
            val favoriteMovie = Movie(
                id = 2,
                title = "Favorite Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "Test overview",
                isSeen = true,
                isWatchlist = false,
                isFavorite = true,
                rating = null
            )
            
            val nonFavoriteMovie = Movie(
                id = 3,
                title = "Non-Favorite Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "Test overview",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null
            )
            
            movieDao.insertOrUpdateMovie(favoriteMovie)
            movieDao.insertOrUpdateMovie(nonFavoriteMovie)
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.moviePreview("Favorite Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Non-Favorite Movie")).assertExists()
        
        composeRule.onNodeWithTag(TestTags.TOGGLE_FAVORITE_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Favorite Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Non-Favorite Movie")).assertDoesNotExist()
    }

    @Test
    fun withWatchlistMovies_switchToWatchlist_displaysWatchlistMovies() {
        runBlocking {
            val seenMovie = Movie(
                id = 4,
                title = "Seen Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "Test overview",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null
            )
            
            val watchlistMovie = Movie(
                id = 5,
                title = "Watchlist Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "Test overview",
                isSeen = false,
                isWatchlist = true,
                isFavorite = false,
                rating = null
            )
            
            movieDao.insertOrUpdateMovie(seenMovie)
            movieDao.insertOrUpdateMovie(watchlistMovie)
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.moviePreview("Seen Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Watchlist Movie")).assertDoesNotExist()
        
        val watchlistText = context.getString(R.string.watchlist)
        composeRule.onNodeWithText(watchlistText).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Watchlist Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Seen Movie")).assertDoesNotExist()
    }

    @Test
    fun withMultipleMovies_changingSortOption_moviesStillDisplay() {
        runBlocking {
            val movieA = Movie(
                id = 6,
                title = "A Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "Test overview",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null,
                addedAt = System.currentTimeMillis() - 1000000
            )

            val movieZ = Movie(
                id = 7,
                title = "Z Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "Test overview",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null
            )

            movieDao.insertOrUpdateMovie(movieA)
            movieDao.insertOrUpdateMovie(movieZ)
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.moviePreview("A Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Z Movie")).assertExists()

        composeRule.onNodeWithTag(TestTags.MY_MOVIES_SORT_BUTTON).performClick()
        composeRule.onNodeWithText(context.getString(R.string.sort_title_asc)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.moviePreview("A Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Z Movie")).assertExists()
    }


}