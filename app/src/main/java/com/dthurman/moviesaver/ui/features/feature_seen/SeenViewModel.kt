package com.dthurman.moviesaver.ui.features.feature_seen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class MovieFilter {
    SEEN,
    WATCHLIST
}

enum class SortOrder(val displayName: String) {
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    DATE_ADDED_ASC("Date Added (Oldest)"),
    DATE_ADDED_DESC("Date Added (Newest)"),
    RELEASE_DATE_ASC("Release Date (Oldest)"),
    RELEASE_DATE_DESC("Release Date (Newest)")
}

@HiltViewModel
class SeenViewModel @Inject constructor(
    private val movieRepository: MovieRepository
): ViewModel() {

    private val _selectedFilter = MutableStateFlow(MovieFilter.SEEN)
    val selectedFilter: StateFlow<MovieFilter> = _selectedFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _showSortMenu = MutableStateFlow(false)
    val showSortMenu: StateFlow<Boolean> = _showSortMenu.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    val movies: StateFlow<List<Movie>> = _selectedFilter
        .flatMapLatest { filter ->
            when (filter) {
                MovieFilter.SEEN -> movieRepository.getSeenMovies()
                MovieFilter.WATCHLIST -> movieRepository.getWatchlistMovies()
            }
        }
        .combine(_sortOrder) { movies, sort ->
            sortMovies(movies, sort)
        }
        .combine(_showFavoritesOnly) { movies, showFavoritesOnly ->
            if (showFavoritesOnly) {
                movies.filter { it.isFavorite }
            } else {
                movies
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun sortMovies(movies: List<Movie>, sortOrder: SortOrder): List<Movie> {
        return when (sortOrder) {
            SortOrder.TITLE_ASC -> movies.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> movies.sortedByDescending { it.title.lowercase() }
            SortOrder.DATE_ADDED_ASC -> movies.sortedBy { it.id }
            SortOrder.DATE_ADDED_DESC -> movies.sortedByDescending { it.id }
            SortOrder.RELEASE_DATE_ASC -> movies.sortedBy { it.releaseDate }
            SortOrder.RELEASE_DATE_DESC -> movies.sortedByDescending { it.releaseDate }
        }
    }

    fun onFilterChanged(filter: MovieFilter) {
        _selectedFilter.value = filter
    }

    fun onSortOrderChanged(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
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