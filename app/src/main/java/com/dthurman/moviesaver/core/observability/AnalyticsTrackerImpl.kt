package com.dthurman.moviesaver.core.observability

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of AnalyticsTracker.
 * Handles all analytics tracking using Firebase Analytics.
 */
@Singleton
class AnalyticsTrackerImpl @Inject constructor() : AnalyticsTracker {

    private val analytics: FirebaseAnalytics = Firebase.analytics

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        analytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                    is Double -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Float -> param(key, value.toDouble())
                    is Boolean -> param(key, if (value) 1L else 0L)
                    else -> param(key, value.toString())
                }
            }
        }
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }

    override fun logScreenView(screenName: String, screenClass: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }

    override fun logMovieSaved(movieId: Int, title: String) {
        logEvent(Events.MOVIE_SAVED, mapOf(
            Params.MOVIE_ID to movieId,
            Params.MOVIE_TITLE to title
        ))
    }

    override fun logCreditsUsed(amount: Int) {
        logEvent(Events.CREDITS_USED, mapOf(
            Params.CREDITS_AMOUNT to amount
        ))
    }

    override fun logCreditsPurchased(sku: String, amount: Int) {
        logEvent(Events.CREDITS_PURCHASED, mapOf(
            Params.PURCHASE_SKU to sku,
            Params.CREDITS_AMOUNT to amount
        ))
    }

    // Additional domain-specific methods (not in interface, but used by features)
    
    override fun logMovieRated(movieId: Int, movieTitle: String, rating: Float) {
        logEvent(Events.MOVIE_RATED, mapOf(
            Params.MOVIE_ID to movieId,
            Params.MOVIE_TITLE to movieTitle,
            Params.RATING to rating
        ))
    }

    override fun logMovieSearched(query: String) {
        logEvent(Events.MOVIE_SEARCHED, mapOf(
            Params.SEARCH_QUERY to query
        ))
    }

    override fun logMovieDetailsViewed(movieId: Int, movieTitle: String) {
        logEvent(Events.MOVIE_DETAILS_VIEWED, mapOf(
            Params.MOVIE_ID to movieId,
            Params.MOVIE_TITLE to movieTitle
        ))
    }

    // Event names
    private object Events {
        const val MOVIE_SAVED = "movie_saved"
        const val MOVIE_RATED = "movie_rated"
        const val MOVIE_SEARCHED = "movie_searched"
        const val MOVIE_DETAILS_VIEWED = "movie_details_viewed"
        const val CREDITS_PURCHASED = "credits_purchased"
        const val CREDITS_USED = "credits_used"
    }

    // Parameter names
    private object Params {
        const val MOVIE_ID = "movie_id"
        const val MOVIE_TITLE = "movie_title"
        const val RATING = "rating"
        const val SEARCH_QUERY = "search_query"
        const val CREDITS_AMOUNT = "credits_amount"
        const val PURCHASE_SKU = "purchase_sku"
    }
}

