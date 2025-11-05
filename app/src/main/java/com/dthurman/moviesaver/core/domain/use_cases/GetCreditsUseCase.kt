package com.dthurman.moviesaver.core.domain.use_cases

import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCreditsUseCase @Inject constructor(
    private val creditsRepository: CreditsRepository
) {
    operator fun invoke(): Flow<Int> {
        return creditsRepository.getCreditsFlow()
    }
}

