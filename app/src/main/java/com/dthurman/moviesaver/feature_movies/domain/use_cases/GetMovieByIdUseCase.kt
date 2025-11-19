package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import javax.inject.Inject

class GetMovieByIdUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): Result<Movie?> {
        return try {
            val movie = movieRepository.getMovieById(movieId)
            Result.success(movie)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



