package com.dthurman.moviesaver.feature_movies.domain.util

/**
 * Defines different filters for retrieving movies.
 * Each filter type can have its own sort order.
 */
sealed class MovieFilter(open val order: MovieOrder = MovieOrder.DATE_ADDED_DESC) {
    data class SeenMovies(override val order: MovieOrder = MovieOrder.DATE_ADDED_DESC) : MovieFilter(order)
    data class WatchlistMovies(override val order: MovieOrder = MovieOrder.DATE_ADDED_DESC) : MovieFilter(order)
    data class FavoriteMovies(override val order: MovieOrder = MovieOrder.DATE_ADDED_DESC) : MovieFilter(order)
}