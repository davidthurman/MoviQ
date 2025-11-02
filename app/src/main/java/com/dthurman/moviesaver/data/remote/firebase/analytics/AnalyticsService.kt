package com.dthurman.moviesaver.data.remote.firebase.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor() {

    private val analytics: FirebaseAnalytics = Firebase.analytics

    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        analytics.logEvent(eventName) {
            params?.forEach { (key, value) ->
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

    fun setUserProperty(name: String, value: String?) {
        analytics.setUserProperty(name, value)
    }

    fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }

    object Events {
        const val MOVIE_SAVED = "movie_saved"
        const val MOVIE_REMOVED = "movie_removed"
        const val MOVIE_RATED = "movie_rated"
        const val MOVIE_SEARCHED = "movie_searched"
        const val MOVIE_DETAILS_VIEWED = "movie_details_viewed"
        const val AI_RECOMMENDATION_REQUESTED = "ai_recommendation_requested"
        const val AI_RECOMMENDATION_RECEIVED = "ai_recommendation_received"
        const val CREDITS_PURCHASED = "credits_purchased"
        const val CREDITS_USED = "credits_used"
        const val USER_SIGNED_IN = "user_signed_in"
        const val USER_SIGNED_OUT = "user_signed_out"
        const val THEME_CHANGED = "theme_changed"
        const val SCREEN_VIEWED = "screen_viewed"
    }

    object Params {
        const val MOVIE_ID = "movie_id"
        const val MOVIE_TITLE = "movie_title"
        const val RATING = "rating"
        const val SEARCH_QUERY = "search_query"
        const val SCREEN_NAME = "screen_name"
        const val CREDITS_AMOUNT = "credits_amount"
        const val PURCHASE_SKU = "purchase_sku"
        const val THEME_MODE = "theme_mode"
        const val RECOMMENDATION_COUNT = "recommendation_count"
        const val ERROR_MESSAGE = "error_message"
    }

    object UserProperties {
        const val SUBSCRIPTION_STATUS = "subscription_status"
        const val TOTAL_MOVIES_SAVED = "total_movies_saved"
        const val CREDITS_BALANCE = "credits_balance"
    }

    fun logMovieSaved(movieId: Int, movieTitle: String) {
        logEvent(Events.MOVIE_SAVED, mapOf(
            Params.MOVIE_ID to movieId,
            Params.MOVIE_TITLE to movieTitle
        ))
    }

    fun logMovieRemoved(movieId: Int, movieTitle: String) {
        logEvent(Events.MOVIE_REMOVED, mapOf(
            Params.MOVIE_ID to movieId,
            Params.MOVIE_TITLE to movieTitle
        ))
    }

    fun logMovieRated(movieId: Int, movieTitle: String, rating: Float) {
        logEvent(Events.MOVIE_RATED, mapOf(
            Params.MOVIE_ID to movieId,
            Params.MOVIE_TITLE to movieTitle,
            Params.RATING to rating
        ))
    }

    fun logMovieSearched(query: String) {
        logEvent(Events.MOVIE_SEARCHED, mapOf(
            Params.SEARCH_QUERY to query
        ))
    }

    fun logMovieDetailsViewed(movieId: Int, movieTitle: String) {
        logEvent(Events.MOVIE_DETAILS_VIEWED, mapOf(
            Params.MOVIE_ID to movieId,
            Params.MOVIE_TITLE to movieTitle
        ))
    }

    fun logAiRecommendationRequested() {
        logEvent(Events.AI_RECOMMENDATION_REQUESTED)
    }

    fun logAiRecommendationReceived(count: Int) {
        logEvent(Events.AI_RECOMMENDATION_RECEIVED, mapOf(
            Params.RECOMMENDATION_COUNT to count
        ))
    }

    fun logCreditsPurchased(sku: String, amount: Int) {
        logEvent(Events.CREDITS_PURCHASED, mapOf(
            Params.PURCHASE_SKU to sku,
            Params.CREDITS_AMOUNT to amount
        ))
    }

    fun logCreditsUsed(amount: Int) {
        logEvent(Events.CREDITS_USED, mapOf(
            Params.CREDITS_AMOUNT to amount
        ))
    }

    fun logUserSignedIn(method: String) {
        logEvent(Events.USER_SIGNED_IN, mapOf(
            FirebaseAnalytics.Param.METHOD to method
        ))
    }

    fun logUserSignedOut() {
        logEvent(Events.USER_SIGNED_OUT)
    }

    fun logThemeChanged(themeMode: String) {
        logEvent(Events.THEME_CHANGED, mapOf(
            Params.THEME_MODE to themeMode
        ))
    }

    fun logScreenView(screenName: String) {
        logEvent(Events.SCREEN_VIEWED, mapOf(
            Params.SCREEN_NAME to screenName,
            FirebaseAnalytics.Param.SCREEN_NAME to screenName
        ))
    }
}

