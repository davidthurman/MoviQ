package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

/**
 * Use case for updating a movie's favorite status.
 * Business rule: Can only favorite movies that are marked as seen.
 */
class UpdateFavoriteStatusUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie, isFavorite: Boolean): Result<Unit> {
        return try {
            // Business rule: Can only favorite movies marked as seen
            if (isFavorite && !movie.isSeen) {
                return Result.failure(IllegalStateException("Cannot favorite a movie that hasn't been seen"))
            }
            
            movieRepository.updateFavoriteStatus(movie, isFavorite)
            
            val eventName = if (isFavorite) "movie_favorited" else "movie_unfavorited"
            analytics.logEvent(eventName, mapOf("movie_id" to movie.id, "movie_title" to movie.title))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

