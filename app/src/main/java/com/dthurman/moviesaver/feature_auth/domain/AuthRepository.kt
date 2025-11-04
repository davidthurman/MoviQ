package com.dthurman.moviesaver.feature_auth.domain

import com.dthurman.moviesaver.core.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
}