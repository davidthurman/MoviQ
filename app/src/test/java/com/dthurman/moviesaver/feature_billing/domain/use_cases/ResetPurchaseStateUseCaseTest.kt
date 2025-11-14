package com.dthurman.moviesaver.feature_billing.domain.use_cases

import com.dthurman.moviesaver.feature_billing.data.repository.FakeBillingRepository
import com.dthurman.moviesaver.feature_billing.domain.PurchaseState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ResetPurchaseStateUseCaseTest {

    private lateinit var resetPurchaseStateUseCase: ResetPurchaseStateUseCase
    private lateinit var fakeBillingRepository: FakeBillingRepository

    @Before
    fun setUp() {
        fakeBillingRepository = FakeBillingRepository()
        resetPurchaseStateUseCase = ResetPurchaseStateUseCase(
            billingRepository = fakeBillingRepository
        )
    }

    @Test
    fun `reset purchase state from Purchasing to Idle`() {
        runBlocking {
            fakeBillingRepository.setPurchaseState(PurchaseState.Purchasing)
            assertThat(fakeBillingRepository.purchaseState.first()).isEqualTo(PurchaseState.Purchasing)

            resetPurchaseStateUseCase.invoke()

            assertThat(fakeBillingRepository.purchaseState.first()).isEqualTo(PurchaseState.Idle)
            assertThat(fakeBillingRepository.resetPurchaseStateCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `reset purchase state from Error to Idle`() {
        runBlocking {
            fakeBillingRepository.setPurchaseState(PurchaseState.Error("Test error"))
            assertThat(fakeBillingRepository.purchaseState.first()).isInstanceOf(PurchaseState.Error::class.java)

            resetPurchaseStateUseCase.invoke()

            assertThat(fakeBillingRepository.purchaseState.first()).isEqualTo(PurchaseState.Idle)
        }
    }

    @Test
    fun `reset purchase state from Canceled to Idle`() {
        runBlocking {
            fakeBillingRepository.setPurchaseState(PurchaseState.Canceled)
            
            resetPurchaseStateUseCase.invoke()

            assertThat(fakeBillingRepository.purchaseState.first()).isEqualTo(PurchaseState.Idle)
        }
    }

    @Test
    fun `reset purchase state when already Idle`() {
        runBlocking {
            fakeBillingRepository.setPurchaseState(PurchaseState.Idle)
            
            resetPurchaseStateUseCase.invoke()

            assertThat(fakeBillingRepository.purchaseState.first()).isEqualTo(PurchaseState.Idle)
            assertThat(fakeBillingRepository.resetPurchaseStateCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `reset purchase state multiple times`() {
        runBlocking {
            fakeBillingRepository.setPurchaseState(PurchaseState.Purchasing)
            resetPurchaseStateUseCase.invoke()
            
            fakeBillingRepository.setPurchaseState(PurchaseState.Error("Error"))
            resetPurchaseStateUseCase.invoke()
            
            fakeBillingRepository.setPurchaseState(PurchaseState.Canceled)
            resetPurchaseStateUseCase.invoke()

            assertThat(fakeBillingRepository.resetPurchaseStateCallCount).isEqualTo(3)
            assertThat(fakeBillingRepository.purchaseState.first()).isEqualTo(PurchaseState.Idle)
        }
    }
}


