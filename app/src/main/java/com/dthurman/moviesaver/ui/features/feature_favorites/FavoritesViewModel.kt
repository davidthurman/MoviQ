package com.dthurman.moviesaver.ui.features.feature_seen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val movieRepository: MovieRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SeenUiState())
    val uiState: MutableStateFlow<SeenUiState> = _uiState

    val movies: StateFlow<List<Movie>> =
        movieRepository.getFavoriteMovies()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

}

data class FavoritesUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)