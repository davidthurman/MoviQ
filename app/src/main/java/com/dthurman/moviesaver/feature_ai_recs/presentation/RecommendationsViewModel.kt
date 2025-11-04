package com.dthurman.moviesaver.feature_ai_recs.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.AcceptRecommendationAsSeenUseCase
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.AcceptRecommendationToWatchlistUseCase
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.RejectRecommendationUseCase
import com.dthurman.moviesaver.feature_billing.domain.PurchaseState
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_CURRENT_INDEX = "current_index"

sealed class RecommendationEvent {
    object LaunchPurchaseFlow : RecommendationEvent()
}

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val movieRepository: MovieRepository,
    private val userRepository: com.dthurman.moviesaver.core.domain.repository.UserRepository,
    private val creditsRepository: com.dthurman.moviesaver.core.domain.repository.CreditsRepository,
    internal val billingRepository: com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository,
    private val acceptToWatchlistUseCase: AcceptRecommendationToWatchlistUseCase,
    private val acceptAsSeenUseCase: AcceptRecommendationAsSeenUseCase,
    private val rejectRecommendationUseCase: RejectRecommendationUseCase,
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
    
    private val _events = MutableSharedFlow<RecommendationEvent>()
    val events: SharedFlow<RecommendationEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            billingRepository.purchaseState.collect { state ->
                when (state) {
                    is PurchaseState.Success -> {
                        val success = billingRepository.processPurchase(state.purchase.purchaseToken)
                        if (success) {
                            _showPurchaseSuccessDialog.value = true
                        }
                        billingRepository.resetPurchaseState()
                    }
                    is PurchaseState.Error -> {
                        billingRepository.resetPurchaseState()
                    }
                    is PurchaseState.Canceled -> {
                        billingRepository.resetPurchaseState()
                    }
                    else -> { }
                }
            }
        }
        
        viewModelScope.launch {
            movieRepository.getSeenMovies().collect { seenMovies ->
                _seenMoviesCount.value = seenMovies.size
            }
        }
        
        viewModelScope.launch {
            creditsRepository.getCreditsFlow().collect { credits ->
                _userCredits.value = credits
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
        if (_seenMoviesCount.value < 5) {
            _showMinimumMoviesDialog.value = true
            return
        }
        
        if (_userCredits.value <= 0) {
            _showNoCreditsDialog.value = true
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                if (userRepository.getCurrentUser() != null) {
                    val creditResult = creditsRepository.deductCredits(1)
                    
                    if (creditResult.isFailure) {
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
        viewModelScope.launch {
            _events.emit(RecommendationEvent.LaunchPurchaseFlow)
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
                rejectRecommendationUseCase(currentRecommendation.id)
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
                acceptToWatchlistUseCase(currentRecommendation)
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
                acceptAsSeenUseCase(currentRecommendation, rating)
                dismissRatingDialog()
                _uiState.update { state ->
                    state.copy(currentIndex = 0)
                }
            }
        }
    }
}

data class RecommendationsUiState(
    val recommendations: List<Movie> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun getCurrentRecommendation(): Movie? {
        return recommendations.getOrNull(currentIndex)
    }
    
    fun hasRecommendations(): Boolean = recommendations.isNotEmpty()
}