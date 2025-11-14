package com.dthurman.moviesaver.feature_onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.feature_onboarding.domain.use_cases.OnboardingUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingUseCases: OnboardingUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.NextPage -> {
                val currentPage = _uiState.value.currentPage
                if (currentPage < 3) {
                    _uiState.update { it.copy(currentPage = currentPage + 1) }
                }
            }
            OnboardingEvent.PreviousPage -> {
                val currentPage = _uiState.value.currentPage
                if (currentPage > 0) {
                    _uiState.update { it.copy(currentPage = currentPage - 1) }
                }
            }
            is OnboardingEvent.GoToPage -> {
                _uiState.update { it.copy(currentPage = event.page) }
            }
            OnboardingEvent.CompleteOnboarding -> {
                viewModelScope.launch {
                    onboardingUseCases.completeOnboarding()
                    _uiState.update { it.copy(isCompleted = true) }
                }
            }
        }
    }
}

data class OnboardingUiState(
    val currentPage: Int = 0,
    val isCompleted: Boolean = false
)

sealed class OnboardingEvent {
    data object NextPage : OnboardingEvent()
    data object PreviousPage : OnboardingEvent()
    data class GoToPage(val page: Int) : OnboardingEvent()
    data object CompleteOnboarding : OnboardingEvent()
}

