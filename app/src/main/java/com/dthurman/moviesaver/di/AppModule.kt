package com.dthurman.moviesaver.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.data.local.MovieDatabase
import com.dthurman.moviesaver.core.data.remote.user.FirestoreUserDataSource
import com.dthurman.moviesaver.core.data.remote.user.UserRemoteDataSource
import com.dthurman.moviesaver.core.data.repository.CreditsRepositoryImpl
import com.dthurman.moviesaver.core.data.repository.UserRepositoryImpl
import com.dthurman.moviesaver.core.data.sync.SyncManager
import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_ai_recs.data.repository.AiRepositoryImpl
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import com.dthurman.moviesaver.feature_auth.data.repository.AuthRepositoryImpl
import com.dthurman.moviesaver.feature_auth.domain.AuthRepository
import com.dthurman.moviesaver.feature_billing.data.repository.BillingRepositoryImpl
import com.dthurman.moviesaver.feature_billing.domain.BillingManager
import com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository
import com.dthurman.moviesaver.feature_movies.data.remote.data_source.FirestoreMovieDataSource
import com.dthurman.moviesaver.feature_movies.data.remote.data_source.MovieRemoteDataSource
import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.MovieInformationDataSource
import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.TheMovieDBDataSource
import com.dthurman.moviesaver.feature_movies.data.repository.MovieRepositoryImpl
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
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
    fun provideAppDatabase(@ApplicationContext context: Context): MovieDatabase {
        return Room.databaseBuilder(
            context,
            MovieDatabase::class.java,
            "Movie"
        ).build()
    }

    @Provides
    fun provideMovieDao(database: MovieDatabase): MovieDao {
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
    fun provideUserRemoteDataSource(
        firestore: FirebaseFirestore,
        errorLogger: ErrorLogger
    ): UserRemoteDataSource {
        return FirestoreUserDataSource(firestore, errorLogger)
    }

    @Provides
    @Singleton
    fun provideMovieRemoteDataSource(
        firestore: FirebaseFirestore,
        movieDao: MovieDao,
        errorLogger: ErrorLogger
    ): MovieRemoteDataSource {
        return FirestoreMovieDataSource(firestore, movieDao, errorLogger)
    }

    @Provides
    @Singleton
    fun provideMovieInformationDataSource(
        errorLogger: ErrorLogger
    ): MovieInformationDataSource {
        return TheMovieDBDataSource(errorLogger)
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
    fun provideBillingManager(
        @ApplicationContext context: Context,
        errorLogger: ErrorLogger
    ): BillingManager {
        return BillingManager(context, errorLogger)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideUserRepositoryImpl(
        firebaseAuth: FirebaseAuth,
        userRemoteDataSource: UserRemoteDataSource,
        errorLogger: ErrorLogger
    ): UserRepositoryImpl {
        return UserRepositoryImpl(firebaseAuth, userRemoteDataSource, errorLogger)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        userRemoteDataSource: UserRemoteDataSource,
        movieRepositoryProvider: Provider<MovieRepository>,
        userRepository: UserRepositoryImpl,
        syncManager: SyncManager,
        analytics: AnalyticsTracker,
        errorLogger: ErrorLogger
    ): AuthRepository {
        return AuthRepositoryImpl(
            firebaseAuth,
            userRemoteDataSource,
            movieRepositoryProvider,
            userRepository,
            syncManager,
            analytics,
            errorLogger
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface AppBindingModule {

    @Singleton
    @Binds
    fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

    @Singleton
    @Binds
    fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    @Singleton
    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    fun bindCreditsRepository(impl: CreditsRepositoryImpl): CreditsRepository

    @Singleton
    @Binds
    fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository
}
