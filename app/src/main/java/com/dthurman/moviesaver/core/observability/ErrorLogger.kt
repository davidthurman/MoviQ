package com.dthurman.moviesaver.core.observability

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

