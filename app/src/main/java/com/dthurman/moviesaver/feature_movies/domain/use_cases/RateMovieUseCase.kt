package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

/**
 * Use case for rating a movie.
 * Encapsulates rating logic and analytics tracking.
 */
class RateMovieUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie, rating: Float): Result<Unit> {
        return try {
            movieRepository.updateRating(movie, rating)
            analytics.logMovieRated(movie.id, movie.title, rating)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

