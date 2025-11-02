package com.dthurman.moviesaver.domain.repository

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.dthurman.moviesaver.data.remote.billing.BillingConnectionState
import com.dthurman.moviesaver.data.remote.billing.PurchaseState
import com.dthurman.moviesaver.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: Flow<User?>
    fun getCurrentUser(): User?
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
    suspend fun clearLocalCache()
    
    fun getCreditsFlow(): Flow<Int>
    suspend fun getCredits(): Int
    suspend fun deductCredit(): Boolean
    suspend fun addCredits(credits: Int): Boolean
    
    val connectionState: StateFlow<BillingConnectionState>
    val purchaseState: StateFlow<PurchaseState>
    val productDetails: StateFlow<ProductDetails?>
    fun launchPurchaseFlow(activity: Activity)
    fun resetPurchaseState()
    suspend fun processPurchase(purchaseToken: String): Boolean
}

