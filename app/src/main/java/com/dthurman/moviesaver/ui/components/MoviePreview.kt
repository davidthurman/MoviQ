package com.dthurman.moviesaver.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.domain.model.Movie

@Composable
fun MoviePreview(
    movie: Movie,
    previewState: MoviePreviewState,
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    onAddMovieClicked: (Movie) -> Unit = {},
    onRemoveMovieClicked: (Movie) -> Unit = {}
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
                    onError = { e ->
                        print(e)
                    }
                )
//                if (previewState == MoviePreviewState.DISCOVER) {
//                    FloatingActionButton(
//                        onClick = { onAddMovieClicked(movie) },
//                        modifier = Modifier
//                            .align(Alignment.TopEnd)
//                            .padding(8.dp),
//                        containerColor = MaterialTheme.colorScheme.primaryContainer,
//                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Add,
//                            contentDescription = "Add Movie to Seen",
//                            tint = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                    }
//                }
            }
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(80.dp)
//                    .padding(12.dp)
//            ) {
//                Text(
//                    text = movie.title,
//                    style = MaterialTheme.typography.titleMedium,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Text(
//                    text = movie.releaseDate,
//                    style = MaterialTheme.typography.bodyMedium,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    modifier = Modifier.padding(top = 4.dp)
//                )
//            }
        }
    }
}

enum class MoviePreviewState {
    DISCOVER,
    SEEN
}