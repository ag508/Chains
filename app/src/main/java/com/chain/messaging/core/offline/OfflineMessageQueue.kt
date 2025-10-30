package com.chain.messaging.core.offline

import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing offline message queuing and synchronization
 */
interface OfflineMessageQueue {
    /**
     * Queue a message for sending when connection is restored
     */
    suspend fun queueMessage(message: Message)
    
    /**
     * Get all queued messages
     */
    suspend fun getQueuedMessages(): List<QueuedMessage>
    
    /**
     * Remove a message from the queue
     */
    suspend fun removeFromQueue(messageId: String)
    
    /**
     * Clear all queued messages
     */
    suspend fun clearQueue()
    
    /**
     * Get queued messages as a flow for real-time updates
     */
    fun getQueuedMessagesFlow(): Flow<List<QueuedMessage>>
    
    /**
     * Process queued messages when connection is restored
     */
    suspend fun processQueuedMessages()
    
    /**
     * Get the number of queued messages
     */
    suspend fun getQueueSize(): Int
    
    /**
     * Initialize offline message queue
     */
    suspend fun initialize()
    
    /**
     * Enable offline mode
     */
    suspend fun enableOfflineMode()
}