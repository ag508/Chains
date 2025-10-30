package com.chain.messaging.core.messaging

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core messaging service that handles message composition, sending, and status tracking.
 * Integrates with blockchain and encryption services for secure message delivery.
 */
@Singleton
class MessagingService @Inject constructor(
    private val messageRepository: MessageRepository,
    private val blockchainManager: BlockchainManager,
    private val encryptionService: SignalEncryptionService
) {
    
    private val _messageStatusUpdates = MutableStateFlow<Map<String, MessageStatus>>(emptyMap())
    val messageStatusUpdates: StateFlow<Map<String, MessageStatus>> = _messageStatusUpdates.asStateFlow()
    
    /**
     * Composes and sends a text message
     */
    suspend fun sendTextMessage(
        chatId: String,
        senderId: String,
        content: String,
        replyTo: String? = null
    ): Result<Message> {
        return sendMessage(
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = MessageType.TEXT,
            replyTo = replyTo
        )
    }
    
    /**
     * Sends a message with specified type
     */
    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        content: String,
        type: MessageType,
        replyTo: String? = null
    ): Result<Message> {
        return try {
            // Create message with initial SENDING status
            val message = Message(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = senderId,
                content = content,
                type = type,
                timestamp = Date(),
                status = MessageStatus.SENDING,
                replyTo = replyTo,
                reactions = emptyList(),
                isEncrypted = true
            )
            
            // Save message locally first
            messageRepository.saveMessage(message).getOrThrow()
            
            // Update status to SENDING
            updateMessageStatus(message.id, MessageStatus.SENDING)
            
            // Encrypt message content
            val recipientAddress = org.signal.libsignal.protocol.SignalProtocolAddress(chatId, 1)
            val encryptedContent = encryptionService.encryptMessage(
                recipientAddress, 
                content.toByteArray()
            ).getOrThrow()
            
            // Send through blockchain
            val blockchainMessage = com.chain.messaging.core.blockchain.EncryptedMessage(
                content = encryptedContent.ciphertext.toString(Charsets.UTF_8),
                type = com.chain.messaging.core.blockchain.MessageType.valueOf(type.name),
                keyId = "signal_key", // Use a default key ID for Signal Protocol
                timestamp = message.timestamp.time
            )
            val txHash = blockchainManager.sendMessage(blockchainMessage)
            
            // Update status to SENT
            updateMessageStatus(message.id, MessageStatus.SENT)
            
            Result.success(message)
            
        } catch (e: Exception) {
            // Update status to FAILED if something went wrong
            updateMessageStatus(message.id, MessageStatus.FAILED)
            Result.failure(e)
        }
    }
    
    /**
     * Updates message status and notifies observers
     */
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        try {
            // Update in repository
            messageRepository.markMessagesAsRead(listOf(messageId))
            
            // Update local state
            val currentStatuses = _messageStatusUpdates.value.toMutableMap()
            currentStatuses[messageId] = status
            _messageStatusUpdates.value = currentStatuses
            
        } catch (e: Exception) {
            // Log error but don't throw to avoid breaking message flow
        }
    }
    
    /**
     * Marks messages as delivered
     */
    suspend fun markMessagesAsDelivered(messageIds: List<String>) {
        messageIds.forEach { messageId ->
            updateMessageStatus(messageId, MessageStatus.DELIVERED)
        }
    }
    
    /**
     * Marks messages as read
     */
    suspend fun markMessagesAsRead(messageIds: List<String>) {
        messageRepository.markMessagesAsRead(messageIds)
        messageIds.forEach { messageId ->
            updateMessageStatus(messageId, MessageStatus.READ)
        }
    }
    
    /**
     * Gets messages for a chat with pagination
     */
    suspend fun getMessages(chatId: String, limit: Int = 50, offset: Int = 0): List<Message> {
        return messageRepository.getMessages(chatId, limit, offset)
    }
    
    /**
     * Observes messages for a chat in real-time
     */
    fun observeMessages(chatId: String): Flow<List<Message>> {
        return messageRepository.observeMessages(chatId)
    }
    
    /**
     * Searches messages across all chats
     */
    suspend fun searchMessages(query: String): List<Message> {
        return messageRepository.searchMessages(query)
    }
    
    /**
     * Deletes messages
     */
    suspend fun deleteMessages(messageIds: List<String>): Result<Unit> {
        return messageRepository.deleteMessages(messageIds)
    }
    
    /**
     * Sends a system message (used for notifications like screenshot detection)
     */
    suspend fun sendSystemMessage(message: Message): Result<Unit> {
        return try {
            messageRepository.saveMessage(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sends a message (overloaded for offline queue compatibility)
     */
    suspend fun sendMessage(message: Message) {
        sendMessage(
            chatId = message.chatId,
            senderId = message.senderId,
            content = message.content,
            type = message.type,
            replyTo = message.replyTo
        ).getOrThrow()
    }
    
    /**
     * Gets recent messages for synchronization
     */
    suspend fun getRecentMessages(limit: Int = 100): List<Message> {
        return messageRepository.getRecentMessages(limit)
    }
    
    /**
     * Initialize messaging service
     */
    suspend fun initialize(): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send message with recipient ID and content
     */
    suspend fun sendMessage(recipientId: String, messageContent: String): Result<Message> {
        return sendMessage(
            chatId = recipientId,
            senderId = "current_user", // This should be the current user ID
            content = messageContent,
            type = MessageType.TEXT
        )
    }
    
    /**
     * Create a group chat
     */
    suspend fun createGroup(groupName: String, memberIds: List<String>): Result<com.chain.messaging.domain.model.Chat> {
        return try {
            val groupId = UUID.randomUUID().toString()
            val chat = com.chain.messaging.domain.model.Chat(
                id = groupId,
                name = groupName,
                type = com.chain.messaging.domain.model.ChatType.GROUP,
                participants = memberIds,
                createdAt = System.currentTimeMillis(),
                lastMessageAt = System.currentTimeMillis(),
                isArchived = false,
                isPinned = false,
                unreadCount = 0
            )
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send a group message
     */
    suspend fun sendGroupMessage(groupId: String, content: String): Result<Message> {
        return sendMessage(
            chatId = groupId,
            senderId = "current_user", // This should be the current user ID
            content = content,
            type = MessageType.TEXT
        )
    }
}