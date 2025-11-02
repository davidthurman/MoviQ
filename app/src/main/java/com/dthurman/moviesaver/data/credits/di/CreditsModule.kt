package com.dthurman.moviesaver.data.credits.di

import com.dthurman.moviesaver.data.credits.CreditsRepositoryImpl
import com.dthurman.moviesaver.domain.repository.CreditsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CreditsModule {

    @Singleton
    @Binds
    fun bindCreditsRepository(
        creditsRepository: CreditsRepositoryImpl
    ): CreditsRepository
}

