package com.dthurman.moviesaver.ui.features.feature_recommendations

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.components.MovieRecommendationCard
import com.dthurman.moviesaver.ui.components.TopBar
import com.dthurman.moviesaver.ui.components.dialogs.MinimumMoviesDialog
import com.dthurman.moviesaver.ui.components.dialogs.NoCreditsDialog
import com.dthurman.moviesaver.ui.components.dialogs.PurchaseSuccessDialog
import com.dthurman.moviesaver.ui.components.dialogs.RatingDialog

@Composable
fun RecommendationsScreen(
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: RecommendationsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RecommendationEvent.LaunchPurchaseFlow -> {
                    activity?.let { viewModel.authRepository.launchPurchaseFlow(it) }
                }
            }
        }
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showRatingDialog by viewModel.showRatingDialog.collectAsStateWithLifecycle()
    val showMinimumMoviesDialog by viewModel.showMinimumMoviesDialog.collectAsStateWithLifecycle()
    val showNoCreditsDialog by viewModel.showNoCreditsDialog.collectAsStateWithLifecycle()
    val showPurchaseSuccessDialog by viewModel.showPurchaseSuccessDialog.collectAsStateWithLifecycle()
    val seenMoviesCount by viewModel.seenMoviesCount.collectAsStateWithLifecycle()
    val userCredits by viewModel.userCredits.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(
            title = stringResource(R.string.ai_recommendations),
            onSettingsClick = onSettingsClick
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (!uiState.isLoading && uiState.error == null && !uiState.hasRecommendations()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.available_credits, userCredits),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            when {
                uiState.isLoading -> {
                    LoadingState(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error ?: stringResource(R.string.error_unknown),
                        onRetry = { viewModel.generateAiRecommendations() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.hasRecommendations() -> {
                    val currentRec = uiState.getCurrentRecommendation()
                    if (currentRec != null) {
                        MovieRecommendationCard(
                            movie = currentRec,
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
                movieTitle = currentRec.title,
                currentRating = currentRec.rating,
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
    
    if (showNoCreditsDialog) {
        NoCreditsDialog(
            onDismiss = { viewModel.dismissNoCreditsDialog() },
            onPurchaseClick = { viewModel.purchaseCredits() }
        )
    }
    
    if (showPurchaseSuccessDialog) {
        PurchaseSuccessDialog(
            onDismiss = { viewModel.dismissPurchaseSuccessDialog() }
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
            text = stringResource(R.string.generating_recommendations),
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
            text = stringResource(R.string.error_something_wrong),
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
            Text(stringResource(R.string.retry))
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
        Image(painter = painterResource(R.drawable.ai_icon), stringResource(R.string.ai_icon_content_description))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.get_ai_recommendations),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ai_recommendations_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGenerate) {
            Text(stringResource(R.string.generate_recommendations))
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
            text = stringResource(R.string.all_out_of_recommendations),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.reviewed_all_recommendations),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGenerateMore) {
            Text(stringResource(R.string.generate_more))
        }
    }
}