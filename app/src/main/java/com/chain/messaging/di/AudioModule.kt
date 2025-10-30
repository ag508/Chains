package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.audio.VoiceMessageProcessor
import com.chain.messaging.core.audio.VoicePlayer
import com.chain.messaging.core.audio.VoiceRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for audio components
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    
    @Provides
    @Singleton
    fun provideVoiceRecorder(
        @ApplicationContext context: Context
    ): VoiceRecorder {
        return VoiceRecorder(context)
    }
    
    @Provides
    @Singleton
    fun provideVoicePlayer(
        @ApplicationContext context: Context
    ): VoicePlayer {
        return VoicePlayer(context)
    }
    
    @Provides
    @Singleton
    fun provideVoiceMessageProcessor(
        @ApplicationContext context: Context
    ): VoiceMessageProcessor {
        return VoiceMessageProcessor(context)
    }
}