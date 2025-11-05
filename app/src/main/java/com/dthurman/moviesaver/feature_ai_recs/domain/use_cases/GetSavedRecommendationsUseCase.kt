package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedRecommendationsUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    operator fun invoke(): Flow<List<Movie>> {
        return aiRepository.getSavedRecommendations()
    }
}

