package com.dthurman.moviesaver.feature_movies.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.domain.use_cases.MoviesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val moviesUseCases: MoviesUseCases
): ViewModel() {

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie.asStateFlow()

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog.asStateFlow()

    private val _showUnseenConfirmDialog = MutableStateFlow(false)
    val showUnseenConfirmDialog: StateFlow<Boolean> = _showUnseenConfirmDialog.asStateFlow()

    fun loadMovie(movie: Movie) {
        viewModelScope.launch {
            val result = moviesUseCases.getMovieById(movie.id)
            _movie.value = result.getOrNull() ?: movie
        }
    }

    fun toggleSeen() {
        val currentMovie = _movie.value ?: return
        val newSeenStatus = !currentMovie.isSeen
        
        if (newSeenStatus) {
            _showRatingDialog.value = true
        } else {
            _showUnseenConfirmDialog.value = true
        }
    }

    fun confirmUnseen() {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            moviesUseCases.removeFromSeen(currentMovie)
            moviesUseCases.updateFavoriteStatus(currentMovie, false)
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
                moviesUseCases.markMovieAsSeen(currentMovie, rating)
                _movie.value = currentMovie.copy(isSeen = true, isWatchlist = false, rating = rating)
            } else if (rating != null) {
                moviesUseCases.rateMovie(currentMovie, rating)
                _movie.value = currentMovie.copy(rating = rating)
            }
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
            if (newWatchlistStatus) {
                moviesUseCases.addToWatchlist(currentMovie)
            } else {
                moviesUseCases.updateWatchlistStatus(currentMovie, false)
            }
            _movie.value = currentMovie.copy(isWatchlist = newWatchlistStatus)
        }
    }

    fun toggleFavorite() {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            moviesUseCases.toggleFavorite(currentMovie)
            _movie.value = currentMovie.copy(isFavorite = !currentMovie.isFavorite)
        }
    }

}