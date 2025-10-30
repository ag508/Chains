package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.notification.NotificationActionHandler
import com.chain.messaging.core.notification.NotificationChannelManager
import com.chain.messaging.core.notification.NotificationManager
import com.chain.messaging.core.notification.NotificationPermissionHelper
import com.chain.messaging.core.notification.NotificationService
import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.domain.repository.SettingsRepository
import com.chain.messaging.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for notification system
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    @Singleton
    fun provideNotificationService(
        @ApplicationContext context: Context
    ): NotificationService {
        return NotificationService(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationChannelManager(
        @ApplicationContext context: Context
    ): NotificationChannelManager {
        return NotificationChannelManager(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationPermissionHelper(
        @ApplicationContext context: Context
    ): NotificationPermissionHelper {
        return NotificationPermissionHelper(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationActionHandler(): NotificationActionHandler {
        return NotificationActionHandler()
    }
    
    @Provides
    @Singleton
    fun provideNotificationManager(
        notificationService: NotificationService,
        notificationActionHandler: NotificationActionHandler,
        messagingService: MessagingService,
        messageRepository: MessageRepository,
        userRepository: UserRepository,
        settingsRepository: SettingsRepository
    ): NotificationManager {
        return NotificationManager(
            notificationService,
            notificationActionHandler,
            messagingService,
            messageRepository,
            userRepository,
            settingsRepository
        )
    }
}