package com.dthurman.moviesaver.feature_movies.data.remote.movie_information

import com.dthurman.moviesaver.core.observability.ErrorLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TheMovieDB implementation of MovieInformationDataSource.
 * Handles all TheMovieDB API calls.
 */
@Singleton
class TheMovieDBDataSource @Inject constructor(
    private val errorLogger: ErrorLogger
) : MovieInformationDataSource {

    private val api = TheMovieDBApi.theMovieApi

    override suspend fun getPopularMovies(): Result<List<MovieInformationDto>> {
        return try {
            val response = api.getPopularMovies()
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                Result.success(movies)
            } else {
                val error = Exception("Failed to fetch popular movies: ${response.code()} ${response.message()}")
                errorLogger.logNetworkError("getPopularMovies", error)
                Result.failure(error)
            }
        } catch (e: Exception) {
            errorLogger.logNetworkError("getPopularMovies", e)
            Result.failure(e)
        }
    }

    override suspend fun searchMovies(query: String): Result<List<MovieInformationDto>> {
        return try {
            val response = api.searchMovies(query = query)
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                Result.success(movies)
            } else {
                val error = Exception("Failed to search movies: ${response.code()} ${response.message()}")
                errorLogger.logNetworkError("searchMovies", error)
                Result.failure(error)
            }
        } catch (e: Exception) {
            errorLogger.logNetworkError("searchMovies", e)
            Result.failure(e)
        }
    }
}

