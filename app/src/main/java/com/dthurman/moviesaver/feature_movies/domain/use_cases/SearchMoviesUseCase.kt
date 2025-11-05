package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

/**
 * Use case for searching movies by title.
 * Handles search execution and analytics tracking.
 */
class SearchMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(query: String): Result<List<Movie>> {
        val trimmedQuery = query.trim()
        
        // Simple validation: minimum 2 characters
        if (trimmedQuery.length < 2) {
            return Result.failure(IllegalArgumentException("Search query must be at least 2 characters"))
        }

        return try {
            val movies = movieRepository.searchMovieByTitle(trimmedQuery)
            analytics.logMovieSearched(trimmedQuery)
            analytics.logEvent("movie_search", mapOf(
                "query" to trimmedQuery,
                "results_count" to movies.size
            ))
            Result.success(movies)
        } catch (e: Exception) {
            analytics.logEvent("movie_search_error", mapOf(
                "query" to trimmedQuery,
                "error" to (e.message ?: "unknown")
            ))
            Result.failure(e)
        }
    }
}

