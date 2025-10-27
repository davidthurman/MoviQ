package com.dthurman.moviesaver.ui.features.feature_discover

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.components.MoviePreview
import com.dthurman.moviesaver.ui.components.MoviePreviewState
import com.dthurman.moviesaver.ui.components.SearchBar
import com.dthurman.moviesaver.ui.theme.AppTheme

@Composable
fun DiscoverScreen(modifier: Modifier = Modifier, viewModel: DiscoverViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DiscoverScreen(
        modifier = modifier,
        movies = uiState.movies,
        onSearch = { title -> viewModel.searchForMovies(title)},
        onAddMovie = { movie -> viewModel.addMovieToSeen(movie)},
        isLoading = uiState.isLoading,
        error = uiState.error
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    modifier: Modifier = Modifier,
    movies: List<Movie>,
    onSearch: (title: String) -> Unit,
    onAddMovie: (movie: Movie) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    val textFieldState = rememberTextFieldState()

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            SearchBar(
                textFieldState = textFieldState,
                onSearch = { query -> onSearch(query) },
                modifier = Modifier
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            items(movies) { movie ->
                MoviePreview(
                    movie = movie,
                    previewState = MoviePreviewState.DISCOVER,
                    onAddMovieClicked = { onAddMovie(it) }
                )
            }
        }
    }
}

val mockMovies = listOf<Movie>()

// Previews
@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        DiscoverScreen(
            movies = mockMovies,
            onSearch = {},
            onAddMovie = {},
            isLoading = false,
            error = null
        )
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
private fun PortraitPreview() {
    AppTheme {
        DiscoverScreen(
            movies = mockMovies,
            onSearch = {},
            onAddMovie = {},
            isLoading = false,
            error = null
        )
    }
}