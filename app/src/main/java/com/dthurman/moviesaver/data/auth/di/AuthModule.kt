package com.dthurman.moviesaver.data.auth.di

import com.dthurman.moviesaver.data.auth.AuthRepositoryImpl
import com.dthurman.moviesaver.data.remote.firestore.FirestoreSyncService
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.CreditsRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        creditsRepository: CreditsRepository,
        firestoreSyncService: FirestoreSyncService,
        movieRepositoryProvider: Provider<MovieRepository>
    ): AuthRepository {
        return AuthRepositoryImpl(
            firebaseAuth, 
            creditsRepository, 
            firestoreSyncService,
            movieRepositoryProvider
        )
    }
}

