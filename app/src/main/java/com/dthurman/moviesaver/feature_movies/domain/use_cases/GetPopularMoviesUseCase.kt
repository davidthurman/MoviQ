package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

class GetPopularMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(): Result<List<Movie>> {
        return try {
            val movies = movieRepository.getPopularMovies()
            analytics.logEvent("popular_movies_fetched", mapOf("count" to movies.size))
            Result.success(movies)
        } catch (e: Exception) {
            analytics.logEvent("popular_movies_error", mapOf("error" to (e.message ?: "unknown")))
            Result.failure(e)
        }
    }
}


