package com.dthurman.moviesaver.feature_movies.presentation.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.domain.use_cases.GetPopularMoviesUseCase
import com.dthurman.moviesaver.feature_movies.domain.use_cases.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val searchMoviesUseCase: SearchMoviesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState

    init {
        getMovies()
    }

    fun getMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = getPopularMoviesUseCase.invoke()
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    movies = result.getOrNull() ?: emptyList(),
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
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
            
            val result = searchMoviesUseCase.invoke(title)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    movies = result.getOrNull() ?: emptyList(),
                    searchHeader = "Results for \"$title\":",
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }
}

data class DiscoverUiState(
    val query: String = "",
    val searchHeader: String = "Popular:",
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)