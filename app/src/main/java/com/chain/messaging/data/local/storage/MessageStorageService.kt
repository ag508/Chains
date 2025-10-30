package com.chain.messaging.data.local.storage

import com.chain.messaging.data.local.dao.MessageDao
import com.chain.messaging.data.local.dao.MessageSearchDao
import com.chain.messaging.data.local.dao.ReactionDao
import com.chain.messaging.data.local.dao.MediaDao
import com.chain.messaging.data.local.entity.MessageEntity
import com.chain.messaging.data.local.entity.ReactionEntity
import com.chain.messaging.data.local.entity.MediaEntity
import com.chain.messaging.data.local.entity.toDomain
import com.chain.messaging.data.local.entity.toEntity
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.core.security.MessageEncryption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing message storage and retrieval with encryption at rest
 */
@Singleton
class MessageStorageService @Inject constructor(
    private val messageDao: MessageDao,
    private val messageSearchDao: MessageSearchDao,
    private val reactionDao: ReactionDao,
    private val mediaDao: MediaDao,
    private val messageEncryption: MessageEncryption,
    private val messageCache: MessageCache
) {
    
    /**
     * Store a message with encryption at rest
     */
    suspend fun storeMessage(message: Message): Result<Unit> {
        return try {
            // Encrypt message content before storing
            val encryptedContent = if (message.isEncrypted) {
                messageEncryption.encryptForStorage(message.content)
            } else {
                message.content
            }
            
            val messageEntity = message.copy(content = encryptedContent).toEntity()
            messageDao.insertMessage(messageEntity)
            
            // Store reactions if any
            message.reactions.forEach { reaction ->
                val reactionEntity = reaction.toEntity(message.id)
                reactionDao.insertReaction(reactionEntity)
            }
            
            // Update cache
            messageCache.putMessage(message)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Store multiple messages in batch
     */
    suspend fun storeMessages(messages: List<Message>): Result<Unit> {
        return try {
            val messageEntities = messages.map { message ->
                val encryptedContent = if (message.isEncrypted) {
                    messageEncryption.encryptForStorage(message.content)
                } else {
                    message.content
                }
                message.copy(content = encryptedContent).toEntity()
            }
            
            messageDao.insertMessages(messageEntities)
            
            // Store all reactions
            val reactionEntities = messages.flatMap { message ->
                message.reactions.map { reaction ->
                    reaction.toEntity(message.id)
                }
            }
            if (reactionEntities.isNotEmpty()) {
                reactionDao.insertReactions(reactionEntities)
            }
            
            // Update cache
            messages.forEach { messageCache.putMessage(it) }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Retrieve messages for a chat with pagination
     */
    suspend fun getMessages(chatId: String, limit: Int, offset: Int): Result<List<Message>> {
        return try {
            // Check cache first
            val cachedMessages = messageCache.getMessages(chatId, limit, offset)
            if (cachedMessages.isNotEmpty()) {
                return Result.success(cachedMessages)
            }
            
            val messageEntities = messageDao.getMessagesByChatId(chatId, limit, offset)
            val messages = messageEntities.map { entity ->
                val decryptedContent = if (entity.isEncrypted) {
                    messageEncryption.decryptFromStorage(entity.content)
                } else {
                    entity.content
                }
                
                val reactions = reactionDao.getReactionsByMessageId(entity.id).map { it.toDomain() }
                entity.copy(content = decryptedContent).toDomain().copy(reactions = reactions)
            }
            
            // Update cache
            messages.forEach { messageCache.putMessage(it) }
            
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe messages for a chat with real-time updates
     */
    fun observeMessages(chatId: String): Flow<List<Message>> {
        return messageDao.observeMessagesByChatId(chatId).map { messageEntities ->
            messageEntities.map { entity ->
                val decryptedContent = if (entity.isEncrypted) {
                    messageEncryption.decryptFromStorage(entity.content)
                } else {
                    entity.content
                }
                
                val reactions = reactionDao.getReactionsByMessageId(entity.id).map { it.toDomain() }
                entity.copy(content = decryptedContent).toDomain().copy(reactions = reactions)
            }
        }
    }
    
    /**
     * Get a specific message by ID
     */
    suspend fun getMessage(messageId: String): Result<Message?> {
        return try {
            // Check cache first
            val cachedMessage = messageCache.getMessage(messageId)
            if (cachedMessage != null) {
                return Result.success(cachedMessage)
            }
            
            val messageEntity = messageDao.getMessageById(messageId)
            if (messageEntity == null) {
                return Result.success(null)
            }
            
            val decryptedContent = if (messageEntity.isEncrypted) {
                messageEncryption.decryptFromStorage(messageEntity.content)
            } else {
                messageEntity.content
            }
            
            val reactions = reactionDao.getReactionsByMessageId(messageId).map { it.toDomain() }
            val message = messageEntity.copy(content = decryptedContent).toDomain().copy(reactions = reactions)
            
            // Update cache
            messageCache.putMessage(message)
            
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update message status
     */
    suspend fun updateMessageStatus(messageIds: List<String>, status: MessageStatus): Result<Unit> {
        return try {
            messageDao.updateMessageStatus(messageIds, status.name)
            
            // Update cache
            messageIds.forEach { messageId ->
                messageCache.updateMessageStatus(messageId, status)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete messages
     */
    suspend fun deleteMessages(messageIds: List<String>): Result<Unit> {
        return try {
            messageDao.deleteMessagesByIds(messageIds)
            
            // Remove from cache
            messageIds.forEach { messageId ->
                messageCache.removeMessage(messageId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search messages using full-text search
     */
    suspend fun searchMessages(
        query: String,
        chatId: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<Message>> {
        return try {
            val messageEntities = if (chatId != null) {
                messageSearchDao.searchMessagesInChat(query, chatId, limit, offset)
            } else {
                messageSearchDao.searchMessages(query, limit, offset)
            }
            
            val messages = messageEntities.map { entity ->
                val decryptedContent = if (entity.isEncrypted) {
                    messageEncryption.decryptFromStorage(entity.content)
                } else {
                    entity.content
                }
                
                val reactions = reactionDao.getReactionsByMessageId(entity.id).map { it.toDomain() }
                entity.copy(content = decryptedContent).toDomain().copy(reactions = reactions)
            }
            
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get search suggestions
     */
    suspend fun getSearchSuggestions(partialQuery: String): Result<List<String>> {
        return try {
            val suggestions = messageSearchDao.getSearchSuggestions(partialQuery)
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Count search results
     */
    suspend fun countSearchResults(query: String, chatId: String? = null): Result<Int> {
        return try {
            val count = if (chatId != null) {
                messageSearchDao.countSearchResultsInChat(query, chatId)
            } else {
                messageSearchDao.countSearchResults(query)
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get unread message count for a chat
     */
    suspend fun getUnreadMessageCount(chatId: String): Result<Int> {
        return try {
            val count = messageDao.getUnreadMessageCount(chatId)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get last message for a chat
     */
    suspend fun getLastMessage(chatId: String): Result<Message?> {
        return try {
            val messageEntity = messageDao.getLastMessageByChatId(chatId)
            if (messageEntity == null) {
                return Result.success(null)
            }
            
            val decryptedContent = if (messageEntity.isEncrypted) {
                messageEncryption.decryptFromStorage(messageEntity.content)
            } else {
                messageEntity.content
            }
            
            val reactions = reactionDao.getReactionsByMessageId(messageEntity.id).map { it.toDomain() }
            val message = messageEntity.copy(content = decryptedContent).toDomain().copy(reactions = reactions)
            
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean up expired disappearing messages
     */
    suspend fun cleanupExpiredMessages(): Result<Int> {
        return try {
            val currentTime = System.currentTimeMillis()
            val expiredMessages = messageDao.getExpiredMessages(currentTime)
            
            // Remove from cache
            expiredMessages.forEach { message ->
                messageCache.removeMessage(message.id)
            }
            
            val deletedCount = messageDao.deleteExpiredMessages(currentTime)
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Rebuild search index for maintenance
     */
    suspend fun rebuildSearchIndex(): Result<Unit> {
        return try {
            messageSearchDao.rebuildSearchIndex()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Optimize search index for better performance
     */
    suspend fun optimizeSearchIndex(): Result<Unit> {
        return try {
            messageSearchDao.optimizeSearchIndex()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}