package com.dthurman.moviesaver.core.observability

/**
 * Interface for tracking analytics events across the application.
 * This abstraction allows for easy testing and swapping of analytics providers.
 */
interface AnalyticsTracker {
    /**
     * Log a generic analytics event
     */
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    
    /**
     * Set the current user ID for analytics tracking
     */
    fun setUserId(userId: String?)
    
    /**
     * Set a user property for analytics
     */
    fun setUserProperty(name: String, value: String)
    
    /**
     * Log a screen view event
     */
    fun logScreenView(screenName: String, screenClass: String)
    
    // Domain-specific analytics methods
    
    /**
     * Log when a movie is saved by the user
     */
    fun logMovieSaved(movieId: Int, title: String)
    
    /**
     * Log when credits are used
     */
    fun logCreditsUsed(amount: Int)
    
    /**
     * Log when credits are purchased
     */
    fun logCreditsPurchased(sku: String, amount: Int)
    
    /**
     * Log when a movie is rated
     */
    fun logMovieRated(movieId: Int, movieTitle: String, rating: Float)
    
    /**
     * Log when a movie is searched
     */
    fun logMovieSearched(query: String)
    
    /**
     * Log when movie details are viewed
     */
    fun logMovieDetailsViewed(movieId: Int, movieTitle: String)
}

