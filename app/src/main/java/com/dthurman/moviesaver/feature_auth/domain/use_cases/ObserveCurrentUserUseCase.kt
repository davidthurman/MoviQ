package com.dthurman.moviesaver.feature_auth.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.User
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<User?> {
        return userRepository.currentUser
    }
}

