package com.dthurman.moviesaver.feature_movies.domain.util

sealed class MovieFilter(open val order: MovieOrder = MovieOrder.DATE_ADDED_DESC) {
    data class SeenMovies(override val order: MovieOrder = MovieOrder.DATE_ADDED_DESC) : MovieFilter(order)
    data class WatchlistMovies(override val order: MovieOrder = MovieOrder.DATE_ADDED_DESC) : MovieFilter(order)
    data class FavoriteMovies(override val order: MovieOrder = MovieOrder.DATE_ADDED_DESC) : MovieFilter(order)
}