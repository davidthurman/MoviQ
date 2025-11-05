package com.dthurman.moviesaver.feature_auth.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_auth.domain.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val analytics: AnalyticsTracker,
    private val errorLogger: ErrorLogger
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        return try {
            val result = authRepository.signInWithGoogle(idToken)
            
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                analytics.logEvent("sign_in_success", mapOf(
                    "method" to "google",
                    "user_id" to user.id
                ))
            } else {
                errorLogger.logAuthError(
                    result.exceptionOrNull() as? Exception 
                        ?: Exception("Unknown sign-in error")
                )
            }
            
            result
        } catch (e: Exception) {
            errorLogger.logAuthError(e)
            Result.failure(e)
        }
    }
}

