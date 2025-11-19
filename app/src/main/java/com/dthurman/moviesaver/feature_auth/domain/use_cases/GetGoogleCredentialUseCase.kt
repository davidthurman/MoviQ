package com.dthurman.moviesaver.feature_auth.domain.use_cases

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject

class GetGoogleCredentialUseCase @Inject constructor(
    private val errorLogger: ErrorLogger
) {
    
    companion object {
        private const val TAG = "GetGoogleCredential"
    }
    
    suspend operator fun invoke(context: Context, webClientId: String): Result<String> {
        val result = attemptGetCredential(context, webClientId)
        return if (result.isSuccess) {
            result
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to get credentials"))
        }

    }
    
    private suspend fun attemptGetCredential(context: Context, webClientId: String): Result<String> {
        return try {
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            
            Log.d(TAG, "Credential received: ${result.credential.javaClass.simpleName}")

            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d(TAG, "Sign-in successful: ${googleIdTokenCredential.displayName} (${googleIdTokenCredential.id})")
                        Result.success(googleIdTokenCredential.idToken)
                    } else {
                        val error = Exception(context.getString(R.string.error_unexpected_credential_type))
                        Log.e(TAG, "Unexpected credential type: ${credential.type}")
                        errorLogger.logAuthError(error)
                        Result.failure(error)
                    }
                }
                else -> {
                    val error = Exception(context.getString(R.string.error_unexpected_credential))
                    Log.e(TAG, "Unexpected credential class: ${credential.javaClass.name}")
                    errorLogger.logAuthError(error)
                    Result.failure(error)
                }
            }
        } catch (e: NoCredentialException) {
            Log.i(TAG, "NoCredentialException: No saved credentials found, attempting sign-in flow")
            attemptSignInWithGoogle(context, webClientId)
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "User cancelled sign-in")
            errorLogger.log("User cancelled Google sign-in")
            Result.failure(Exception(context.getString(R.string.error_sign_in_cancelled), e))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException: ${e.type} - ${e.message}")
            errorLogger.logAuthError(e)
            Result.failure(Exception("Credential error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception: ${e.javaClass.simpleName} - ${e.message}")
            errorLogger.logAuthError(e)
            Result.failure(e)
        }
    }
    
    private suspend fun attemptSignInWithGoogle(context: Context, webClientId: String): Result<String> {
        return try {
            Log.d(TAG, "Attempting sign-in with GetSignInWithGoogleOption")
            val credentialManager = CredentialManager.create(context)
            
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()
            
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            
            Log.d(TAG, "Sign-in credential received: ${result.credential.javaClass.simpleName}")
            
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d(TAG, "Sign-in successful: ${googleIdTokenCredential.displayName} (${googleIdTokenCredential.id})")
                        Result.success(googleIdTokenCredential.idToken)
                    } else {
                        val error = Exception(context.getString(R.string.error_unexpected_credential_type))
                        Log.e(TAG, "Unexpected credential type: ${credential.type}")
                        errorLogger.logAuthError(error)
                        Result.failure(error)
                    }
                }
                else -> {
                    val error = Exception(context.getString(R.string.error_unexpected_credential))
                    Log.e(TAG, "Unexpected credential class: ${credential.javaClass.name}")
                    errorLogger.logAuthError(error)
                    Result.failure(error)
                }
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "User cancelled sign-in flow")
            errorLogger.log("User cancelled Google sign-in flow")
            Result.failure(Exception(context.getString(R.string.error_sign_in_cancelled), e))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException during sign-in: ${e.type} - ${e.message}")
            
            val errorMessage = if (e.message?.contains("NETWORK_ERROR", ignoreCase = true) == true ||
                                   e.cause?.message?.contains("TimeoutException", ignoreCase = true) == true) {
                "Network error during sign-in. Please check your internet connection and try again."
            } else {
                context.getString(R.string.error_no_credentials_found)
            }
            
            errorLogger.logAuthError(e)
            Result.failure(Exception(errorMessage, e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception during sign-in: ${e.javaClass.simpleName} - ${e.message}")
            errorLogger.logAuthError(e)
            Result.failure(e)
        }
    }
}


