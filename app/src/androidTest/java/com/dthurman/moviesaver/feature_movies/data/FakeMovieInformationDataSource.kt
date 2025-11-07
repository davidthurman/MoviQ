package com.dthurman.moviesaver.feature_movies.data

import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.MovieInformationDataSource
import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.MovieInformationDto

class FakeMovieInformationDataSource : MovieInformationDataSource {
    
    var shouldSucceed = true
    var popularMovies = listOf(
        MovieInformationDto(
            adult = false,
            backdrop_path = "/5XNQBqnBwPA9yT0jZ0p3s8bbLh0.jpg",
            id = 157336,
            original_language = "en",
            original_title = "Interstellar",
            overview = "The adventures of a group of explorers who make use of a newly discovered wormhole to surpass the limitations on human space travel and conquer the vast distances involved in an interstellar voyage.",
            popularity = 150.0,
            poster_path = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
            release_date = "2014-11-07",
            title = "Interstellar",
            video = false,
            vote_average = 8.4,
            vote_count = 30000
        ),
        MovieInformationDto(
            adult = false,
            backdrop_path = "/v8xVDqt8uCul3c3mgx4VpGCwxJC.jpg",
            id = 278,
            original_language = "en",
            original_title = "The Shawshank Redemption",
            overview = "Imprisoned in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison.",
            popularity = 120.0,
            poster_path = "/9cqNxx0GxF0bflZmeSMuL5tnGzr.jpg",
            release_date = "1994-09-23",
            title = "The Shawshank Redemption",
            video = false,
            vote_average = 8.7,
            vote_count = 25000
        ),
        MovieInformationDto(
            adult = false,
            backdrop_path = "/backdrop.jpg",
            id = 8326,
            original_language = "en",
            original_title = "Holes",
            overview = "A wrongfully convicted boy is sent to a brutal desert detention camp where he must dig holes.",
            popularity = 80.0,
            poster_path = "/poster.jpg",
            release_date = "2003-04-18",
            title = "Holes",
            video = false,
            vote_average = 7.0,
            vote_count = 5000
        )
    )
    
    override suspend fun getPopularMovies(): Result<List<MovieInformationDto>> {
        return if (shouldSucceed) {
            Result.success(popularMovies)
        } else {
            Result.failure(Exception("Failed to fetch popular movies"))
        }
    }
    
    override suspend fun searchMovies(query: String): Result<List<MovieInformationDto>> {
        return if (shouldSucceed) {
            val filtered = popularMovies.filter { 
                it.title.contains(query, ignoreCase = true) 
            }
            Result.success(filtered)
        } else {
            Result.failure(Exception("Failed to search movies"))
        }
    }
    
    fun reset() {
        shouldSucceed = true
    }
}

