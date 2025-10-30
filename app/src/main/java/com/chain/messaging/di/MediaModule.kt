package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.media.MediaHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for media-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    
    @Provides
    @Singleton
    fun provideMediaHandler(
        @ApplicationContext context: Context
    ): MediaHandler {
        return MediaHandler(context)
    }
}