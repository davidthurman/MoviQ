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

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog.asStateFlow()

    private val _showUnseenConfirmDialog = MutableStateFlow(false)
    val showUnseenConfirmDialog: StateFlow<Boolean> = _showUnseenConfirmDialog.asStateFlow()

    fun loadMovie(movie: Movie) {
        viewModelScope.launch {
            val dbMovie = movieRepository.getMovieById(movie.id)
            _movie.value = dbMovie ?: movie
        }
    }

    fun toggleSeen() {
        val currentMovie = _movie.value ?: return
        val newSeenStatus = !currentMovie.isSeen
        
        if (newSeenStatus) {
            // Show rating dialog when marking as seen
            _showRatingDialog.value = true
        } else {
            // Show confirmation dialog when unmarking as seen
            _showUnseenConfirmDialog.value = true
        }
    }

    fun confirmUnseen() {
        val currentMovie = _movie.value ?: return
        // Remove seen status, clear rating, and remove from favorites
        viewModelScope.launch {
            movieRepository.updateSeenStatus(currentMovie, false)
            movieRepository.updateRating(currentMovie, null)
            movieRepository.updateFavoriteStatus(currentMovie, false)
            _movie.value = currentMovie.copy(isSeen = false, rating = null, isFavorite = false)
        }
        _showUnseenConfirmDialog.value = false
    }

    fun dismissUnseenDialog() {
        _showUnseenConfirmDialog.value = false
    }

    fun confirmSeenWithRating(rating: Float?) {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            if (!currentMovie.isSeen) {
                // If not yet marked as seen, mark it as seen and remove from watchlist
                movieRepository.updateSeenStatus(currentMovie, true)
                var watchlistStatus = false
                _movie.value = currentMovie.copy(isSeen = true, isWatchlist = watchlistStatus, rating = rating)
            } else {
                // If already seen, just update the rating
                _movie.value = currentMovie.copy(rating = rating)
            }
            movieRepository.updateRating(currentMovie, rating)
        }
        _showRatingDialog.value = false
    }

    fun dismissRatingDialog() {
        _showRatingDialog.value = false
    }

    fun openRatingDialog() {
        _showRatingDialog.value = true
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