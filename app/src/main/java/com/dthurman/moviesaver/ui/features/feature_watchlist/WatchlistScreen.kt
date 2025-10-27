package com.dthurman.moviesaver.ui.features.feature_watchlist

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dthurman.moviesaver.domain.model.Movie

@Composable
fun WatchlistScreen(
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    viewModel: WatchlistViewModel = hiltViewModel()
) {

}
