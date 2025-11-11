package com.dthurman.moviesaver.feature_movies.presentation.detail

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.dthurman.moviesaver.R
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
class DetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var movieDao: MovieDao

    lateinit var context: Context

    private lateinit var testMovie: Movie

    @OptIn(ExperimentalMaterial3Api::class)
    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()

        testMovie = Movie(
            id = 1,
            title = "Test Movie",
            posterUrl = "https://example.com/poster.jpg",
            backdropUrl = "https://example.com/backdrop.jpg",
            releaseDate = "2024-01-01",
            overview = "This is a test movie overview",
            isSeen = false,
            isWatchlist = false,
            isFavorite = false,
            rating = null
        )

        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = testMovie)
                    }
                }
            }
        }
    }

    @Test
    fun detailScreen_displaysMovieInformation() {
        composeRule.onNodeWithTag(TestTags.DETAIL_MOVIE_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText("Test Movie").assertExists()
        composeRule.onNodeWithText("2024-01-01").assertExists()
        composeRule.onNodeWithTag(TestTags.DETAIL_MOVIE_OVERVIEW).assertIsDisplayed()
        composeRule.onNodeWithText("This is a test movie overview").assertExists()
    }

    @Test
    fun detailScreen_displaysImage() {
        composeRule.onNodeWithTag(TestTags.MOVIE_DETAIL_IMAGE).assertExists()
    }

    @Test
    fun detailScreen_displaysWatchlistAndSeenButtons() {
        composeRule.onNodeWithTag(TestTags.DETAIL_WATCHLIST_BUTTON).assertExists()
        composeRule.onNodeWithTag(TestTags.DETAIL_SEEN_BUTTON).assertExists()
    }

    @Test
    fun unseenMovie_doesNotDisplayFavoriteButton() {
        composeRule.onNodeWithTag(TestTags.SET_MOVIE_AS_FAVORITE_BUTTON).assertDoesNotExist()
    }

    @Test
    fun unseenMovie_doesNotDisplayRating() {
        composeRule.onNodeWithTag(TestTags.DETAIL_RATING_DISPLAY).assertDoesNotExist()
    }

    @Test
    fun clickWatchlistButton_togglesWatchlistStatus() {
        composeRule.waitForIdle()
        
        val addToWatchlistDescription = context.getString(R.string.add_to_watchlist)
        composeRule.onNodeWithContentDescription(addToWatchlistDescription).assertExists()
        
        composeRule.onNodeWithTag(TestTags.DETAIL_WATCHLIST_BUTTON).performClick()
        composeRule.waitForIdle()
        
        val inWatchlistDescription = context.getString(R.string.in_watchlist)
        composeRule.onNodeWithContentDescription(inWatchlistDescription).assertExists()
    }

    @Test
    fun clickSeenButton_opensRatingDialog() {
        composeRule.waitForIdle()
        
        val addToSeenDescription = context.getString(R.string.add_to_seen)
        composeRule.onNodeWithContentDescription(addToSeenDescription).assertExists()
        
        composeRule.onNodeWithTag(TestTags.DETAIL_SEEN_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.rate_movie)).assertExists()
    }

    @Test
    fun ratingDialog_canSelectRatingAndSave() {
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.DETAIL_SEEN_BUTTON).performClick()
        composeRule.waitForIdle()
        
        val star3Description = context.getString(R.string.star_content_description, 3)
        composeRule.onNodeWithContentDescription(star3Description).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.save))).performClick()
        composeRule.waitForIdle()
        
        val inSeenDescription = context.getString(R.string.in_seen)
        composeRule.onNodeWithContentDescription(inSeenDescription, useUnmergedTree = true).assertExists()
    }

    @Test
    fun ratingDialog_canCancel() {
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.DETAIL_SEEN_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.cancel))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.rate_movie)).assertDoesNotExist()
        
        val addToSeenDescription = context.getString(R.string.add_to_seen)
        composeRule.onNodeWithContentDescription(addToSeenDescription).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun seenMovie_displaysFavoriteButton() {
        val seenMovie = testMovie.copy(isSeen = true, rating = 4.0f)
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = seenMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.SET_MOVIE_AS_FAVORITE_BUTTON).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun seenMovie_displaysRating() {
        val seenMovie = testMovie.copy(isSeen = true, rating = 4.0f)
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = seenMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.DETAIL_RATING_DISPLAY).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun seenMovie_clickFavoriteButton_togglesFavorite() {
        val seenMovie = testMovie.copy(isSeen = true, rating = 4.0f, isFavorite = false)
        
        runBlocking {
            movieDao.insertOrUpdateMovie(seenMovie)
        }
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = seenMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.SET_MOVIE_AS_FAVORITE_BUTTON).performClick()
        composeRule.waitForIdle()
        
        runBlocking {
            val updatedMovie = movieDao.getMovieById(seenMovie.id)
            assert(updatedMovie?.isFavorite == true)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun seenMovie_clickRatingDisplay_opensRatingDialog() {
        val seenMovie = testMovie.copy(isSeen = true, rating = 3.0f)
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = seenMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.DETAIL_RATING_DISPLAY).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.rate_movie)).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun seenMovie_clickSeenButton_opensRemoveDialog() {
        val seenMovie = testMovie.copy(isSeen = true, rating = 4.0f)
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = seenMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.DETAIL_SEEN_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.remove_from_seen)).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun removeFromSeenDialog_canConfirm() {
        val seenMovie = testMovie.copy(isSeen = true, rating = 4.0f)
        
        runBlocking {
            movieDao.insertOrUpdateMovie(seenMovie)
        }
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = seenMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.DETAIL_SEEN_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.remove))).performClick()
        composeRule.waitForIdle()
        
        val addToSeenDescription = context.getString(R.string.add_to_seen)
        composeRule.onNodeWithContentDescription(addToSeenDescription).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun removeFromSeenDialog_canCancel() {
        val seenMovie = testMovie.copy(isSeen = true, rating = 4.0f)
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = seenMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.DETAIL_SEEN_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.cancel))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.remove_from_seen)).assertDoesNotExist()
        
        val inSeenDescription = context.getString(R.string.in_seen)
        composeRule.onNodeWithContentDescription(inSeenDescription).assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun watchlistMovie_addToSeen_removesFromWatchlist() {
        val watchlistMovie = testMovie.copy(isWatchlist = true, isSeen = false)
        
        runBlocking {
            movieDao.insertOrUpdateMovie(watchlistMovie)
        }
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = watchlistMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        
        val inWatchlistDescription = context.getString(R.string.in_watchlist)
        composeRule.onNodeWithContentDescription(inWatchlistDescription).assertExists()
        
        composeRule.onNodeWithTag(TestTags.DETAIL_SEEN_BUTTON).performClick()
        composeRule.waitForIdle()
        
        val star4Description = context.getString(R.string.star_content_description, 4)
        composeRule.onNodeWithContentDescription(star4Description).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.save))).performClick()
        composeRule.waitForIdle()
        
        runBlocking {
            val updatedMovie = movieDao.getMovieById(watchlistMovie.id)
            assert(updatedMovie?.isSeen == true)
            assert(updatedMovie?.isWatchlist == false)
            assert(updatedMovie?.rating == 4.0f)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun seenFavoriteMovie_removeFromSeen_alsoRemovesFavorite() {
        val favoriteMovie = testMovie.copy(isSeen = true, isFavorite = true, rating = 5.0f)
        
        runBlocking {
            movieDao.insertOrUpdateMovie(favoriteMovie)
        }
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = favoriteMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.DETAIL_SEEN_BUTTON).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.remove))).performClick()
        composeRule.waitForIdle()
        
        runBlocking {
            val updatedMovie = movieDao.getMovieById(favoriteMovie.id)
            assert(updatedMovie?.isSeen == false)
            assert(updatedMovie?.isFavorite == false)
            assert(updatedMovie?.rating == null)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun seenMovie_updateRating_persistsNewRating() {
        val seenMovie = testMovie.copy(isSeen = true, rating = 3.0f)
        
        runBlocking {
            movieDao.insertOrUpdateMovie(seenMovie)
        }
        
        composeRule.activity.setContent {
            var showBottomSheet by rememberSaveable { mutableStateOf(true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AppTheme {
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        DetailScreen(movie = seenMovie)
                    }
                }
            }
        }
        
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.DETAIL_RATING_DISPLAY).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.rate_movie)).assertExists()
        
        val star5Description = context.getString(R.string.star_content_description, 5)
        composeRule.onAllNodesWithContentDescription(star5Description)[1].performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.save))).performClick()
        composeRule.waitForIdle()
        
        runBlocking {
            val updatedMovie = movieDao.getMovieById(seenMovie.id)
            assert(updatedMovie?.rating == 5.0f)
        }
    }
}

