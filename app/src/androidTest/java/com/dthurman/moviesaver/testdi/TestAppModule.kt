package com.dthurman.moviesaver.testdi

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.dthurman.moviesaver.core.data.local.MovieDao
import com.dthurman.moviesaver.core.data.local.MovieDatabase
import com.dthurman.moviesaver.core.data.remote.user.FirestoreUserDataSource
import com.dthurman.moviesaver.core.data.remote.user.UserRemoteDataSource
import com.dthurman.moviesaver.core.data.repository.FakeCreditsRepository
import com.dthurman.moviesaver.core.data.repository.FakeUserRepository
import com.dthurman.moviesaver.core.data.repository.LocalDataManagerImpl
import com.dthurman.moviesaver.core.data.repository.UserRepositoryImpl
import com.dthurman.moviesaver.core.data.sync.MovieSyncService
import com.dthurman.moviesaver.core.data.sync.SyncManager
import com.dthurman.moviesaver.core.domain.repository.CreditsRepository
import com.dthurman.moviesaver.core.domain.repository.LocalDataManager
import com.dthurman.moviesaver.core.domain.repository.UserRepository
import com.dthurman.moviesaver.core.data.observability.FakeAnalyticsTracker
import com.dthurman.moviesaver.core.data.observability.FakeErrorLogger
import com.dthurman.moviesaver.core.observability.AnalyticsTracker
import com.dthurman.moviesaver.core.observability.ErrorLogger
import com.dthurman.moviesaver.feature_ai_recs.data.repository.FakeAiRepository
import com.dthurman.moviesaver.feature_ai_recs.data.repository.FakeRecommendationRepository
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.AiRepository
import com.dthurman.moviesaver.feature_ai_recs.domain.repository.RecommendationRepository
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.AcceptRecommendationAsSeenUseCase
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.AcceptRecommendationToWatchlistUseCase
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.AiRecsUseCases
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.GenerateAiRecommendationsUseCase
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.GetSavedRecommendationsUseCase
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.GetSeenMoviesCountUseCase
import com.dthurman.moviesaver.feature_ai_recs.domain.use_cases.RejectRecommendationUseCase
import com.dthurman.moviesaver.feature_auth.data.repository.AuthRepositoryImpl
import com.dthurman.moviesaver.feature_auth.domain.AuthRepository
import com.dthurman.moviesaver.feature_auth.domain.use_cases.AuthUseCases
import com.dthurman.moviesaver.feature_auth.domain.use_cases.GetGoogleCredentialUseCase
import com.dthurman.moviesaver.feature_auth.domain.use_cases.ObserveCurrentUserUseCase
import com.dthurman.moviesaver.feature_auth.domain.use_cases.SignInWithGoogleUseCase
import com.dthurman.moviesaver.feature_auth.domain.use_cases.SignOutUseCase
import com.dthurman.moviesaver.feature_billing.data.repository.BillingRepositoryImpl
import com.dthurman.moviesaver.feature_billing.domain.BillingManager
import com.dthurman.moviesaver.feature_billing.domain.repository.BillingRepository
import com.dthurman.moviesaver.feature_billing.domain.use_cases.BillingUseCases
import com.dthurman.moviesaver.feature_billing.domain.use_cases.LaunchPurchaseFlowUseCase
import com.dthurman.moviesaver.feature_billing.domain.use_cases.ObservePurchaseStateUseCase
import com.dthurman.moviesaver.feature_billing.domain.use_cases.ProcessPurchaseUseCase
import com.dthurman.moviesaver.feature_billing.domain.use_cases.ResetPurchaseStateUseCase
import com.dthurman.moviesaver.feature_movies.data.FakeMovieInformationDataSource
import com.dthurman.moviesaver.feature_movies.data.remote.data_source.FirestoreMovieDataSource
import com.dthurman.moviesaver.feature_movies.data.remote.data_source.MovieRemoteDataSource
import com.dthurman.moviesaver.feature_movies.data.remote.movie_information.MovieInformationDataSource
import com.dthurman.moviesaver.feature_movies.data.repository.MovieRepositoryImpl
import com.dthurman.moviesaver.feature_movies.domain.repository.MovieRepository
import com.dthurman.moviesaver.feature_movies.domain.use_cases.GetMovieByIdUseCase
import com.dthurman.moviesaver.feature_movies.domain.use_cases.GetPopularMoviesUseCase
import com.dthurman.moviesaver.feature_movies.domain.use_cases.GetUserMoviesUseCase
import com.dthurman.moviesaver.feature_movies.domain.use_cases.MoviesUseCases
import com.dthurman.moviesaver.feature_movies.domain.use_cases.SearchMoviesUseCase
import com.dthurman.moviesaver.feature_movies.domain.use_cases.UpdateFavoriteStatusUseCase
import com.dthurman.moviesaver.feature_movies.domain.use_cases.UpdateRatingUseCase
import com.dthurman.moviesaver.feature_movies.domain.use_cases.UpdateSeenStatusUseCase
import com.dthurman.moviesaver.feature_movies.domain.use_cases.UpdateWatchlistStatusUseCase
import com.dthurman.moviesaver.feature_onboarding.domain.repository.OnboardingRepository
import com.dthurman.moviesaver.feature_onboarding.domain.use_cases.CompleteOnboardingUseCase
import com.dthurman.moviesaver.feature_onboarding.domain.use_cases.HasCompletedOnboardingUseCase
import com.dthurman.moviesaver.feature_onboarding.domain.use_cases.OnboardingUseCases
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): MovieDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            MovieDatabase::class.java,
        ).build()
    }

    @Provides
    fun provideMovieDao(database: MovieDatabase): MovieDao {
        return database.movieDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        auth.useEmulator("10.0.2.2", 9099)
        return auth
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
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
    fun provideMovieInformationDataSource(): MovieInformationDataSource {
        return FakeMovieInformationDataSource()
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
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .build()
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
        userRepository: UserRepository
    ): AuthRepository {
        return com.dthurman.moviesaver.feature_auth.data.repository.FakeAuthRepository(
            userRepository as FakeUserRepository
        )
    }

    @Provides
    @Singleton
    fun provideMoviesUseCases(
        getUserMoviesUseCase: GetUserMoviesUseCase,
        getMovieByIdUseCase: GetMovieByIdUseCase,
        getPopularMoviesUseCase: GetPopularMoviesUseCase,
        searchMoviesUseCase: SearchMoviesUseCase,
        updateWatchlistStatusUseCase: UpdateWatchlistStatusUseCase,
        updateSeenStatusUseCase: UpdateSeenStatusUseCase,
        updateRatingUseCase: UpdateRatingUseCase,
        updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase
    ): MoviesUseCases {
        return MoviesUseCases(
            getUserMovies = getUserMoviesUseCase,
            getMovieById = getMovieByIdUseCase,
            getPopularMovies = getPopularMoviesUseCase,
            searchMovies = searchMoviesUseCase,
            updateWatchlistStatus = updateWatchlistStatusUseCase,
            updateSeenStatus = updateSeenStatusUseCase,
            updateRating = updateRatingUseCase,
            updateFavoriteStatus = updateFavoriteStatusUseCase
        )
    }

    @Provides
    @Singleton
    fun provideAiRecsUseCases(
        generateRecommendationsUseCase: GenerateAiRecommendationsUseCase,
        getSavedRecommendationsUseCase: GetSavedRecommendationsUseCase,
        acceptToWatchlistUseCase: AcceptRecommendationToWatchlistUseCase,
        acceptAsSeenUseCase: AcceptRecommendationAsSeenUseCase,
        rejectRecommendationUseCase: RejectRecommendationUseCase,
        getSeenMoviesCountUseCase: GetSeenMoviesCountUseCase
    ): AiRecsUseCases {
        return AiRecsUseCases(
            generateRecommendations = generateRecommendationsUseCase,
            getSavedRecommendations = getSavedRecommendationsUseCase,
            acceptToWatchlist = acceptToWatchlistUseCase,
            acceptAsSeen = acceptAsSeenUseCase,
            rejectRecommendation = rejectRecommendationUseCase,
            getSeenMoviesCount = getSeenMoviesCountUseCase
        )
    }

    @Provides
    @Singleton
    fun provideAuthUseCases(
        signInWithGoogleUseCase: SignInWithGoogleUseCase,
        signOutUseCase: SignOutUseCase,
        observeCurrentUserUseCase: ObserveCurrentUserUseCase,
        getGoogleCredentialUseCase: GetGoogleCredentialUseCase
    ): AuthUseCases {
        return AuthUseCases(
            signInWithGoogle = signInWithGoogleUseCase,
            signOut = signOutUseCase,
            observeCurrentUser = observeCurrentUserUseCase,
            getGoogleCredential = getGoogleCredentialUseCase
        )
    }

    @Provides
    @Singleton
    fun provideBillingUseCases(
        launchPurchaseFlowUseCase: LaunchPurchaseFlowUseCase,
        observePurchaseStateUseCase: ObservePurchaseStateUseCase,
        processPurchaseUseCase: ProcessPurchaseUseCase,
        resetPurchaseStateUseCase: ResetPurchaseStateUseCase
    ): BillingUseCases {
        return BillingUseCases(
            launchPurchaseFlow = launchPurchaseFlowUseCase,
            observePurchaseState = observePurchaseStateUseCase,
            processPurchase = processPurchaseUseCase,
            resetPurchaseState = resetPurchaseStateUseCase
        )
    }
    
    @Provides
    @Singleton
    fun provideRecommendationRepository(): RecommendationRepository {
        return FakeRecommendationRepository()
    }
    
    @Provides
    @Singleton
    fun provideAiRepository(
        recommendationRepository: RecommendationRepository
    ): AiRepository {
        return FakeAiRepository(recommendationRepository)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return FakeUserRepository()
    }
    
    @Provides
    @Singleton
    fun provideCreditsRepository(): CreditsRepository {
        return FakeCreditsRepository()
    }
    
    @Provides
    @Singleton
    fun provideAnalyticsTracker(): AnalyticsTracker {
        return FakeAnalyticsTracker()
    }
    
    @Provides
    @Singleton
    fun provideErrorLogger(): ErrorLogger {
        return FakeErrorLogger()
    }
    
    @Provides
    @Singleton
    fun provideOnboardingRepository(): OnboardingRepository {
        return com.dthurman.moviesaver.feature_onboarding.data.repository.FakeOnboardingRepository()
    }
    
    @Provides
    @Singleton
    fun provideOnboardingUseCases(
        hasCompletedOnboardingUseCase: HasCompletedOnboardingUseCase,
        completeOnboardingUseCase: CompleteOnboardingUseCase
    ): OnboardingUseCases {
        return OnboardingUseCases(
            hasCompletedOnboarding = hasCompletedOnboardingUseCase,
            completeOnboarding = completeOnboardingUseCase
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface TestAppBindingModule {

    @Singleton
    @Binds
    fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

    @Singleton
    @Binds
    fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository

    @Singleton
    @Binds
    fun bindLocalDataManager(impl: LocalDataManagerImpl): LocalDataManager
}
