package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.profile.ProfileImageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for profile-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {
    
    @Provides
    @Singleton
    fun provideProfileImageManager(
        @ApplicationContext context: Context
    ): ProfileImageManager {
        return ProfileImageManager(context)
    }
}