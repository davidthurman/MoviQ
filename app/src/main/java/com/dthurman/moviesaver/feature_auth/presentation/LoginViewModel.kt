package com.dthurman.moviesaver.feature_auth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_auth.domain.use_cases.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val errorLogger: ErrorLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = authUseCases.observeCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.SignInWithGoogle -> handleGoogleSignIn(event.context, event.webClientId)
            LoginEvent.ResetState -> {
                _uiState.update { it.copy(user = null, error = null) }
            }
            LoginEvent.SignOut -> {
                viewModelScope.launch {
                    authUseCases.signOut()
                    _uiState.update { it.copy(user = null, error = null) }
                }
            }
        }
    }

    private fun handleGoogleSignIn(context: Context, webClientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val credentialResult = authUseCases.getGoogleCredential(context, webClientId)
            
            if (credentialResult.isSuccess) {
                val idToken = credentialResult.getOrNull()!!
                signInWithGoogle(idToken, context)
            } else {
                val exception = credentialResult.exceptionOrNull()!!
                val errorMessage = exception.message ?: context.getString(R.string.error_unknown_occurred)
                errorLogger.logAuthError(exception)
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    private fun signInWithGoogle(idToken: String, context: Context) {
        viewModelScope.launch {
            val result = authUseCases.signInWithGoogle(idToken)
            _uiState.update {
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    it.copy(isLoading = false, user = user, error = null)
                } else {
                    val error = result.exceptionOrNull()?.message 
                        ?: context.getString(R.string.error_unknown_occurred)
                    it.copy(isLoading = false, error = error)
                }
            }
        }
    }
}

data class LoginUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class LoginEvent {
    data class SignInWithGoogle(val context: Context, val webClientId: String) : LoginEvent()
    data object ResetState : LoginEvent()
    data object SignOut : LoginEvent()
}

