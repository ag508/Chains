package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.QueuedMessageEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface QueuedMessageDao {
    
    @Query("SELECT * FROM queued_messages ORDER BY priority ASC, queuedAt ASC")
    suspend fun getAllQueuedMessages(): List<QueuedMessageEntity>
    
    @Query("SELECT * FROM queued_messages ORDER BY priority ASC, queuedAt ASC")
    fun getAllQueuedMessagesFlow(): Flow<List<QueuedMessageEntity>>
    
    @Query("SELECT * FROM queued_messages WHERE id = :id")
    suspend fun getQueuedMessageById(id: String): QueuedMessageEntity?
    
    @Query("SELECT * FROM queued_messages WHERE chatId = :chatId ORDER BY queuedAt ASC")
    suspend fun getQueuedMessagesByChatId(chatId: String): List<QueuedMessageEntity>
    
    @Query("SELECT COUNT(*) FROM queued_messages")
    suspend fun getQueueSize(): Int
    
    @Query("SELECT COUNT(*) FROM queued_messages WHERE retryCount >= maxRetries")
    suspend fun getFailedMessageCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueuedMessage(queuedMessage: QueuedMessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueuedMessages(queuedMessages: List<QueuedMessageEntity>)
    
    @Update
    suspend fun updateQueuedMessage(queuedMessage: QueuedMessageEntity)
    
    @Query("DELETE FROM queued_messages WHERE id = :id")
    suspend fun deleteQueuedMessage(id: String)
    
    @Query("DELETE FROM queued_messages WHERE chatId = :chatId")
    suspend fun deleteQueuedMessagesByChatId(chatId: String)
    
    @Query("DELETE FROM queued_messages")
    suspend fun deleteAllQueuedMessages()
    
    @Query("DELETE FROM queued_messages WHERE retryCount >= maxRetries")
    suspend fun deleteFailedMessages()
    
    @Query("DELETE FROM queued_messages WHERE queuedAt < :cutoffTime")
    suspend fun deleteOldQueuedMessages(cutoffTime: LocalDateTime)
}