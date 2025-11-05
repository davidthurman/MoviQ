package com.dthurman.moviesaver.feature_auth.domain.use_cases

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.dthurman.moviesaver.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject

class GetGoogleCredentialUseCase @Inject constructor() {
    
    suspend operator fun invoke(context: Context, webClientId: String): Result<String> {
        return try {
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

            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Result.success(googleIdTokenCredential.idToken)
                    } else {
                        Result.failure(Exception(context.getString(R.string.error_unexpected_credential_type)))
                    }
                }
                else -> {
                    Result.failure(Exception(context.getString(R.string.error_unexpected_credential)))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

