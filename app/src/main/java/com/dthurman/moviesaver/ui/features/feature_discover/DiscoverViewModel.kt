package com.dthurman.moviesaver.ui.features.feature_discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState

    init {
        getMovies()
    }

    fun getMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val results = movieRepository.getPopularMovies()
                _uiState.value = _uiState.value.copy(movies = results, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun searchForMovies(title: String) {
        if (title.isBlank()) {
            getMovies()
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val results = movieRepository.searchMovieByTitle(title)
                _uiState.value = _uiState.value.copy(movies = results, searchHeader = "Results for \"$title\":", isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun addMovieToSeen(movie: Movie) {
        viewModelScope.launch {
            movieRepository.addMovieToSeen(movie)
        }
    }
}

data class DiscoverUiState(
    val query: String = "",
    val searchHeader: String = "Popular:",
    val mode: Mode = Mode.Popular,
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    enum class Mode { Popular, Search }
}