package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

/**
 * Use case for marking a movie as seen.
 * Encapsulates the business rule: "When a movie is marked as seen, it should be removed from watchlist"
 */
class MarkMovieAsSeenUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie, rating: Float? = null): Result<Unit> {
        return try {
            // Business rule: Remove from watchlist when marking as seen
            movieRepository.updateWatchlistStatus(movie, false)
            movieRepository.updateSeenStatus(movie, true)
            
            if (rating != null) {
                movieRepository.updateRating(movie, rating)
            }
            
            // Analytics tracking
            analytics.logMovieSaved(movie.id, movie.title)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

