package com.dthurman.moviesaver.data.auth

import android.util.Log
import com.dthurman.moviesaver.data.remote.firestore.FirestoreSyncService
import com.dthurman.moviesaver.domain.model.User
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.CreditsRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Provider

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val creditsRepository: CreditsRepository,
    private val firestoreSyncService: FirestoreSyncService,
    private val movieRepositoryProvider: Provider<MovieRepository>
) : AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    private val movieRepository: MovieRepository
        get() = movieRepositoryProvider.get()

    override val currentUser: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.toUser()
            val result = trySend(user)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        val initialUser = firebaseAuth.currentUser?.toUser()
        trySend(initialUser)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener) 
        }
    }

    override fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toUser()
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user?.toUser()
            if (user != null) {
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                if (isNewUser) {
                    firestoreSyncService.createOrUpdateUserProfile(user, credits = 10)
                    creditsRepository.initializeCredits(user.id, initialCredits = 10)
                } else {
                    firestoreSyncService.createOrUpdateUserProfile(user)
                    creditsRepository.syncFromFirestore(user.id)
                }
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to get user information"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            // Clear local cache before signing out
            clearLocalCache()
            firebaseAuth.signOut()
            Log.d(TAG, "User signed out and local cache cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out: ${e.message}", e)
            // Still sign out from Firebase even if cache clearing fails
            firebaseAuth.signOut()
        }
    }
    
    override suspend fun clearLocalCache() {
        try {
            Log.d(TAG, "Clearing local cache...")
            movieRepository.clearAllLocalData()
            creditsRepository.clearAllLocalCredits()
            Log.d(TAG, "Local cache cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local cache: ${e.message}", e)
            throw e
        }
    }

    private fun com.google.firebase.auth.FirebaseUser.toUser(): User {
        return User(
            id = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString()
        )
    }
}

