package com.dthurman.moviesaver.data

import com.dthurman.moviesaver.data.local.database.MovieDao
import com.dthurman.moviesaver.data.remote.TheMovieApi.theMovieApi
import com.dthurman.moviesaver.data.remote.toDomain
import com.dthurman.moviesaver.data.remote.toEntity
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultMovieRepository @Inject constructor(
    private val movieDao: MovieDao,
) : MovieRepository {

    override fun getSeenMovies(): Flow<List<Movie>> {
        return movieDao.getSeenMovies().map { items -> items.map { it.toDomain() } }
    }

    override fun getWatchlistMovies(): Flow<List<Movie>> {
        return movieDao.getWatchlistMovies().map { items -> items.map { it.toDomain() } }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return movieDao.getFavoriteMovies().map { items -> items.map { it.toDomain() } }
    }

    override suspend fun getMovieById(movieId: Int): Movie? {
        return movieDao.getMovieById(movieId)?.toDomain()
    }

    override suspend fun updateSeenStatus(movie: Movie, isSeen: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateSeenStatus(movie.id, isSeen)
            movieDao.updateWatchlistStatus(movie.id, false)
        } else {
            movieDao.insertOrUpdateMovie(movie.copy(isSeen = isSeen).toEntity())
        }
    }

    override suspend fun updateWatchlistStatus(movie: Movie, isWatchlist: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateWatchlistStatus(movie.id, isWatchlist)
        } else {
            movieDao.insertOrUpdateMovie(movie.copy(isWatchlist = isWatchlist).toEntity())
        }
    }

    override suspend fun updateFavoriteStatus(movie: Movie, isFavorite: Boolean) {
        val existingMovie = movieDao.getMovieById(movie.id)
        if (existingMovie != null) {
            movieDao.updateFavoriteStatus(movie.id, isFavorite)
        } else {
            movieDao.insertOrUpdateMovie(movie.copy(isFavorite = isFavorite).toEntity())
        }
    }

    override suspend fun getPopularMovies(): List<Movie> {
        val response = theMovieApi.getPopularMovies()
        if (response.isSuccessful) {
            val apiMovies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
            return enrichMoviesWithLocalStatus(apiMovies)
        } else {
            throw Exception("Failed to fetch movies: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun searchMovieByTitle(title: String): List<Movie> {
        val response = theMovieApi.searchMovies(query = title)
        if (response.isSuccessful) {
            val apiMovies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
            return enrichMoviesWithLocalStatus(apiMovies)
        } else {
            throw Exception("Failed to search movies: ${response.code()} ${response.message()}")
        }
    }

    private suspend fun enrichMoviesWithLocalStatus(apiMovies: List<Movie>): List<Movie> {
        if (apiMovies.isEmpty()) return emptyList()
        
        val movieIds = apiMovies.map { it.id }
        val localMovies = movieDao.getMoviesByIds(movieIds)
        val localMovieMap = localMovies.associateBy { it.id }
        
        return apiMovies.map { apiMovie ->
            val localMovie = localMovieMap[apiMovie.id]
            if (localMovie != null) {
                apiMovie.copy(
                    isSeen = localMovie.isSeen,
                    isWatchlist = localMovie.isWatchlist,
                    isFavorite = localMovie.isFavorite
                )
            } else {
                apiMovie
            }
        }
    }
}