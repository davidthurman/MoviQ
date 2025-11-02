package com.dthurman.moviesaver.domain.repository

import com.dthurman.moviesaver.domain.model.Movie
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI-powered features
 */
interface AiRepository {
    suspend fun generatePersonalizedRecommendations(): List<Movie>
    fun getSavedRecommendations(): Flow<List<Movie>>
}

