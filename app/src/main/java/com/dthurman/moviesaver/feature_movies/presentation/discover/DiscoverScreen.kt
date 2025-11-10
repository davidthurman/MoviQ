package com.dthurman.moviesaver.feature_movies.presentation.discover

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.presentation.discover.components.SearchBar
import com.dthurman.moviesaver.feature_movies.presentation.shared.MovieList
import com.dthurman.moviesaver.ui.theme.AppTheme

@Composable
fun DiscoverScreen(
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DiscoverScreen(
        modifier = modifier,
        movies = uiState.movies,
        searchHeader = uiState.searchHeader,
        onSearch = { title -> viewModel.onEvent(DiscoverEvent.SearchMovie(title)) },
        onMovieClick = onMovieClick,
        onSettingsClick = onSettingsClick,
        isLoading = uiState.isLoading,
        error = uiState.error
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    modifier: Modifier = Modifier,
    movies: List<Movie>,
    searchHeader: String,
    onSearch: (title: String) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSettingsClick: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            SearchBar(
                onSearch = { query -> onSearch(query) },
                modifier = Modifier.padding(vertical = 20.dp),
                trailingIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.settings_content_description))
                    }
                }
            )
        }
        MovieList(movies = movies, onMovieClick = onMovieClick, searchHeader = searchHeader)
    }
}

val mockMovies = listOf<Movie>()

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        DiscoverScreen(
            movies = mockMovies,
            searchHeader = "Results:",
            onSearch = {},
            onMovieClick = {},
            onSettingsClick = {},
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
            searchHeader = "Results:",
            onSearch = {},
            onMovieClick = {},
            onSettingsClick = {},
            isLoading = false,
            error = null
        )
    }
}