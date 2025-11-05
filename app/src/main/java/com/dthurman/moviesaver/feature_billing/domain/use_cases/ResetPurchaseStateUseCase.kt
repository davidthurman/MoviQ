package com.dthurman.moviesaver.feature_billing.domain.use_cases

import com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository
import javax.inject.Inject

class ResetPurchaseStateUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    operator fun invoke() {
        billingRepository.resetPurchaseState()
    }
}

