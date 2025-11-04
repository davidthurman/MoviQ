package com.dthurman.moviesaver.feature_billing.domain

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.dthurman.moviesaver.core.observability.ErrorLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val errorLogger: ErrorLogger
) : PurchasesUpdatedListener {
    companion object {
        private const val TAG = "BILLING_MANAGER"
        const val PRODUCT_ID_50_CREDITS = "movie_generation_credits"
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
                    _connectionState.value = BillingConnectionState.CONNECTED
                    queryProductDetails()
                } else {
                    _connectionState.value = BillingConnectionState.FAILED
                    val error = Exception("Billing setup failed: ${billingResult.debugMessage}")
                    errorLogger.logBillingError(billingResult.responseCode, error)
                }
            }

            override fun onBillingServiceDisconnected() {
                _connectionState.value = BillingConnectionState.DISCONNECTED
                errorLogger.log("Billing service disconnected, attempting to reconnect")
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
                val error = Exception("Failed to query product details: ${billingResult.debugMessage}")
                errorLogger.logBillingError(billingResult.responseCode, error)
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val productDetails = _productDetails.value
        if (productDetails == null) {
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
                _purchaseState.value = PurchaseState.Canceled
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
                val error = Exception("Purchase failed: ${billingResult.debugMessage}")
                errorLogger.logBillingError(billingResult.responseCode, error)
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
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
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

