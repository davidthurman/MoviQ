package com.dthurman.moviesaver.data.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for logging crashes and non-fatal errors to Firebase Crashlytics.
 * 
 * This service provides a centralized way to track exceptions, log custom messages,
 * and set user context for better crash reporting and debugging.
 */
@Singleton
class CrashlyticsService @Inject constructor() {

    private val crashlytics: FirebaseCrashlytics = Firebase.crashlytics

    /**
     * Sets whether crash collection is enabled.
     * Should be set based on user consent.
     * 
     * @param enabled Whether to enable crash collection
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    /**
     * Records a non-fatal exception to Crashlytics.
     * Use this for caught exceptions that you want to track.
     * 
     * @param throwable The exception to log
     */
    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    /**
     * Logs a message to Crashlytics.
     * These logs will be included in crash reports.
     * 
     * @param message The message to log
     */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * Sets a custom key-value pair that will be included in crash reports.
     * 
     * @param key The key for the custom data
     * @param value The value to associate with the key
     */
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

    /**
     * Sets the user identifier for crash reports.
     * This helps identify which users are experiencing crashes.
     * 
     * @param userId The user's unique identifier
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * Clears the user identifier.
     * Call this when the user signs out.
     */
    fun clearUserId() {
        crashlytics.setUserId("")
    }

    /**
     * Forces a crash for testing purposes.
     * WARNING: Only use this for testing crash reporting setup!
     */
    fun testCrash() {
        throw RuntimeException("Test crash from CrashlyticsService")
    }

    // Custom key constants for common properties
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

    // Convenience methods for common scenarios

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

