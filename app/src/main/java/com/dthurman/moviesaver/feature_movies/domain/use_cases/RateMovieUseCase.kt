package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

class RateMovieUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie, rating: Float): Result<Unit> {
        if (rating !in 0.0f..5.0f) {
            return Result.failure(IllegalArgumentException("Rating must be between 0 and 5"))
        }
        
        return try {
            movieRepository.updateRating(movie, rating)
            analytics.logMovieRated(movie.id, movie.title, rating)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

