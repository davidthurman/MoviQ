package com.dthurman.moviesaver.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user AI generation credits
 */
interface CreditsRepository {
    /**
     * Get current credits for the user as a Flow
     */
    fun getCreditsFlow(userId: String): Flow<Int>
    
    /**
     * Get current credits for the user (one-time fetch)
     */
    suspend fun getCredits(userId: String): Int
    
    /**
     * Deduct one credit from the user's account
     * @return true if deduction was successful, false if no credits available
     */
    suspend fun deductCredit(userId: String): Boolean
    
    /**
     * Add purchased credits to the user's account
     * @param userId The user's ID
     * @param credits Number of credits to add
     */
    suspend fun addCredits(userId: String, credits: Int)
    
    /**
     * Initialize credits for a new user
     */
    suspend fun initializeCredits(userId: String, initialCredits: Int = 10)
    
    /**
     * Sync credits from Firestore to local database
     */
    suspend fun syncFromFirestore(userId: String)
    
    /**
     * Clear all local credits data (for logout)
     */
    suspend fun clearAllLocalCredits()
}

