package com.dthurman.moviesaver.data.remote

import com.dthurman.moviesaver.data.local.database.MovieEntity
import com.dthurman.moviesaver.domain.model.Movie

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w1280"
fun MovieDto.toDomain(): Movie = Movie(
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

fun MovieEntity.toDomain(): Movie = Movie(
    id = id,
    title = title,
    posterUrl = POSTER_BASE + posterUrl,
    backdropUrl = BACKDROP_BASE + backdropUrl,
    releaseDate = releaseDate,
    overview = overview,
    isSeen = isSeen,
    isWatchlist = isWatchlist,
    isFavorite = isFavorite,
    rating = rating
)

fun Movie.toEntity(): MovieEntity = MovieEntity(
    id = id,
    title = title,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    releaseDate = releaseDate,
    overview = overview,
    isSeen = isSeen,
    isWatchlist = isWatchlist,
    isFavorite = isFavorite,
    rating = rating,
    lastModified = System.currentTimeMillis()
)