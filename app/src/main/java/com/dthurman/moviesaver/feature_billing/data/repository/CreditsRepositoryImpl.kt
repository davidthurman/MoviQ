package com.dthurman.moviesaver.feature_billing.data.repository

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_billing.domain.BillingConnectionState
import com.dthurman.moviesaver.feature_billing.domain.BillingManager
import com.dthurman.moviesaver.feature_billing.domain.PurchaseState
import com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager,
    private val creditsRepository: CreditsRepository,
    private val userRepository: UserRepository,
    private val analytics: AnalyticsTracker,
    private val errorLogger: ErrorLogger
) : BillingRepository {

    companion object {
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
            val user = userRepository.getCurrentUser()

            if (user != null) {
                val result = creditsRepository.addCredits(CREDITS_FOR_PURCHASE)
                if (result.isSuccess) {
                    analytics.logCreditsPurchased(
                        sku = BillingManager.PRODUCT_ID_50_CREDITS,
                        amount = CREDITS_FOR_PURCHASE
                    )
                    true
                } else {
                    false
                }
            } else {
                errorLogger.log("Purchase processing failed: User not logged in")
                false
            }
        } catch (e: Exception) {
            errorLogger.logBillingError(0, e)
            false
        }
    }
}