package com.dthurman.moviesaver.feature_movies.presentation.discover

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.domain.use_cases.MoviesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moviesUseCases: MoviesUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DiscoverUiState(searchHeader = context.getString(R.string.popular_movies))
    )
    val uiState: StateFlow<DiscoverUiState> = _uiState

    init {
        getMovies()
    }

    fun getMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = moviesUseCases.getPopularMovies.invoke()
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    movies = result.getOrNull() ?: emptyList(),
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
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
            
            val result = moviesUseCases.searchMovies.invoke(title)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    movies = result.getOrNull() ?: emptyList(),
                    searchHeader = context.getString(R.string.search_results_for, title),
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
}

data class DiscoverUiState(
    val query: String = "",
    val searchHeader: String = "",
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)