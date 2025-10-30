package com.chain.messaging.domain.repository

import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.GroupChat
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat-related operations.
 */
interface ChatRepository {
    
    /**
     * Get all chats for the current user
     */
    suspend fun getChats(): List<Chat>
    
    /**
     * Get chat by ID
     */
    suspend fun getChatById(chatId: String): Chat?
    
    /**
     * Create a new direct chat
     */
    suspend fun createDirectChat(participantId: String): Result<Chat>
    
    /**
     * Create a new group chat
     */
    suspend fun createGroupChat(name: String, participantIds: List<String>): Result<GroupChat>
    
    /**
     * Update chat settings
     */
    suspend fun updateChatSettings(chatId: String, settings: com.chain.messaging.domain.model.ChatSettings): Result<Unit>
    
    /**
     * Add members to group chat
     */
    suspend fun addMembersToGroup(chatId: String, memberIds: List<String>): Result<Unit>
    
    /**
     * Remove members from group chat
     */
    suspend fun removeMembersFromGroup(chatId: String, memberIds: List<String>): Result<Unit>
    
    /**
     * Leave group chat
     */
    suspend fun leaveGroup(chatId: String): Result<Unit>
    
    /**
     * Observe chats
     */
    fun observeChats(): Flow<List<Chat>>
    
    /**
     * Save chat to local database
     */
    suspend fun saveChat(chat: Chat): Result<Unit>
    
    /**
     * Delete chat by ID
     */
    suspend fun deleteChat(chatId: String): Result<Unit>
    
    /**
     * Mark chat as read (reset unread count to 0)
     */
    suspend fun markChatAsRead(chatId: String): Result<Unit>
    
    /**
     * Get unread message count for a specific chat
     */
    suspend fun getUnreadMessageCount(chatId: String): Int
}