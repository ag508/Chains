package com.chain.messaging.di

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.messaging.MessageStatusTracker
import com.chain.messaging.core.messaging.TypingIndicatorService
import com.chain.messaging.core.privacy.DisappearingMessageManager
import com.chain.messaging.domain.repository.ChatRepository
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.domain.repository.SettingsRepository
import com.chain.messaging.domain.usecase.AddReactionUseCase
import com.chain.messaging.domain.usecase.ArchiveChatUseCase
import com.chain.messaging.domain.usecase.DeleteChatUseCase
import com.chain.messaging.domain.usecase.GetChatsUseCase
import com.chain.messaging.domain.usecase.GetMessagesUseCase
import com.chain.messaging.domain.usecase.GetReactionsUseCase
import com.chain.messaging.domain.usecase.GetUserSettingsUseCase
import com.chain.messaging.domain.usecase.MessageStatusUseCase
import com.chain.messaging.domain.usecase.PinChatUseCase
import com.chain.messaging.domain.usecase.RemoveReactionUseCase
import com.chain.messaging.domain.usecase.SendMessageUseCase
import com.chain.messaging.domain.usecase.TypingIndicatorUseCase
import com.chain.messaging.domain.usecase.UpdateUserSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for use case dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    @Provides
    @Singleton
    fun provideGetChatsUseCase(
        chatRepository: ChatRepository
    ): GetChatsUseCase {
        return GetChatsUseCase(chatRepository)
    }
    
    @Provides
    @Singleton
    fun provideSendMessageUseCase(
        messagingService: MessagingService,
        disappearingMessageManager: com.chain.messaging.core.privacy.DisappearingMessageManager
    ): SendMessageUseCase {
        return SendMessageUseCase(messagingService, disappearingMessageManager)
    }
    
    @Provides
    @Singleton
    fun provideGetMessagesUseCase(
        messageRepository: MessageRepository
    ): GetMessagesUseCase {
        return GetMessagesUseCase(messageRepository)
    }
    
    @Provides
    @Singleton
    fun provideTypingIndicatorUseCase(
        typingIndicatorService: TypingIndicatorService
    ): TypingIndicatorUseCase {
        return TypingIndicatorUseCase(typingIndicatorService)
    }
    
    @Provides
    @Singleton
    fun provideMessageStatusUseCase(
        messageStatusTracker: MessageStatusTracker
    ): MessageStatusUseCase {
        return MessageStatusUseCase(messageStatusTracker)
    }
    
    @Provides
    @Singleton
    fun provideArchiveChatUseCase(
        chatRepository: ChatRepository
    ): ArchiveChatUseCase {
        return ArchiveChatUseCase(chatRepository)
    }
    
    @Provides
    @Singleton
    fun provideDeleteChatUseCase(
        chatRepository: ChatRepository,
        messageRepository: MessageRepository
    ): DeleteChatUseCase {
        return DeleteChatUseCase(chatRepository, messageRepository)
    }
    
    @Provides
    @Singleton
    fun providePinChatUseCase(
        chatRepository: ChatRepository
    ): PinChatUseCase {
        return PinChatUseCase(chatRepository)
    }
    
    @Provides
    @Singleton
    fun provideAddReactionUseCase(
        messageRepository: MessageRepository
    ): AddReactionUseCase {
        return AddReactionUseCase(messageRepository)
    }
    
    @Provides
    @Singleton
    fun provideRemoveReactionUseCase(
        messageRepository: MessageRepository
    ): RemoveReactionUseCase {
        return RemoveReactionUseCase(messageRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetReactionsUseCase(
        messageRepository: MessageRepository
    ): GetReactionsUseCase {
        return GetReactionsUseCase(messageRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetUserSettingsUseCase(
        settingsRepository: SettingsRepository
    ): GetUserSettingsUseCase {
        return GetUserSettingsUseCase(settingsRepository)
    }
    
    @Provides
    @Singleton
    fun provideUpdateUserSettingsUseCase(
        settingsRepository: SettingsRepository
    ): UpdateUserSettingsUseCase {
        return UpdateUserSettingsUseCase(settingsRepository)
    }
}