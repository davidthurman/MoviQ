package com.dthurman.moviesaver.feature_auth.presentation

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_auth.domain.use_cases.AuthUseCases
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
        try {
            _uiState.value = LoginUiState.Loading
            
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            
            if (resultCode != ConnectionResult.SUCCESS) {
                val errorMessage = when (resultCode) {
                    ConnectionResult.SERVICE_MISSING -> context.getString(R.string.error_play_services_missing)
                    ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> context.getString(R.string.error_play_services_update_required)
                    ConnectionResult.SERVICE_DISABLED -> context.getString(R.string.error_play_services_disabled)
                    else -> context.getString(R.string.error_play_services_unavailable, resultCode)
                }
                errorLogger.log(context.getString(R.string.error_play_services_check_failed, errorMessage))
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
                        signInWithGoogle(idToken, context)
                    } else {
                        val error = context.getString(R.string.error_unexpected_credential_type)
                        errorLogger.log(context.getString(R.string.error_login_error, error))
                        _uiState.value = LoginUiState.Error(error)
                    }
                }
                else -> {
                    val error = context.getString(R.string.error_unexpected_credential)
                    errorLogger.log(context.getString(R.string.error_login_error, error))
                    _uiState.value = LoginUiState.Error(error)
                }
            }
        } catch (e: NoCredentialException) {
            errorLogger.log(context.getString(R.string.error_auth_no_credential, e.message ?: ""))
            errorLogger.setCustomKey("auth_error_type", "no_credential")
            errorLogger.logAuthError(e)
            val detailedError = context.getString(R.string.error_no_credentials_found)
            _uiState.value = LoginUiState.Error(detailedError)
        } catch (e: GetCredentialException) {
            errorLogger.log(context.getString(R.string.error_auth_get_credential, e.message ?: ""))
            errorLogger.setCustomKey("auth_error_type", "get_credential")
            errorLogger.setCustomKey("auth_error_message", e.message ?: "unknown")
            errorLogger.logAuthError(e)
            val errorMsg = e.message ?: context.getString(R.string.error_sign_in_error)
            val detailedError = if (errorMsg.contains("no provider", ignoreCase = true)) {
                context.getString(R.string.error_credential_provider_not_found)
            } else {
                errorMsg
            }
            _uiState.value = LoginUiState.Error(detailedError)
        } catch (e: Exception) {
            errorLogger.log(context.getString(R.string.error_auth_general, e.javaClass.simpleName, e.message ?: ""))
            errorLogger.setCustomKey("auth_error_type", "general")
            errorLogger.setCustomKey("auth_error_class", e.javaClass.simpleName)
            errorLogger.logAuthError(e)
            _uiState.value = LoginUiState.Error(e.message ?: context.getString(R.string.error_unknown_occurred))
        }
    }
}

sealed class LoginUiState {
    data object Initial : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

