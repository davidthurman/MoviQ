package com.dthurman.moviesaver.feature_ai_recs.domain.repository

import com.dthurman.moviesaver.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface RecommendationRepository {
    fun getSavedRecommendations(): Flow<List<Movie>>
    suspend fun saveRecommendations(recommendations: List<Movie>)
    suspend fun markAsNotInterested(movieId: Int)
    suspend fun getNotInterestedMovies(): List<Movie>
}

