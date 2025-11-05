package com.dthurman.moviesaver.core.observability

interface ErrorLogger {
    fun log(message: String)
    fun recordException(exception: Throwable)
    fun setUserId(userId: String?)
    fun clearUserId()
    fun setCustomKey(key: String, value: Any)
    fun logAuthError(exception: Throwable)
    fun logBillingError(responseCode: Int, exception: Throwable)
    fun logDatabaseError(operation: String, exception: Throwable)
    fun logNetworkError(operation: String, exception: Throwable)
    fun logAiError(exception: Throwable)
}

