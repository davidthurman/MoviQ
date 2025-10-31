package com.dthurman.moviesaver.data.ai

import android.util.Log
import com.dthurman.moviesaver.data.local.database.MovieDao
import com.dthurman.moviesaver.data.remote.TheMovieApi.theMovieApi
import com.dthurman.moviesaver.data.remote.toDomain
import com.dthurman.moviesaver.data.remote.toEntity
import com.dthurman.moviesaver.domain.model.MovieRecommendation
import com.dthurman.moviesaver.domain.repository.AiRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val firebaseAiService: FirebaseAiService,
    private val movieRepository: MovieRepository,
    private val movieDao: MovieDao,
    private val authRepository: com.dthurman.moviesaver.domain.repository.AuthRepository,
    private val firestoreSyncService: com.dthurman.moviesaver.data.remote.firestore.FirestoreSyncService
) : AiRepository {

    override suspend fun generatePersonalizedRecommendations(): List<MovieRecommendation> {
        val seenMovies = movieRepository.getSeenMovies().first()
        val watchlistMovies = movieRepository.getWatchlistMovies().first()
        val notInterestedMovies = movieRepository.getNotInterestedMovies()
        
        val targetCount = 5
        val maxAttempts = 3
        val movieRecommendations = mutableListOf<MovieRecommendation>()
        
        // Create a set of IDs to quickly check if a movie should be excluded
        val seenMovieIds = seenMovies.map { it.id }.toSet()
        val watchlistMovieIds = watchlistMovies.map { it.id }.toSet()
        val notInterestedMovieIds = notInterestedMovies.map { it.id }.toSet()
        
        var attempt = 0
        while (movieRecommendations.size < targetCount && attempt < maxAttempts) {
            attempt++
            val needed = targetCount - movieRecommendations.size
            
            Log.d("AiRepository", "Attempt $attempt: Requesting $needed more recommendations")
            Log.d("AiRepository", "Excluding ${seenMovieIds.size} seen + ${watchlistMovieIds.size} watchlist + ${notInterestedMovieIds.size} not interested movies")
            
            val aiRecommendations = firebaseAiService.generateRecommendations(
                seenMovies = seenMovies,
                watchlistMovies = watchlistMovies,
                notInterestedMovies = notInterestedMovies,
                count = needed + 3 // Request a few extra in case some are filtered
            )
            
            for (aiRec in aiRecommendations) {
                if (movieRecommendations.size >= targetCount) break
                
                try {
                    var response = theMovieApi.searchMovies(query = aiRec.title, year = aiRec.year)
                    
                    if (response.isSuccessful) {
                        var movies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                        if (movies.isEmpty()) {
                            response = theMovieApi.searchMovies(query = aiRec.title, year = null)
                            if (response.isSuccessful) {
                                movies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                                movies = movies.filter { movie ->
                                    val movieYear = movie.releaseDate.takeIf { it.length >= 4 }?.take(4)?.toIntOrNull() ?: 0
                                    val yearDiff = kotlin.math.abs(movieYear - aiRec.year)
                                    yearDiff <= 2
                                }
                            }
                        }
                        
                        val movie = movies.firstOrNull()
                        if (movie != null) {
                            // Check if this movie is already in seen or watchlist
                            if (movie.id in seenMovieIds) {
                                Log.d("AiRepository", "✗ Skipping '${movie.title}' - already in seen list")
                                continue
                            }
                            
                            if (movie.id in watchlistMovieIds) {
                                Log.d("AiRepository", "✗ Skipping '${movie.title}' - already in watchlist")
                                continue
                            }
                            
                            if (movie.id in notInterestedMovieIds) {
                                Log.d("AiRepository", "✗ Skipping '${movie.title}' - user marked as not interested")
                                continue
                            }
                            
                            // Check if we already added this movie in this session
                            if (movieRecommendations.any { it.movie.id == movie.id }) {
                                Log.d("AiRepository", "✗ Skipping '${movie.title}' - duplicate in current recommendations")
                                continue
                            }
                            
                            val existingMovie = movieRepository.getMovieById(movie.id)
                            val movieWithStatus = existingMovie ?: movie
                            
                            movieRecommendations.add(
                                MovieRecommendation(
                                    movie = movieWithStatus,
                                    aiReason = aiRec.reason
                                )
                            )
                            Log.d("AiRepository", "✓ Added '${movie.title}' (${movieRecommendations.size}/$targetCount)")
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
        }
        
        if (movieRecommendations.size < targetCount) {
            Log.w("AiRepository", "Only found ${movieRecommendations.size}/$targetCount unique recommendations after $maxAttempts attempts")
        } else {
            Log.d("AiRepository", "Successfully generated $targetCount recommendations")
        }

        saveRecommendations(movieRecommendations)
        
        return movieRecommendations
    }

    override fun getSavedRecommendations(): Flow<List<MovieRecommendation>> {
        return movieDao.getRecommendations().map { entities ->
            entities.map { entity ->
                MovieRecommendation(
                    movie = entity.toDomain(),
                    aiReason = entity.aiReason ?: ""
                )
            }
        }
    }

    override suspend fun saveRecommendations(recommendations: List<MovieRecommendation>) {
        Log.d("AiRepository", "Saving ${recommendations.size} recommendations to Room and Firestore")
        
        val movieEntities = recommendations.map { rec ->
            rec.movie.toEntity().copy(aiReason = rec.aiReason)
        }
        movieEntities.forEach { movieDao.insertOrUpdateMovie(it) }
        
        Log.d("AiRepository", "Saved ${recommendations.size} movies with aiReason to Room")
        
        syncRecommendationsToFirestore(movieEntities)
    }
    
    private suspend fun syncRecommendationsToFirestore(movies: List<com.dthurman.moviesaver.data.local.database.MovieEntity>) {
        try {
            val userId = authRepository.getCurrentUser()?.id
            if (userId != null) {
                var synced = 0
                movies.forEach { movie ->
                    Log.d("AiRepository", "Syncing to Firestore: ${movie.title} | aiReason: ${movie.aiReason?.take(30)}")
                    val result = firestoreSyncService.syncMovie(movie, userId)
                    if (result.isSuccess) {
                        synced++
                    } else {
                        Log.e("AiRepository", "Failed to sync: ${movie.title}: ${result.exceptionOrNull()?.message}")
                    }
                }
                
                Log.d("AiRepository", "Synced $synced/${movies.size} recommendations to Firestore")
            } else {
                Log.w("AiRepository", "Cannot sync recommendations: user not logged in")
            }
        } catch (e: Exception) {
            Log.e("AiRepository", "Error syncing recommendations to Firestore: ${e.message}", e)
        }
    }

    override suspend fun clearRecommendations() {
        // Not used - recommendations are managed individually
    }
    
    override suspend fun syncRecommendationsFromFirestore() {
        // Recommendations sync automatically via the regular movie sync
        // since they're just movies with aiReason populated
    }
}

