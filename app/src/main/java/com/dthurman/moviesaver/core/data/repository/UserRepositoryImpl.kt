package com.dthurman.moviesaver.core.data.repository

import com.dthurman.moviesaver.core.data.remote.user.UserRemoteDataSource
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val errorLogger: ErrorLogger
) : UserRepository {

    private val _currentUserCache = MutableStateFlow<User?>(null)

    override val currentUser: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val result = userRemoteDataSource.fetchUserProfile(firebaseUser.uid)
                        val user = result.getOrNull() ?: firebaseUser.toUser()
                        _currentUserCache.value = user
                        trySend(user)
                    } catch (e: Exception) {
                        errorLogger.logAuthError(e)
                        val user = firebaseUser.toUser()
                        _currentUserCache.value = user
                        trySend(user)
                    }
                }
            } else {
                _currentUserCache.value = null
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = userRemoteDataSource.fetchUserProfile(firebaseUser.uid)
                    val user = result.getOrNull() ?: firebaseUser.toUser()
                    _currentUserCache.value = user
                    trySend(user)
                } catch (e: Exception) {
                    errorLogger.logAuthError(e)
                    val user = firebaseUser.toUser()
                    _currentUserCache.value = user
                    trySend(user)
                }
            }
        } else {
            trySend(null)
        }

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return _currentUserCache.value ?: firebaseAuth.currentUser?.toUser()
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            userRemoteDataSource.updateUserProfile(user)
            _currentUserCache.value = user
            Result.success(Unit)
        } catch (e: Exception) {
            errorLogger.logAuthError(e)
            Result.failure(e)
        }
    }

    override suspend fun refreshUserProfile(): Result<User> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("No authenticated user"))
            
            val result = userRemoteDataSource.fetchUserProfile(userId)
            val user = result.getOrNull()
                ?: return Result.failure(Exception("User profile not found"))
            _currentUserCache.value = user
            Result.success(user)
        } catch (e: Exception) {
            errorLogger.logAuthError(e)
            Result.failure(e)
        }
    }

    internal fun setCurrentUser(user: User?) {
        _currentUserCache.value = user
    }

    internal fun clearCache() {
        _currentUserCache.value = null
    }

    private fun FirebaseUser.toUser(credits: Int = 10): User {
        return User(
            id = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            credits = credits
        )
    }
}