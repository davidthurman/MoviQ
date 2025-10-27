package com.dthurman.moviesaver.data.the_movie_db

import com.dthurman.moviesaver.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TheMovieApi {

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val AUTH_TOKEN = BuildConfig.MOVIES_API_KEY

    val theMovieApi: TheMovieApiInterface by lazy {
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
            .create(TheMovieApiInterface::class.java)
    }

}