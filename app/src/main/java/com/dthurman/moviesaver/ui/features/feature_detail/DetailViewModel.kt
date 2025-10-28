package com.dthurman.moviesaver.ui.features.feature_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    val movieRepository: MovieRepository
): ViewModel() {

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie.asStateFlow()

    fun loadMovie(movieId: Int) {
        viewModelScope.launch {
            _movie.value = movieRepository.getMovieById(movieId)
        }
    }

    fun toggleSeen() {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            movieRepository.updateSeenStatus(currentMovie, !currentMovie.isSeen)
            _movie.value = movieRepository.getMovieById(currentMovie.id)
        }
    }

    fun toggleWatchlist() {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            movieRepository.updateWatchlistStatus(currentMovie, !currentMovie.isWatchlist)
            _movie.value = movieRepository.getMovieById(currentMovie.id)
        }
    }

    fun toggleFavorite() {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            movieRepository.updateFavoriteStatus(currentMovie, !currentMovie.isFavorite)
            _movie.value = movieRepository.getMovieById(currentMovie.id)
        }
    }

}