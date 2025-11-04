package com.dthurman.moviesaver.feature_ai_recs.domain.repository

import com.dthurman.moviesaver.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun generatePersonalizedRecommendations(): List<Movie>
    fun getSavedRecommendations(): Flow<List<Movie>>
}