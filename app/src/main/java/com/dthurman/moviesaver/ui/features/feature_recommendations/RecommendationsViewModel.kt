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

    init {
        // Restore current index from saved state
        val savedIndex = savedStateHandle.get<Int>(KEY_CURRENT_INDEX) ?: 0
        
        // Load saved recommendations from database
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

    /**
     * Generate AI-powered personalized recommendations based on user's saved movies
     */
    fun generateAiRecommendations() {
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

    /**
     * Skip to the next recommendation
     */
    fun skipToNext() {
        viewModelScope.launch {
            _uiState.update { state ->
                val nextIndex = state.currentIndex + 1
                if (nextIndex < state.recommendations.size) {
                    // Save current index
                    savedStateHandle[KEY_CURRENT_INDEX] = nextIndex
                    state.copy(currentIndex = nextIndex)
                } else {
                    // No more recommendations, clear from database
                    aiRepository.clearRecommendations()
                    savedStateHandle[KEY_CURRENT_INDEX] = 0
                    state.copy(recommendations = emptyList(), currentIndex = 0)
                }
            }
        }
    }

    /**
     * Add current movie to watchlist and skip to next
     */
    fun addToWatchlist() {
        val currentRecommendation = _uiState.value.getCurrentRecommendation()
        if (currentRecommendation != null) {
            viewModelScope.launch {
                movieRepository.updateWatchlistStatus(currentRecommendation.movie, true)
                skipToNext()
            }
        }
    }

    /**
     * Show rating dialog before marking as seen
     */
    fun showRatingDialog() {
        _showRatingDialog.value = true
    }

    /**
     * Dismiss rating dialog
     */
    fun dismissRatingDialog() {
        _showRatingDialog.value = false
    }

    /**
     * Mark current movie as seen with optional rating, then skip to next
     */
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
    
    fun isLastRecommendation(): Boolean {
        return currentIndex == recommendations.size - 1
    }
}