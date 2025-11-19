package com.dthurman.moviesaver.feature_billing.data.repository

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.dthurman.moviesaver.feature_billing.domain.BillingConnectionState
import com.dthurman.moviesaver.feature_billing.domain.PurchaseState
import com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBillingRepository : BillingRepository {
    
    private val _connectionState = MutableStateFlow(BillingConnectionState.CONNECTED)
    override val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    override val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()
    
    var shouldProcessPurchaseSucceed = true
    var processPurchaseCallCount = 0
    var lastPurchaseToken: String? = null
    var launchPurchaseFlowCallCount = 0
    var resetPurchaseStateCallCount = 0

    override fun launchPurchaseFlow(activity: Activity) {
        launchPurchaseFlowCallCount++
        _purchaseState.value = PurchaseState.Purchasing
    }

    override fun resetPurchaseState() {
        resetPurchaseStateCallCount++
        _purchaseState.value = PurchaseState.Idle
    }

    override suspend fun processPurchase(purchaseToken: String): Boolean {
        processPurchaseCallCount++
        lastPurchaseToken = purchaseToken
        return shouldProcessPurchaseSucceed
    }

    fun setPurchaseState(state: PurchaseState) {
        _purchaseState.value = state
    }

    fun setConnectionState(state: BillingConnectionState) {
        _connectionState.value = state
    }

    fun reset() {
        _connectionState.value = BillingConnectionState.CONNECTED
        _purchaseState.value = PurchaseState.Idle
        _productDetails.value = null
        shouldProcessPurchaseSucceed = true
        processPurchaseCallCount = 0
        lastPurchaseToken = null
        launchPurchaseFlowCallCount = 0
        resetPurchaseStateCallCount = 0
    }
}



