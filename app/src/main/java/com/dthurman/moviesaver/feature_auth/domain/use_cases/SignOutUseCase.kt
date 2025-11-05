package com.dthurman.moviesaver.feature_auth.domain.use_cases

import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_auth.domain.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val analytics: AnalyticsTracker,
    private val errorLogger: ErrorLogger
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.signOut()
            analytics.logEvent("sign_out", emptyMap())
            Result.success(Unit)
        } catch (e: Exception) {
            errorLogger.logAuthError(e)
            Result.failure(e)
        }
    }
}

