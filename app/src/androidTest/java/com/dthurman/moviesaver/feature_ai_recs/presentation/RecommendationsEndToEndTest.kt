package com.dthurman.moviesaver.feature_ai_recs.presentation

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import com.dthurman.moviesaver.core.data.repository.FakeCreditsRepository
import com.dthurman.moviesaver.core.data.repository.FakeUserRepository
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.feature_ai_recs.data.repository.FakeAiRepository
import com.dthurman.moviesaver.feature_ai_recs.data.repository.FakeRecommendationRepository
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.RecommendationRepository
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
class RecommendationsEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var movieDao: MovieDao
    
    @Inject
    lateinit var aiRepository: AiRepository
    
    @Inject
    lateinit var recommendationRepository: RecommendationRepository
    
    @Inject
    lateinit var creditsRepository: CreditsRepository
    
    @Inject
    lateinit var userRepository: UserRepository
    
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
    fun endToEnd_navigateToRecommendations_clickRecommendation_opensDetailSheet() {
        runBlocking {
            val testUser = User(
                id = "test_user_123",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = null,
                credits = 10
            )
            (userRepository as FakeUserRepository).setCurrentUser(testUser)
            (creditsRepository as FakeCreditsRepository).setCredits(10)
            
            val seenMovies = (1..5).map { i ->
                Movie(
                    id = i,
                    title = "Seen Movie $i",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-01-0$i",
                    overview = "Overview $i",
                    isSeen = true,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            }
            seenMovies.forEach { movieDao.insertOrUpdateMovie(it) }
            
            val recommendations = listOf(
                Movie(
                    id = 100,
                    title = "Detail Movie",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-01",
                    overview = "Movie with details",
                    aiReason = "Check this out",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            )
            
            (aiRepository as FakeAiRepository).recommendationsToGenerate = recommendations
        }
        
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_recommendations))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Detail Movie")).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.MOVIE_DETAIL_IMAGE).assertExists()
        composeRule.onNodeWithText("Detail Movie").assertExists()
        
        Espresso.pressBack()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Detail Movie")).assertExists()
    }

    @Test
    fun endToEnd_addRecommendationToWatchlist_navigateToWatchlist_verifyMovieExists() {
        runBlocking {
            val testUser = User(
                id = "test_user_123",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = null,
                credits = 10
            )
            (userRepository as FakeUserRepository).setCurrentUser(testUser)
            (creditsRepository as FakeCreditsRepository).setCredits(10)
            
            val seenMovies = (1..5).map { i ->
                Movie(
                    id = i,
                    title = "Seen Movie $i",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-01-0$i",
                    overview = "Overview $i",
                    isSeen = true,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            }
            seenMovies.forEach { movieDao.insertOrUpdateMovie(it) }
            
            val recommendations = listOf(
                Movie(
                    id = 100,
                    title = "Watchlist Movie",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-01",
                    overview = "Movie to add to watchlist",
                    aiReason = "You'll love this",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            )
            
            (aiRepository as FakeAiRepository).recommendationsToGenerate = recommendations
        }
        
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_recommendations))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Watchlist Movie")).assertExists()
        
        composeRule.onNodeWithText(context.getString(R.string.watchlist)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.watchlist)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Watchlist Movie")).assertExists()
    }

    @Test
    fun endToEnd_markRecommendationAsSeen_navigateToSeen_verifyMovieExists() {
        runBlocking {
            val testUser = User(
                id = "test_user_123",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = null,
                credits = 10
            )
            (userRepository as FakeUserRepository).setCurrentUser(testUser)
            (creditsRepository as FakeCreditsRepository).setCredits(10)
            
            val seenMovies = (1..5).map { i ->
                Movie(
                    id = i,
                    title = "Seen Movie $i",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-01-0$i",
                    overview = "Overview $i",
                    isSeen = true,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            }
            seenMovies.forEach { movieDao.insertOrUpdateMovie(it) }
            
            val recommendations = listOf(
                Movie(
                    id = 100,
                    title = "Movie To Rate",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-01",
                    overview = "Movie to mark as seen",
                    aiReason = "Perfect for you",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            )
            
            (aiRepository as FakeAiRepository).recommendationsToGenerate = recommendations
        }
        
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_recommendations))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Movie To Rate")).assertExists()
        
        composeRule.onNodeWithText(context.getString(R.string.seen)).performClick()
        composeRule.waitForIdle()
        
        val star3Description = context.getString(R.string.star_content_description, 3)
        composeRule.onNodeWithContentDescription(star3Description).assertExists()
        composeRule.onNodeWithContentDescription(star3Description).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.save))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Movie To Rate")).assertExists()
    }

    @Test
    fun endToEnd_navigateBetweenScreens_recommendationsStatePersists() {
        runBlocking {
            val testUser = User(
                id = "test_user_123",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = null,
                credits = 10
            )
            (userRepository as FakeUserRepository).setCurrentUser(testUser)
            (creditsRepository as FakeCreditsRepository).setCredits(10)
            
            val seenMovies = (1..5).map { i ->
                Movie(
                    id = i,
                    title = "Seen Movie $i",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-01-0$i",
                    overview = "Overview $i",
                    isSeen = true,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            }
            seenMovies.forEach { movieDao.insertOrUpdateMovie(it) }
            
            val recommendations = listOf(
                Movie(
                    id = 100,
                    title = "First Rec",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-01",
                    overview = "First",
                    aiReason = "Great choice",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                ),
                Movie(
                    id = 101,
                    title = "Second Rec",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-02",
                    overview = "Second",
                    aiReason = "Another one",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            )
            
            (aiRepository as FakeAiRepository).recommendationsToGenerate = recommendations
        }
        
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_recommendations))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("First Rec")).assertExists()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_movies))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_search))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.nav_recommendations))).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("First Rec")).assertExists()
    }
}

