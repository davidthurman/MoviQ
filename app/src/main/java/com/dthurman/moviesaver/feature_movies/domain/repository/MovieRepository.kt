package com.dthurman.moviesaver.feature_movies.domain.repository

import com.dthurman.moviesaver.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getSeenMovies(): Flow<List<Movie>>
    fun getWatchlistMovies(): Flow<List<Movie>>
    fun getFavoriteMovies(): Flow<List<Movie>>
    suspend fun getMovieById(movieId: Int): Movie?
    suspend fun updateSeenStatus(movie: Movie, isSeen: Boolean)
    suspend fun updateWatchlistStatus(movie: Movie, isWatchlist: Boolean)
    suspend fun updateFavoriteStatus(movie: Movie, isFavorite: Boolean)
    suspend fun updateRating(movie: Movie, rating: Float?)
    suspend fun getPopularMovies(): List<Movie>
    suspend fun searchMovieByTitle(title: String): List<Movie>
}