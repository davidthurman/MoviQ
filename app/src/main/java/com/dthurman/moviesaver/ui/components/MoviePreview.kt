package com.dthurman.moviesaver.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.domain.model.Movie

@Composable
fun MoviePreview(
    movie: Movie,
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onMovieClick(movie) },
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier) {
            Box {
                val imagePosterUrl = "https://image.tmdb.org/t/p/w500" + movie.posterUrl
                AsyncImage(
                    model = imagePosterUrl,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                    error = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f),
                    onError = { e -> print(e) }
                )
                if (movie.isFavorite) {
                    Box(modifier = Modifier
                        .padding(top = 12.dp)
                        .padding(end = 12.dp)
                        .clip(shape = CircleShape)
                        .align(Alignment.TopEnd)
                        .background(color = MaterialTheme.colorScheme.primaryContainer),

                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(6.dp)
                                .size(24.dp),
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Hearted",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                if (movie.rating != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 8.dp, bottom = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        StarRatingDisplay(
                            rating = movie.rating,
                            starSize = 12.dp,
                            spacing = 2.dp
                        )
                    }
                }
            }
        }
    }
}