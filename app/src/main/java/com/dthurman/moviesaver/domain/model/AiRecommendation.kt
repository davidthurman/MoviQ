package com.dthurman.moviesaver.domain.model

data class AiMovieRecommendation(
    val title: String,
    val year: Int,
    val reason: String
)

data class MovieRecommendation(
    val movie: Movie,
    val aiReason: String
)


