package com.dthurman.moviesaver.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface CreditsRepository {
    fun getCreditsFlow(): Flow<Int>
    suspend fun getCredits(): Int
    suspend fun deductCredits(amount: Int): Result<Unit>
    suspend fun addCredits(amount: Int): Result<Unit>
}




