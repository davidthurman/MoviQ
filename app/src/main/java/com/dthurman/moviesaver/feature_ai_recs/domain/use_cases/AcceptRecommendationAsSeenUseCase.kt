package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.use_cases.UpdateSeenStatusUseCase
import javax.inject.Inject

class AcceptRecommendationAsSeenUseCase @Inject constructor(
    private val updateSeenStatusUseCase: UpdateSeenStatusUseCase,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie, rating: Float? = null): Result<Unit> {
        return try {
            updateSeenStatusUseCase(movie, true, rating)
            analytics.logEvent("ai_recommendation_accepted_seen", mapOf(
                "movie_id" to movie.id,
                "rated" to (rating != null)
            ))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

