package com.dthurman.moviesaver.core.data.repository

import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCreditsRepository : CreditsRepository {
    
    private val _credits = MutableStateFlow(0)
    
    var shouldDeductSucceed = true
    var shouldAddSucceed = true
    var deductCreditsCallCount = 0
    var addCreditsCallCount = 0
    var lastDeductedAmount: Int? = null
    var lastAddedAmount: Int? = null
    
    override fun getCreditsFlow(): Flow<Int> {
        return _credits.asStateFlow()
    }
    
    override suspend fun getCredits(): Int {
        return _credits.value
    }
    
    override suspend fun deductCredits(amount: Int): Result<Unit> {
        deductCreditsCallCount++
        lastDeductedAmount = amount
        
        return if (shouldDeductSucceed) {
            if (_credits.value < amount) {
                Result.failure(Exception("Insufficient credits"))
            } else {
                _credits.value -= amount
                Result.success(Unit)
            }
        } else {
            Result.failure(Exception("Failed to deduct credits"))
        }
    }
    
    override suspend fun addCredits(amount: Int): Result<Unit> {
        addCreditsCallCount++
        lastAddedAmount = amount
        
        return if (shouldAddSucceed) {
            _credits.value += amount
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to add credits"))
        }
    }
    
    fun setCredits(amount: Int) {
        _credits.value = amount
    }
    
    fun reset() {
        _credits.value = 0
        shouldDeductSucceed = true
        shouldAddSucceed = true
        deductCreditsCallCount = 0
        addCreditsCallCount = 0
        lastDeductedAmount = null
        lastAddedAmount = null
    }
}


