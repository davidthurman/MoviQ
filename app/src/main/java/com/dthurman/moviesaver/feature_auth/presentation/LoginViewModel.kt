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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val errorLogger: ErrorLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = authUseCases.observeCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun signInWithGoogle(idToken: String, context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authUseCases.signInWithGoogle(idToken)
            _uiState.value = if (result.isSuccess) {
                val user = result.getOrNull()!!
                LoginUiState.Success(user)
            } else {
                val error = result.exceptionOrNull()?.message 
                    ?: context.getString(R.string.error_unknown_occurred)
                LoginUiState.Error(error)
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Initial
    }

    fun signOut() {
        viewModelScope.launch {
            authUseCases.signOut()
            _uiState.value = LoginUiState.Initial
        }
    }

    suspend fun handleGoogleSignIn(context: Context, webClientId: String) {
        _uiState.value = LoginUiState.Loading
        
        val credentialResult = authUseCases.getGoogleCredential(context, webClientId)
        
        if (credentialResult.isSuccess) {
            val idToken = credentialResult.getOrNull()!!
            signInWithGoogle(idToken, context)
        } else {
            val exception = credentialResult.exceptionOrNull()!!
            val errorMessage = exception.message ?: context.getString(R.string.error_unknown_occurred)
            errorLogger.logAuthError(exception)
            _uiState.value = LoginUiState.Error(errorMessage)
        }
    }
}

sealed class LoginUiState {
    data object Initial : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

