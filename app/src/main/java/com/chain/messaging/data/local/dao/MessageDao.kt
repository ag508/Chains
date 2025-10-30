package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.MessageEntity
import com.chain.messaging.data.local.entity.MessageWithReactions
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Message operations
 */
@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesByChatId(chatId: String, limit: Int, offset: Int): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun observeMessagesByChatId(chatId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageByChatId(chatId: String): MessageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Query("UPDATE messages SET status = :status WHERE id IN (:messageIds)")
    suspend fun updateMessageStatus(messageIds: List<String>, status: String)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE id IN (:messageIds)")
    suspend fun deleteMessagesByIds(messageIds: List<String>)
    
    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)
    
    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId AND status != 'READ'")
    suspend fun getUnreadMessageCount(chatId: String): Int
    
    // Disappearing message queries
    @Query("SELECT * FROM messages WHERE isDisappearing = 1 AND expiresAt <= :currentTime")
    suspend fun getExpiredMessages(currentTime: Long): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE isDisappearing = 1 AND expiresAt <= :time AND expiresAt > :currentTime")
    suspend fun getMessagesExpiringBefore(time: Long, currentTime: Long = System.currentTimeMillis()): List<MessageEntity>
    
    @Query("DELETE FROM messages WHERE isDisappearing = 1 AND expiresAt <= :currentTime")
    suspend fun deleteExpiredMessages(currentTime: Long): Int
    
    @Query("SELECT * FROM messages WHERE isDisappearing = 1 AND expiresAt IS NOT NULL ORDER BY expiresAt ASC")
    fun observeDisappearingMessages(): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<MessageEntity>
    
    @Query("SELECT * FROM messages")
    suspend fun getAllMessages(): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getMessagesSince(since: Long): List<MessageEntity>
    
    // Methods for loading messages with reactions
    @Transaction
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesWithReactionsByChatId(chatId: String, limit: Int, offset: Int): List<MessageWithReactions>
    
    @Transaction
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageWithReactionsById(messageId: String): MessageWithReactions?
    
    @Transaction
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun observeMessagesWithReactionsByChatId(chatId: String): Flow<List<MessageWithReactions>>
    
    @Transaction
    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessagesWithReactions(query: String): List<MessageWithReactions>

    @Query("SELECT * FROM messages WHERE senderId != :currentUserId ORDER BY timestamp DESC")
    fun getIncomingMessages(currentUserId: String): Flow<List<MessageEntity>>
}