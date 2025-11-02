package com.dthurman.moviesaver.domain.repository

import com.dthurman.moviesaver.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    fun getCurrentUser(): User?
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
    suspend fun clearLocalCache()
}

