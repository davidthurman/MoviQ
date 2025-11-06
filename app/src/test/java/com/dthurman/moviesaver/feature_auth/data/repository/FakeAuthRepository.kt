package com.dthurman.moviesaver.feature_auth.data.repository

import com.dthurman.moviesaver.core.data.repository.FakeUserRepository
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.feature_auth.domain.AuthRepository

class FakeAuthRepository(
    private val userRepository: FakeUserRepository? = null
) : AuthRepository {
    
    var shouldSignInSucceed = true
    var signInCallCount = 0
    var lastIdToken: String? = null
    var signOutCallCount = 0
    var mockUser: User? = null
    
    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        signInCallCount++
        lastIdToken = idToken
        
        return if (shouldSignInSucceed) {
            val user = mockUser ?: User(
                id = "test_user_id",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = "https://example.com/photo.jpg",
                credits = 10
            )
            Result.success(user)
        } else {
            Result.failure(Exception("Failed to sign in with Google"))
        }
    }
    
    override suspend fun signOut() {
        signOutCallCount++
        userRepository?.setCurrentUser(null)
    }
    
    fun reset() {
        shouldSignInSucceed = true
        signInCallCount = 0
        lastIdToken = null
        signOutCallCount = 0
        mockUser = null
    }
}

