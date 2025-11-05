package com.dthurman.moviesaver.feature_auth.data.repository

import com.dthurman.moviesaver.core.data.remote.user.UserRemoteDataSource
import com.dthurman.moviesaver.core.data.repository.UserRepositoryImpl
import com.dthurman.moviesaver.core.data.sync.MovieSyncService
import com.dthurman.moviesaver.core.data.sync.SyncManager
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.domain.repository.LocalDataManager
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_auth.domain.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val localDataManager: LocalDataManager,
    private val movieSyncService: MovieSyncService,
    private val userRepository: UserRepositoryImpl, // Internal access to set user
    private val syncManager: SyncManager,
    private val analytics: AnalyticsTracker,
    private val errorLogger: ErrorLogger
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

                if (isNewUser) {
                    val newUser = firebaseUser.toUser(credits = 10)
                    userRemoteDataSource.createUserProfile(newUser)
                    userRepository.setCurrentUser(newUser)
                    analytics.setUserId(newUser.id)
                    errorLogger.setUserId(newUser.id)
                    syncManager.startPeriodicSync()
                    Result.success(newUser)
                } else {
                    val userResult = userRemoteDataSource.fetchUserProfile(firebaseUser.uid)
                    val user = userResult.getOrNull() ?: firebaseUser.toUser()
                    userRemoteDataSource.updateUserProfile(user)
                    userRepository.setCurrentUser(user)
                    analytics.setUserId(user.id)
                    errorLogger.setUserId(user.id)
                    movieSyncService.syncFromFirestore()
                    syncManager.startPeriodicSync()
                    Result.success(user)
                }
            } else {
                val error = Exception("Failed to get user information")
                errorLogger.logAuthError(error)
                Result.failure(error)
            }
        } catch (e: Exception) {
            errorLogger.logAuthError(e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            syncManager.stopPeriodicSync()
            localDataManager.clearAllLocalData()
            userRepository.clearCache()
            analytics.setUserId(null)
            errorLogger.clearUserId()
            firebaseAuth.signOut()
        } catch (e: Exception) {
            errorLogger.logAuthError(e)
            syncManager.stopPeriodicSync()
            userRepository.clearCache()
            analytics.setUserId(null)
            errorLogger.clearUserId()
            firebaseAuth.signOut()
        }
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