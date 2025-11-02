package com.dthurman.moviesaver.data

import android.util.Log
import com.dthurman.moviesaver.data.local.MovieDao
import com.dthurman.moviesaver.data.remote.firebase.ai.FirebaseAiService
import com.dthurman.moviesaver.data.remote.themovieapi.TheMovieApi
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.AiRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.abs

class AiRepositoryImpl @Inject constructor(
    private val firebaseAiService: FirebaseAiService,
    private val movieRepository: MovieRepository,
    private val movieDao: MovieDao
) : AiRepository {

    override suspend fun generatePersonalizedRecommendations(): List<Movie> {
        val seenMovies = movieRepository.getSeenMovies().first()
        val watchlistMovies = movieRepository.getWatchlistMovies().first()
        val notInterestedMovies = movieRepository.getNotInterestedMovies()

        val targetCount = 5
        val maxAttempts = 3
        val movieRecommendations = mutableListOf<Movie>()

        val seenMovieIds = seenMovies.map { it.id }.toSet()
        val watchlistMovieIds = watchlistMovies.map { it.id }.toSet()
        val notInterestedMovieIds = notInterestedMovies.map { it.id }.toSet()

        var attempt = 0
        while (movieRecommendations.size < targetCount && attempt < maxAttempts) {
            attempt++
            val needed = targetCount - movieRecommendations.size

            val aiRecommendations = firebaseAiService.generateRecommendations(
                seenMovies = seenMovies,
                watchlistMovies = watchlistMovies,
                notInterestedMovies = notInterestedMovies,
                count = needed + 3 // Request a few extra in case some are filtered
            )

            for (aiRec in aiRecommendations) {
                if (movieRecommendations.size >= targetCount) break
                try {
                    var response = TheMovieApi.theMovieApi.searchMovies(query = aiRec.title, year = aiRec.year)

                    if (response.isSuccessful) {
                        var movies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                        if (movies.isEmpty()) {
                            response = TheMovieApi.theMovieApi.searchMovies(query = aiRec.title, year = null)
                            if (response.isSuccessful) {
                                movies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                                movies = movies.filter { movie ->
                                    val movieYear = movie.releaseDate.takeIf { it.length >= 4 }?.take(4)?.toIntOrNull() ?: 0
                                    val yearDiff = abs(movieYear - aiRec.year)
                                    yearDiff <= 2
                                }
                            }
                        }

                        val movie = movies.firstOrNull()
                        if (movie != null) {
                            if (movie.id in seenMovieIds || movie.id in watchlistMovieIds || movie.id in notInterestedMovieIds) {
                                continue
                            }

                            if (movieRecommendations.any { it.id == movie.id }) {
                                continue
                            }

                            val existingMovie = movieRepository.getMovieById(movie.id)
                            val movieWithStatus = (existingMovie ?: movie).copy(aiReason = aiRec.reason)

                            movieRecommendations.add(movieWithStatus)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AiRepository", "Error fetching movie '${aiRec.title}': ${e.message}", e)
                    continue
                }
            }
        }
        movieRepository.saveRecommendations(movieRecommendations)
        return movieRecommendations
    }

    override fun getSavedRecommendations(): Flow<List<Movie>> {
        return movieDao.getRecommendations().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }
}