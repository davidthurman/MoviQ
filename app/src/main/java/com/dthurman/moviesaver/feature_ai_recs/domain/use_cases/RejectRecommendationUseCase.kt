package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.RecommendationRepository
import javax.inject.Inject

/**
 * Use case for rejecting an AI recommendation.
 * Marks the movie as "not interested".
 */
class RejectRecommendationUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movieId: Int): Result<Unit> {
        return try {
            recommendationRepository.markAsNotInterested(movieId)
            analytics.logEvent("ai_recommendation_rejected", mapOf("movie_id" to movieId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

