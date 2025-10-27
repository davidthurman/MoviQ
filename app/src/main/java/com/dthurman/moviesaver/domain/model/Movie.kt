package com.dthurman.moviesaver.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val releaseDate: String,
    val overview: String
)