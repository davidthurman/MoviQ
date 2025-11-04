package com.dthurman.moviesaver.core.observability

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of ErrorLogger.
 * Handles all error logging and crash reporting using Firebase Crashlytics.
 */
@Singleton
class ErrorLoggerImpl @Inject constructor() : ErrorLogger {

    private val crashlytics: FirebaseCrashlytics = Firebase.crashlytics

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(exception: Throwable) {
        crashlytics.recordException(exception)
    }

    override fun setUserId(userId: String?) {
        if (userId != null) {
            crashlytics.setUserId(userId)
        } else {
            clearUserId()
        }
    }

    override fun clearUserId() {
        crashlytics.setUserId("")
    }

    override fun setCustomKey(key: String, value: Any) {
        when (value) {
            is String -> crashlytics.setCustomKey(key, value)
            is Boolean -> crashlytics.setCustomKey(key, value)
            is Int -> crashlytics.setCustomKey(key, value)
            is Long -> crashlytics.setCustomKey(key, value)
            is Float -> crashlytics.setCustomKey(key, value)
            is Double -> crashlytics.setCustomKey(key, value)
            else -> crashlytics.setCustomKey(key, value.toString())
        }
    }

    override fun logAuthError(exception: Exception) {
        setCustomKey(Keys.ERROR_CONTEXT, "auth_error")
        log("Authentication error: ${exception.message}")
        recordException(exception)
    }

    override fun logBillingError(responseCode: Int, exception: Exception) {
        setCustomKey(Keys.ERROR_CONTEXT, "billing_error")
        setCustomKey(Keys.ERROR_CODE, responseCode)
        log("Billing error (code $responseCode): ${exception.message}")
        recordException(exception)
    }

    override fun logDatabaseError(operation: String, exception: Exception) {
        setCustomKey(Keys.ERROR_CONTEXT, "database_error")
        setCustomKey(Keys.USER_ACTION, operation)
        log("Database error during $operation: ${exception.message}")
        recordException(exception)
    }

    override fun logNetworkError(operation: String, exception: Exception) {
        setCustomKey(Keys.ERROR_CONTEXT, "network_error")
        setCustomKey(Keys.USER_ACTION, operation)
        log("Network error during $operation: ${exception.message}")
        recordException(exception)
    }

    override fun logAiError(exception: Exception) {
        setCustomKey(Keys.ERROR_CONTEXT, "ai_error")
        log("AI error: ${exception.message}")
        recordException(exception)
    }

    // Additional helper methods (not in interface, but useful)

    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    fun logNetworkError(endpoint: String, error: Throwable) {
        setCustomKey(Keys.API_ENDPOINT, endpoint)
        setCustomKey(Keys.ERROR_CONTEXT, "network_error")
        log("Network error at $endpoint: ${error.message}")
        recordException(error)
    }

    fun logAiError(error: Throwable) {
        setCustomKey(Keys.ERROR_CONTEXT, "ai_error")
        log("AI service error: ${error.message}")
        recordException(error)
    }

    fun setScreenContext(screenName: String) {
        setCustomKey(Keys.SCREEN_NAME, screenName)
    }

    fun setUserContext(userId: String?, creditsBalance: Int?, movieCount: Int?) {
        userId?.let { setUserId(it) }
        creditsBalance?.let { setCustomKey(Keys.CREDITS_BALANCE, it) }
        movieCount?.let { setCustomKey(Keys.MOVIE_COUNT, it) }
    }

    // Context keys for crash reports
    private object Keys {
        const val SCREEN_NAME = "screen_name"
        const val USER_ACTION = "user_action"
        const val API_ENDPOINT = "api_endpoint"
        const val NETWORK_STATUS = "network_status"
        const val CREDITS_BALANCE = "credits_balance"
        const val MOVIE_COUNT = "movie_count"
        const val ERROR_CODE = "error_code"
        const val ERROR_CONTEXT = "error_context"
    }
}

