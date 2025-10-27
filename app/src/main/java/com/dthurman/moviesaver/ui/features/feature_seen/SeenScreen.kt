package com.dthurman.moviesaver.ui.features.feature_seen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.components.MovieList
import com.dthurman.moviesaver.ui.components.MoviePreviewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeenScreen(
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    viewModel: SeenViewModel = hiltViewModel()
) {
    val movies by viewModel.movies.collectAsStateWithLifecycle()
    Column(modifier = modifier) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface,
            ),
            title = {
                Text("My Movies")
            }
        )
        if (movies.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
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

//    Scaffold(
//        modifier = modifier.fillMaxSize(),
//        topBar = {
//            TopAppBar(
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
//                ),
//                title = {
//                    Text("My Movies")
//                }
//            )
//        }
//    ) { innerPadding ->
//        Column(modifier = Modifier.padding(innerPadding)) {
//            if (movies.isNotEmpty()) {
//                Box(modifier = Modifier.fillMaxSize()) {
//                    LazyVerticalGrid(
//                        modifier = Modifier.fillMaxSize(),
//                        columns = GridCells.Adaptive(minSize = 128.dp)
//                    ) {
//                        items(movies) { movie ->
//                            MoviePreview(movie, MoviePreviewState.SEEN)
//                        }
//                    }
//                }
//            } else {
//                Text("You haven't saved any movies yet! Use the discover screen to find some.")
//            }
//        }
//    }
}
