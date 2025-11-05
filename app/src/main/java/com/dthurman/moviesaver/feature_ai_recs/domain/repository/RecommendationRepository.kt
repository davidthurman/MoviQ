package com.dthurman.moviesaver.feature_ai_recs.domain.repository

import com.dthurman.moviesaver.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing AI-generated movie recommendations and not-interested movies.
 * This handles the persistence and retrieval of recommendation data.
 */
interface RecommendationRepository {
    /**
     * Get all saved AI recommendations as a Flow for reactive updates.
     */
    fun getSavedRecommendations(): Flow<List<Movie>>
    
    /**
     * Save a list of AI-generated recommendations to local storage.
     * These will be synced to the cloud.
     */
    suspend fun saveRecommendations(recommendations: List<Movie>)
    
    /**
     * Mark a movie as not interested (rejected recommendation).
     * This will exclude the movie from future recommendations.
     */
    suspend fun markAsNotInterested(movieId: Int)
    
    /**
     * Get all movies that the user has marked as not interested.
     * Used to filter out unwanted recommendations.
     */
    suspend fun getNotInterestedMovies(): List<Movie>
}

