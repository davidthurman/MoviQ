package com.dthurman.moviesaver.feature_billing.domain.use_cases

import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository
import javax.inject.Inject

class ProcessPurchaseUseCase @Inject constructor(
    private val billingRepository: BillingRepository,
    private val errorLogger: ErrorLogger
) {
    suspend operator fun invoke(purchaseToken: String): Result<Unit> {
        return try {
            val success = billingRepository.processPurchase(purchaseToken)
            if (success) {
                Result.success(Unit)
            } else {
                val exception = Exception("Failed to process purchase")
                errorLogger.logBillingError(0, exception)
                Result.failure(exception)
            }
        } catch (e: Exception) {
            errorLogger.logBillingError(0, e)
            Result.failure(e)
        }
    }
}

