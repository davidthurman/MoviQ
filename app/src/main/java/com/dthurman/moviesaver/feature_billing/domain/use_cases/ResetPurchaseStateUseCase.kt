package com.dthurman.moviesaver.feature_billing.domain.use_cases

import com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository
import javax.inject.Inject

/**
 * Use case for resetting the purchase state.
 * Clears any purchase-related UI state after handling.
 */
class ResetPurchaseStateUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    operator fun invoke() {
        billingRepository.resetPurchaseState()
    }
}

