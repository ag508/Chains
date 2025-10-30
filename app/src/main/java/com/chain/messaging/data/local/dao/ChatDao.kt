package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Chat operations
 */
@Dao
interface ChatDao {
    
    @Query("SELECT * FROM chats ORDER BY updatedAt DESC")
    suspend fun getAllChats(): List<ChatEntity>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?
    
    @Query("SELECT * FROM chats ORDER BY updatedAt DESC")
    fun observeAllChats(): Flow<List<ChatEntity>>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observeChatById(chatId: String): Flow<ChatEntity?>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observeChat(chatId: String): Flow<ChatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)
    
    @Update
    suspend fun updateChat(chat: ChatEntity)
    
    @Query("UPDATE chats SET unreadCount = :count WHERE id = :chatId")
    suspend fun updateUnreadCount(chatId: String, count: Int)
    
    @Query("UPDATE chats SET updatedAt = :timestamp WHERE id = :chatId")
    suspend fun updateLastActivity(chatId: String, timestamp: Long)
    
    @Delete
    suspend fun deleteChat(chat: ChatEntity)
    
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)
    
    @Query("SELECT COUNT(*) FROM chats")
    suspend fun getChatCount(): Int
}