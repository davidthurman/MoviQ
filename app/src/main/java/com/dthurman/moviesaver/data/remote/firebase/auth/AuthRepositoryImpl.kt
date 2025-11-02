package com.dthurman.moviesaver.data.remote.firebase.auth

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.ProductDetails
import com.dthurman.moviesaver.data.remote.billing.BillingConnectionState
import com.dthurman.moviesaver.data.remote.billing.BillingManager
import com.dthurman.moviesaver.data.remote.billing.PurchaseState
import com.dthurman.moviesaver.data.remote.firebase.firestore.FirestoreSyncService
import com.dthurman.moviesaver.domain.model.User
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Provider

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreSyncService: FirestoreSyncService,
    private val movieRepositoryProvider: Provider<MovieRepository>,
    private val billingManager: BillingManager
) : AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
        private const val CREDITS_FOR_PURCHASE = 50
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    private val movieRepository: MovieRepository
        get() = movieRepositoryProvider.get()

    private val _currentUserCache = MutableStateFlow<User?>(null)

    override val currentUser: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = firestoreSyncService.fetchUserProfile(firebaseUser.uid)
                    val user = result.getOrNull() ?: firebaseUser.toUser()
                    _currentUserCache.value = user
                    val sendResult = trySend(user)
                }
            } else {
                _currentUserCache.value = null
                val sendResult = trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val result = firestoreSyncService.fetchUserProfile(firebaseUser.uid)
                val user = result.getOrNull() ?: firebaseUser.toUser()
                _currentUserCache.value = user
                val sendResult = trySend(user)
            }
        } else {
            val sendResult = trySend(null)
        }
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener) 
        }
    }

    override fun getCurrentUser(): User? {
        return _currentUserCache.value ?: firebaseAuth.currentUser?.toUser()
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                
                if (isNewUser) {
                    val newUser = firebaseUser.toUser(credits = 10)
                    firestoreSyncService.createOrUpdateUserProfile(newUser)
                    _currentUserCache.value = newUser
                    Log.d(TAG, "New user created with 10 credits")
                    Result.success(newUser)
                } else {
                    val userResult = firestoreSyncService.fetchUserProfile(firebaseUser.uid)
                    val user = userResult.getOrNull() ?: firebaseUser.toUser()
                    firestoreSyncService.createOrUpdateUserProfile(user)
                    _currentUserCache.value = user
                    Log.d(TAG, "Existing user signed in with ${user.credits} credits")
                    Result.success(user)
                }
            } else {
                Result.failure(Exception("Failed to get user information"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            clearLocalCache()
            _currentUserCache.value = null
            firebaseAuth.signOut()
            Log.d(TAG, "User signed out and local cache cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out: ${e.message}", e)
            firebaseAuth.signOut()
        }
    }
    
    override suspend fun clearLocalCache() {
        try {
            Log.d(TAG, "Clearing local cache...")
            movieRepository.clearAllLocalData()
            _currentUserCache.value = null
            Log.d(TAG, "Local cache cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local cache: ${e.message}", e)
            throw e
        }
    }

    private suspend fun <T> retryWithExponentialBackoff(
        maxAttempts: Int = MAX_RETRY_ATTEMPTS,
        initialDelayMs: Long = RETRY_DELAY_MS,
        operation: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(maxAttempts) { attempt ->
            try {
                val result = operation()
                if (result.isSuccess) {
                    if (attempt > 0) {
                        Log.d(TAG, "Operation succeeded on attempt ${attempt + 1}")
                    }
                    return result
                }
                lastException = result.exceptionOrNull() as? Exception
            } catch (e: Exception) {
                lastException = e
            }
            
            if (attempt < maxAttempts - 1) {
                Log.w(TAG, "Attempt ${attempt + 1} failed, retrying in ${currentDelay}ms...")
                kotlinx.coroutines.delay(currentDelay)
                currentDelay *= 2
            }
        }
        
        return Result.failure(lastException ?: Exception("Operation failed after $maxAttempts attempts"))
    }

    override fun getCreditsFlow(): Flow<Int> {
        return _currentUserCache.map { it?.credits ?: 0 }
    }

    override suspend fun getCredits(): Int {
        val cachedUser = _currentUserCache.value
        if (cachedUser != null) {
            return cachedUser.credits
        }
        
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val result = firestoreSyncService.fetchUserProfile(userId)
            val user = result.getOrNull()
            if (user != null) {
                _currentUserCache.value = user
                return user.credits
            }
        }
        return 0
    }

    override suspend fun deductCredit(): Boolean {
        val user = _currentUserCache.value
        if (user == null) {
            Log.w(TAG, "Cannot deduct credit: user not logged in")
            return false
        }
        
        if (user.credits <= 0) {
            Log.w(TAG, "No credits available to deduct")
            return false
        }
        
        val newCredits = user.credits - 1
        
        val result = retryWithExponentialBackoff {
            firestoreSyncService.updateUserCredits(user.id, newCredits)
        }
        
        return if (result.isSuccess) {
            _currentUserCache.value = user.copy(credits = newCredits)
            Log.d(TAG, "Credit deducted successfully. Remaining: $newCredits")
            true
        } else {
            Log.e(TAG, "Failed to deduct credit after ${MAX_RETRY_ATTEMPTS} attempts: ${result.exceptionOrNull()?.message}")
            false
        }
    }

    override suspend fun addCredits(credits: Int): Boolean {
        val user = _currentUserCache.value
        if (user == null) {
            Log.w(TAG, "Cannot add credits: user not logged in")
            return false
        }
        
        val newCredits = user.credits + credits
        
        val result = retryWithExponentialBackoff {
            firestoreSyncService.updateUserCredits(user.id, newCredits)
        }
        
        return if (result.isSuccess) {
            _currentUserCache.value = user.copy(credits = newCredits)
            Log.d(TAG, "Added $credits credits. New total: $newCredits")
            true
        } else {
            Log.e(TAG, "Failed to add credits after ${MAX_RETRY_ATTEMPTS} attempts: ${result.exceptionOrNull()?.message}")
            false
        }
    }

    override val connectionState: StateFlow<BillingConnectionState>
        get() = billingManager.connectionState

    override val purchaseState: StateFlow<PurchaseState>
        get() = billingManager.purchaseState

    override val productDetails: StateFlow<ProductDetails?>
        get() = billingManager.productDetails

    override fun launchPurchaseFlow(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    override fun resetPurchaseState() {
        billingManager.resetPurchaseState()
    }

    override suspend fun processPurchase(purchaseToken: String): Boolean {
        return try {
            val user = getCurrentUser()
            
            if (user != null) {
                val success = addCredits(CREDITS_FOR_PURCHASE)
                
                if (success) {
                    Log.d(TAG, "Successfully processed purchase - added $CREDITS_FOR_PURCHASE credits")
                    true
                } else {
                    Log.e(TAG, "Failed to process purchase - could not add credits")
                    false
                }
            } else {
                Log.e(TAG, "Cannot process purchase: user not logged in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing purchase: ${e.message}", e)
            false
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
