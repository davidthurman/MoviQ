package com.dthurman.moviesaver.feature_movies.presentation

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.app.AppScaffold
import com.dthurman.moviesaver.core.app.MainActivity
import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.util.TestTags
import com.dthurman.moviesaver.di.AppBindingModule
import com.dthurman.moviesaver.di.AppModule
import com.dthurman.moviesaver.di.ObservabilityModule
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
@UninstallModules(AppModule::class, AppBindingModule::class, ObservabilityModule::class)
class MoviesEndToEndTest {

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
    fun endToEnd_openSeenMovie_viewDetail_closeDetail() {
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
        composeRule.onNodeWithTag(TestTags.moviePreview("Test Movie")).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.MOVIE_DETAIL_IMAGE).assertExists()
        
        Espresso.pressBack()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Test Movie")).assertExists()
    }

    @Test
    fun endToEnd_watchlistMovieToSeen() {
        runBlocking {
            val watchlistMovie = Movie(
                id = 2,
                title = "Watchlist Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "A great movie to watch",
                isSeen = false,
                isWatchlist = true,
                isFavorite = false,
                rating = null
            )
            movieDao.insertOrUpdateMovie(watchlistMovie)
        }

        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.watchlist)) and !hasText(context.getString(R.string.in_watchlist))).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.moviePreview("Watchlist Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Watchlist Movie")).performClick()
        composeRule.waitForIdle()

        val addToSeenDescription = context.getString(R.string.add_to_seen)
        composeRule.onNodeWithContentDescription(addToSeenDescription).performClick()
        composeRule.waitForIdle()

        val star4Description = context.getString(R.string.star_content_description, 4)
        composeRule.onNodeWithContentDescription(star4Description).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasText(context.getString(R.string.save))).performClick()
        composeRule.waitForIdle()

        Espresso.pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.moviePreview("Watchlist Movie")).assertDoesNotExist()
    }

    @Test
    fun endToEnd_markMovieAsFavorite_thenToggleFilter() {
        runBlocking {
            val movie1 = Movie(
                id = 3,
                title = "Regular Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "A regular movie",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null
            )
            movieDao.insertOrUpdateMovie(movie1)
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.moviePreview("Regular Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Regular Movie")).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.MOVIE_DETAIL_IMAGE).assertExists()
        composeRule.onNodeWithTag(TestTags.SET_MOVIE_AS_FAVORITE_BUTTON).performClick()
        composeRule.waitForIdle()

        Espresso.pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.moviePreview("Regular Movie")).assertExists()
        
        composeRule.onNodeWithTag(TestTags.TOGGLE_FAVORITE_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.moviePreview("Regular Movie")).assertExists()
    }

    @Test
    fun endToEnd_sortMovies_openDetail_verifyPersistence() {
        runBlocking {
            val movieA = Movie(
                id = 4,
                title = "A Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "First alphabetically",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null,
                addedAt = System.currentTimeMillis() - 1000000
            )
            val movieZ = Movie(
                id = 5,
                title = "Z Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-02",
                overview = "Last alphabetically",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null
            )
            movieDao.insertOrUpdateMovie(movieA)
            movieDao.insertOrUpdateMovie(movieZ)
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.MY_MOVIES_SORT_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasText(context.getString(R.string.sort_title_asc))).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.moviePreview("A Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Z Movie")).assertExists()

        composeRule.onNodeWithTag(TestTags.moviePreview("A Movie")).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.MOVIE_DETAIL_IMAGE).assertExists()
        
        Espresso.pressBack()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("A Movie")).assertExists()
    }

    @Test
    fun endToEnd_multipleMovies_switchTabs_verifyCorrectMoviesDisplay() {
        runBlocking {
            val seenMovie = Movie(
                id = 6,
                title = "Seen Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "Seen movie",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null
            )
            val watchlistMovie = Movie(
                id = 7,
                title = "Watchlist Movie 2",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-02",
                overview = "Watchlist movie",
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
        composeRule.onNodeWithTag(TestTags.moviePreview("Watchlist Movie 2")).assertDoesNotExist()

        composeRule.onNode(hasText(context.getString(R.string.watchlist)) and !hasText(context.getString(R.string.in_watchlist))).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.moviePreview("Watchlist Movie 2")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Seen Movie")).assertDoesNotExist()
    }

    @Test
    fun endToEnd_favoriteFilter_showsOnlyFavorites() {
        runBlocking {
            val favoriteMovie = Movie(
                id = 8,
                title = "Favorite Movie",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-01",
                overview = "Favorite",
                isSeen = true,
                isWatchlist = false,
                isFavorite = true,
                rating = null
            )
            val regularMovie = Movie(
                id = 9,
                title = "Regular Movie 2",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                releaseDate = "2024-01-02",
                overview = "Regular",
                isSeen = true,
                isWatchlist = false,
                isFavorite = false,
                rating = null
            )
            movieDao.insertOrUpdateMovie(favoriteMovie)
            movieDao.insertOrUpdateMovie(regularMovie)
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.moviePreview("Favorite Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Regular Movie 2")).assertExists()

        composeRule.onNodeWithTag(TestTags.TOGGLE_FAVORITE_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.moviePreview("Favorite Movie")).assertExists()
        composeRule.onNodeWithTag(TestTags.moviePreview("Regular Movie 2")).assertDoesNotExist()
    }

}
