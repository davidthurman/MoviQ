package com.dthurman.moviesaver.feature_movies.data.remote.themoviedb

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMovieApiInterface {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<MovieListDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("year") year: Int? = null,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<MovieListDto>

}