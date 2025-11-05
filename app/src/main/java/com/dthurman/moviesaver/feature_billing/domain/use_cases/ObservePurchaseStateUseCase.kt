package com.dthurman.moviesaver.feature_billing.domain.use_cases

import com.dthurman.moviesaver.feature_billing.domain.PurchaseState
import com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObservePurchaseStateUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    operator fun invoke(): StateFlow<PurchaseState> {
        return billingRepository.purchaseState
    }
}

