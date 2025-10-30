package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.privacy.*
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.messaging.MessagingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for privacy-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object PrivacyModule {
    
    @Provides
    @Singleton
    fun provideScreenshotDetector(
        @ApplicationContext context: Context
    ): ScreenshotDetector {
        return ScreenshotDetectorImpl(context)
    }
    
    @Provides
    @Singleton
    fun provideDisappearingMessageManager(
        @ApplicationContext context: Context,
        messageRepository: MessageRepository,
        blockchainManager: BlockchainManager,
        screenshotDetector: ScreenshotDetector
    ): DisappearingMessageManager {
        return DisappearingMessageManagerImpl(
            context,
            messageRepository,
            blockchainManager,
            screenshotDetector
        )
    }
    
    @Provides
    @Singleton
    fun provideDisappearingMessageNotificationService(
        messagingService: MessagingService,
        disappearingMessageManager: DisappearingMessageManager,
        screenshotDetector: ScreenshotDetector
    ): DisappearingMessageNotificationService {
        return DisappearingMessageNotificationServiceImpl(
            messagingService,
            disappearingMessageManager,
            screenshotDetector
        )
    }
    
    @Provides
    @Singleton
    fun providePrivacyEventHandler(
        notificationService: com.chain.messaging.core.notification.NotificationService
    ): PrivacyEventHandler {
        return PrivacyEventHandlerImpl(notificationService)
    }
}