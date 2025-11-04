package com.dthurman.moviesaver.feature_auth.presentation

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.feature_auth.domain.AuthRepository
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
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
    private val userRepository: com.dthurman.moviesaver.core.domain.repository.UserRepository,
    private val errorLogger: ErrorLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = userRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
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
            
            // Check if Google Play Services is available
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            
            if (resultCode != ConnectionResult.SUCCESS) {
                val errorMessage = when (resultCode) {
                    ConnectionResult.SERVICE_MISSING -> "Google Play Services is not installed"
                    ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "Google Play Services needs to be updated"
                    ConnectionResult.SERVICE_DISABLED -> "Google Play Services is disabled"
                    else -> "Google Play Services is not available (code: $resultCode)"
                }
                errorLogger.log("Play Services check failed: $errorMessage")
                _uiState.value = LoginUiState.Error(errorMessage)
                return
            }
            
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
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
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        signInWithGoogle(idToken)
                    } else {
                        val error = "Unexpected credential type"
                        errorLogger.log("Login error: $error")
                        _uiState.value = LoginUiState.Error(error)
                    }
                }
                else -> {
                    val error = "Unexpected credential"
                    errorLogger.log("Login error: $error")
                    _uiState.value = LoginUiState.Error(error)
                }
            }
        } catch (e: NoCredentialException) {
            errorLogger.log("Auth Error - NoCredentialException: ${e.message}")
            errorLogger.setCustomKey("auth_error_type", "no_credential")
            errorLogger.logAuthError(e)
            val detailedError = "No credentials found. Make sure:\n1. Google Play Services is updated\n2. You have a Google account on this device\n3. The app has proper OAuth configuration"
            _uiState.value = LoginUiState.Error(detailedError)
        } catch (e: GetCredentialException) {
            errorLogger.log("Auth Error - GetCredentialException: ${e.message}")
            errorLogger.setCustomKey("auth_error_type", "get_credential")
            errorLogger.setCustomKey("auth_error_message", e.message ?: "unknown")
            errorLogger.logAuthError(e)
            val errorMsg = e.message ?: "Sign-in error"
            val detailedError = if (errorMsg.contains("no provider", ignoreCase = true)) {
                "Credential provider not found. Please update Google Play Services from the Play Store and try again."
            } else {
                errorMsg
            }
            _uiState.value = LoginUiState.Error(detailedError)
        } catch (e: Exception) {
            errorLogger.log("Auth Error - General Exception: ${e.javaClass.simpleName} - ${e.message}")
            errorLogger.setCustomKey("auth_error_type", "general")
            errorLogger.setCustomKey("auth_error_class", e.javaClass.simpleName)
            errorLogger.logAuthError(e)
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

