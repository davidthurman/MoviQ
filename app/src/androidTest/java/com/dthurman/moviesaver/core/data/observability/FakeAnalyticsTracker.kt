package com.dthurman.moviesaver.core.data.observability

import com.dthurman.moviesaver.core.observability.AnalyticsTracker

class FakeAnalyticsTracker: AnalyticsTracker {
    val events = mutableMapOf<String, Map<String, Any>>()

    override fun logEvent(
        eventName: String,
        params: Map<String, Any>
    ) {
        events[eventName] = params
    }

    override fun setUserId(userId: String?) {
        logEvent("set_user_id", mapOf("userId" to (userId ?: "null")))
    }

    override fun setUserProperty(name: String, value: String) {
        logEvent("set_user_property", mapOf("name" to name, "value" to value))
    }

    override fun logScreenView(screenName: String, screenClass: String) {
        logEvent("screen_view", mapOf("screen_name" to screenName, "screen_class" to screenClass))
    }

    override fun logMovieSaved(movieId: Int, title: String) {
        logEvent("movie_saved", mapOf("movie_id" to movieId, "title" to title))
    }

    override fun logCreditsUsed(amount: Int) {
        logEvent("credits_used", mapOf("amount" to amount))
    }

    override fun logCreditsPurchased(sku: String, amount: Int) {
        logEvent("credits_purchased", mapOf("sku" to sku, "amount" to amount))
    }

    override fun logMovieRated(
        movieId: Int,
        movieTitle: String,
        rating: Float
    ) {
        logEvent("movie_rated", mapOf("movie_id" to movieId, "movie_title" to movieTitle, "rating" to rating))
    }

    override fun logMovieSearched(query: String) {
        logEvent("movie_searched", mapOf("query" to query))
    }

    override fun logMovieDetailsViewed(movieId: Int, movieTitle: String) {
        logEvent("movie_details_viewed", mapOf("movie_id" to movieId, "movie_title" to movieTitle))
    }

}

