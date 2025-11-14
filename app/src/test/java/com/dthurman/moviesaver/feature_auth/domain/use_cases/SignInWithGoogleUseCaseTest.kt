package com.dthurman.moviesaver.feature_auth.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.data.observability.FakeErrorLogger
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.feature_auth.data.repository.FakeAuthRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SignInWithGoogleUseCaseTest {

    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker
    private lateinit var fakeErrorLogger: FakeErrorLogger

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        fakeAnalyticsTracker = FakeAnalyticsTracker()
        fakeErrorLogger = FakeErrorLogger()
        signInWithGoogleUseCase = SignInWithGoogleUseCase(
            authRepository = fakeAuthRepository,
            analytics = fakeAnalyticsTracker,
            errorLogger = fakeErrorLogger
        )
    }

    @Test
    fun `sign in with valid token returns success`() {
        runBlocking {
            fakeAuthRepository.shouldSignInSucceed = true
            val result = signInWithGoogleUseCase.invoke("valid_id_token_123")

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeAuthRepository.signInCallCount).isEqualTo(1)
            assertThat(fakeAuthRepository.lastIdToken).isEqualTo("valid_id_token_123")
            assertThat(fakeErrorLogger.exceptions).isEmpty()
        }
    }

    @Test
    fun `sign in with valid token returns user`() {
        runBlocking {
            val mockUser = User(
                id = "user_123",
                email = "user@example.com",
                displayName = "John Doe",
                photoUrl = "https://example.com/photo.jpg",
                credits = 10
            )
            fakeAuthRepository.mockUser = mockUser
            fakeAuthRepository.shouldSignInSucceed = true

            val result = signInWithGoogleUseCase.invoke("token_456")

            assertThat(result.isSuccess).isTrue()
            val user = result.getOrNull()
            assertThat(user).isNotNull()
            assertThat(user?.id).isEqualTo("user_123")
            assertThat(user?.email).isEqualTo("user@example.com")
            assertThat(user?.displayName).isEqualTo("John Doe")
        }
    }

    @Test
    fun `sign in with valid token logs analytics event`() {
        runBlocking {
            fakeAuthRepository.shouldSignInSucceed = true
            val result = signInWithGoogleUseCase.invoke("token_789")

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeAnalyticsTracker.events).containsKey("sign_in_success")
            val eventParams = fakeAnalyticsTracker.events["sign_in_success"]
            assertThat(eventParams?.get("method")).isEqualTo("google")
            assertThat(eventParams?.get("user_id")).isEqualTo("test_user_id")
        }
    }

    @Test
    fun `sign in with failed authentication returns failure`() {
        runBlocking {
            fakeAuthRepository.shouldSignInSucceed = false
            val result = signInWithGoogleUseCase.invoke("invalid_token")

            assertThat(result.isFailure).isTrue()
            assertThat(fakeAuthRepository.signInCallCount).isEqualTo(1)
            assertThat(result.exceptionOrNull()?.message).isEqualTo("Failed to sign in with Google")
        }
    }

    @Test
    fun `sign in with failed authentication logs error`() {
        runBlocking {
            fakeAuthRepository.shouldSignInSucceed = false
            signInWithGoogleUseCase.invoke("invalid_token")

            assertThat(fakeErrorLogger.logs).isNotEmpty()
            assertThat(fakeErrorLogger.exceptions).hasSize(1)
            assertThat(fakeErrorLogger.customKeys["error_context"]).isEqualTo("auth_error")
        }
    }

    @Test
    fun `sign in with failed authentication does not log analytics success event`() {
        runBlocking {
            fakeAuthRepository.shouldSignInSucceed = false
            signInWithGoogleUseCase.invoke("invalid_token")

            assertThat(fakeAnalyticsTracker.events).doesNotContainKey("sign_in_success")
        }
    }

    @Test
    fun `sign in with empty token still attempts authentication`() {
        runBlocking {
            fakeAuthRepository.shouldSignInSucceed = true
            val result = signInWithGoogleUseCase.invoke("")

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeAuthRepository.signInCallCount).isEqualTo(1)
            assertThat(fakeAuthRepository.lastIdToken).isEqualTo("")
        }
    }

    @Test
    fun `sign in with multiple calls tracks each token`() {
        runBlocking {
            fakeAuthRepository.shouldSignInSucceed = true

            signInWithGoogleUseCase.invoke("token_1")
            assertThat(fakeAuthRepository.lastIdToken).isEqualTo("token_1")

            signInWithGoogleUseCase.invoke("token_2")
            assertThat(fakeAuthRepository.lastIdToken).isEqualTo("token_2")

            assertThat(fakeAuthRepository.signInCallCount).isEqualTo(2)
        }
    }
}


