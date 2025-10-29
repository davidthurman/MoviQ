package com.dthurman.moviesaver.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val releaseDate: String,
    val overview: String,
    val isSeen: Boolean = false,
    val isWatchlist: Boolean = false,
    val isFavorite: Boolean = false,
    val rating: Float? = null
)