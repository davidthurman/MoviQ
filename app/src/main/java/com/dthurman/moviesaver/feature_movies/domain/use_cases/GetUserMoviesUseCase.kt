package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import com.dthurman.moviesaver.feature_movies.domain.util.MovieFilter
import com.dthurman.moviesaver.feature_movies.domain.util.MovieOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
) {
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

    private fun sortMovies(movies: List<Movie>, order: MovieOrder): List<Movie> {
        return when (order) {
            MovieOrder.TITLE_ASC -> movies.sortedBy { it.title.lowercase() }
            MovieOrder.TITLE_DESC -> movies.sortedByDescending { it.title.lowercase() }
            MovieOrder.DATE_ADDED_ASC -> movies.sortedBy { it.addedAt }
            MovieOrder.DATE_ADDED_DESC -> movies.sortedByDescending { it.addedAt }
            MovieOrder.RELEASE_DATE_ASC -> movies.sortedBy { it.releaseDate }
            MovieOrder.RELEASE_DATE_DESC -> movies.sortedByDescending { it.releaseDate }
            MovieOrder.RATING_ASC -> movies.sortedBy { it.rating ?: 0f }
            MovieOrder.RATING_DESC -> movies.sortedByDescending { it.rating ?: 0f }
        }
    }
}

