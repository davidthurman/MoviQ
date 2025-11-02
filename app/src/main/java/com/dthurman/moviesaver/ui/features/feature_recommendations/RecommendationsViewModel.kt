package com.dthurman.moviesaver.ui.features.feature_recommendations

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.data.billing.PurchaseState
import com.dthurman.moviesaver.domain.model.MovieRecommendation
import com.dthurman.moviesaver.domain.repository.AiRepository
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.BillingRepository
import com.dthurman.moviesaver.domain.repository.CreditsRepository
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
    private val creditsRepository: CreditsRepository,
    private val authRepository: AuthRepository,
    private val billingRepository: BillingRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog

    private val _showMinimumMoviesDialog = MutableStateFlow(false)
    val showMinimumMoviesDialog: StateFlow<Boolean> = _showMinimumMoviesDialog
    
    private val _showNoCreditsDialog = MutableStateFlow(false)
    val showNoCreditsDialog: StateFlow<Boolean> = _showNoCreditsDialog

    private val _seenMoviesCount = MutableStateFlow(0)
    val seenMoviesCount: StateFlow<Int> = _seenMoviesCount
    
    private val _userCredits = MutableStateFlow(0)
    val userCredits: StateFlow<Int> = _userCredits
    
    private val _showPurchaseSuccessDialog = MutableStateFlow(false)
    val showPurchaseSuccessDialog: StateFlow<Boolean> = _showPurchaseSuccessDialog
    
    var currentActivity: Activity? = null

    init {
        // Monitor purchase state
        viewModelScope.launch {
            billingRepository.purchaseState.collect { state ->
                when (state) {
                    is PurchaseState.Success -> {
                        // Process the purchase
                        val success = billingRepository.processPurchase(state.purchase.purchaseToken)
                        if (success) {
                            _showPurchaseSuccessDialog.value = true
                        }
                        billingRepository.resetPurchaseState()
                    }
                    is PurchaseState.Error -> {
                        // Handle error (could show error dialog)
                        billingRepository.resetPurchaseState()
                    }
                    is PurchaseState.Canceled -> {
                        billingRepository.resetPurchaseState()
                    }
                    else -> { /* No action needed for other states */ }
                }
            }
        }
        
        // Track seen movies count
        viewModelScope.launch {
            movieRepository.getSeenMovies().collect { seenMovies ->
                _seenMoviesCount.value = seenMovies.size
            }
        }
        
        // Track user credits
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id
            if (userId != null) {
                creditsRepository.getCreditsFlow(userId).collect { credits ->
                    _userCredits.value = credits
                }
            }
        }
        
        val savedIndex = savedStateHandle.get<Int>(KEY_CURRENT_INDEX) ?: 0
        
        viewModelScope.launch {
            aiRepository.getSavedRecommendations().collect { savedRecommendations ->
                _uiState.update { 
                    it.copy(
                        recommendations = savedRecommendations,
                        currentIndex = if (savedRecommendations.isNotEmpty()) {
                            savedIndex.coerceIn(0, savedRecommendations.size - 1)
                        } else {
                            0
                        }
                    )
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
        
        // Check if user has credits
        if (_userCredits.value <= 0) {
            _showNoCreditsDialog.value = true
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val userId = authRepository.getCurrentUser()?.id
                if (userId != null) {
                    // Deduct credit first
                    val creditDeducted = creditsRepository.deductCredit(userId)
                    
                    if (!creditDeducted) {
                        _showNoCreditsDialog.value = true
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }
                    
                    val recommendations = aiRepository.generatePersonalizedRecommendations()
                    _uiState.update { 
                        it.copy(
                            recommendations = recommendations,
                            currentIndex = 0,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "User not logged in"
                        )
                    }
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
    
    fun dismissNoCreditsDialog() {
        _showNoCreditsDialog.value = false
    }
    
    fun purchaseCredits() {
        currentActivity?.let { activity ->
            billingRepository.launchPurchaseFlow(activity)
            dismissNoCreditsDialog()
        }
    }
    
    fun dismissPurchaseSuccessDialog() {
        _showPurchaseSuccessDialog.value = false
    }

    fun skipToNext() {
        val currentRecommendation = _uiState.value.getCurrentRecommendation()
        if (currentRecommendation != null) {
            viewModelScope.launch {
                // Mark movie as not interested and clear aiReason
                movieRepository.markAsNotInterested(currentRecommendation.movie.id)
                _uiState.update { state ->
                    state.copy(currentIndex = 0)
                }
            }
        }
    }

    fun addToWatchlist() {
        val currentRecommendation = _uiState.value.getCurrentRecommendation()
        if (currentRecommendation != null) {
            viewModelScope.launch {
                movieRepository.clearAiReasonAndUpdateWatchlist(currentRecommendation.movie)
                _uiState.update { state ->
                    state.copy(currentIndex = 0)
                }
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
                movieRepository.clearAiReasonAndMarkSeen(currentRecommendation.movie, rating)
                dismissRatingDialog()
                _uiState.update { state ->
                    state.copy(currentIndex = 0)
                }
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