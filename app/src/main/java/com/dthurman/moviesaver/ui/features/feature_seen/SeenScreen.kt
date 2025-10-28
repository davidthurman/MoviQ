package com.dthurman.moviesaver.ui.features.feature_seen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.components.MovieList
import com.dthurman.moviesaver.ui.components.MoviePreviewState
import com.dthurman.moviesaver.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeenScreen(
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: SeenViewModel = hiltViewModel()
) {
    val movies by viewModel.movies.collectAsStateWithLifecycle()
    Column(modifier = modifier) {
        TopBar(title = "Seen", onSettingsClick = onSettingsClick)
        if (movies.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 16.dp)) {
                MovieList(
                    movies = movies,
                    previewState = MoviePreviewState.SEEN,
                    onMovieClick = onMovieClick
                )
            }
        } else {
            Text("You haven't saved any movies yet! Use the discover screen to find some.")
        }
    }
}
