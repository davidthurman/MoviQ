package com.dthurman.moviesaver.data.billing

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.ProductDetails
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.BillingRepository
import com.dthurman.moviesaver.domain.repository.CreditsRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager,
    private val creditsRepository: CreditsRepository,
    private val authRepository: AuthRepository
) : BillingRepository {

    companion object {
        private const val TAG = "BillingRepository"
        private const val CREDITS_FOR_PURCHASE = 50
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
            val userId = authRepository.getCurrentUser()?.id
            
            if (userId != null) {
                // Add credits to user's account
                creditsRepository.addCredits(userId, CREDITS_FOR_PURCHASE)
                Log.d(TAG, "Successfully added $CREDITS_FOR_PURCHASE credits for purchase")
                true
            } else {
                Log.e(TAG, "Cannot process purchase: user not logged in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing purchase: ${e.message}", e)
            false
        }
    }
}

