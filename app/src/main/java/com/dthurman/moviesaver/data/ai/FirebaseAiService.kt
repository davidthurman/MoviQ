package com.dthurman.moviesaver.data.ai

import com.dthurman.moviesaver.domain.model.AiMovieRecommendation
import com.dthurman.moviesaver.domain.model.Movie
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.content
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class FirebaseAiService @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val gson: Gson
) {
    suspend fun generateRecommendations(
        seenMovies: List<Movie>,
        watchlistMovies: List<Movie> = emptyList(),
        notInterestedMovies: List<Movie> = emptyList(),
        count: Int = 5
    ): List<AiMovieRecommendation> {
        val prompt = buildPrompt(seenMovies, watchlistMovies, notInterestedMovies, count)
        
        try {
            val response = generativeModel.generateContent(
                content {
                    text(prompt)
                }
            )
            val jsonText = response.text ?: throw AiException("Empty response from AI")
            val cleanedJson = extractJson(jsonText)
            val type = object : TypeToken<List<AiMovieRecommendation>>() {}.type
            return gson.fromJson(cleanedJson, type)
                ?: throw AiException("Failed to parse AI response")
        } catch (e: JsonSyntaxException) {
            throw AiException("Invalid JSON format in AI response: ${e.message}", e)
        } catch (e: Exception) {
            throw AiException("Failed to generate recommendations: ${e.message}", e)
        }
    }

    private fun buildPrompt(
        seenMovies: List<Movie>,
        watchlistMovies: List<Movie>,
        notInterestedMovies: List<Movie>,
        count: Int
    ): String {
        val moviesList = seenMovies.take(50).joinToString("\n") { movie ->
            buildString {
                append("- ${movie.title} (${movie.releaseDate.take(4)})")
                if (movie.rating != null) {
                    append(" - User Rating: ${movie.rating}/5")
                }
                if (movie.isFavorite) {
                    append(" ⭐ FAVORITE")
                }
            }
        }
        
        val watchlistList = watchlistMovies.joinToString("\n") { movie ->
            "- ${movie.title} (${movie.releaseDate.take(4)})"
        }
        
        val notInterestedList = notInterestedMovies.joinToString("\n") { movie ->
            "- ${movie.title} (${movie.releaseDate.take(4)})"
        }

        return if (moviesList.isNotEmpty()) {
            buildString {
                append("""
                    You are a movie recommendation expert. Based on the user's viewing history, suggest $count personalized movie recommendations.
                    
                    User's watched movies (with their ratings and favorites marked):
                    $moviesList
                """.trimIndent())
                
                if (watchlistList.isNotEmpty()) {
                    append("\n\n")
                    append("""
                        Movies already in their watchlist (DO NOT recommend these):
                        $watchlistList
                    """.trimIndent())
                }
                
                if (notInterestedList.isNotEmpty()) {
                    append("\n\n")
                    append("""
                        Movies the user is NOT interested in (DO NOT recommend these):
                        $notInterestedList
                    """.trimIndent())
                }
                
                append("\n\n")
                append("""
                    Return ONLY a valid JSON array with exactly $count movie recommendations. Each object must have:
                    - "title": The exact movie title (string)
                    - "year": The release year (integer)
                    - "reason": Why they'll enjoy it based on their history (1-2 sentences, string)
                    
                    IMPORTANT: Do NOT recommend any movies from their watched list, watchlist, or movies they're not interested in!
                    
                    Example format:
                    [
                      {
                        "title": "The Shawshank Redemption",
                        "year": 1994,
                        "reason": "Given your love for character-driven dramas like The Green Mile, this redemptive story will resonate with you."
                      }
                    ]
                    
                    Return ONLY the JSON array, no other text or markdown formatting.
                """.trimIndent())
            }
        } else {
            """
                The user hasn't watched any movies yet. Recommend $count popular, highly-rated movies across different genres.
                
                Return ONLY a valid JSON array with exactly $count movie recommendations. Each object must have:
                - "title": The exact movie title (string)
                - "year": The release year (integer)
                - "reason": Why it's worth watching (1-2 sentences, string)
                
                Example format:
                [
                  {
                    "title": "The Shawshank Redemption",
                    "year": 1994,
                    "reason": "A timeless tale of hope and friendship that consistently ranks as one of the greatest films ever made."
                  }
                ]
                
                Return ONLY the JSON array, no other text or markdown formatting.
            """.trimIndent()
        }
    }

    private fun extractJson(text: String): String {
        val codeBlockRegex = "```(?:json)?\\s*([\\s\\S]*?)```".toRegex()
        val match = codeBlockRegex.find(text)
        
        return if (match != null) {
            match.groupValues[1].trim()
        } else {
            val jsonArrayRegex = "\\[\\s*\\{[\\s\\S]*}\\s*]".toRegex()
            jsonArrayRegex.find(text)?.value?.trim() ?: text.trim()
        }
    }
}

class AiException(message: String, cause: Throwable? = null) : Exception(message, cause)

