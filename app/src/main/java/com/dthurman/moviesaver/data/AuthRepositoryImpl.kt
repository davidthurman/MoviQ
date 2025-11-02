package com.dthurman.moviesaver.data

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.dthurman.moviesaver.data.remote.billing.BillingConnectionState
import com.dthurman.moviesaver.data.remote.billing.BillingManager
import com.dthurman.moviesaver.data.remote.billing.PurchaseState
import com.dthurman.moviesaver.data.remote.firebase.analytics.AnalyticsService
import com.dthurman.moviesaver.data.remote.firebase.analytics.CrashlyticsService
import com.dthurman.moviesaver.data.remote.firebase.firestore.FirestoreSyncService
import com.dthurman.moviesaver.data.sync.SyncManager
import com.dthurman.moviesaver.domain.model.User
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
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
    private val billingManager: BillingManager,
    private val syncManager: SyncManager,
    private val analyticsService: AnalyticsService,
    private val crashlyticsService: CrashlyticsService
) : AuthRepository {

    companion object {
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
                    try {
                        val result = firestoreSyncService.fetchUserProfile(firebaseUser.uid)
                        val user = result.getOrNull() ?: firebaseUser.toUser()
                        _currentUserCache.value = user
                        val sendResult = trySend(user)
                    } catch (e: Exception) {
                        crashlyticsService.logAuthError(e)
                        val user = firebaseUser.toUser()
                        _currentUserCache.value = user
                        val sendResult = trySend(user)
                    }
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
                try {
                    val result = firestoreSyncService.fetchUserProfile(firebaseUser.uid)
                    val user = result.getOrNull() ?: firebaseUser.toUser()
                    _currentUserCache.value = user
                    val sendResult = trySend(user)
                } catch (e: Exception) {
                    crashlyticsService.logAuthError(e)
                    val user = firebaseUser.toUser()
                    _currentUserCache.value = user
                    val sendResult = trySend(user)
                }
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
                    analyticsService.setUserId(newUser.id)
                    crashlyticsService.setUserId(newUser.id)
                    syncManager.startPeriodicSync()
                    Result.success(newUser)
                } else {
                    val userResult = firestoreSyncService.fetchUserProfile(firebaseUser.uid)
                    val user = userResult.getOrNull() ?: firebaseUser.toUser()
                    firestoreSyncService.createOrUpdateUserProfile(user)
                    _currentUserCache.value = user
                    analyticsService.setUserId(user.id)
                    crashlyticsService.setUserId(user.id)
                    movieRepository.syncFromFirestore()
                    syncManager.startPeriodicSync()
                    Result.success(user)
                }
            } else {
                val error = Exception("Failed to get user information")
                crashlyticsService.logAuthError(error)
                Result.failure(error)
            }
        } catch (e: Exception) {
            crashlyticsService.logAuthError(e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            syncManager.stopPeriodicSync()
            clearLocalCache()
            _currentUserCache.value = null
            analyticsService.setUserId(null)
            crashlyticsService.clearUserId()
            firebaseAuth.signOut()
        } catch (e: Exception) {
            crashlyticsService.logAuthError(e)
            syncManager.stopPeriodicSync()
            analyticsService.setUserId(null)
            crashlyticsService.clearUserId()
            firebaseAuth.signOut()
        }
    }

    override suspend fun clearLocalCache() {
        try {
            movieRepository.clearAllLocalData()
            _currentUserCache.value = null
        } catch (e: Exception) {
            crashlyticsService.logDatabaseError("clearLocalCache", e)
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
                    return result
                }
                lastException = result.exceptionOrNull() as? Exception
            } catch (e: Exception) {
                lastException = e
            }

            if (attempt < maxAttempts - 1) {
                delay(currentDelay)
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
            return false
        }

        if (user.credits <= 0) {
            return false
        }

        val newCredits = user.credits - 1

        val result = retryWithExponentialBackoff {
            firestoreSyncService.updateUserCredits(user.id, newCredits)
        }

        return if (result.isSuccess) {
            _currentUserCache.value = user.copy(credits = newCredits)
            analyticsService.logCreditsUsed(1)
            true
        } else {
            false
        }
    }

    override suspend fun addCredits(credits: Int): Boolean {
        val user = _currentUserCache.value
        if (user == null) {
            return false
        }

        val newCredits = user.credits + credits

        val result = retryWithExponentialBackoff {
            firestoreSyncService.updateUserCredits(user.id, newCredits)
        }

        return if (result.isSuccess) {
            _currentUserCache.value = user.copy(credits = newCredits)
            true
        } else {
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
                    analyticsService.logCreditsPurchased(
                        sku = BillingManager.PRODUCT_ID_50_CREDITS,
                        amount = CREDITS_FOR_PURCHASE
                    )
                }
                success
            } else {
                crashlyticsService.log("Purchase processing failed: User not logged in")
                false
            }
        } catch (e: Exception) {
            crashlyticsService.logBillingError(0, e)
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