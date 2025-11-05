package com.dthurman.moviesaver.feature_movies.presentation.my_movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.domain.util.MovieFilter
import com.dthurman.moviesaver.feature_movies.domain.util.MovieOrder
import com.dthurman.moviesaver.feature_movies.presentation.my_movies.components.EmptyState
import com.dthurman.moviesaver.feature_movies.presentation.my_movies.components.GenericLoadingScreen
import com.dthurman.moviesaver.feature_movies.presentation.shared.MovieList
import com.dthurman.moviesaver.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeenScreen(
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: SeenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        TopBar(title = stringResource(R.string.my_movies), onSettingsClick = onSettingsClick)
        Column(modifier = Modifier.fillMaxSize()) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp)
            ) {
                val filters = listOf(
                    MovieFilter.SeenMovies() to R.string.seen,
                    MovieFilter.WatchlistMovies() to R.string.watchlist
                )
                filters.forEachIndexed { index, (filter, labelRes) ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = filters.size
                        ),
                        onClick = { viewModel.onEvent(SeenEvent.FilterChange(filter)) },
                        selected = filter::class == state.selectedFilter::class,
                        label = { 
                            Text(text = stringResource(labelRes)) 
                        }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier) {
                    TextButton(
                        onClick = { viewModel.onEvent(SeenEvent.ToggleSortMenu) }
                    ) {
                        Text(stringResource(R.string.sort_label, stringResource(state.sortOrder.displayNameRes)))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.sort_options_content_description)
                        )
                    }

                    DropdownMenu(
                        expanded = state.showSortMenu,
                        onDismissRequest = { viewModel.onEvent(SeenEvent.DismissSortMenu) }
                    ) {
                        MovieOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(stringResource(order.displayNameRes)) },
                                onClick = { viewModel.onEvent(SeenEvent.OrderChange(order)) }
                            )
                        }
                    }
                }
                FilterChip(
                    shape = CircleShape,
                    selected = state.showFavoritesOnly,
                    onClick = { viewModel.onEvent(SeenEvent.FavoritesToggled) },
                    label = {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = if (state.showFavoritesOnly) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.show_favorites_content_description)
                        )
                    },
                )
            }
            
            when {
                state.isLoading -> {
                    GenericLoadingScreen()
                }
                state.movies.isNotEmpty() -> {
                    MovieList(
                        modifier = modifier.fillMaxSize(),
                        movies = state.movies,
                        onMovieClick = onMovieClick
                    )
                }
                else -> {
                    EmptyState(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
