package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving saved AI recommendations.
 * Returns a Flow for reactive updates.
 */
class GetRecommendationsUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    operator fun invoke(): Flow<List<Movie>> {
        return recommendationRepository.getSavedRecommendations()
    }
}

