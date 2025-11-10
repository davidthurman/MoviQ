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
import kotlinx.coroutines.flow.update
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
        searchForMovies()
    }

    fun onEvent(event: DiscoverEvent) {
        when (event) {
            is DiscoverEvent.SearchMovie -> {
                searchForMovies(event.query)
            }
        }
    }

    private fun searchForMovies(title: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = if (title.isNullOrEmpty()) {
                moviesUseCases.getPopularMovies.invoke()
            } else {
                moviesUseCases.searchMovies.invoke(title)
            }
            
            _uiState.update {
                if (result.isSuccess) {
                    val header = if (title.isNullOrEmpty()) {
                        context.getString(R.string.popular_movies)
                    } else {
                        context.getString(R.string.search_results_for, title)
                    }
                    it.copy(
                        movies = result.getOrNull() ?: emptyList(),
                        searchHeader = header,
                        isLoading = false,
                        error = null
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
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

sealed class DiscoverEvent {
    data class SearchMovie(val query: String): DiscoverEvent()
}