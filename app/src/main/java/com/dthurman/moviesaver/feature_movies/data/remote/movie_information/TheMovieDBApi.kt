package com.dthurman.moviesaver.feature_movies.data.remote.movie_information

import com.dthurman.moviesaver.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TheMovieDBApi {

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val AUTH_TOKEN = BuildConfig.MOVIES_API_KEY

    val theMovieApi: TheMovieDBApiInterface by lazy {
        val httpClient = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $AUTH_TOKEN")
                .addHeader("accept", "application/json")
                .build()
            chain.proceed(request)
        }).build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TheMovieDBApiInterface::class.java)
    }

}