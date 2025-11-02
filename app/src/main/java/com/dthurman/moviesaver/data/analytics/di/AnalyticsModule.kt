package com.dthurman.moviesaver.data.analytics.di

import com.dthurman.moviesaver.data.analytics.AnalyticsService
import com.dthurman.moviesaver.data.analytics.CrashlyticsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing analytics and crashlytics dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsService(): AnalyticsService {
        return AnalyticsService()
    }

    @Provides
    @Singleton
    fun provideCrashlyticsService(): CrashlyticsService {
        return CrashlyticsService()
    }
}

