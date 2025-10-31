package com.dthurman.moviesaver.ui.features.feature_recommendations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.domain.model.MovieRecommendation
import com.dthurman.moviesaver.domain.repository.AiRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_CURRENT_INDEX = "current_index"

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val movieRepository: MovieRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog

    private val _showMinimumMoviesDialog = MutableStateFlow(false)
    val showMinimumMoviesDialog: StateFlow<Boolean> = _showMinimumMoviesDialog

    private val _seenMoviesCount = MutableStateFlow(0)
    val seenMoviesCount: StateFlow<Int> = _seenMoviesCount

    init {
        // Track seen movies count
        viewModelScope.launch {
            movieRepository.getSeenMovies().collect { seenMovies ->
                _seenMoviesCount.value = seenMovies.size
            }
        }
        val savedIndex = savedStateHandle.get<Int>(KEY_CURRENT_INDEX) ?: 0
        
        viewModelScope.launch {
            aiRepository.getSavedRecommendations().collect { savedRecommendations ->
                if (savedRecommendations.isNotEmpty() && _uiState.value.recommendations.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            recommendations = savedRecommendations,
                            currentIndex = savedIndex.coerceIn(0, savedRecommendations.size - 1)
                        )
                    }
                }
            }
        }
    }

    fun generateAiRecommendations() {
        // Check if user has at least 5 seen movies
        if (_seenMoviesCount.value < 5) {
            _showMinimumMoviesDialog.value = true
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val recommendations = aiRepository.generatePersonalizedRecommendations()
                _uiState.update { 
                    it.copy(
                        recommendations = recommendations,
                        currentIndex = 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to generate recommendations"
                    )
                }
            }
        }
    }

    fun dismissMinimumMoviesDialog() {
        _showMinimumMoviesDialog.value = false
    }

    fun skipToNext() {
        viewModelScope.launch {
            _uiState.update { state ->
                val nextIndex = state.currentIndex + 1
                if (nextIndex < state.recommendations.size) {
                    savedStateHandle[KEY_CURRENT_INDEX] = nextIndex
                    state.copy(currentIndex = nextIndex)
                } else {
                    aiRepository.clearRecommendations()
                    savedStateHandle[KEY_CURRENT_INDEX] = 0
                    state.copy(recommendations = emptyList(), currentIndex = 0)
                }
            }
        }
    }

    fun addToWatchlist() {
        val currentRecommendation = _uiState.value.getCurrentRecommendation()
        if (currentRecommendation != null) {
            viewModelScope.launch {
                movieRepository.updateWatchlistStatus(currentRecommendation.movie, true)
                skipToNext()
            }
        }
    }

    fun showRatingDialog() {
        _showRatingDialog.value = true
    }

    fun dismissRatingDialog() {
        _showRatingDialog.value = false
    }

    fun markAsSeenWithRating(rating: Float?) {
        val currentRecommendation = _uiState.value.getCurrentRecommendation()
        if (currentRecommendation != null) {
            viewModelScope.launch {
                movieRepository.updateSeenStatus(currentRecommendation.movie, true)
                if (rating != null) {
                    movieRepository.updateRating(currentRecommendation.movie, rating)
                }
                dismissRatingDialog()
                skipToNext()
            }
        }
    }
}

data class RecommendationsUiState(
    val recommendations: List<MovieRecommendation> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun getCurrentRecommendation(): MovieRecommendation? {
        return recommendations.getOrNull(currentIndex)
    }
    
    fun hasRecommendations(): Boolean = recommendations.isNotEmpty()
}