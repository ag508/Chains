package com.chain.messaging.di

import com.chain.messaging.data.local.ChainDatabase
import com.chain.messaging.data.local.dao.UserSettingsDao
import com.chain.messaging.data.repository.SettingsRepositoryImpl
import com.chain.messaging.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for settings-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    
    @Provides
    fun provideUserSettingsDao(database: ChainDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }
    
    @Provides
    @Singleton
    fun provideSettingsRepository(
        userSettingsDao: UserSettingsDao
    ): SettingsRepository {
        return SettingsRepositoryImpl(userSettingsDao)
    }
}