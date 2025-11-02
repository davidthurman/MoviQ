package com.dthurman.moviesaver.ui.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
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
import com.dthurman.moviesaver.ui.features.feature_recommendations.RecommendationsScreen
import com.dthurman.moviesaver.ui.features.feature_seen.SeenScreen

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
                    Destination.RECOMMENDATIONS -> RecommendationsScreen(
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
    @StringRes val labelRes: Int,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
    @StringRes val contentDescriptionRes: Int
) {
    SEEN("seen", R.string.nav_movies, iconRes = R.drawable.outline_visibility_24, contentDescriptionRes = R.string.nav_movies),
    RECOMMENDATIONS("recommendations", R.string.nav_recommendations, iconRes = R.drawable.outline_wand_stars_24, contentDescriptionRes = R.string.nav_recommendations),
    DISCOVER("discover", R.string.nav_search, icon = Icons.Default.Search, contentDescriptionRes = R.string.nav_search),
}
