package com.dthurman.moviesaver.feature_movies.data.remote.movie_information

import androidx.room.PrimaryKey
import com.dthurman.moviesaver.core.domain.model.Movie

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w1280"

data class MovieInformationDto(
    val adult: Boolean,
    val backdrop_path: String?,
    @PrimaryKey val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
)

fun MovieInformationDto.toDomain(): Movie = Movie(
    id = id,
    title = title,
    posterUrl = poster_path?.let { POSTER_BASE + it } ?: "",
    backdropUrl = backdrop_path?.let { BACKDROP_BASE + it } ?: "",
    releaseDate = release_date,
    overview = overview,
    isSeen = false,
    isWatchlist = false,
    isFavorite = false,
    rating = null
)


data class MovieListDto(
    val page: Int,
    val results: List<MovieInformationDto>,
    val total_pages: Int,
    val total_results: Int
)