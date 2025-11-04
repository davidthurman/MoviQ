package com.dthurman.moviesaver.feature_movies.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dthurman.moviesaver.core.domain.model.Movie

@Composable
fun MovieList(
    movies: List<Movie>,
    modifier: Modifier = Modifier,
    searchHeader: String? = null,
    onMovieClick: (Movie) -> Unit = {},
) {
    Box(modifier = modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            columns = GridCells.Adaptive(minSize = 128.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            if (searchHeader != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(searchHeader)
                }
            }
            items(movies) { movie ->
                MoviePreview(
                    movie = movie,
                    onMovieClick = onMovieClick,
                )
            }
        }
        Box(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.1f)
                        )
                    )
                ).align(Alignment.BottomCenter)
        )
    }

}