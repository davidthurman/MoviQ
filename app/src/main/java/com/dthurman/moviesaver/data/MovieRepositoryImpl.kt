package com.dthurman.moviesaver.data

import com.dthurman.moviesaver.data.local.database.MovieEntity
import com.dthurman.moviesaver.data.local.database.MovieDao
import com.dthurman.moviesaver.data.the_movie_db.TheMovieApi.theMovieApi
import com.dthurman.moviesaver.data.the_movie_db.toDomain
import com.dthurman.moviesaver.data.the_movie_db.toEntity
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultMovieRepository @Inject constructor(
    private val movieDao: MovieDao,
) : MovieRepository {

    override fun seenMovies(): Flow<List<Movie>> {
        return movieDao.getSeenMovies().map { items -> items.map { it.toDomain() } }
    }

    override suspend fun addMovieToSeen(movie: Movie) {
        movieDao.addMovieToSeen( movie.toEntity())
    }

    override suspend fun getPopularMovies(): List<Movie> {
        val response = theMovieApi.getPopularMovies()
        if (response.isSuccessful) {
            return response.body()?.results?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to fetch movies: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun searchMovieByTitle(title: String): List<Movie> {
        val response = theMovieApi.searchMovies(query = title)
        if (response.isSuccessful) {
            return response.body()?.results?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to search movies: ${response.code()} ${response.message()}")
        }
    }
}