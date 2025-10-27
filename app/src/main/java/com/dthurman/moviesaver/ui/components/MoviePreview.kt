package com.dthurman.moviesaver.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
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
    onAddMovieClicked: (Movie) -> Unit = {},
    onRemoveMovieClicked: (Movie) -> Unit = {}
) {
    Box(modifier = modifier) {
        val imagePosterUrl = "https://image.tmdb.org/t/p/w500" + movie.posterUrl
        AsyncImage(
            model = imagePosterUrl,
            placeholder = painterResource(R.drawable.ic_launcher_foreground),
            error = painterResource(R.drawable.ic_launcher_background),
            contentDescription = movie.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(),
            onError = { e ->
                print(e)
            }
        )
        Text(
            movie.title,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .shadow(10.dp, RectangleShape)
                .fillMaxWidth()
        )
        if (previewState == MoviePreviewState.DISCOVER) {
            FloatingActionButton(onClick = { onAddMovieClicked(movie) }, modifier = Modifier.align(Alignment.BottomEnd)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Movie to Seen")
            }
        }
    }
}

enum class MoviePreviewState {
    DISCOVER,
    SEEN
}