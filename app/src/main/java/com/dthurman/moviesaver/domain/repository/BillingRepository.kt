package com.dthurman.moviesaver.domain.repository

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.dthurman.moviesaver.data.billing.BillingConnectionState
import com.dthurman.moviesaver.data.billing.PurchaseState
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val connectionState: StateFlow<BillingConnectionState>
    val purchaseState: StateFlow<PurchaseState>
    val productDetails: StateFlow<ProductDetails?>
    
    fun launchPurchaseFlow(activity: Activity)
    fun resetPurchaseState()
    suspend fun processPurchase(purchaseToken: String): Boolean
}

