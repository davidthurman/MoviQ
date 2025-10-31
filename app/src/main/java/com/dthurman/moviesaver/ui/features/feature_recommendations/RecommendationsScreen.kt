package com.dthurman.moviesaver.ui.features.feature_recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.components.MinimumMoviesDialog
import com.dthurman.moviesaver.ui.components.MovieRecommendationCard
import com.dthurman.moviesaver.ui.components.RatingDialog
import com.dthurman.moviesaver.ui.components.TopBar

@Composable
fun RecommendationsScreen(
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: RecommendationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showRatingDialog by viewModel.showRatingDialog.collectAsStateWithLifecycle()
    val showMinimumMoviesDialog by viewModel.showMinimumMoviesDialog.collectAsStateWithLifecycle()
    val seenMoviesCount by viewModel.seenMoviesCount.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(
            title = "AI Recommendations",
            onSettingsClick = onSettingsClick
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.generateAiRecommendations() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.hasRecommendations() -> {
                    val currentRec = uiState.getCurrentRecommendation()
                    if (currentRec != null) {
                        MovieRecommendationCard(
                            recommendation = currentRec,
                            onMovieClick = onMovieClick,
                            onSkip = { viewModel.skipToNext() },
                            onAddToWatchlist = { viewModel.addToWatchlist() },
                            onMarkAsSeen = { viewModel.showRatingDialog() },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AllDoneState(
                            onGenerateMore = { viewModel.generateAiRecommendations() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                else -> {
                    InitialState(
                        onGenerate = { viewModel.generateAiRecommendations() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showRatingDialog) {
        val currentRec = uiState.getCurrentRecommendation()
        if (currentRec != null) {
            RatingDialog(
                movieTitle = currentRec.movie.title,
                currentRating = currentRec.movie.rating,
                onDismiss = { viewModel.dismissRatingDialog() },
                onRatingSelected = { rating ->
                    viewModel.markAsSeenWithRating(rating)
                }
            )
        }
    }

    if (showMinimumMoviesDialog) {
        MinimumMoviesDialog(
            currentCount = seenMoviesCount,
            onDismiss = { viewModel.dismissMinimumMoviesDialog() }
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Generating personalized recommendations...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun InitialState(
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✨",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Get AI-Powered Recommendations",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Let AI analyze your viewing history and suggest movies you'll love",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGenerate) {
            Text("Generate Recommendations")
        }
    }
}

@Composable
private fun AllDoneState(
    onGenerateMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "All Out of Recommendations!",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You've reviewed all your recommendations. Ready for more?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGenerateMore) {
            Text("Generate More")
        }
    }
}