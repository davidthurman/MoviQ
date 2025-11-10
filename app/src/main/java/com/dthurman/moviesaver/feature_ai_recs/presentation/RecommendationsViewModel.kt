package com.dthurman.moviesaver.feature_ai_recs.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.use_cases.GetCreditsUseCase
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.AiRecsUseCases
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.InsufficientCreditsException
import com.dthurman.moviesaver.feature_billing.domain.PurchaseState
import com.dthurman.moviesaver.feature_billing.domain.use_cases.BillingUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val aiRecsUseCases: AiRecsUseCases,
    private val billingUseCases: BillingUseCases,
    private val getCreditsUseCase: GetCreditsUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState

    init {
        observePurchaseState()
        observeSeenMoviesCount()
        observeUserCredits()
        observeSavedRecommendations()
    }

    private fun observePurchaseState() {
        viewModelScope.launch {
            billingUseCases.observePurchaseState().collect { state ->
                when (state) {
                    is PurchaseState.Success -> {
                        val result = billingUseCases.processPurchase(state.purchase.purchaseToken)
                        if (result.isSuccess) {
                            _uiState.update { it.copy(showPurchaseSuccessDialog = true) }
                        }
                        billingUseCases.resetPurchaseState()
                    }
                    is PurchaseState.Error -> {
                        billingUseCases.resetPurchaseState()
                    }
                    is PurchaseState.Canceled -> {
                        billingUseCases.resetPurchaseState()
                    }
                    else -> { }
                }
            }
        }
    }

    private fun observeSeenMoviesCount() {
        viewModelScope.launch {
            aiRecsUseCases.getSeenMoviesCount().collect { count ->
                _uiState.update { it.copy(seenMoviesCount = count) }
            }
        }
    }

    private fun observeUserCredits() {
        viewModelScope.launch {
            getCreditsUseCase().collect { credits ->
                _uiState.update { it.copy(userCredits = credits) }
            }
        }
    }

    private fun observeSavedRecommendations() {
        viewModelScope.launch {
            aiRecsUseCases.getSavedRecommendations().collect { savedRecommendations ->
                _uiState.update { it.copy(recommendations = savedRecommendations) }
            }
        }
    }

    fun onEvent(event: RecommendationsEvent) {
        when (event) {
            RecommendationsEvent.GenerateAiRecommendations -> generateAiRecommendations()
            RecommendationsEvent.AddToWatchlist -> addToWatchlist()
            RecommendationsEvent.SkipToNext -> skipToNext()
            RecommendationsEvent.ShowRatingDialog -> {
                _uiState.update { it.copy(showRatingDialog = true) }
            }
            RecommendationsEvent.DismissRatingDialog -> {
                _uiState.update { it.copy(showRatingDialog = false) }
            }
            RecommendationsEvent.DismissNoCreditsDialog -> {
                _uiState.update { it.copy(showNoCreditsDialog = false) }
            }
            RecommendationsEvent.DismissPurchaseSuccessDialog -> {
                _uiState.update { it.copy(showPurchaseSuccessDialog = false) }
            }
            RecommendationsEvent.DismissMinMoviesDialog -> {
                _uiState.update { it.copy(showMinMoviesDialog = false) }
            }
            is RecommendationsEvent.LaunchPurchaseFlow -> {
                billingUseCases.launchPurchaseFlow(event.activity)
                _uiState.update { it.copy(showNoCreditsDialog = false) }
            }
            is RecommendationsEvent.MarkAsSeenWithRating -> {
                markAsSeenWithRating(event.rating)
            }
        }
    }

    private fun addToWatchlist() {
        val currentRecommendation = _uiState.value.getCurrentRecommendation() ?: return
        
        viewModelScope.launch {
            aiRecsUseCases.acceptToWatchlist(currentRecommendation)
        }
    }

    private fun skipToNext() {
        val currentRecommendation = _uiState.value.getCurrentRecommendation() ?: return
        
        viewModelScope.launch {
            aiRecsUseCases.rejectRecommendation(currentRecommendation.id)
        }
    }

    private fun markAsSeenWithRating(rating: Float?) {
        val currentRecommendation = _uiState.value.getCurrentRecommendation() ?: return
        
        viewModelScope.launch {
            aiRecsUseCases.acceptAsSeen(currentRecommendation, rating)
            _uiState.update { it.copy(showRatingDialog = false) }
        }
    }

    private fun generateAiRecommendations() {
        val currentState = _uiState.value
        
        if (currentState.seenMoviesCount < 5) {
            _uiState.update { it.copy(showMinMoviesDialog = true) }
            return
        }

        if (currentState.userCredits <= 0) {
            _uiState.update { it.copy(showNoCreditsDialog = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = aiRecsUseCases.generateRecommendations()

            if (result.isSuccess) {
                val recommendations = result.getOrNull() ?: emptyList()
                _uiState.update {
                    it.copy(
                        recommendations = recommendations,
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                val exception = result.exceptionOrNull()
                when (exception) {
                    is InsufficientCreditsException -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                showNoCreditsDialog = true
                            ) 
                        }
                    }
                    else -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception?.message
                            ) 
                        }
                    }
                }
            }
        }
    }
}

data class RecommendationsUiState(
    val recommendations: List<Movie> = emptyList(),
    val seenMoviesCount: Int = 0,
    val userCredits: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showRatingDialog: Boolean = false,
    val showNoCreditsDialog: Boolean = false,
    val showPurchaseSuccessDialog: Boolean = false,
    val showMinMoviesDialog: Boolean = false
) {
    fun getCurrentRecommendation(): Movie? = recommendations.firstOrNull()
    
    fun hasRecommendations(): Boolean = recommendations.isNotEmpty()
}

sealed class RecommendationsEvent {
    data object GenerateAiRecommendations : RecommendationsEvent()
    data object AddToWatchlist : RecommendationsEvent()
    data object SkipToNext : RecommendationsEvent()
    data object ShowRatingDialog : RecommendationsEvent()
    data object DismissRatingDialog : RecommendationsEvent()
    data object DismissNoCreditsDialog : RecommendationsEvent()
    data object DismissPurchaseSuccessDialog : RecommendationsEvent()
    data object DismissMinMoviesDialog : RecommendationsEvent()
    data class LaunchPurchaseFlow(val activity: Activity) : RecommendationsEvent()
    data class MarkAsSeenWithRating(val rating: Float?) : RecommendationsEvent()
}