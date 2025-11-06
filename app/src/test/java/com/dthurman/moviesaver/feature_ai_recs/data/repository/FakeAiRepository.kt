package com.dthurman.moviesaver.feature_ai_recs.data.repository

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAiRepository : AiRepository {
    
    private val _savedRecommendations = MutableStateFlow<List<Movie>>(emptyList())
    
    var shouldGenerateSucceed = true
    var generateCallCount = 0
    var recommendationsToGenerate = emptyList<Movie>()
    
    override suspend fun generatePersonalizedRecommendations(): List<Movie> {
        generateCallCount++
        
        return if (shouldGenerateSucceed) {
            recommendationsToGenerate
        } else {
            throw Exception("Failed to generate recommendations")
        }
    }
    
    override fun getSavedRecommendations(): Flow<List<Movie>> {
        return _savedRecommendations.asStateFlow()
    }
    
    fun setSavedRecommendations(recommendations: List<Movie>) {
        _savedRecommendations.value = recommendations
    }
    
    fun reset() {
        _savedRecommendations.value = emptyList()
        shouldGenerateSucceed = true
        generateCallCount = 0
        recommendationsToGenerate = emptyList()
    }
}

