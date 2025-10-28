package com.dthurman.moviesaver.ui.features.feature_watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val movieRepository: MovieRepository
): ViewModel() {

    val movies: StateFlow<List<Movie>> =
        movieRepository.getWatchlistMovies()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

}

sealed interface WatchlistUiState {}