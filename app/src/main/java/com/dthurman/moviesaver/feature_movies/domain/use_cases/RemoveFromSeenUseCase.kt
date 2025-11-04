package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

/**
 * Use case for removing a movie from the seen list.
 * Also clears the rating when unmarking as seen.
 */
class RemoveFromSeenUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie): Result<Unit> {
        return try {
            // Business rule: Clear rating when unmarking as seen
            movieRepository.updateSeenStatus(movie, false)
            movieRepository.updateRating(movie, null)
            
            analytics.logEvent("movie_removed_from_seen", mapOf("movie_id" to movie.id))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

