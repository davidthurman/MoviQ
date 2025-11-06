package com.dthurman.moviesaver.feature_billing.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeErrorLogger
import com.dthurman.moviesaver.feature_billing.data.repository.FakeBillingRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ProcessPurchaseUseCaseTest {

    private lateinit var processPurchaseUseCase: ProcessPurchaseUseCase
    private lateinit var fakeBillingRepository: FakeBillingRepository
    private lateinit var fakeErrorLogger: FakeErrorLogger

    @Before
    fun setUp() {
        fakeBillingRepository = FakeBillingRepository()
        fakeErrorLogger = FakeErrorLogger()
        processPurchaseUseCase = ProcessPurchaseUseCase(
            billingRepository = fakeBillingRepository,
            errorLogger = fakeErrorLogger
        )
    }

    @Test
    fun `process purchase with valid token returns success`() {
        runBlocking {
            fakeBillingRepository.shouldProcessPurchaseSucceed = true
            val result = processPurchaseUseCase.invoke("valid_token_123")

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeBillingRepository.processPurchaseCallCount).isEqualTo(1)
            assertThat(fakeBillingRepository.lastPurchaseToken).isEqualTo("valid_token_123")
            assertThat(fakeErrorLogger.exceptions).isEmpty()
        }
    }

    @Test
    fun `process purchase with failed processing returns failure`() {
        runBlocking {
            fakeBillingRepository.shouldProcessPurchaseSucceed = false
            val result = processPurchaseUseCase.invoke("token_456")

            assertThat(result.isFailure).isTrue()
            assertThat(fakeBillingRepository.processPurchaseCallCount).isEqualTo(1)
            assertThat(fakeBillingRepository.lastPurchaseToken).isEqualTo("token_456")
            assertThat(result.exceptionOrNull()?.message).isEqualTo("Failed to process purchase")
        }
    }

    @Test
    fun `process purchase with failed processing logs error`() {
        runBlocking {
            fakeBillingRepository.shouldProcessPurchaseSucceed = false
            processPurchaseUseCase.invoke("token_789")

            assertThat(fakeErrorLogger.logs).isNotEmpty()
            assertThat(fakeErrorLogger.exceptions).hasSize(1)
            assertThat(fakeErrorLogger.customKeys["error_context"]).isEqualTo("billing_error")
        }
    }

    @Test
    fun `process purchase with empty token still attempts processing`() {
        runBlocking {
            fakeBillingRepository.shouldProcessPurchaseSucceed = true
            val result = processPurchaseUseCase.invoke("")

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeBillingRepository.processPurchaseCallCount).isEqualTo(1)
            assertThat(fakeBillingRepository.lastPurchaseToken).isEqualTo("")
        }
    }

    @Test
    fun `process purchase with multiple calls tracks each token`() {
        runBlocking {
            fakeBillingRepository.shouldProcessPurchaseSucceed = true
            
            processPurchaseUseCase.invoke("token_1")
            assertThat(fakeBillingRepository.lastPurchaseToken).isEqualTo("token_1")
            
            processPurchaseUseCase.invoke("token_2")
            assertThat(fakeBillingRepository.lastPurchaseToken).isEqualTo("token_2")
            
            assertThat(fakeBillingRepository.processPurchaseCallCount).isEqualTo(2)
        }
    }
}

