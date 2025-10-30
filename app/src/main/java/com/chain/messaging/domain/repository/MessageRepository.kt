package com.chain.messaging.domain.repository

import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for message-related operations.
 */
interface MessageRepository {
    
    /**
     * Send a message
     */
    suspend fun sendMessage(message: Message): Result<Unit>
    
    /**
     * Get messages for a specific chat
     */
    suspend fun getMessages(chatId: String, limit: Int, offset: Int): List<Message>
    
    /**
     * Get message by ID
     */
    suspend fun getMessageById(messageId: String): Message?
    
    /**
     * Search messages across all chats
     */
    suspend fun searchMessages(query: String): List<Message>
    
    /**
     * Delete messages
     */
    suspend fun deleteMessages(messageIds: List<String>): Result<Unit>
    
    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(messageIds: List<String>): Result<Unit>
    
    /**
     * Observe messages for a specific chat
     */
    fun observeMessages(chatId: String): Flow<List<Message>>
    
    /**
     * Save message to local database
     */
    suspend fun saveMessage(message: Message): Result<Unit>
    
    /**
     * Get expired messages that should be deleted
     */
    suspend fun getExpiredMessages(currentTime: Long): List<Message>
    
    /**
     * Get messages that will expire before the specified time
     */
    suspend fun getMessagesExpiringBefore(time: Long): List<Message>
    
    /**
     * Delete a single message by ID
     */
    suspend fun deleteMessage(messageId: String): Result<Unit>
    
    /**
     * Delete expired messages and return count of deleted messages
     */
    suspend fun deleteExpiredMessages(currentTime: Long): Int
    
    /**
     * Observe all disappearing messages
     */
    fun observeDisappearingMessages(): Flow<List<Message>>
    
    /**
     * Get recent messages for synchronization
     */
    suspend fun getRecentMessages(limit: Int): List<Message>
    
    /**
     * Update an existing message
     */
    suspend fun updateMessage(message: Message): Result<Unit>
    
    /**
     * Delete all messages in a specific chat
     */
    suspend fun deleteMessagesByChat(chatId: String): Result<Unit>
    
    /**
     * Add a reaction to a message
     * If the user has already reacted with the same emoji, it removes the reaction
     * Otherwise, it adds the new reaction
     */
    suspend fun addReaction(messageId: String, userId: String, emoji: String): Result<Unit>
    
    /**
     * Remove a specific reaction from a message
     */
    suspend fun removeReaction(messageId: String, userId: String, emoji: String): Result<Unit>
    
    /**
     * Get all reactions for a specific message
     */
    suspend fun getReactions(messageId: String): List<com.chain.messaging.domain.model.Reaction>
    
    /**
     * Observe reactions for a specific message
     */
    fun observeReactions(messageId: String): Flow<List<com.chain.messaging.domain.model.Reaction>>
    
    /**
     * Check if a user has reacted to a message with a specific emoji
     */
    suspend fun hasUserReacted(messageId: String, userId: String, emoji: String): Boolean
    
    /**
     * Get incoming messages (messages received but not sent by current user)
     */
    suspend fun getIncomingMessages(): List<Message>
}