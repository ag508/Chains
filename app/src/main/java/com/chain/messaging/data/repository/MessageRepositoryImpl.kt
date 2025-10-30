package com.chain.messaging.data.repository

import android.util.Log
import com.chain.messaging.core.auth.AuthenticationService
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.data.local.dao.MessageDao
import com.chain.messaging.data.local.dao.ReactionDao
import com.chain.messaging.data.local.entity.toDomain
import com.chain.messaging.data.local.entity.toEntity
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.Reaction
import com.chain.messaging.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.signal.libsignal.protocol.SignalProtocolAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MessageRepository
 */
@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val reactionDao: ReactionDao,
    private val blockchainManager: BlockchainManager,
    private val encryptionService: SignalEncryptionService,
    private val authenticationService: AuthenticationService
) : MessageRepository {
    
    companion object {
        private const val TAG = "MessageRepositoryImpl"
    }
    
    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            Log.d(TAG, "Sending message: ${message.id}")
            
            // Get current user to verify sender
            val currentUser = authenticationService.getCurrentUser()
            if (currentUser == null) {
                Log.e(TAG, "Cannot send message: No authenticated user")
                return Result.failure(IllegalStateException("No authenticated user"))
            }
            
            // Verify the message sender matches current user
            if (message.senderId != currentUser.userId) {
                Log.e(TAG, "Cannot send message: Sender ID mismatch")
                return Result.failure(IllegalStateException("Sender ID mismatch"))
            }
            
            // Update message status to SENDING and save locally first
            val sendingMessage = message.copy(status = MessageStatus.SENDING)
            messageDao.insertMessage(sendingMessage.toEntity())
            Log.d(TAG, "Message saved locally with SENDING status")
            
            // Check if connected to blockchain network
            if (!blockchainManager.isConnected()) {
                Log.w(TAG, "Not connected to blockchain network, message will be queued")
                // Message remains in SENDING status and will be retried when connection is restored
                return Result.success(Unit)
            }
            
            // Encrypt message content for the recipient
            val recipientAddress = SignalProtocolAddress(getRecipientId(message), 1)
            val messageContent = message.content.toByteArray()
            
            val encryptionResult = encryptionService.encryptMessage(recipientAddress, messageContent)
            if (encryptionResult.isFailure) {
                Log.e(TAG, "Failed to encrypt message", encryptionResult.exceptionOrNull())
                // Update message status to FAILED
                val failedMessage = message.copy(status = MessageStatus.FAILED)
                messageDao.updateMessage(failedMessage.toEntity())
                return Result.failure(encryptionResult.exceptionOrNull() ?: Exception("Encryption failed"))
            }
            
            val encryptedMessage = encryptionResult.getOrThrow()
            Log.d(TAG, "Message encrypted successfully")
            
            // Send encrypted message through blockchain
            val blockchainResult = try {
                val transactionHash = blockchainManager.sendMessage(
                    recipientId = getRecipientId(message),
                    encryptedContent = String(encryptedMessage.ciphertext),
                    messageType = message.type.name
                )
                
                Log.d(TAG, "Message sent to blockchain with transaction hash: $transactionHash")
                
                // Update message status to SENT
                val sentMessage = message.copy(status = MessageStatus.SENT)
                messageDao.updateMessage(sentMessage.toEntity())
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message through blockchain", e)
                
                // Update message status to FAILED
                val failedMessage = message.copy(status = MessageStatus.FAILED)
                messageDao.updateMessage(failedMessage.toEntity())
                
                Result.failure(e)
            }
            
            blockchainResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error sending message", e)
            
            // Try to update message status to FAILED if possible
            try {
                val failedMessage = message.copy(status = MessageStatus.FAILED)
                messageDao.updateMessage(failedMessage.toEntity())
            } catch (updateException: Exception) {
                Log.e(TAG, "Failed to update message status to FAILED", updateException)
            }
            
            Result.failure(e)
        }
    }
    
    /**
     * Extract recipient ID from message based on chat type
     */
    private fun getRecipientId(message: Message): String {
        // For direct messages, the recipient is the other participant in the chat
        // For group messages, this would be handled differently
        // For now, we'll use a simple approach where chatId contains recipient info
        
        // If it's a direct chat, the chatId might be in format "user1_user2"
        // We need to extract the recipient (not the sender)
        val chatParticipants = message.chatId.split("_")
        return if (chatParticipants.size == 2) {
            // Direct chat - return the participant that's not the sender
            chatParticipants.firstOrNull { it != message.senderId } ?: message.chatId
        } else {
            // Group chat or other format - use chatId as recipient
            message.chatId
        }
    }
    
    override suspend fun getMessages(chatId: String, limit: Int, offset: Int): List<Message> {
        return try {
            if (chatId.isBlank() || limit <= 0) {
                return emptyList()
            }
            messageDao.getMessagesWithReactionsByChatId(chatId, limit, offset).mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to convert message entity to domain", e)
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get messages", e)
            emptyList()
        }
    }
    
    override suspend fun getMessageById(messageId: String): Message? {
        return try {
            messageDao.getMessageWithReactionsById(messageId)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun searchMessages(query: String): List<Message> {
        return try {
            if (query.isBlank()) {
                return emptyList()
            }
            messageDao.searchMessagesWithReactions(query).mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to convert message entity to domain", e)
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search messages", e)
            emptyList()
        }
    }
    
    override suspend fun deleteMessages(messageIds: List<String>): Result<Unit> {
        return try {
            messageDao.deleteMessagesByIds(messageIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markMessagesAsRead(messageIds: List<String>): Result<Unit> {
        return try {
            messageDao.updateMessageStatus(messageIds, "READ")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeMessages(chatId: String): Flow<List<Message>> {
        return if (chatId.isBlank()) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            messageDao.observeMessagesWithReactionsByChatId(chatId).map { messagesWithReactions ->
                messagesWithReactions.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to convert message entity to domain", e)
                        null // Skip invalid entities
                    }
                }
            }
        }
    }
    
    override suspend fun saveMessage(message: Message): Result<Unit> {
        return try {
            messageDao.insertMessage(message.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Disappearing message methods
    override suspend fun getExpiredMessages(currentTime: Long): List<Message> {
        return try {
            messageDao.getExpiredMessages(currentTime).mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to convert expired message entity to domain", e)
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get expired messages", e)
            emptyList()
        }
    }
    
    override suspend fun getMessagesExpiringBefore(time: Long): List<Message> {
        return try {
            messageDao.getMessagesExpiringBefore(time).mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to convert expiring message entity to domain", e)
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get messages expiring before time", e)
            emptyList()
        }
    }
    
    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            messageDao.deleteMessagesByIds(listOf(messageId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteExpiredMessages(currentTime: Long): Int {
        return try {
            messageDao.deleteExpiredMessages(currentTime)
        } catch (e: Exception) {
            0
        }
    }
    
    override fun observeDisappearingMessages(): Flow<List<Message>> {
        return messageDao.observeDisappearingMessages().map { entities ->
            entities.mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to convert disappearing message entity to domain", e)
                    null // Skip invalid entities
                }
            }
        }
    }
    
    override suspend fun getRecentMessages(limit: Int): List<Message> {
        return try {
            if (limit <= 0) {
                return emptyList()
            }
            messageDao.getRecentMessages(limit).mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to convert recent message entity to domain", e)
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get recent messages", e)
            emptyList()
        }
    }
    
    override suspend fun updateMessage(message: Message): Result<Unit> {
        return try {
            messageDao.updateMessage(message.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteMessagesByChat(chatId: String): Result<Unit> {
        return try {
            messageDao.deleteMessagesByChatId(chatId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addReaction(messageId: String, userId: String, emoji: String): Result<Unit> {
        return try {
            Log.d(TAG, "Adding reaction: messageId=$messageId, userId=$userId, emoji=$emoji")
            
            // Check if the user has already reacted with this emoji
            val existingReaction = reactionDao.getSpecificReaction(messageId, userId, emoji)
            
            if (existingReaction != null) {
                // Remove the existing reaction (toggle behavior)
                Log.d(TAG, "Removing existing reaction")
                reactionDao.deleteSpecificReaction(messageId, userId, emoji)
            } else {
                // Add new reaction
                Log.d(TAG, "Adding new reaction")
                val reaction = Reaction(
                    userId = userId,
                    emoji = emoji,
                    timestamp = java.util.Date()
                )
                reactionDao.insertReaction(reaction.toEntity(messageId))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add reaction", e)
            Result.failure(e)
        }
    }
    
    override suspend fun removeReaction(messageId: String, userId: String, emoji: String): Result<Unit> {
        return try {
            Log.d(TAG, "Removing reaction: messageId=$messageId, userId=$userId, emoji=$emoji")
            reactionDao.deleteSpecificReaction(messageId, userId, emoji)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove reaction", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getReactions(messageId: String): List<Reaction> {
        return try {
            if (messageId.isBlank()) {
                return emptyList()
            }
            reactionDao.getReactionsByMessageId(messageId).mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to convert reaction entity to domain", e)
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get reactions", e)
            emptyList()
        }
    }
    
    override fun observeReactions(messageId: String): Flow<List<Reaction>> {
        return if (messageId.isBlank()) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            reactionDao.observeReactionsByMessageId(messageId).map { entities ->
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to convert reaction entity to domain", e)
                        null // Skip invalid entities
                    }
                }
            }
        }
    }
    
    override suspend fun hasUserReacted(messageId: String, userId: String, emoji: String): Boolean {
        return try {
            reactionDao.hasUserReacted(messageId, userId, emoji)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check user reaction", e)
            false
        }
    }
    
    override suspend fun getIncomingMessages(): List<Message> {
        return try {
            // Get current user to filter out their own messages
            val currentUser = authenticationService.getCurrentUser()
            if (currentUser == null) {
                Log.w(TAG, "No authenticated user, returning empty list")
                return emptyList()
            }
            
            // Get all recent messages and filter out messages sent by current user
            messageDao.getAllMessages().mapNotNull { entity ->
                try {
                    val message = entity.toDomain()
                    // Only return messages not sent by current user
                    if (message.senderId != currentUser.userId) {
                        message
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to convert message entity to domain", e)
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get incoming messages", e)
            emptyList()
        }
    }
}