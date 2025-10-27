package com.dthurman.moviesaver.data.the_movie_db

import com.dthurman.moviesaver.data.local.database.MovieEntity
import com.dthurman.moviesaver.domain.model.Movie

private const val IMAGE_BASE = "https://image.tmdb.org/t/p/w500"

fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title,
    posterUrl = IMAGE_BASE + poster_path,
    releaseDate = release_date,
    overview = overview,
)

fun MovieEntity.toDomain(): Movie = Movie(
    id = id,
    title = title,
    posterUrl = posterUrl,
    releaseDate = releaseDate,
    overview = overview,
)

fun Movie.toEntity(): MovieEntity = MovieEntity(
    id = id,
    title = title,
    posterUrl = posterUrl,
    releaseDate = releaseDate,
    overview = overview,
)