package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

/**
 * Use case for updating a movie's watchlist status.
 * Handles removing from watchlist when needed.
 */
class UpdateWatchlistStatusUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie, isWatchlist: Boolean): Result<Unit> {
        return try {
            movieRepository.updateWatchlistStatus(movie, isWatchlist)
            
            val eventName = if (isWatchlist) "movie_added_to_watchlist" else "movie_removed_from_watchlist"
            analytics.logEvent(eventName, mapOf("movie_id" to movie.id, "movie_title" to movie.title))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

