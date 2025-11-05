package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
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
    private val movieRepository: MovieRepository
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
        }
        
        return sourceFlow.map { movies -> 
            sortMovies(movies, filter.order)
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

