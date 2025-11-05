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
import com.dthurman.moviesaver.feature_movies.presentation.my_movies.components.EmptyState
import com.dthurman.moviesaver.feature_movies.presentation.my_movies.components.GenericLoadingScreen
import com.dthurman.moviesaver.feature_movies.presentation.shared.MovieList
import com.dthurman.moviesaver.ui.components.TopBar
import kotlin.collections.isNotEmpty

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
    val isInitialLoad by viewModel.isInitialLoad.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        TopBar(title = stringResource(R.string.my_movies), onSettingsClick = onSettingsClick)
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
                                    MovieFilter.SEEN -> stringResource(R.string.seen)
                                    MovieFilter.WATCHLIST -> stringResource(R.string.watchlist)
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
                        Text(stringResource(R.string.sort_label, stringResource(sortOrder.displayNameRes)))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.sort_options_content_description)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { viewModel.onDismissSortMenu() }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(stringResource(order.displayNameRes)) },
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
                            contentDescription = stringResource(R.string.show_favorites_content_description)
                        )
                    },
                )
            }
            
            when {
                isInitialLoad -> {
                    GenericLoadingScreen()
                }
                movies != null && movies!!.isNotEmpty() -> {
                    MovieList(
                        modifier = modifier.fillMaxSize(),
                        movies = movies!!,
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
