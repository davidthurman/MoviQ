package com.dthurman.moviesaver.core.domain.use_cases

import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing user's credit balance.
 * Returns a Flow that emits updates whenever the credit balance changes.
 */
class GetCreditsUseCase @Inject constructor(
    private val creditsRepository: CreditsRepository
) {
    operator fun invoke(): Flow<Int> {
        return creditsRepository.getCreditsFlow()
    }
}

