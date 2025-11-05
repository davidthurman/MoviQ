package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import javax.inject.Inject

class GenerateAiRecommendationsUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val creditsRepository: CreditsRepository,
    private val userRepository: UserRepository,
    private val analytics: AnalyticsTracker,
    private val errorLogger: ErrorLogger
) {
    suspend operator fun invoke(): Result<List<Movie>> {
        return try {
            val user = userRepository.getCurrentUser()
                ?: return Result.failure(IllegalStateException("User not logged in"))

            val currentCredits = creditsRepository.getCredits()
            if (currentCredits <= 0) {
                return Result.failure(InsufficientCreditsException(currentCredits))
            }

            val creditResult = creditsRepository.deductCredits(1)
            if (creditResult.isFailure) {
                return Result.failure(creditResult.exceptionOrNull() ?: Exception("Failed to deduct credits"))
            }

            val recommendations = aiRepository.generatePersonalizedRecommendations()
            
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

class InsufficientCreditsException(val currentCredits: Int) : 
    Exception("Insufficient credits. Current balance: $currentCredits")

