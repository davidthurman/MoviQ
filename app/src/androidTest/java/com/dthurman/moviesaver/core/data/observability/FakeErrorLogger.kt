package com.dthurman.moviesaver.core.data.observability

import com.dthurman.moviesaver.core.observability.ErrorLogger

class FakeErrorLogger : ErrorLogger {
    val logs = mutableListOf<String>()
    val exceptions = mutableListOf<Throwable>()
    val customKeys = mutableMapOf<String, Any>()
    var currentUserId: String? = null

    override fun log(message: String) {
        logs.add(message)
    }

    override fun recordException(exception: Throwable) {
        exceptions.add(exception)
    }

    override fun setUserId(userId: String?) {
        this.currentUserId = userId
    }

    override fun clearUserId() {
        this.currentUserId = null
    }

    override fun setCustomKey(key: String, value: Any) {
        customKeys[key] = value
    }

    override fun logAuthError(exception: Throwable) {
        setCustomKey("error_context", "auth_error")
        log("Authentication error: ${exception.message}")
        recordException(exception)
    }

    override fun logBillingError(responseCode: Int, exception: Throwable) {
        setCustomKey("error_context", "billing_error")
        setCustomKey("error_code", responseCode)
        log("Billing error (code $responseCode): ${exception.message}")
        recordException(exception)
    }

    override fun logDatabaseError(operation: String, exception: Throwable) {
        setCustomKey("user_action", operation)
        log("Database error during $operation: ${exception.message}")
        recordException(exception)
    }

    override fun logNetworkError(operation: String, exception: Throwable) {
        setCustomKey("error_context", "network_error")
        setCustomKey("user_action", operation)
        log("Network error during $operation: ${exception.message}")
        recordException(exception)
    }

    override fun logAiError(exception: Throwable) {
        setCustomKey("error_context", "ai_error")
        log("AI error: ${exception.message}")
        recordException(exception)
    }

    fun reset() {
        logs.clear()
        exceptions.clear()
        customKeys.clear()
        currentUserId = null
    }
}


