package com.dthurman.moviesaver.feature_ai_recs.data.repository

import android.util.Log
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.TheMovieDBApi
import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.toDomain
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.abs

class AiRepositoryImpl @Inject constructor(
    private val aiService: AiService,
    private val movieRepository: MovieRepository,
    private val errorLogger: ErrorLogger
) : AiRepository {

    override suspend fun generatePersonalizedRecommendations(): List<Movie> {
        return try {
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

                val aiRecommendations = try {
                    aiService.generateRecommendations(
                        seenMovies = seenMovies,
                        watchlistMovies = watchlistMovies,
                        notInterestedMovies = notInterestedMovies,
                        count = needed + 3 // Request a few extra in case some are filtered
                    )
                } catch (e: Exception) {
                    errorLogger.logAiError(e)
                    throw e
                }

            for (aiRec in aiRecommendations) {
                if (movieRecommendations.size >= targetCount) break
                try {
                    var response = TheMovieDBApi.theMovieApi.searchMovies(query = aiRec.title, year = aiRec.year)

                    if (response.isSuccessful) {
                        var movies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                        if (movies.isEmpty()) {
                            response = TheMovieDBApi.theMovieApi.searchMovies(query = aiRec.title, year = null)
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
                            Log.d("AiRepository", "✓ Added '${movie.title}' (${movieRecommendations.size}/$targetCount)")
                        } else {
                            Log.d("AiRepository", "✗ No match found for: ${aiRec.title} (${aiRec.year})")
                        }
                    } else {
                        Log.d("AiRepository", "✗ TMDB API error: ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("AiRepository", "Error fetching movie '${aiRec.title}': ${e.message}", e)
                    errorLogger.logNetworkError("TMDB search for ${aiRec.title}", e)
                    continue
                }
            }
            }
            movieRepository.saveRecommendations(movieRecommendations)
            movieRecommendations
        } catch (e: Exception) {
            errorLogger.logAiError(e)
            throw e
        }
    }

    override fun getSavedRecommendations(): Flow<List<Movie>> {
        return movieRepository.getAiRecommendations()
    }
}