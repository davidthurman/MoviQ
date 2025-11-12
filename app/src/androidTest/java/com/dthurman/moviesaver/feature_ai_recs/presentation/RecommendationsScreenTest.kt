package com.dthurman.moviesaver.feature_ai_recs.presentation

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
import com.dthurman.moviesaver.core.data.repository.FakeCreditsRepository
import com.dthurman.moviesaver.core.data.repository.FakeUserRepository
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.util.TestTags
import com.dthurman.moviesaver.di.AppBindingModule
import com.dthurman.moviesaver.di.AppModule
import com.dthurman.moviesaver.di.ObservabilityModule
import com.dthurman.moviesaver.feature_ai_recs.data.repository.FakeAiRepository
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
class RecommendationsScreenTest {

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
            val navController = rememberNavController()
            AppTheme {
                AppNavHost(
                    navController = navController,
                    startDestination = Destination.RECOMMENDATIONS.route,
                    onMovieClick = { },
                    onSettingsClick = { }
                )
            }
        }
    }

    @Test
    fun initialState_showsGenerateButton() {
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.get_ai_recommendations)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).assertExists()
    }

    @Test
    fun generateWithLessThan5Movies_showsMinimumDialog() {
        runBlocking {
            val movies = listOf(
                Movie(
                    id = 1,
                    title = "Movie 1",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-01-01",
                    overview = "Overview",
                    isSeen = true,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                ),
                Movie(
                    id = 2,
                    title = "Movie 2",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-01-02",
                    overview = "Overview",
                    isSeen = true,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            )
            movies.forEach { movieDao.insertOrUpdateMovie(it) }
        }
        
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNode(hasText(context.getString(R.string.add_minimum_movies_message), substring = true)).assertExists()
    }

    @Test
    fun generateRecommendations_displaysFirstRecommendation() {
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
                    title = "AI Recommendation 1",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-01",
                    overview = "AI recommended movie",
                    aiReason = "Because you loved similar movies",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                ),
                Movie(
                    id = 101,
                    title = "AI Recommendation 2",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-02",
                    overview = "Another AI recommended movie",
                    aiReason = "Based on your viewing history",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            )
            
            (aiRepository as FakeAiRepository).recommendationsToGenerate = recommendations
        }
        
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("AI Recommendation 1")).assertExists()
        composeRule.onNode(hasText("Because you loved similar movies", substring = true)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.skip)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.watchlist)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.seen)).assertExists()
    }

    @Test
    fun skipRecommendation_showsNextRecommendation() {
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
                    title = "First Recommendation",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-01",
                    overview = "First movie",
                    aiReason = "Reason 1",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                ),
                Movie(
                    id = 101,
                    title = "Second Recommendation",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-02",
                    overview = "Second movie",
                    aiReason = "Reason 2",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            )
            
            (aiRepository as FakeAiRepository).recommendationsToGenerate = recommendations
        }
        
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("First Recommendation")).assertExists()
        
        composeRule.onNodeWithText(context.getString(R.string.skip)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Second Recommendation")).assertExists()
        composeRule.onNode(hasText("Reason 2", substring = true)).assertExists()
    }

    @Test
    fun markRecommendationAsSeen_showsRatingDialog() {
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
        
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Movie To Rate")).assertExists()
        
        composeRule.onNodeWithText(context.getString(R.string.seen)).performClick()
        composeRule.waitForIdle()
        
        val star3Description = context.getString(R.string.star_content_description, 3)
        composeRule.onNodeWithContentDescription(star3Description).assertExists()
    }

    @Test
    fun skipAllRecommendations_returnsToInitialState() {
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
                    title = "Only Recommendation",
                    posterUrl = "",
                    backdropUrl = "",
                    releaseDate = "2024-02-01",
                    overview = "Single movie",
                    aiReason = "Just one",
                    isSeen = false,
                    isWatchlist = false,
                    isFavorite = false,
                    rating = null
                )
            )
            
            (aiRepository as FakeAiRepository).recommendationsToGenerate = recommendations
        }
        
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag(TestTags.moviePreview("Only Recommendation")).assertExists()
        
        composeRule.onNodeWithText(context.getString(R.string.skip)).performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithText(context.getString(R.string.get_ai_recommendations)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.generate_recommendations)).assertExists()
    }
}

