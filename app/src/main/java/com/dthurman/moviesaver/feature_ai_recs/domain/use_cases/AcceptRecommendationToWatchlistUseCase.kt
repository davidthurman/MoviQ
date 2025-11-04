package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

/**
 * Use case for accepting an AI recommendation and adding it to watchlist.
 * Removes the AI recommendation reason when accepting.
 */
class AcceptRecommendationToWatchlistUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie): Result<Unit> {
        return try {
            val cleanMovie = movie.copy(aiReason = null)
            movieRepository.updateWatchlistStatus(cleanMovie, true)
            analytics.logEvent("ai_recommendation_accepted_watchlist", mapOf("movie_id" to movie.id))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

