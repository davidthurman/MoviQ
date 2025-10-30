package com.dthurman.moviesaver.data.ai

import android.util.Log
import com.dthurman.moviesaver.data.local.database.RecommendationDao
import com.dthurman.moviesaver.data.local.database.RecommendationEntity
import com.dthurman.moviesaver.data.remote.TheMovieApi.theMovieApi
import com.dthurman.moviesaver.data.remote.toDomain
import com.dthurman.moviesaver.domain.model.MovieRecommendation
import com.dthurman.moviesaver.domain.repository.AiRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of AiRepository that uses Firebase AI and TMDB API
 */
class AiRepositoryImpl @Inject constructor(
    private val firebaseAiService: FirebaseAiService,
    private val movieRepository: MovieRepository,
    private val recommendationDao: RecommendationDao
) : AiRepository {

    override suspend fun generatePersonalizedRecommendations(): List<MovieRecommendation> {
        val seenMovies = movieRepository.getSeenMovies().first()
        val aiRecommendations = firebaseAiService.generateRecommendations(seenMovies)
        Log.d("AiRepository", "AI returned ${aiRecommendations.size} recommendations")
        val movieRecommendations = mutableListOf<MovieRecommendation>()
        
        for (aiRec in aiRecommendations) {
            try {
                Log.d("AiRepository", "Searching for: ${aiRec.title} (${aiRec.year})")
                var response = theMovieApi.searchMovies(query = aiRec.title, year = aiRec.year)
                
                if (response.isSuccessful) {
                    var movies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                    Log.d("AiRepository", "TMDB returned ${movies.size} results for '${aiRec.title}' year=${aiRec.year}")
                    if (movies.isEmpty()) {
                        Log.d("AiRepository", "No results with year filter, trying without year")
                        response = theMovieApi.searchMovies(query = aiRec.title, year = null)
                        if (response.isSuccessful) {
                            movies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                            Log.d("AiRepository", "TMDB returned ${movies.size} results for '${aiRec.title}' (no year filter)")
                            movies = movies.filter { movie ->
                                val movieYear = movie.releaseDate.takeIf { it.length >= 4 }?.take(4)?.toIntOrNull() ?: 0
                                val yearDiff = kotlin.math.abs(movieYear - aiRec.year)
                                yearDiff <= 2
                            }
                            Log.d("AiRepository", "After year filtering (±2 years): ${movies.size} results")
                        }
                    }
                    val movie = movies.firstOrNull()
                    if (movie != null) {
                        Log.d("AiRepository", "✓ Found match: ${movie.title} (${movie.releaseDate.take(4)})")
                        val existingMovie = movieRepository.getMovieById(movie.id)
                        val movieWithStatus = existingMovie ?: movie
                        
                        movieRecommendations.add(
                            MovieRecommendation(
                                movie = movieWithStatus,
                                aiReason = aiRec.reason
                            )
                        )
                    } else {
                        Log.d("AiRepository", "✗ No match found for: ${aiRec.title} (${aiRec.year})")
                    }
                } else {
                    Log.d("AiRepository", "✗ TMDB API error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("AiRepository", "Error fetching movie '${aiRec.title}': ${e.message}", e)
                continue
            }
        }
        
        Log.d("AiRepository", "Final result: ${movieRecommendations.size} movies with TMDB data")
        
        // Save recommendations to database
        saveRecommendations(movieRecommendations)
        
        return movieRecommendations
    }

    override fun getSavedRecommendations(): Flow<List<MovieRecommendation>> {
        return recommendationDao.getAllRecommendations().map { entities ->
            entities.mapNotNull { entity ->
                val movie = movieRepository.getMovieById(entity.movieId)
                if (movie != null) {
                    MovieRecommendation(
                        movie = movie,
                        aiReason = entity.aiReason
                    )
                } else null
            }
        }
    }

    override suspend fun saveRecommendations(recommendations: List<MovieRecommendation>) {
        // Clear old recommendations first
        recommendationDao.clearAllRecommendations()
        
        // Save new recommendations
        val entities = recommendations.mapIndexed { index, rec ->
            RecommendationEntity(
                movieId = rec.movie.id,
                aiReason = rec.aiReason,
                orderIndex = index
            )
        }
        recommendationDao.insertRecommendations(entities)
        Log.d("AiRepository", "Saved ${entities.size} recommendations to database")
    }

    override suspend fun clearRecommendations() {
        recommendationDao.clearAllRecommendations()
        Log.d("AiRepository", "Cleared all recommendations from database")
    }
}

