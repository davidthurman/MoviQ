package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSeenMoviesCountUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(): Flow<Int> {
        return movieRepository.getSeenMovies()
            .map { movies -> movies.size }
    }
}

