package com.dthurman.moviesaver.core.data.repository

import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.data.remote.user.UserRemoteDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditsRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val analytics: AnalyticsTracker
) : CreditsRepository {

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    override fun getCreditsFlow(): Flow<Int> {
        return userRepository.currentUser.map { it?.credits ?: 0 }
    }

    override suspend fun getCredits(): Int {
        val user = userRepository.getCurrentUser()
        if (user != null) {
            return user.credits
        }

        val result = userRepository.refreshUserProfile()
        return result.getOrNull()?.credits ?: 0
    }

    override suspend fun deductCredits(amount: Int): Result<Unit> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("No user logged in"))

        if (user.credits < amount) {
            return Result.failure(Exception("Insufficient credits"))
        }

        val newCredits = user.credits - amount

        val result = retryWithExponentialBackoff {
            userRemoteDataSource.updateUserCredits(user.id, newCredits)
        }

        return if (result.isSuccess) {
            val updatedUser = user.copy(credits = newCredits)
            userRepository.updateUserProfile(updatedUser)
            analytics.logCreditsUsed(amount)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to deduct credits"))
        }
    }

    override suspend fun addCredits(amount: Int): Result<Unit> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("No user logged in"))

        val newCredits = user.credits + amount

        val result = retryWithExponentialBackoff {
            userRemoteDataSource.updateUserCredits(user.id, newCredits)
        }

        return if (result.isSuccess) {
            val updatedUser = user.copy(credits = newCredits)
            userRepository.updateUserProfile(updatedUser)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to add credits"))
        }
    }

    private suspend fun <T> retryWithExponentialBackoff(
        maxAttempts: Int = MAX_RETRY_ATTEMPTS,
        initialDelayMs: Long = RETRY_DELAY_MS,
        operation: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null

        repeat(maxAttempts) { attempt ->
            try {
                val result = operation()
                if (result.isSuccess) {
                    return result
                }
                lastException = result.exceptionOrNull() as? Exception
            } catch (e: Exception) {
                lastException = e
            }

            if (attempt < maxAttempts - 1) {
                delay(currentDelay)
                currentDelay *= 2
            }
        }

        return Result.failure(lastException ?: Exception("Operation failed after $maxAttempts attempts"))
    }
}

