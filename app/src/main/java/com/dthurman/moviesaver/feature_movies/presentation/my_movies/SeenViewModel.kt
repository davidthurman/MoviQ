package com.dthurman.moviesaver.feature_movies.presentation.my_movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.domain.use_cases.MoviesUseCases
import com.dthurman.moviesaver.feature_movies.domain.util.MovieFilter
import com.dthurman.moviesaver.feature_movies.domain.util.MovieOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SeenViewModel @Inject constructor(
    private val moviesUseCases: MoviesUseCases
): ViewModel() {

    private val _sortOrder = MutableStateFlow(MovieOrder.DATE_ADDED_DESC)
    val sortOrder: StateFlow<MovieOrder> = _sortOrder.asStateFlow()

    private val _selectedFilter = MutableStateFlow<MovieFilter>(MovieFilter.SeenMovies())
    val selectedFilter: StateFlow<MovieFilter> = _selectedFilter.asStateFlow()

    private val _showSortMenu = MutableStateFlow(false)
    val showSortMenu: StateFlow<Boolean> = _showSortMenu.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _isInitialLoad = MutableStateFlow(true)
    val isInitialLoad: StateFlow<Boolean> = _isInitialLoad.asStateFlow()

    val movies: StateFlow<List<Movie>?> = combine(
        _selectedFilter,
        _sortOrder
    ) { filter, order ->
        when (filter) {
            is MovieFilter.SeenMovies -> MovieFilter.SeenMovies(order)
            is MovieFilter.WatchlistMovies -> MovieFilter.WatchlistMovies(order)
            is MovieFilter.FavoriteMovies -> MovieFilter.FavoriteMovies(order)
            else -> filter
        }
    }
        .flatMapLatest { filter ->
            moviesUseCases.getUserMovies(filter)
        }
        .combine(_showFavoritesOnly) { movies, showFavoritesOnly ->
            if (showFavoritesOnly) {
                movies.filter { it.isFavorite }
            } else {
                movies
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            movies.collect { movieList ->
                if (movieList != null && _isInitialLoad.value) {
                    _isInitialLoad.value = false
                }
            }
        }
    }

    fun onFilterChanged(filter: MovieFilter) {
        _selectedFilter.value = filter
    }

    fun onSortOrderChanged(order: MovieOrder) {
        _sortOrder.value = order
        _showSortMenu.value = false
    }

    fun onToggleSortMenu() {
        _showSortMenu.value = !_showSortMenu.value
    }

    fun onDismissSortMenu() {
        _showSortMenu.value = false
    }

    fun onToggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }
}

data class SeenUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)