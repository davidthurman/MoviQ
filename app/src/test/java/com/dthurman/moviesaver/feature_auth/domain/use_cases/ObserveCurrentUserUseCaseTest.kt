package com.dthurman.moviesaver.feature_auth.domain.use_cases

import com.dthurman.moviesaver.core.data.repository.FakeUserRepository
import com.dthurman.moviesaver.core.domain.model.User
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ObserveCurrentUserUseCaseTest {

    private lateinit var observeCurrentUserUseCase: ObserveCurrentUserUseCase
    private lateinit var fakeUserRepository: FakeUserRepository

    @Before
    fun setUp() {
        fakeUserRepository = FakeUserRepository()
        observeCurrentUserUseCase = ObserveCurrentUserUseCase(
            userRepository = fakeUserRepository
        )
    }

    @Test
    fun `observe current user returns null when no user is logged in`() {
        runBlocking {
            val user = observeCurrentUserUseCase.invoke().first()

            assertThat(user).isNull()
        }
    }

    @Test
    fun `observe current user returns user when logged in`() {
        runBlocking {
            val mockUser = User(
                id = "user_123",
                email = "user@example.com",
                displayName = "John Doe",
                photoUrl = "https://example.com/photo.jpg",
                credits = 10
            )
            fakeUserRepository.setCurrentUser(mockUser)

            val user = observeCurrentUserUseCase.invoke().first()

            assertThat(user).isNotNull()
            assertThat(user?.id).isEqualTo("user_123")
            assertThat(user?.email).isEqualTo("user@example.com")
            assertThat(user?.displayName).isEqualTo("John Doe")
            assertThat(user?.credits).isEqualTo(10)
        }
    }

    @Test
    fun `observe current user reflects changes in user state`() {
        runBlocking {
            val initialUser = User(
                id = "user_123",
                email = "user@example.com",
                displayName = "John Doe",
                photoUrl = null,
                credits = 10
            )
            fakeUserRepository.setCurrentUser(initialUser)

            val firstUser = observeCurrentUserUseCase.invoke().first()
            assertThat(firstUser?.displayName).isEqualTo("John Doe")

            val updatedUser = initialUser.copy(displayName = "Jane Doe", credits = 20)
            fakeUserRepository.setCurrentUser(updatedUser)

            val secondUser = observeCurrentUserUseCase.invoke().first()
            assertThat(secondUser?.displayName).isEqualTo("Jane Doe")
            assertThat(secondUser?.credits).isEqualTo(20)
        }
    }

    @Test
    fun `observe current user returns null after user logs out`() {
        runBlocking {
            val mockUser = User(
                id = "user_123",
                email = "user@example.com",
                displayName = "John Doe",
                photoUrl = null,
                credits = 10
            )
            fakeUserRepository.setCurrentUser(mockUser)

            val userBeforeLogout = observeCurrentUserUseCase.invoke().first()
            assertThat(userBeforeLogout).isNotNull()

            fakeUserRepository.setCurrentUser(null)

            val userAfterLogout = observeCurrentUserUseCase.invoke().first()
            assertThat(userAfterLogout).isNull()
        }
    }

    @Test
    fun `observe current user returns user with correct credits`() {
        runBlocking {
            val mockUser = User(
                id = "user_456",
                email = "premium@example.com",
                displayName = "Premium User",
                photoUrl = "https://example.com/premium.jpg",
                credits = 100
            )
            fakeUserRepository.setCurrentUser(mockUser)

            val user = observeCurrentUserUseCase.invoke().first()

            assertThat(user).isNotNull()
            assertThat(user?.credits).isEqualTo(100)
        }
    }
}



