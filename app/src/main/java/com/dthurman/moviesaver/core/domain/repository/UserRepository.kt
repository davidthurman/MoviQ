package com.dthurman.moviesaver.core.domain.repository

import com.dthurman.moviesaver.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentUser: Flow<User?>
    suspend fun getCurrentUser(): User?
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun refreshUserProfile(): Result<User>
}