package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

class AcceptRecommendationToWatchlistUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie): Result<Unit> {
        return try {
            movieRepository.updateWatchlistStatus(movie, true)
            analytics.logEvent("ai_recommendation_accepted_watchlist", mapOf("movie_id" to movie.id))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

