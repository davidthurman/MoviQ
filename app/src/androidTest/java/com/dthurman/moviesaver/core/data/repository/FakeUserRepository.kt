package com.dthurman.moviesaver.core.data.repository

import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserRepository : UserRepository {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()
    
    var shouldUpdateSucceed = true
    var shouldRefreshSucceed = true
    var updateUserCallCount = 0
    var refreshUserCallCount = 0
    var lastUpdatedUser: User? = null
    
    override suspend fun getCurrentUser(): User? {
        return _currentUser.value
    }
    
    override suspend fun updateUserProfile(user: User): Result<Unit> {
        updateUserCallCount++
        lastUpdatedUser = user
        
        return if (shouldUpdateSucceed) {
            _currentUser.value = user
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to update user profile"))
        }
    }
    
    override suspend fun refreshUserProfile(): Result<User> {
        refreshUserCallCount++
        
        return if (shouldRefreshSucceed) {
            val user = _currentUser.value ?: User(
                id = "refreshed_user_id",
                email = "refreshed@example.com",
                displayName = "Refreshed User",
                photoUrl = "https://example.com/refreshed.jpg",
                credits = 15
            )
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Failed to refresh user profile"))
        }
    }
    
    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }
}


