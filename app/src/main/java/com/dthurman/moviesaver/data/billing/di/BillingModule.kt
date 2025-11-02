package com.dthurman.moviesaver.data.billing.di

import android.content.Context
import com.dthurman.moviesaver.data.billing.BillingManager
import com.dthurman.moviesaver.data.billing.BillingRepositoryImpl
import com.dthurman.moviesaver.domain.repository.BillingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingProviderModule {
    
    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context
    ): BillingManager {
        return BillingManager(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface BillingBindingModule {

    @Singleton
    @Binds
    fun bindBillingRepository(
        billingRepository: BillingRepositoryImpl
    ): BillingRepository
}

