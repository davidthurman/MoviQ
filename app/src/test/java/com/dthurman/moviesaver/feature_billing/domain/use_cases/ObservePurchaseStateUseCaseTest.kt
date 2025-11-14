package com.dthurman.moviesaver.feature_billing.domain.use_cases

import com.dthurman.moviesaver.feature_billing.data.repository.FakeBillingRepository
import com.dthurman.moviesaver.feature_billing.domain.PurchaseState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ObservePurchaseStateUseCaseTest {

    private lateinit var observePurchaseStateUseCase: ObservePurchaseStateUseCase
    private lateinit var fakeBillingRepository: FakeBillingRepository

    @Before
    fun setUp() {
        fakeBillingRepository = FakeBillingRepository()
        observePurchaseStateUseCase = ObservePurchaseStateUseCase(
            billingRepository = fakeBillingRepository
        )
    }

    @Test
    fun `observe purchase state returns initial Idle state`() {
        runBlocking {
            val stateFlow = observePurchaseStateUseCase.invoke()
            val currentState = stateFlow.first()

            assertThat(currentState).isEqualTo(PurchaseState.Idle)
        }
    }

    @Test
    fun `observe purchase state reflects Purchasing state`() {
        runBlocking {
            fakeBillingRepository.setPurchaseState(PurchaseState.Purchasing)
            val stateFlow = observePurchaseStateUseCase.invoke()
            val currentState = stateFlow.first()

            assertThat(currentState).isEqualTo(PurchaseState.Purchasing)
        }
    }

    @Test
    fun `observe purchase state reflects Pending state`() {
        runBlocking {
            fakeBillingRepository.setPurchaseState(PurchaseState.Pending)
            val stateFlow = observePurchaseStateUseCase.invoke()
            val currentState = stateFlow.first()

            assertThat(currentState).isEqualTo(PurchaseState.Pending)
        }
    }

    @Test
    fun `observe purchase state reflects Canceled state`() {
        runBlocking {
            fakeBillingRepository.setPurchaseState(PurchaseState.Canceled)
            val stateFlow = observePurchaseStateUseCase.invoke()
            val currentState = stateFlow.first()

            assertThat(currentState).isEqualTo(PurchaseState.Canceled)
        }
    }

    @Test
    fun `observe purchase state reflects Error state with message`() {
        runBlocking {
            val errorMessage = "Payment method declined"
            fakeBillingRepository.setPurchaseState(PurchaseState.Error(errorMessage))
            val stateFlow = observePurchaseStateUseCase.invoke()
            val currentState = stateFlow.first()

            assertThat(currentState).isInstanceOf(PurchaseState.Error::class.java)
            assertThat((currentState as PurchaseState.Error).message).isEqualTo(errorMessage)
        }
    }

    @Test
    fun `observe purchase state returns same StateFlow instance`() {
        val stateFlow1 = observePurchaseStateUseCase.invoke()
        val stateFlow2 = observePurchaseStateUseCase.invoke()

        assertThat(stateFlow1).isSameInstanceAs(stateFlow2)
    }
}


