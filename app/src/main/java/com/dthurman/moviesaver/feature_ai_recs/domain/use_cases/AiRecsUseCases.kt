package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

data class AiRecsUseCases(
    val generateRecommendations: GenerateAiRecommendationsUseCase,
    val getSavedRecommendations: GetSavedRecommendationsUseCase,
    val acceptToWatchlist: AcceptRecommendationToWatchlistUseCase,
    val acceptAsSeen: AcceptRecommendationAsSeenUseCase,
    val rejectRecommendation: RejectRecommendationUseCase,
    val getSeenMoviesCount: GetSeenMoviesCountUseCase
)

