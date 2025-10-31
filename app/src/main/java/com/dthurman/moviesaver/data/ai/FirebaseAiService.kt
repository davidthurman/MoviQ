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
        seenMovies: List<Movie>
    ): List<AiMovieRecommendation> {
        val prompt = buildPrompt(seenMovies)
        
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

    private fun buildPrompt(seenMovies: List<Movie>): String {
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

        return if (moviesList.isNotEmpty()) {
            """
                You are a movie recommendation expert. Based on the user's viewing history, suggest 5 personalized movie recommendations.
                
                User's watched movies (with their ratings and favorites marked):
                $moviesList
                
                Return ONLY a valid JSON array with exactly 5 movie recommendations. Each object must have:
                - "title": The exact movie title (string)
                - "year": The release year (integer)
                - "reason": Why they'll enjoy it based on their history (1-2 sentences, string)
                
                Example format:
                [
                  {
                    "title": "The Shawshank Redemption",
                    "year": 1994,
                    "reason": "Given your love for character-driven dramas like The Green Mile, this redemptive story will resonate with you."
                  }
                ]
                
                Return ONLY the JSON array, no other text or markdown formatting.
            """.trimIndent()
        } else {
            """
                The user hasn't watched any movies yet. Recommend 5 popular, highly-rated movies across different genres.
                
                Return ONLY a valid JSON array with exactly 5 movie recommendations. Each object must have:
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

