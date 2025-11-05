package com.dthurman.moviesaver.feature_movies.domain.util

/**
 * Defines different filters for retrieving movies.
 * Each filter type can have its own sort order.
 */
sealed class MovieFilter(val order: MovieOrder = MovieOrder.DATE_ADDED_DESC) {
    data class SeenMovies(val sortOrder: MovieOrder = MovieOrder.DATE_ADDED_DESC) : MovieFilter(sortOrder)
    data class WatchlistMovies(val sortOrder: MovieOrder = MovieOrder.DATE_ADDED_DESC) : MovieFilter(sortOrder)
    data class FavoriteMovies(val sortOrder: MovieOrder = MovieOrder.DATE_ADDED_DESC) : MovieFilter(sortOrder)
}