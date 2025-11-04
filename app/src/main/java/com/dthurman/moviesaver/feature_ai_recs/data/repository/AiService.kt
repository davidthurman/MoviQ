package com.dthurman.moviesaver.feature_ai_recs.data.repository

import android.content.Context
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_ai_recs.data.AiMovieRecommendation
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.content
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AiService @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val gson: Gson,
    @ApplicationContext private val context: Context
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
            val jsonText = response.text ?: throw AiException(context.getString(R.string.error_empty_ai_response))
            val cleanedJson = extractJson(jsonText)
            val type = object : TypeToken<List<AiMovieRecommendation>>() {}.type
            return gson.fromJson(cleanedJson, type)
                ?: throw AiException(context.getString(R.string.error_failed_to_parse))
        } catch (e: JsonSyntaxException) {
            throw AiException(context.getString(R.string.error_invalid_json, e.message ?: ""), e)
        } catch (e: Exception) {
            throw AiException(context.getString(R.string.error_failed_to_generate) + ": ${e.message}", e)
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
                if (movie.rating != null) {
                    append(context.getString(R.string.ai_movie_item_with_rating, 
                        movie.title, movie.releaseDate.take(4), movie.rating))
                } else {
                    append(context.getString(R.string.ai_movie_item_format, 
                        movie.title, movie.releaseDate.take(4)))
                }
                if (movie.isFavorite) {
                    append(" ${context.getString(R.string.ai_movie_favorite_marker)}")
                }
            }
        }
        
        val watchlistList = watchlistMovies.joinToString("\n") { movie ->
            context.getString(R.string.ai_movie_item_format, movie.title, movie.releaseDate.take(4))
        }
        
        val notInterestedList = notInterestedMovies.joinToString("\n") { movie ->
            context.getString(R.string.ai_movie_item_format, movie.title, movie.releaseDate.take(4))
        }

        return if (moviesList.isNotEmpty()) {
            buildString {
                append(context.getString(R.string.ai_prompt_expert_intro, count))
                append("\n\n")
                append(context.getString(R.string.ai_prompt_watched_movies))
                append("\n")
                append(moviesList)
                
                if (watchlistList.isNotEmpty()) {
                    append("\n\n")
                    append(context.getString(R.string.ai_prompt_watchlist_header))
                    append("\n")
                    append(watchlistList)
                }
                
                if (notInterestedList.isNotEmpty()) {
                    append("\n\n")
                    append(context.getString(R.string.ai_prompt_not_interested_header))
                    append("\n")
                    append(notInterestedList)
                }
                
                append("\n\n")
                append(context.getString(R.string.ai_prompt_json_instructions, count))
            }
        } else {
            context.getString(R.string.ai_prompt_new_user, count, count)
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
