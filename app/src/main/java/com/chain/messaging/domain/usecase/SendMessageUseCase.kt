package com.chain.messaging.domain.usecase

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.privacy.DisappearingMessageManager
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageType
import javax.inject.Inject

/**
 * Use case for sending messages.
 * Encapsulates the business logic for message creation and sending.
 */
class SendMessageUseCase @Inject constructor(
    private val messagingService: MessagingService,
    private val disappearingMessageManager: DisappearingMessageManager
) {
    
    suspend operator fun invoke(
        chatId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        replyToMessageId: String? = null
    ): Result<Message> {
        // For now, use a default sender ID - in production this would come from auth service
        val senderId = "current_user"
        
        return sendMessageWithDisappearing(
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = messageType,
            replyTo = replyToMessageId
        )
    }
    
    suspend fun execute(
        chatId: String,
        senderId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        replyTo: String? = null
    ): Result<Message> {
        return messagingService.sendMessage(
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = type,
            replyTo = replyTo
        )
    }
    
    suspend fun sendTextMessage(
        chatId: String,
        senderId: String,
        content: String,
        replyTo: String? = null
    ): Result<Message> {
        return sendMessageWithDisappearing(
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = MessageType.TEXT,
            replyTo = replyTo
        )
    }
    
    private suspend fun sendMessageWithDisappearing(
        chatId: String,
        senderId: String,
        content: String,
        type: MessageType,
        replyTo: String? = null
    ): Result<Message> {
        return try {
            // First send the message normally
            val result = messagingService.sendMessage(
                chatId = chatId,
                senderId = senderId,
                content = content,
                type = type,
                replyTo = replyTo
            )
            
            // If successful, process for disappearing message expiration
            result.fold(
                onSuccess = { message ->
                    val processedMessage = disappearingMessageManager.processMessageForExpiration(message, chatId)
                    
                    // If the message was modified (made disappearing), save the updated version
                    if (processedMessage != message) {
                        messagingService.sendSystemMessage(processedMessage)
                    }
                    
                    Result.success(processedMessage)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}