package com.chain.messaging.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.chain.messaging.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for full-text search operations on messages
 */
@Dao
interface MessageSearchDao {
    
    /**
     * Search messages using full-text search
     */
    @Query("""
        SELECT m.* FROM messages m
        JOIN messages_fts fts ON m.id = fts.id
        WHERE messages_fts MATCH :query
        ORDER BY m.timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchMessages(query: String, limit: Int = 50, offset: Int = 0): List<MessageEntity>
    
    /**
     * Search messages in a specific chat using full-text search
     */
    @Query("""
        SELECT m.* FROM messages m
        JOIN messages_fts fts ON m.id = fts.id
        WHERE messages_fts MATCH :query AND m.chatId = :chatId
        ORDER BY m.timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchMessagesInChat(query: String, chatId: String, limit: Int = 50, offset: Int = 0): List<MessageEntity>
    
    /**
     * Search messages from a specific sender using full-text search
     */
    @Query("""
        SELECT m.* FROM messages m
        JOIN messages_fts fts ON m.id = fts.id
        WHERE messages_fts MATCH :query AND m.senderId = :senderId
        ORDER BY m.timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchMessagesFromSender(query: String, senderId: String, limit: Int = 50, offset: Int = 0): List<MessageEntity>
    
    /**
     * Search messages by type using full-text search
     */
    @Query("""
        SELECT m.* FROM messages m
        JOIN messages_fts fts ON m.id = fts.id
        WHERE messages_fts MATCH :query AND m.type = :messageType
        ORDER BY m.timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchMessagesByType(query: String, messageType: String, limit: Int = 50, offset: Int = 0): List<MessageEntity>
    
    /**
     * Search messages within a date range using full-text search
     */
    @Query("""
        SELECT m.* FROM messages m
        JOIN messages_fts fts ON m.id = fts.id
        WHERE messages_fts MATCH :query 
        AND m.timestamp BETWEEN :startTime AND :endTime
        ORDER BY m.timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchMessagesInDateRange(
        query: String, 
        startTime: Long, 
        endTime: Long, 
        limit: Int = 50, 
        offset: Int = 0
    ): List<MessageEntity>
    
    /**
     * Get search suggestions based on partial query
     */
    @Query("""
        SELECT DISTINCT substr(content, 1, 50) as suggestion
        FROM messages_fts
        WHERE content MATCH :partialQuery || '*'
        LIMIT 10
    """)
    suspend fun getSearchSuggestions(partialQuery: String): List<String>
    
    /**
     * Count search results
     */
    @Query("""
        SELECT COUNT(*) FROM messages m
        JOIN messages_fts fts ON m.id = fts.id
        WHERE messages_fts MATCH :query
    """)
    suspend fun countSearchResults(query: String): Int
    
    /**
     * Count search results in a specific chat
     */
    @Query("""
        SELECT COUNT(*) FROM messages m
        JOIN messages_fts fts ON m.id = fts.id
        WHERE messages_fts MATCH :query AND m.chatId = :chatId
    """)
    suspend fun countSearchResultsInChat(query: String, chatId: String): Int
    
    /**
     * Search messages with highlighting (returns content snippets)
     */
    @Query("""
        SELECT m.id, m.chatId, m.senderId, m.timestamp, m.type,
               snippet(messages_fts, '[', ']', '...', -1, 32) as highlightedContent
        FROM messages m
        JOIN messages_fts fts ON m.id = fts.id
        WHERE messages_fts MATCH :query
        ORDER BY m.timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchMessagesWithHighlights(query: String, limit: Int = 50, offset: Int = 0): List<MessageSearchResult>
    
    /**
     * Rebuild FTS index (for maintenance)
     */
    @Query("INSERT INTO messages_fts(messages_fts) VALUES('rebuild')")
    suspend fun rebuildSearchIndex()
    
    /**
     * Optimize FTS index (for maintenance)
     */
    @Query("INSERT INTO messages_fts(messages_fts) VALUES('optimize')")
    suspend fun optimizeSearchIndex()
}

/**
 * Data class for search results with highlighting
 */
data class MessageSearchResult(
    val id: String,
    val chatId: String,
    val senderId: String,
    val timestamp: Long,
    val type: String,
    val highlightedContent: String
)