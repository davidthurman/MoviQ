package com.dthurman.moviesaver.feature_billing.domain.repository

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.dthurman.moviesaver.feature_billing.domain.BillingConnectionState
import com.dthurman.moviesaver.feature_billing.domain.PurchaseState
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val connectionState: StateFlow<BillingConnectionState>
    val purchaseState: StateFlow<PurchaseState>
    val productDetails: StateFlow<ProductDetails?>
    fun launchPurchaseFlow(activity: Activity)
    fun resetPurchaseState()
    suspend fun processPurchase(purchaseToken: String): Boolean
}