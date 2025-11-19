package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

class UpdateSeenStatusUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    suspend operator fun invoke(movie: Movie, isSeen: Boolean, rating: Float? = null): Result<Unit> {
        return try {
            if (isSeen) {
                movieRepository.updateWatchlistStatus(movie, false)
                movieRepository.updateSeenStatus(movie, true)
                
                if (rating != null) {
                    movieRepository.updateRating(movie, rating)
                }
                
                analytics.logMovieSaved(movie.id, movie.title)
            } else {
                movieRepository.updateSeenStatus(movie, false)
                movieRepository.updateRating(movie, null)
                
                analytics.logEvent("movie_removed_from_seen", mapOf("movie_id" to movie.id))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



