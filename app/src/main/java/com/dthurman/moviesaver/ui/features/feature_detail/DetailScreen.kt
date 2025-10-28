package com.dthurman.moviesaver.ui.features.feature_detail

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.domain.model.Movie

@Composable
fun DetailScreen(
    movie: Movie,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    // Load the movie into the ViewModel on first composition
    LaunchedEffect(movie.id) {
        viewModel.loadMovie(movie.id)
    }
    
    // Observe the movie state from the ViewModel
    val observedMovie by viewModel.movie.collectAsState()
    
    // Use the observed movie if available, otherwise use the passed movie
    val currentMovie = observedMovie ?: movie
    
    DetailScreen(
        movie = currentMovie,
        toggleSeen = { viewModel.toggleSeen() },
        toggleWatchlist = { viewModel.toggleWatchlist() },
        modifier = modifier,
    )
}

@Composable
internal fun DetailScreen(
    movie: Movie,
    toggleSeen: () -> Unit,
    toggleWatchlist: () -> Unit,
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
                modifier = Modifier
                    .fillMaxWidth()
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
                    Icon(Icons.Outlined.Add, "In Watchlist")
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
                    Icon(Icons.Outlined.Add, "Add to Seen")
                    Spacer(Modifier.width(8.dp))
                    Text("Seen")
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth().weight(1.0f),
                    onClick = toggleSeen,
                ) {
                    Icon(Icons.Outlined.Add, "Marked as Seen")
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