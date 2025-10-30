package com.chain.messaging.data.local.storage

import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cache for messages to improve performance
 */
@Singleton
class MessageCache @Inject constructor() {
    
    private val messageCache = ConcurrentHashMap<String, Message>()
    private val chatMessagesCache = ConcurrentHashMap<String, MutableList<String>>()
    private val cacheMutex = Mutex()
    
    // Cache configuration
    private val maxCacheSize = 1000
    private val maxChatCacheSize = 100
    
    /**
     * Put a message in the cache
     */
    suspend fun putMessage(message: Message) {
        cacheMutex.withLock {
            // Add to message cache
            messageCache[message.id] = message
            
            // Add to chat messages cache
            val chatMessages = chatMessagesCache.getOrPut(message.chatId) { mutableListOf() }
            if (!chatMessages.contains(message.id)) {
                chatMessages.add(0, message.id) // Add to beginning for chronological order
                
                // Limit chat cache size
                if (chatMessages.size > maxChatCacheSize) {
                    val removedMessageId = chatMessages.removeAt(chatMessages.size - 1)
                    messageCache.remove(removedMessageId)
                }
            }
            
            // Limit overall cache size
            if (messageCache.size > maxCacheSize) {
                evictOldestMessages()
            }
        }
    }
    
    /**
     * Get a message from cache
     */
    fun getMessage(messageId: String): Message? {
        return messageCache[messageId]
    }
    
    /**
     * Get messages for a chat from cache
     */
    fun getMessages(chatId: String, limit: Int, offset: Int): List<Message> {
        val chatMessages = chatMessagesCache[chatId] ?: return emptyList()
        
        val startIndex = offset
        val endIndex = minOf(startIndex + limit, chatMessages.size)
        
        if (startIndex >= chatMessages.size) {
            return emptyList()
        }
        
        return chatMessages.subList(startIndex, endIndex)
            .mapNotNull { messageId -> messageCache[messageId] }
    }
    
    /**
     * Update message status in cache
     */
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        cacheMutex.withLock {
            val message = messageCache[messageId]
            if (message != null) {
                messageCache[messageId] = message.copy(status = status)
            }
        }
    }
    
    /**
     * Remove a message from cache
     */
    suspend fun removeMessage(messageId: String) {
        cacheMutex.withLock {
            val message = messageCache.remove(messageId)
            if (message != null) {
                val chatMessages = chatMessagesCache[message.chatId]
                chatMessages?.remove(messageId)
                
                // Clean up empty chat caches
                if (chatMessages?.isEmpty() == true) {
                    chatMessagesCache.remove(message.chatId)
                }
            }
        }
    }
    
    /**
     * Remove all messages for a chat from cache
     */
    suspend fun removeChatMessages(chatId: String) {
        cacheMutex.withLock {
            val chatMessages = chatMessagesCache.remove(chatId)
            chatMessages?.forEach { messageId ->
                messageCache.remove(messageId)
            }
        }
    }
    
    /**
     * Clear entire cache
     */
    suspend fun clearCache() {
        cacheMutex.withLock {
            messageCache.clear()
            chatMessagesCache.clear()
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            totalMessages = messageCache.size,
            totalChats = chatMessagesCache.size,
            maxCacheSize = maxCacheSize,
            maxChatCacheSize = maxChatCacheSize
        )
    }
    
    /**
     * Check if message is cached
     */
    fun isMessageCached(messageId: String): Boolean {
        return messageCache.containsKey(messageId)
    }
    
    /**
     * Check if chat has cached messages
     */
    fun hasCachedMessages(chatId: String): Boolean {
        return chatMessagesCache.containsKey(chatId) && 
               chatMessagesCache[chatId]?.isNotEmpty() == true
    }
    
    /**
     * Get cached message count for a chat
     */
    fun getCachedMessageCount(chatId: String): Int {
        return chatMessagesCache[chatId]?.size ?: 0
    }
    
    /**
     * Evict oldest messages when cache is full
     */
    private fun evictOldestMessages() {
        // Simple LRU eviction - remove 10% of cache
        val evictionCount = maxCacheSize / 10
        val messagesToEvict = messageCache.entries
            .sortedBy { it.value.timestamp }
            .take(evictionCount)
        
        messagesToEvict.forEach { (messageId, message) ->
            messageCache.remove(messageId)
            val chatMessages = chatMessagesCache[message.chatId]
            chatMessages?.remove(messageId)
            
            // Clean up empty chat caches
            if (chatMessages?.isEmpty() == true) {
                chatMessagesCache.remove(message.chatId)
            }
        }
    }
    
    /**
     * Preload messages for a chat
     */
    suspend fun preloadChatMessages(chatId: String, messages: List<Message>) {
        cacheMutex.withLock {
            val chatMessages = chatMessagesCache.getOrPut(chatId) { mutableListOf() }
            
            messages.forEach { message ->
                messageCache[message.id] = message
                if (!chatMessages.contains(message.id)) {
                    chatMessages.add(message.id)
                }
            }
            
            // Sort by timestamp (newest first)
            chatMessages.sortByDescending { messageId ->
                messageCache[messageId]?.timestamp?.time ?: 0L
            }
            
            // Limit chat cache size
            if (chatMessages.size > maxChatCacheSize) {
                val excess = chatMessages.size - maxChatCacheSize
                repeat(excess) {
                    val removedMessageId = chatMessages.removeAt(chatMessages.size - 1)
                    messageCache.remove(removedMessageId)
                }
            }
        }
    }
}

/**
 * Cache statistics data class
 */
data class CacheStats(
    val totalMessages: Int,
    val totalChats: Int,
    val maxCacheSize: Int,
    val maxChatCacheSize: Int
)