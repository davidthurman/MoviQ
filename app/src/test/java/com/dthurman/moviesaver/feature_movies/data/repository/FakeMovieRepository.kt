package com.dthurman.moviesaver.feature_movies.data.repository

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.data.mockMovies
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeMovieRepository: MovieRepository {

    val movies = mockMovies.toMutableList()

    override fun getSeenMovies(): Flow<List<Movie>> {
        return flow { emit(movies.filter { it.isSeen }) }
    }

    override fun getWatchlistMovies(): Flow<List<Movie>> {
        return flow { emit(movies.filter { it.isWatchlist }) }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return flow { emit(movies.filter { it.isFavorite }) }
    }

    override suspend fun getMovieById(movieId: Int): Movie? {
        return movies.find { it.id == movieId }
    }

    override suspend fun updateSeenStatus(
        movie: Movie,
        isSeen: Boolean
    ) {
        val index = movies.indexOfFirst { it.id == movie.id }
        if (index != -1) {
            movies[index] = movies[index].copy(
                isSeen = isSeen,
                lastModified = System.currentTimeMillis()
            )
        } else {
            movies.add(
                movie.copy(
                    isSeen = isSeen,
                    lastModified = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun updateWatchlistStatus(
        movie: Movie,
        isWatchlist: Boolean
    ) {
        val index = movies.indexOfFirst { it.id == movie.id }
        if (index != -1) {
            movies[index] = movies[index].copy(
                isWatchlist = isWatchlist,
                lastModified = System.currentTimeMillis()
            )
        } else {
            movies.add(
                movie.copy(
                    isWatchlist = isWatchlist,
                    lastModified = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun updateFavoriteStatus(
        movie: Movie,
        isFavorite: Boolean
    ) {
        val index = movies.indexOfFirst { it.id == movie.id }
        if (index != -1) {
            movies[index] = movies[index].copy(
                isFavorite = isFavorite,
                lastModified = System.currentTimeMillis()
            )
        } else {
            movies.add(
                movie.copy(
                    isFavorite = isFavorite,
                    lastModified = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun updateRating(
        movie: Movie,
        rating: Float?
    ) {
        val index = movies.indexOfFirst { it.id == movie.id }
        if (index != -1) {
            movies[index] = movies[index].copy(
                rating = rating,
                lastModified = System.currentTimeMillis()
            )
        } else {
            movies.add(
                movie.copy(
                    rating = rating,
                    lastModified = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun getPopularMovies(): List<Movie> {
        return movies
    }

    override suspend fun searchMovieByTitle(title: String): List<Movie> {
        return movies.filter { it.title.contains(title, ignoreCase = true) }
    }
}