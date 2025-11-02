package com.dthurman.moviesaver.data.remote.firebase.ai

import android.util.Log
import com.dthurman.moviesaver.data.local.MovieDao
import com.dthurman.moviesaver.data.local.MovieEntity
import com.dthurman.moviesaver.data.remote.firebase.firestore.FirestoreSyncService
import com.dthurman.moviesaver.data.remote.themovieapi.TheMovieApi.theMovieApi
import com.dthurman.moviesaver.data.toDomain
import com.dthurman.moviesaver.data.toEntity
import com.dthurman.moviesaver.domain.model.Movie
import com.dthurman.moviesaver.domain.repository.AiRepository
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.abs

class AiRepositoryImpl @Inject constructor(
    private val firebaseAiService: FirebaseAiService,
    private val movieRepository: MovieRepository,
    private val movieDao: MovieDao,
    private val authRepository: AuthRepository,
    private val firestoreSyncService: FirestoreSyncService
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
                                    val yearDiff = abs(movieYear - aiRec.year)
                                    yearDiff <= 2
                                }
                            }
                        }
                        
                        val movie = movies.firstOrNull()
                        if (movie != null) {
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
                            
                            if (movieRecommendations.any { it.id == movie.id }) {
                                Log.d("AiRepository", "✗ Skipping '${movie.title}' - duplicate in current recommendations")
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
                    continue
                }
            }
        }
        saveRecommendations(movieRecommendations)
        return movieRecommendations
    }

    override fun getSavedRecommendations(): Flow<List<Movie>> {
        return movieDao.getRecommendations().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun saveRecommendations(recommendations: List<Movie>) {
        val movieEntities = recommendations.map { it.toEntity() }
        movieEntities.forEach { movieDao.insertOrUpdateMovie(it) }
        syncRecommendationsToFirestore(movieEntities)
    }
    
    private suspend fun syncRecommendationsToFirestore(movies: List<MovieEntity>) {
        try {
            val userId = authRepository.getCurrentUser()?.id
            if (userId != null) {
                var synced = 0
                movies.forEach { movie ->
                    val result = firestoreSyncService.syncMovie(movie, userId)
                    if (result.isSuccess) {
                        synced++
                    } else {
                        Log.e("AiRepository", "Failed to sync: ${movie.title}: ${result.exceptionOrNull()?.message}")
                    }
                }
            } else {
                Log.w("AiRepository", "Cannot sync recommendations: user not logged in")
            }
        } catch (e: Exception) {
            Log.e("AiRepository", "Error syncing recommendations to Firestore: ${e.message}", e)
        }
    }
}

