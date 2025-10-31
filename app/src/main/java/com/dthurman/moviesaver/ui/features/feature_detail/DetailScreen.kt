package com.dthurman.moviesaver.ui.features.feature_detail

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.components.FloatingFavoriteButton
import com.dthurman.moviesaver.ui.components.RatingDialog
import com.dthurman.moviesaver.ui.components.StarRatingDisplay

@Composable
fun DetailScreen(
    movie: Movie,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(movie.id) {
        viewModel.loadMovie(movie)
    }

    val observedMovie by viewModel.movie.collectAsState()
    val currentMovie = observedMovie ?: movie
    val showRatingDialog by viewModel.showRatingDialog.collectAsState()
    val showUnseenConfirmDialog by viewModel.showUnseenConfirmDialog.collectAsState()
    
    DetailScreen(
        movie = currentMovie,
        toggleSeen = { viewModel.toggleSeen() },
        toggleFavorite = { viewModel.toggleFavorite() },
        toggleWatchlist = { viewModel.toggleWatchlist() },
        onRatingClick = { viewModel.openRatingDialog() },
        modifier = modifier,
    )

    if (showRatingDialog) {
        RatingDialog(
            movieTitle = currentMovie.title,
            currentRating = currentMovie.rating,
            onDismiss = { viewModel.dismissRatingDialog() },
            onRatingSelected = { rating -> viewModel.confirmSeenWithRating(rating) }
        )
    }

    if (showUnseenConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUnseenDialog() },
            title = {
                Text(
                    text = "Remove from Seen?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                val message = if (currentMovie.isFavorite) {
                    "This will remove \"${currentMovie.title}\" from your seen movies and favorites, and clear its rating."
                } else {
                    "This will remove \"${currentMovie.title}\" from your seen movies and clear its rating."
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmUnseen() }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissUnseenDialog() }
                ) {
                    Text("Cancel")
                }
            }
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            SubcomposeAsyncImage(
                model = movie.backdropUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    androidx.compose.foundation.Image(
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
                        .padding(end = 8.dp, top = 8.dp),
                    isFavorite = movie.isFavorite,
                    onClick = toggleFavorite,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
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
                    onClick = { onRatingClick() }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            if (!movie.isWatchlist) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().weight(1.0f),
                    onClick = toggleWatchlist,
                ) {
                    Icon(Icons.Outlined.Add, "Add to Watchlist")
                    Spacer(Modifier.width(8.dp))
                    Text("Watchlist")
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth().weight(1.0f),
                    onClick = toggleWatchlist,
                ) {
                    Icon(Icons.Outlined.Check, "In Watchlist")
                    Spacer(Modifier.width(8.dp))
                    Text("Watchlist")
                }
            }
            Spacer(Modifier.width(16.dp))
            if (!movie.isSeen) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().weight(1.0f),
                    onClick = toggleSeen,
                ) {
                    Icon(painter = painterResource(R.drawable.outline_visibility_24), "Add to Seen")
                    Spacer(Modifier.width(8.dp))
                    Text("Seen")
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth().weight(1.0f),
                    onClick = toggleSeen,
                ) {
                    Icon(Icons.Outlined.Check, "In Seen")
                    Spacer(Modifier.width(8.dp))
                    Text("Seen")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = movie.overview,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}