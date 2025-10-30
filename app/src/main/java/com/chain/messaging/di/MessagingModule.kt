package com.chain.messaging.di

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.messaging.MessageStatusTracker
import com.chain.messaging.core.messaging.TypingIndicatorService
import com.chain.messaging.domain.repository.MessageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for messaging service dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object MessagingModule {
    
    @Provides
    @Singleton
    fun provideMessagingService(
        messageRepository: MessageRepository,
        blockchainManager: BlockchainManager,
        encryptionService: SignalEncryptionService
    ): MessagingService {
        return MessagingService(messageRepository, blockchainManager, encryptionService)
    }
    
    @Provides
    @Singleton
    fun provideTypingIndicatorService(): TypingIndicatorService {
        return TypingIndicatorService()
    }
    
    @Provides
    @Singleton
    fun provideMessageStatusTracker(): MessageStatusTracker {
        return MessageStatusTracker()
    }
}