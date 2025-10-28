package com.dthurman.moviesaver.ui.nav

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.features.feature_discover.DiscoverScreen
import com.dthurman.moviesaver.ui.features.feature_favorites.FavoritesScreen
import com.dthurman.moviesaver.ui.features.feature_seen.SeenScreen
import com.dthurman.moviesaver.ui.features.feature_watchlist.WatchlistScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    onMovieClick: (Movie) -> Unit,
    onSettingsClick: () -> Unit,
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
                    Destination.SEEN -> SeenScreen(
                        onMovieClick = onMovieClick,
                        onSettingsClick = onSettingsClick
                    )
                    Destination.FAVORITES -> FavoritesScreen(
                        onMovieClick = onMovieClick,
                        onSettingsClick = onSettingsClick
                    )
                    Destination.WATCHLIST -> WatchlistScreen(
                        onMovieClick = onMovieClick,
                        onSettingsClick = onSettingsClick
                    )
                    Destination.DISCOVER -> DiscoverScreen(
                        onMovieClick = onMovieClick,
                        onSettingsClick = onSettingsClick
                    )
                }
            }
        }
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
    val contentDescription: String
) {
    SEEN("seen", "Seen", iconRes = R.drawable.outline_visibility_24, contentDescription = "Seen"),
    FAVORITES("favorites", "Favorites", icon = Icons.Default.Favorite, contentDescription = "Favorite"),
    WATCHLIST("watchlist", "Watchlist", icon = Icons.Default.Add, contentDescription = "Watchlist"),
    DISCOVER("discover", "Search", icon = Icons.Default.Search, contentDescription = "Search"),
}
