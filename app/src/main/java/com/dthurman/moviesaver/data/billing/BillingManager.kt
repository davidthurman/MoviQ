package com.dthurman.moviesaver.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.QueryProductDetailsParams.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_ID_50_CREDITS = "credits_50"
    }

    private var billingClient: BillingClient? = null
    
    private val _connectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        startConnection()
    }

    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    _connectionState.value = BillingConnectionState.CONNECTED
                    queryProductDetails()
                } else {
                    Log.e(TAG, "Billing connection failed: ${billingResult.debugMessage}")
                    _connectionState.value = BillingConnectionState.FAILED
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
                _connectionState.value = BillingConnectionState.DISCONNECTED
                // Try to reconnect
                startConnection()
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            Product.newBuilder()
                .setProductId(PRODUCT_ID_50_CREDITS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                val details = productDetailsList.firstOrNull()
                _productDetails.value = details
                Log.d(TAG, "Product details loaded: ${details?.name}")
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val productDetails = _productDetails.value
        if (productDetails == null) {
            Log.e(TAG, "Product details not available")
            _purchaseState.value = PurchaseState.Error("Product not available")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        
        if (billingResult?.responseCode != BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: ${billingResult?.debugMessage}")
            _purchaseState.value = PurchaseState.Error("Failed to start purchase")
        } else {
            _purchaseState.value = PurchaseState.Purchasing
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled the purchase")
                _purchaseState.value = PurchaseState.Canceled
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                _purchaseState.value = PurchaseState.Success(purchase)
                acknowledgePurchase(purchase)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseState.value = PurchaseState.Pending
            Log.d(TAG, "Purchase is pending")
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged")
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    fun endConnection() {
        billingClient?.endConnection()
        _connectionState.value = BillingConnectionState.DISCONNECTED
    }
}

enum class BillingConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Purchasing : PurchaseState()
    object Pending : PurchaseState()
    data class Success(val purchase: Purchase) : PurchaseState()
    object Canceled : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

