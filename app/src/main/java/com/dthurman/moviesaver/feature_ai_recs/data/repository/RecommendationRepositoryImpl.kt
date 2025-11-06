package com.dthurman.moviesaver.feature_ai_recs.data.repository

import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.data.sync.SyncManager
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.model.SyncState
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val syncManager: SyncManager,
    private val errorLogger: ErrorLogger
) : RecommendationRepository {

    override fun getSavedRecommendations(): Flow<List<Movie>> {
        return movieDao.getRecommendations()
    }

    override suspend fun saveRecommendations(recommendations: List<Movie>) {
        try {
            val moviesToSave = recommendations.map { movie ->
                movie.copy(syncState = SyncState.PENDING_CREATE)
            }
            moviesToSave.forEach { movieDao.insertOrUpdateMovie(it) }
            syncManager.triggerSync()
        } catch (e: Exception) {
            errorLogger.logDatabaseError("saveRecommendations", e)
            throw e
        }
    }

    override suspend fun markAsNotInterested(movieId: Int) {
        try {
            movieDao.updateNotInterested(movieId, true)
            syncManager.triggerSync()
        } catch (e: Exception) {
            errorLogger.logDatabaseError("markAsNotInterested", e)
            throw e
        }
    }

    override suspend fun getNotInterestedMovies(): List<Movie> {
        return try {
            movieDao.getNotInterestedMovies()
        } catch (e: Exception) {
            errorLogger.logDatabaseError("getNotInterestedMovies", e)
            throw e
        }
    }
}


