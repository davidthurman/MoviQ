package com.dthurman.moviesaver.feature_movies.data.remote.movie_information

interface MovieInformationDataSource {
    suspend fun getPopularMovies(): Result<List<MovieInformationDto>>
    suspend fun searchMovies(query: String): Result<List<MovieInformationDto>>
}

