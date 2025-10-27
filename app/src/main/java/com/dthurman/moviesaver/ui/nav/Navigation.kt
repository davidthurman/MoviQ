package com.dthurman.moviesaver.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.features.feature_discover.DiscoverScreen
import com.dthurman.moviesaver.ui.features.feature_seen.SeenScreen
import com.dthurman.moviesaver.ui.features.feature_watchlist.WatchlistScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.SEEN -> SeenScreen(onMovieClick = onMovieClick)
                    Destination.WATCHLIST -> WatchlistScreen(onMovieClick = onMovieClick)
                    Destination.DISCOVER -> DiscoverScreen(onMovieClick = onMovieClick)
                }
            }
        }
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    SEEN("seen", "Seen", Icons.AutoMirrored.Default.List, "Seen"),
    WATCHLIST("watchlist", "Watchlist", Icons.Default.Add, "Watchlist"),
    DISCOVER("discover", "Search", Icons.Default.Search, "Search"),
}
