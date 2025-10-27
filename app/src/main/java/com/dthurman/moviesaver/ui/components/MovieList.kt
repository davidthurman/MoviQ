package com.dthurman.moviesaver.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dthurman.moviesaver.domain.model.Movie

@Composable
fun MovieList(
    movies: List<Movie>,
    previewState: MoviePreviewState,
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    onAddMovieClicked: (Movie) -> Unit = {},
    onRemoveMovieClicked: (Movie) -> Unit = {}
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxWidth(),
        columns = GridCells.Adaptive(minSize = 128.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(movies) { movie ->
            MoviePreview(
                movie = movie,
                previewState = previewState,
                onMovieClick = onMovieClick,
                onAddMovieClicked = onAddMovieClicked,
                onRemoveMovieClicked = onRemoveMovieClicked
            )
        }
    }
}