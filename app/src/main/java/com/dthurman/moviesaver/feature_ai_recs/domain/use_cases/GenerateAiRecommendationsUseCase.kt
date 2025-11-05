package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import javax.inject.Inject

/**
 * Use case for generating AI-powered movie recommendations.
 * 
 * Business rules:
 * - User must be logged in
 * - User must have at least 1 credit
 * - Deducts 1 credit upon successful generation
 */
class GenerateAiRecommendationsUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val creditsRepository: CreditsRepository,
    private val userRepository: UserRepository,
    private val analytics: AnalyticsTracker,
    private val errorLogger: ErrorLogger
) {
    suspend operator fun invoke(): Result<List<Movie>> {
        return try {
            // Business rule: User must be logged in
            val user = userRepository.getCurrentUser()
                ?: return Result.failure(IllegalStateException("User not logged in"))

            // Business rule: User must have credits
            val currentCredits = creditsRepository.getCredits()
            if (currentCredits <= 0) {
                return Result.failure(InsufficientCreditsException(currentCredits))
            }

            // Deduct credit first (fail fast if this fails)
            val creditResult = creditsRepository.deductCredits(1)
            if (creditResult.isFailure) {
                return Result.failure(creditResult.exceptionOrNull() ?: Exception("Failed to deduct credits"))
            }

            // Generate recommendations
            val recommendations = aiRepository.generatePersonalizedRecommendations()
            
            // Track analytics
            analytics.logEvent("ai_recommendations_generated", mapOf(
                "user_id" to user.id,
                "count" to recommendations.size,
                "credits_remaining" to (currentCredits - 1)
            ))

            Result.success(recommendations)
        } catch (e: Exception) {
            errorLogger.logAiError(e)
            Result.failure(e)
        }
    }
}

/**
 * Exception thrown when user has insufficient credits for an operation.
 */
class InsufficientCreditsException(val currentCredits: Int) : 
    Exception("Insufficient credits. Current balance: $currentCredits")

