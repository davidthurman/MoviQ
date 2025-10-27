package com.dthurman.moviesaver.ui.features.feature_seen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeenViewModel @Inject constructor(
    private val movieRepository: MovieRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SeenUiState())
    val uiState: MutableStateFlow<SeenUiState> = _uiState

    val movies: StateFlow<List<Movie>> =
        movieRepository.seenMovies()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

}

data class SeenUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)