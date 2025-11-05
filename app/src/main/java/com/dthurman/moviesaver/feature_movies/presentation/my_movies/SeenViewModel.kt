package com.dthurman.moviesaver.feature_movies.presentation.my_movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.domain.use_cases.MoviesUseCases
import com.dthurman.moviesaver.feature_movies.domain.util.MovieFilter
import com.dthurman.moviesaver.feature_movies.domain.util.MovieOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeenViewModel @Inject constructor(
    private val moviesUseCases: MoviesUseCases
): ViewModel() {

    private val _state = MutableStateFlow(SeenUiState())
    val state: StateFlow<SeenUiState> = _state.asStateFlow()

    init {
        loadMovies()
    }

    fun onEvent(event: SeenEvent) {
        when (event) {
            SeenEvent.ToggleSortMenu -> {
                _state.value = _state.value.copy(showSortMenu = true)
            }
            SeenEvent.DismissSortMenu -> {
                _state.value = _state.value.copy(showSortMenu = false)
            }
            SeenEvent.FavoritesToggled -> {
                _state.value = _state.value.copy(
                    showFavoritesOnly = !_state.value.showFavoritesOnly
                )
                loadMovies()
            }
            is SeenEvent.FilterChange -> {
                _state.value = _state.value.copy(selectedFilter = event.filter)
                loadMovies()
            }
            is SeenEvent.OrderChange -> {
                _state.value = _state.value.copy(
                    sortOrder = event.order,
                    showSortMenu = false
                )
                loadMovies()
            }
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val filter = when (_state.value.selectedFilter) {
                is MovieFilter.SeenMovies -> MovieFilter.SeenMovies(_state.value.sortOrder)
                is MovieFilter.WatchlistMovies -> MovieFilter.WatchlistMovies(_state.value.sortOrder)
                is MovieFilter.FavoriteMovies -> MovieFilter.FavoriteMovies(_state.value.sortOrder)
            }
            
            moviesUseCases.getUserMovies(filter).collect { movies ->
                val filteredMovies = if (_state.value.showFavoritesOnly) {
                    movies.filter { it.isFavorite }
                } else {
                    movies
                }
                
                _state.value = _state.value.copy(
                    movies = filteredMovies,
                    isLoading = false
                )
            }
        }
    }
}

data class SeenUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSortMenu: Boolean = false,
    val showFavoritesOnly: Boolean = false,
    val selectedFilter: MovieFilter = MovieFilter.SeenMovies(),
    val sortOrder: MovieOrder = MovieOrder.DATE_ADDED_DESC
)

sealed class SeenEvent {
    data class OrderChange(val order: MovieOrder): SeenEvent()
    data class FilterChange(val filter: MovieFilter): SeenEvent()
    object FavoritesToggled: SeenEvent()
    object ToggleSortMenu: SeenEvent()
    object DismissSortMenu: SeenEvent()
}