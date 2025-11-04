package com.dthurman.moviesaver.core.observability

/**
 * Interface for logging errors and crashes across the application.
 * This abstraction allows for easy testing and swapping of crash reporting providers.
 */
interface ErrorLogger {
    fun log(message: String)
    fun recordException(exception: Throwable)
    fun setUserId(userId: String?)
    fun clearUserId()
    fun setCustomKey(key: String, value: Any)
    fun logAuthError(exception: Exception)
    fun logBillingError(responseCode: Int, exception: Exception)
    fun logDatabaseError(operation: String, exception: Exception)
    fun logNetworkError(operation: String, exception: Exception)
    fun logAiError(exception: Exception)
}

