package com.dthurman.moviesaver.ui.features.feature_login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.data.remote.firebase.analytics.CrashlyticsService
import com.dthurman.moviesaver.domain.model.User
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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
    private val authRepository: AuthRepository,
    private val crashlyticsService: CrashlyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.getCurrentUser()
        )

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.signInWithGoogle(idToken)
            _uiState.value = if (result.isSuccess) {
                val user = result.getOrNull()!!
                LoginUiState.Success(user)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                LoginUiState.Error(error)
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Initial
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = LoginUiState.Initial
        }
    }

    suspend fun handleGoogleSignIn(context: Context, webClientId: String) {
        try {
            _uiState.value = LoginUiState.Loading
            
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )

            val credential = result.credential
            when (credential) {
                is androidx.credentials.CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        signInWithGoogle(idToken)
                    } else {
                        val error = "Unexpected credential type"
                        crashlyticsService.log("Login error: $error")
                        _uiState.value = LoginUiState.Error(error)
                    }
                }
                else -> {
                    val error = "Unexpected credential"
                    crashlyticsService.log("Login error: $error")
                    _uiState.value = LoginUiState.Error(error)
                }
            }
        } catch (e: GetCredentialException) {
            crashlyticsService.logAuthError(e)
            _uiState.value = LoginUiState.Error(e.message ?: "Sign-in cancelled")
        } catch (e: Exception) {
            crashlyticsService.logAuthError(e)
            _uiState.value = LoginUiState.Error(e.message ?: "Unknown error occurred")
        }
    }
}

sealed class LoginUiState {
    data object Initial : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

