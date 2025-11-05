package com.dthurman.moviesaver.core.observability

interface AnalyticsTracker {
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String)
    fun logScreenView(screenName: String, screenClass: String)
    fun logMovieSaved(movieId: Int, title: String)
    fun logCreditsUsed(amount: Int)
    fun logCreditsPurchased(sku: String, amount: Int)
    fun logMovieRated(movieId: Int, movieTitle: String, rating: Float)
    fun logMovieSearched(query: String)
    fun logMovieDetailsViewed(movieId: Int, movieTitle: String)
}

