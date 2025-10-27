package com.dthurman.moviesaver.ui.features.feature_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    val movieRepository: MovieRepository
): ViewModel() {

    fun addToSeen(movie: Movie) {
        viewModelScope.launch {
            movieRepository.addMovieToSeen(movie)
        }
    }

    fun addToWatchlist(movie: Movie) {
        viewModelScope.launch {
            movieRepository.addMovieToSeen(movie)
        }
    }

}

sealed interface DetailUiState {



}