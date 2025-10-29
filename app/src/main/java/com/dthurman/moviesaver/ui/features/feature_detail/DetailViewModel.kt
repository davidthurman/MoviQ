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

    fun loadMovie(movie: Movie) {
        viewModelScope.launch {
            val dbMovie = movieRepository.getMovieById(movie.id)
            _movie.value = dbMovie ?: movie
        }
    }

    fun toggleSeen() {
        val currentMovie = _movie.value ?: return
        val newSeenStatus = !currentMovie.isSeen
        viewModelScope.launch {
            movieRepository.updateSeenStatus(currentMovie, newSeenStatus)
            var watchlistStatus = currentMovie.isWatchlist
            if (newSeenStatus) {
                watchlistStatus = false
            }
            _movie.value = currentMovie.copy(isSeen = newSeenStatus, isWatchlist = watchlistStatus)
        }
    }

    fun toggleWatchlist() {
        val currentMovie = _movie.value ?: return
        val newWatchlistStatus = !currentMovie.isWatchlist
        viewModelScope.launch {
            movieRepository.updateWatchlistStatus(currentMovie, newWatchlistStatus)
            _movie.value = currentMovie.copy(isWatchlist = newWatchlistStatus)
        }
    }

    fun toggleFavorite() {
        val currentMovie = _movie.value ?: return
        val newFavoriteStatus = !currentMovie.isFavorite
        viewModelScope.launch {
            movieRepository.updateFavoriteStatus(currentMovie, newFavoriteStatus)
            _movie.value = currentMovie.copy(isFavorite = newFavoriteStatus)
        }
    }

}