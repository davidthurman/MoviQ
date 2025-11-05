package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import com.dthurman.moviesaver.feature_movies.domain.util.MovieFilter
import com.dthurman.moviesaver.feature_movies.domain.util.MovieOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Unified use case for retrieving movies based on a filter.
 * This consolidates all movie retrieval logic into a single, flexible use case.
 * 
 * Supports:
 * - Different movie lists (seen, watchlist, favorites, popular, etc.)
 * - Custom sorting/ordering
 * - Search functionality
 * - Analytics tracking
 */
class GetMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val analytics: AnalyticsTracker
) {
    /**
     * Get movies as a Flow (for reactive/observed data like user's saved movies).
     * Use this when you need continuous updates from the database.
     */
    operator fun invoke(filter: MovieFilter): Flow<List<Movie>> {
        val sourceFlow = when (filter) {
            is MovieFilter.SeenMovies -> movieRepository.getSeenMovies()
            is MovieFilter.WatchlistMovies -> movieRepository.getWatchlistMovies()
            is MovieFilter.FavoriteMovies -> movieRepository.getFavoriteMovies()
            is MovieFilter.AiRecommendations -> movieRepository.getAiRecommendations()
            is MovieFilter.AllMovies -> movieRepository.getSeenMovies() // You may want to combine all sources
            is MovieFilter.PopularMovies -> {
                // For popular movies, we need to fetch once and convert to flow
                // This will be handled by the suspend variant
                throw IllegalArgumentException("Use suspend invoke() for PopularMovies and SearchResults")
            }
            is MovieFilter.SearchResults -> {
                throw IllegalArgumentException("Use suspend invoke() for PopularMovies and SearchResults")
            }
        }
        
        return sourceFlow.map { movies -> 
            sortMovies(movies, filter.order)
        }
    }
    
    /**
     * Get movies as a suspend function (for one-time fetches like search or popular movies).
     * Use this for API calls or one-time data retrieval.
     */
    suspend fun getSuspend(filter: MovieFilter): Result<List<Movie>> {
        return try {
            val movies = when (filter) {
                is MovieFilter.PopularMovies -> {
                    analytics.logEvent("popular_movies_fetched_request", emptyMap())
                    movieRepository.getPopularMovies()
                }
                is MovieFilter.SearchResults -> {
                    val trimmedQuery = filter.query.trim()
                    
                    if (trimmedQuery.length < 2) {
                        analytics.logEvent("movie_search_invalid", mapOf("query" to trimmedQuery))
                        return Result.failure(IllegalArgumentException("Search query must be at least 2 characters"))
                    }
                    
                    analytics.logMovieSearched(trimmedQuery)
                    movieRepository.searchMovieByTitle(trimmedQuery)
                }
                else -> {
                    // For filters that are normally flows, we could snapshot them,
                    // but it's better to use the Flow-based invoke()
                    throw IllegalArgumentException("Use Flow-based invoke() for ${filter::class.simpleName}")
                }
            }
            
            val sortedMovies = sortMovies(movies, filter.order)
            
            // Analytics
            when (filter) {
                is MovieFilter.PopularMovies -> {
                    analytics.logEvent("popular_movies_fetched", mapOf("count" to sortedMovies.size))
                }
                is MovieFilter.SearchResults -> {
                    analytics.logEvent("movie_search", mapOf(
                        "query" to filter.query,
                        "results_count" to sortedMovies.size
                    ))
                }
                else -> { /* no-op */ }
            }
            
            Result.success(sortedMovies)
        } catch (e: Exception) {
            when (filter) {
                is MovieFilter.PopularMovies -> {
                    analytics.logEvent("popular_movies_error", mapOf("error" to (e.message ?: "unknown")))
                }
                is MovieFilter.SearchResults -> {
                    analytics.logEvent("movie_search_error", mapOf(
                        "query" to filter.query,
                        "error" to (e.message ?: "unknown")
                    ))
                }
                else -> {
                    analytics.logEvent("get_movies_error", mapOf(
                        "filter" to filter::class.simpleName.orEmpty(),
                        "error" to (e.message ?: "unknown")
                    ))
                }
            }
            Result.failure(e)
        }
    }
    
    /**
     * Sorts movies according to the specified order.
     */
    private fun sortMovies(movies: List<Movie>, order: MovieOrder): List<Movie> {
        return when (order) {
            MovieOrder.TITLE_ASC -> movies.sortedBy { it.title.lowercase() }
            MovieOrder.TITLE_DESC -> movies.sortedByDescending { it.title.lowercase() }
            MovieOrder.DATE_ADDED_ASC -> movies.sortedBy { it.id }
            MovieOrder.DATE_ADDED_DESC -> movies.sortedByDescending { it.id }
            MovieOrder.RELEASE_DATE_ASC -> movies.sortedBy { it.releaseDate }
            MovieOrder.RELEASE_DATE_DESC -> movies.sortedByDescending { it.releaseDate }
            MovieOrder.RATING_ASC -> movies.sortedBy { it.rating ?: 0f }
            MovieOrder.RATING_DESC -> movies.sortedByDescending { it.rating ?: 0f }
        }
    }
}

