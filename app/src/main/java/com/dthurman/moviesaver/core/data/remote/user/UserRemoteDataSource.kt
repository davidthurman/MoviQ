package com.dthurman.moviesaver.core.data.remote.user

import com.dthurman.moviesaver.core.domain.model.User

interface UserRemoteDataSource {
    suspend fun fetchUserProfile(userId: String): Result<User?>
    suspend fun createUserProfile(user: User): Result<Unit>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun updateUserCredits(userId: String, credits: Int): Result<Unit>
}


