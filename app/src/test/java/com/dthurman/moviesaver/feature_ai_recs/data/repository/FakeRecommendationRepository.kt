package com.dthurman.moviesaver.feature_ai_recs.data.repository

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeRecommendationRepository : RecommendationRepository {
    
    private val _savedRecommendations = MutableStateFlow<List<Movie>>(emptyList())
    private val _notInterestedMovies = mutableListOf<Movie>()
    
    var saveRecommendationsCallCount = 0
    var markAsNotInterestedCallCount = 0
    var lastSavedRecommendations: List<Movie>? = null
    var lastMarkedNotInterestedId: Int? = null
    
    override fun getSavedRecommendations(): Flow<List<Movie>> {
        return _savedRecommendations.asStateFlow()
    }
    
    override suspend fun saveRecommendations(recommendations: List<Movie>) {
        saveRecommendationsCallCount++
        lastSavedRecommendations = recommendations
        _savedRecommendations.value = recommendations
    }
    
    override suspend fun markAsNotInterested(movieId: Int) {
        markAsNotInterestedCallCount++
        lastMarkedNotInterestedId = movieId
        
        val movie = _savedRecommendations.value.find { it.id == movieId }
        if (movie != null && !_notInterestedMovies.any { it.id == movieId }) {
            _notInterestedMovies.add(movie.copy(notInterested = true))
        }
        
        _savedRecommendations.value = _savedRecommendations.value.filter { it.id != movieId }
    }
    
    override suspend fun getNotInterestedMovies(): List<Movie> {
        return _notInterestedMovies.toList()
    }
    
    fun setSavedRecommendations(recommendations: List<Movie>) {
        _savedRecommendations.value = recommendations
    }
    
    fun reset() {
        _savedRecommendations.value = emptyList()
        _notInterestedMovies.clear()
        saveRecommendationsCallCount = 0
        markAsNotInterestedCallCount = 0
        lastSavedRecommendations = null
        lastMarkedNotInterestedId = null
    }
}

