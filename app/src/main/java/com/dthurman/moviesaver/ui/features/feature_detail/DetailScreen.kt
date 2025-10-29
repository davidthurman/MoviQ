package com.dthurman.moviesaver.ui.features.feature_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import coil.compose.AsyncImage
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.components.FloatingFavoriteButton

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
    
    DetailScreen(
        movie = currentMovie,
        toggleSeen = { viewModel.toggleSeen() },
        toggleFavorite = { viewModel.toggleFavorite() },
        toggleWatchlist = { viewModel.toggleWatchlist() },
        modifier = modifier,
    )
}

@Composable
internal fun DetailScreen(
    movie: Movie,
    toggleSeen: () -> Unit,
    toggleWatchlist: () -> Unit,
    toggleFavorite: () -> Unit,
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
            val imagePosterUrl = "https://image.tmdb.org/t/p/w1280${movie.backdropUrl}"
            AsyncImage(
                model = imagePosterUrl,
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_background),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
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
            FloatingFavoriteButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp, top = 4.dp),
                isFavorite = movie.isFavorite,
                onClick = toggleFavorite,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = movie.releaseDate,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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