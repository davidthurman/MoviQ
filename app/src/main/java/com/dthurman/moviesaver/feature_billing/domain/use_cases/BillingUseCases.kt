package com.dthurman.moviesaver.feature_billing.domain.use_cases

data class BillingUseCases(
    val launchPurchaseFlow: LaunchPurchaseFlowUseCase,
    val observePurchaseState: ObservePurchaseStateUseCase,
    val processPurchase: ProcessPurchaseUseCase,
    val resetPurchaseState: ResetPurchaseStateUseCase
)


