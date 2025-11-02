package com.dthurman.moviesaver.domain.repository

import com.dthurman.moviesaver.domain.model.MovieRecommendation
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI-powered features
 */
interface AiRepository {
    suspend fun generatePersonalizedRecommendations(): List<MovieRecommendation>
    fun getSavedRecommendations(): Flow<List<MovieRecommendation>>
    suspend fun saveRecommendations(recommendations: List<MovieRecommendation>)
}

