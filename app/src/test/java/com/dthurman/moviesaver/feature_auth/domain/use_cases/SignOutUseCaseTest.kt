package com.dthurman.moviesaver.feature_auth.domain.use_cases

import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.data.observability.FakeErrorLogger
import com.dthurman.moviesaver.core.data.repository.FakeUserRepository
import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.feature_auth.data.repository.FakeAuthRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SignOutUseCaseTest {

    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var fakeAnalyticsTracker: FakeAnalyticsTracker
    private lateinit var fakeErrorLogger: FakeErrorLogger

    @Before
    fun setUp() {
        fakeUserRepository = FakeUserRepository()
        fakeAuthRepository = FakeAuthRepository(fakeUserRepository)
        fakeAnalyticsTracker = FakeAnalyticsTracker()
        fakeErrorLogger = FakeErrorLogger()
        signOutUseCase = SignOutUseCase(
            authRepository = fakeAuthRepository,
            analytics = fakeAnalyticsTracker,
            errorLogger = fakeErrorLogger
        )
    }

    @Test
    fun `sign out returns success`() {
        runBlocking {
            val result = signOutUseCase.invoke()

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeAuthRepository.signOutCallCount).isEqualTo(1)
            assertThat(fakeErrorLogger.exceptions).isEmpty()
        }
    }

    @Test
    fun `sign out logs analytics event`() {
        runBlocking {
            val result = signOutUseCase.invoke()

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeAnalyticsTracker.events).containsKey("sign_out")
            val eventParams = fakeAnalyticsTracker.events["sign_out"]
            assertThat(eventParams).isEmpty()
        }
    }

    @Test
    fun `sign out with multiple calls increments count`() {
        runBlocking {
            signOutUseCase.invoke()
            signOutUseCase.invoke()
            signOutUseCase.invoke()

            assertThat(fakeAuthRepository.signOutCallCount).isEqualTo(3)
        }
    }

    @Test
    fun `sign out does not log errors on success`() {
        runBlocking {
            signOutUseCase.invoke()

            assertThat(fakeErrorLogger.logs).isEmpty()
            assertThat(fakeErrorLogger.exceptions).isEmpty()
        }
    }

    @Test
    fun `sign out clears current user to null`() {
        runBlocking {
            val mockUser = User(
                id = "user_123",
                email = "user@example.com",
                displayName = "Test User",
                photoUrl = null,
                credits = 10
            )
            fakeUserRepository.setCurrentUser(mockUser)

            val userBeforeSignOut = fakeUserRepository.currentUser.first()
            assertThat(userBeforeSignOut).isNotNull()

            signOutUseCase.invoke()

            val userAfterSignOut = fakeUserRepository.currentUser.first()
            assertThat(userAfterSignOut).isNull()
        }
    }

    @Test
    fun `sign out when no user is logged in still succeeds`() {
        runBlocking {
            fakeUserRepository.setCurrentUser(null)

            val result = signOutUseCase.invoke()

            assertThat(result.isSuccess).isTrue()
            assertThat(fakeAuthRepository.signOutCallCount).isEqualTo(1)
        }
    }
}

