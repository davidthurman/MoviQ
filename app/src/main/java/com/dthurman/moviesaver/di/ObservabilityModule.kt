package com.dthurman.moviesaver.di

import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.core.observability.AnalyticsTrackerImpl
import com.dthurman.moviesaver.core.observability.ErrorLoggerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ObservabilityModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(
        impl: AnalyticsTrackerImpl
    ): AnalyticsTracker

    @Binds
    @Singleton
    abstract fun bindErrorLogger(
        impl: ErrorLoggerImpl
    ): ErrorLogger
}

