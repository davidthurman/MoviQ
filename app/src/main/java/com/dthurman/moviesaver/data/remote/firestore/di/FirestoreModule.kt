package com.dthurman.moviesaver.data.remote.firestore.di

import com.dthurman.moviesaver.data.remote.firestore.FirestoreSyncService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

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
        firestore: FirebaseFirestore
    ): FirestoreSyncService {
        return FirestoreSyncService(firestore)
    }
}

