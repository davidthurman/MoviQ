package com.dthurman.moviesaver.feature_movies.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.domain.use_cases.MoviesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val moviesUseCases: MoviesUseCases
): ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    fun loadMovie(movie: Movie) {
        viewModelScope.launch {
            val result = moviesUseCases.getMovieById(movie.id)
            _uiState.update { it.copy(movie = result.getOrNull() ?: movie) }
        }
    }
    
    fun onEvent(event: DetailEvent) {
        val currentMovie = _uiState.value.movie ?: return
        when (event) {
            DetailEvent.ToggleFavorite -> {
                val newFavoriteStatus = !currentMovie.isFavorite
                viewModelScope.launch {
                    moviesUseCases.updateFavoriteStatus(currentMovie, newFavoriteStatus)
                    _uiState.update { it.copy(movie = currentMovie.copy(isFavorite = newFavoriteStatus)) }
                }
            }
            DetailEvent.ToggleWatchlist -> {
                val newWatchlistStatus = !currentMovie.isWatchlist
                viewModelScope.launch {
                    moviesUseCases.updateWatchlistStatus(currentMovie, newWatchlistStatus)
                    _uiState.update { it.copy(movie = currentMovie.copy(isWatchlist = newWatchlistStatus)) }
                }
            }
            DetailEvent.ToggleSeen -> {
                if (!currentMovie.isSeen) {
                    _uiState.update { it.copy(showRatingDialog = true) }
                } else {
                    _uiState.update { it.copy(showUnseenConfirmDialog = true) }
                }
            }
            DetailEvent.OpenRatingDialog -> {
                _uiState.update { it.copy(showRatingDialog = true) }
            }
            DetailEvent.DismissRatingDialog -> {
                _uiState.update { it.copy(showRatingDialog = false) }
            }
            DetailEvent.DismissUnseenDialog -> {
                _uiState.update { it.copy(showUnseenConfirmDialog = false) }
            }
            DetailEvent.ConfirmUnseen -> {
                viewModelScope.launch {
                    moviesUseCases.updateSeenStatus(currentMovie, false)
                    moviesUseCases.updateFavoriteStatus(currentMovie, false)
                    _uiState.update { it.copy(movie = currentMovie.copy(isSeen = false, rating = null, isFavorite = false)) }
                }
                _uiState.update { it.copy(showUnseenConfirmDialog = false) }
            }
            is DetailEvent.ConfirmSeenWithRating -> {
                confirmSeenWithRating(currentMovie, event.rating)
            }
        }
    }

    private fun confirmSeenWithRating(currentMovie: Movie, rating: Float?) {
        viewModelScope.launch {
            if (!currentMovie.isSeen) {
                moviesUseCases.updateSeenStatus(currentMovie, true, rating)
                _uiState.update { it.copy(movie = currentMovie.copy(isSeen = true, isWatchlist = false, rating = rating)) }
            } else if (rating != null) {
                moviesUseCases.updateRating(currentMovie, rating)
                _uiState.update { it.copy(movie = currentMovie.copy(rating = rating)) }
            }
        }
        _uiState.update { it.copy(showRatingDialog = false) }
    }
}

data class DetailUiState(
    val movie: Movie? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showRatingDialog: Boolean = false,
    val showUnseenConfirmDialog: Boolean = false
)

sealed class DetailEvent {
    object ToggleFavorite: DetailEvent()       
    object ToggleWatchlist: DetailEvent()
    object ToggleSeen: DetailEvent()
    object OpenRatingDialog: DetailEvent()
    object DismissRatingDialog: DetailEvent()
    object DismissUnseenDialog: DetailEvent()
    object ConfirmUnseen: DetailEvent()
    data class ConfirmSeenWithRating(val rating: Float?): DetailEvent()
}