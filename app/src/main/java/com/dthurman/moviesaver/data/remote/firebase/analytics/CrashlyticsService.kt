package com.dthurman.moviesaver.data.remote.firebase.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashlyticsService @Inject constructor() {

    private val crashlytics: FirebaseCrashlytics = Firebase.crashlytics

    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    fun log(message: String) {
        crashlytics.log(message)
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Float) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }

    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    fun clearUserId() {
        crashlytics.setUserId("")
    }

    fun testCrash() {
        throw RuntimeException("Test crash from CrashlyticsService")
    }

    object Keys {
        const val SCREEN_NAME = "screen_name"
        const val USER_ACTION = "user_action"
        const val API_ENDPOINT = "api_endpoint"
        const val NETWORK_STATUS = "network_status"
        const val CREDITS_BALANCE = "credits_balance"
        const val MOVIE_COUNT = "movie_count"
        const val ERROR_CODE = "error_code"
        const val ERROR_CONTEXT = "error_context"
    }

    fun logNetworkError(endpoint: String, error: Throwable) {
        setCustomKey(Keys.API_ENDPOINT, endpoint)
        setCustomKey(Keys.ERROR_CONTEXT, "network_error")
        log("Network error at $endpoint: ${error.message}")
        recordException(error)
    }

    fun logDatabaseError(operation: String, error: Throwable) {
        setCustomKey(Keys.ERROR_CONTEXT, "database_error")
        setCustomKey(Keys.USER_ACTION, operation)
        log("Database error during $operation: ${error.message}")
        recordException(error)
    }

    fun logAuthError(error: Throwable) {
        setCustomKey(Keys.ERROR_CONTEXT, "auth_error")
        log("Authentication error: ${error.message}")
        recordException(error)
    }

    fun logBillingError(errorCode: Int, error: Throwable) {
        setCustomKey(Keys.ERROR_CONTEXT, "billing_error")
        setCustomKey(Keys.ERROR_CODE, errorCode)
        log("Billing error (code $errorCode): ${error.message}")
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
}

