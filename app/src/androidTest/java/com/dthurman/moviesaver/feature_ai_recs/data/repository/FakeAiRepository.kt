package com.dthurman.moviesaver.feature_ai_recs.data.repository

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow

class FakeAiRepository(
    private val recommendationRepository: RecommendationRepository
) : AiRepository {
    
    var shouldGenerateSucceed = true
    var generateCallCount = 0
    var recommendationsToGenerate = emptyList<Movie>()
    
    override suspend fun generatePersonalizedRecommendations(): List<Movie> {
        generateCallCount++
        
        return if (shouldGenerateSucceed) {
            recommendationRepository.saveRecommendations(recommendationsToGenerate)
            recommendationsToGenerate
        } else {
            throw Exception("Failed to generate recommendations")
        }
    }
    
    override fun getSavedRecommendations(): Flow<List<Movie>> {
        return recommendationRepository.getSavedRecommendations()
    }
}

