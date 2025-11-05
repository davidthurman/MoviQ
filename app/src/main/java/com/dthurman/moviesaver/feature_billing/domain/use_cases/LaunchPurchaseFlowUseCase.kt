package com.dthurman.moviesaver.feature_billing.domain.use_cases

import android.app.Activity
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository
import javax.inject.Inject

/**
 * Use case for launching the in-app purchase flow.
 * Handles the Google Play billing dialog.
 */
class LaunchPurchaseFlowUseCase @Inject constructor(
    private val billingRepository: BillingRepository,
    private val errorLogger: ErrorLogger
) {
    operator fun invoke(activity: Activity): Result<Unit> {
        return try {
            billingRepository.launchPurchaseFlow(activity)
            Result.success(Unit)
        } catch (e: Exception) {
            errorLogger.logBillingError(0, e)
            Result.failure(e)
        }
    }
}

