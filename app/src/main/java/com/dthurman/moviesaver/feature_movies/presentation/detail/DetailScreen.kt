package com.dthurman.moviesaver.feature_movies.presentation.detail
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.util.TestTags
import com.dthurman.moviesaver.feature_movies.presentation.detail.components.FloatingFavoriteButton
import com.dthurman.moviesaver.feature_movies.presentation.detail.components.PrimaryButton
import com.dthurman.moviesaver.feature_movies.presentation.detail.components.RemoveFromSeenDialog
import com.dthurman.moviesaver.feature_movies.presentation.shared.RatingDialog
import com.dthurman.moviesaver.feature_movies.presentation.shared.StarRatingDisplay

@Composable
fun DetailScreen(
    movie: Movie,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(movie.id) {
        viewModel.loadMovie(movie)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    DetailScreen(
        movie = uiState.movie ?: movie,
        toggleSeen = { viewModel.onEvent(DetailEvent.ToggleSeen) },
        toggleFavorite = { viewModel.onEvent(DetailEvent.ToggleFavorite) },
        toggleWatchlist = { viewModel.onEvent(DetailEvent.ToggleWatchlist) },
        onRatingClick = { viewModel.onEvent(DetailEvent.OpenRatingDialog) },
        modifier = modifier,
    )

    if (uiState.showRatingDialog) {
        uiState.movie?.let {
            RatingDialog(
                movieTitle = it.title,
                currentRating = it.rating,
                onDismiss = { viewModel.onEvent(DetailEvent.DismissRatingDialog) },
                onRatingSelected = { rating -> viewModel.onEvent(DetailEvent.ConfirmSeenWithRating(rating)) }
            )
        }

    }

    if (uiState.showUnseenConfirmDialog) {
        RemoveFromSeenDialog(
            onDismiss = { viewModel.onEvent(DetailEvent.DismissUnseenDialog) },
            onConfirm = { viewModel.onEvent(DetailEvent.ConfirmUnseen) },
            currentMovie = movie
        )
    }
}

@Composable
internal fun DetailScreen(
    movie: Movie,
    toggleSeen: () -> Unit,
    toggleWatchlist: () -> Unit,
    toggleFavorite: () -> Unit,
    onRatingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        DetailImage(movie, toggleFavorite)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag(TestTags.DETAIL_MOVIE_TITLE)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = movie.releaseDate,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (movie.isSeen) {
                StarRatingDisplay(
                    rating = movie.rating,
                    starSize = 20.dp,
                    spacing = 4.dp,
                    onClick = { onRatingClick() },
                    modifier = Modifier.testTag(TestTags.DETAIL_RATING_DISPLAY)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            PrimaryButton(
                modifier = Modifier.fillMaxWidth().weight(1f).testTag(TestTags.DETAIL_WATCHLIST_BUTTON),
                text = stringResource(R.string.watchlist),
                onClick = toggleWatchlist,
                isFilled = movie.isWatchlist,
                startIcon = if (!movie.isWatchlist) Icons.Outlined.Add else Icons.Outlined.Check,
                iconContentDescription = if (movie.isWatchlist) stringResource(R.string.in_watchlist) else stringResource(R.string.add_to_watchlist)
            )
            Spacer(Modifier.width(16.dp))
            PrimaryButton(
                modifier = Modifier.fillMaxWidth().weight(1f).testTag(TestTags.DETAIL_SEEN_BUTTON),
                text = stringResource(R.string.seen),
                onClick = toggleSeen,
                isFilled = movie.isSeen,
                startIcon = if (!movie.isSeen) Icons.Outlined.Add else Icons.Outlined.Check,
                iconContentDescription = if (movie.isSeen) stringResource(R.string.in_seen) else stringResource(R.string.add_to_seen)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.overview),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = movie.overview,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            modifier = Modifier.testTag(TestTags.DETAIL_MOVIE_OVERVIEW)
        )
    }
}

@Composable
private fun DetailImage(
    movie: Movie,
    toggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(movie.backdropUrl)
                .crossfade(true)
                .build(),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .testTag(TestTags.MOVIE_DETAIL_IMAGE),
            loading = {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            error = {
                Image(
                    painter = painterResource(R.drawable.background_placeholder),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                )
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )
        if (movie.isSeen) {
            FloatingFavoriteButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp, top = 8.dp)
                    .testTag(TestTags.SET_MOVIE_AS_FAVORITE_BUTTON),
                isFavorite = movie.isFavorite,
                onClick = toggleFavorite,
            )
        }
    }
}