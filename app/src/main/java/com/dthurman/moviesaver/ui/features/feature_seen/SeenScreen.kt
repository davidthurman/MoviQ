package com.dthurman.moviesaver.ui.features.feature_seen

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.ui.components.EmptyState
import com.dthurman.moviesaver.ui.components.MovieList
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
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val showSortMenu by viewModel.showSortMenu.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        TopBar(title = "My Movies", onSettingsClick = onSettingsClick)
        Column(modifier = Modifier.fillMaxSize()) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp)
            ) {
                MovieFilter.entries.forEachIndexed { index, filter ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = MovieFilter.entries.size
                        ),
                        onClick = { viewModel.onFilterChanged(filter) },
                        selected = filter == selectedFilter,
                        label = { 
                            Text(
                                text = when (filter) {
                                    MovieFilter.SEEN -> "Seen"
                                    MovieFilter.WATCHLIST -> "Watchlist"
                                }
                            ) 
                        }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier) {
                    TextButton(
                        onClick = { viewModel.onToggleSortMenu() }
                    ) {
                        Text("Sort: ${sortOrder.displayName}")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort options"
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { viewModel.onDismissSortMenu() }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.displayName) },
                                onClick = { viewModel.onSortOrderChanged(order) }
                            )
                        }
                    }
                }
                FilterChip(
                    shape = CircleShape,
                    selected = showFavoritesOnly,
                    onClick = { viewModel.onToggleFavoritesOnly() },
                    label = {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = if (showFavoritesOnly) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Show favorites only"
                        )
                    },
                )
            }
            
            if (movies.isNotEmpty()) {
                MovieList(
                    modifier = modifier.fillMaxSize(),
                    movies = movies,
                    onMovieClick = onMovieClick
                )
            } else {
                EmptyState(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
