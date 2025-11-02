package com.dthurman.moviesaver.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.dthurman.moviesaver.data.AiRepositoryImpl
import com.dthurman.moviesaver.data.AuthRepositoryImpl
import com.dthurman.moviesaver.data.DefaultMovieRepository
import com.dthurman.moviesaver.data.local.AppDatabase
import com.dthurman.moviesaver.data.local.MovieDao
import com.dthurman.moviesaver.data.remote.billing.BillingManager
import com.dthurman.moviesaver.data.remote.firebase.analytics.AnalyticsService
import com.dthurman.moviesaver.data.remote.firebase.analytics.CrashlyticsService
import com.dthurman.moviesaver.data.remote.firebase.firestore.FirestoreSyncService
import com.dthurman.moviesaver.data.sync.SyncManager
import com.dthurman.moviesaver.domain.repository.AiRepository
import com.dthurman.moviesaver.domain.repository.AuthRepository
import com.dthurman.moviesaver.domain.repository.MovieRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.vertexAI
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "Movie"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun provideMovieDao(database: AppDatabase): MovieDao {
        return database.movieDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val cacheSettings = PersistentCacheSettings.newBuilder()
            .setSizeBytes(100 * 1024 * 1024) // 100 MB cache
            .build()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(cacheSettings)
            .build()
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideFirestoreSyncService(
        firestore: FirebaseFirestore,
        movieDao: MovieDao
    ): FirestoreSyncService {
        return FirestoreSyncService(firestore, movieDao)
    }

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return com.google.firebase.Firebase.vertexAI.generativeModel(
            modelName = "gemini-2.0-flash-exp",
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 2048
            }
        )
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

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

    @Provides
    @Singleton
    fun provideBillingManager(@ApplicationContext context: Context): BillingManager {
        return BillingManager(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestoreSyncService: FirestoreSyncService,
        movieRepositoryProvider: Provider<MovieRepository>,
        billingManager: BillingManager,
        syncManager: SyncManager,
        analyticsService: AnalyticsService
    ): AuthRepository {
        return AuthRepositoryImpl(
            firebaseAuth,
            firestoreSyncService,
            movieRepositoryProvider,
            billingManager,
            syncManager,
            analyticsService
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface AppBindingModule {

    @Singleton
    @Binds
    fun bindMovieRepository(impl: DefaultMovieRepository): MovieRepository

    @Singleton
    @Binds
    fun bindAiRepository(impl: AiRepositoryImpl): AiRepository
}

