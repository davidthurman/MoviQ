package com.dthurman.moviesaver.data.auth

import com.dthurman.moviesaver.domain.model.User
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        android.util.Log.d("TESTING123", "AuthRepository: Creating currentUser Flow")
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.toUser()
            android.util.Log.d("TESTING123", "AuthRepository: Auth state listener fired - user=${user?.email ?: "null"}")
            val result = trySend(user)
            android.util.Log.d("TESTING123", "AuthRepository: trySend result=${result.isSuccess}")
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        android.util.Log.d("TESTING123", "AuthRepository: Auth listener added")
        // Send initial value immediately
        val initialUser = firebaseAuth.currentUser?.toUser()
        android.util.Log.d("TESTING123", "AuthRepository: Sending initial user=${initialUser?.email ?: "null"}")
        trySend(initialUser)
        awaitClose { 
            android.util.Log.d("TESTING123", "AuthRepository: Closing Flow, removing listener")
            firebaseAuth.removeAuthStateListener(authStateListener) 
        }
    }

    override fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toUser()
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        android.util.Log.d("TESTING123", "AuthRepository: signInWithGoogle called")
        return try {
            android.util.Log.d("TESTING123", "AuthRepository: Creating credential")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            android.util.Log.d("TESTING123", "AuthRepository: Signing in with credential")
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            android.util.Log.d("TESTING123", "AuthRepository: Sign in completed, getting user")
            val user = authResult.user?.toUser()
            android.util.Log.d("TESTING123", "AuthRepository: User from result=${user?.email ?: "null"}")
            if (user != null) {
                android.util.Log.d("TESTING123", "AuthRepository: Returning success with user=${user.email}")
                Result.success(user)
            } else {
                android.util.Log.e("TESTING123", "AuthRepository: User is null!")
                Result.failure(Exception("Failed to get user information"))
            }
        } catch (e: Exception) {
            android.util.Log.e("TESTING123", "AuthRepository: Sign in exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
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

