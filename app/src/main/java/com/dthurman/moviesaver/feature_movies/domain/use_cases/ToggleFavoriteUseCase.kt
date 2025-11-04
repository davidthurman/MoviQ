package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

/**
 * Use case for toggling favorite status of a movie.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie): Result<Unit> {
        return try {
            val newStatus = !movie.isFavorite
            movieRepository.updateFavoriteStatus(movie, newStatus)
            
            if (newStatus) {
                analytics.logEvent("movie_favorited", mapOf("movie_id" to movie.id))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

