package com.dthurman.moviesaver.feature_movies.data.remote.movie_information

/**
 * Interface for fetching movie information from external sources.
 * Abstraction over movie information APIs (TheMovieDB, OMDB, etc.)
 */
interface MovieInformationDataSource {
    suspend fun getPopularMovies(): Result<List<MovieInformationDto>>
    suspend fun searchMovies(query: String): Result<List<MovieInformationDto>>
}

