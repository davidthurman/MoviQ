package com.dthurman.moviesaver.core.data.repository

import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.domain.repository.LocalDataManager
import com.dthurman.moviesaver.core.observability.ErrorLogger
import javax.inject.Inject

class LocalDataManagerImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val errorLogger: ErrorLogger
) : LocalDataManager {
    
    override suspend fun clearAllLocalData() {
        try {
            movieDao.clearAllMovies()
        } catch (e: Exception) {
            errorLogger.logDatabaseError("clearAllLocalData", e)
            throw e
        }
    }
}

