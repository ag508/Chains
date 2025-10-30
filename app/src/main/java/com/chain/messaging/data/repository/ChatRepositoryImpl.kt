package com.chain.messaging.data.repository

import com.chain.messaging.data.local.dao.ChatDao
import com.chain.messaging.data.local.entity.toDomain
import com.chain.messaging.data.local.entity.toEntity
import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatSettings
import com.chain.messaging.domain.model.ChatType
import com.chain.messaging.domain.model.GroupChat
import com.chain.messaging.domain.repository.ChatRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ChatRepository
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {
    
    private val gson = Gson()
    
    override suspend fun getChats(): List<Chat> {
        return try {
            chatDao.getAllChats().mapNotNull { entity ->
                try {
                    val participants = gson.fromJson(entity.participants, Array<String>::class.java)?.toList() ?: emptyList()
                    val admins = gson.fromJson(entity.admins, Array<String>::class.java)?.toList() ?: emptyList()
                    entity.toDomain(participants, admins)
                } catch (e: Exception) {
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getChatById(chatId: String): Chat? {
        return try {
            if (chatId.isBlank()) {
                return null
            }
            chatDao.getChatById(chatId)?.let { entity ->
                try {
                    val participants = gson.fromJson(entity.participants, Array<String>::class.java)?.toList() ?: emptyList()
                    val admins = gson.fromJson(entity.admins, Array<String>::class.java)?.toList() ?: emptyList()
                    entity.toDomain(participants, admins)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun createDirectChat(participantId: String): Result<Chat> {
        return try {
            if (participantId.isBlank()) {
                return Result.failure(IllegalArgumentException("Participant ID cannot be blank"))
            }
            
            val chatId = UUID.randomUUID().toString()
            val currentTime = Date()
            
            val chat = Chat(
                id = chatId,
                type = ChatType.DIRECT,
                name = "", // Direct chats don't have names
                participants = listOf(participantId), // Current user will be added by the calling code
                admins = emptyList(),
                settings = ChatSettings(),
                lastMessage = null,
                unreadCount = 0,
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            val participantsJson = gson.toJson(chat.participants)
            val adminsJson = gson.toJson(chat.admins)
            
            chatDao.insertChat(chat.toEntity(participantsJson, adminsJson))
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createGroupChat(name: String, participantIds: List<String>): Result<GroupChat> {
        return try {
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Group name cannot be blank"))
            }
            if (participantIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("Group must have at least one participant"))
            }
            val validParticipants = participantIds.filter { it.isNotBlank() }
            if (validParticipants.isEmpty()) {
                return Result.failure(IllegalArgumentException("All participant IDs are invalid"))
            }
            
            val chatId = UUID.randomUUID().toString()
            val currentTime = Date()
            
            val chat = Chat(
                id = chatId,
                type = ChatType.GROUP,
                name = name,
                participants = validParticipants,
                admins = emptyList(), // Creator will be added as admin by calling code
                settings = ChatSettings(),
                lastMessage = null,
                unreadCount = 0,
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            val participantsJson = gson.toJson(chat.participants)
            val adminsJson = gson.toJson(chat.admins)
            
            chatDao.insertChat(chat.toEntity(participantsJson, adminsJson))
            
            val groupChat = GroupChat(
                chat = chat,
                maxMembers = 100000,
                inviteLink = null,
                permissions = com.chain.messaging.domain.model.GroupPermissions(),
                description = null
            )
            
            Result.success(groupChat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateChatSettings(chatId: String, settings: ChatSettings): Result<Unit> {
        return try {
            val existingChat = chatDao.getChatById(chatId)
            if (existingChat != null) {
                val participants = gson.fromJson(existingChat.participants, Array<String>::class.java).toList()
                val admins = gson.fromJson(existingChat.admins, Array<String>::class.java).toList()
                val chat = existingChat.toDomain(participants, admins).copy(settings = settings)
                
                val participantsJson = gson.toJson(chat.participants)
                val adminsJson = gson.toJson(chat.admins)
                
                chatDao.updateChat(chat.toEntity(participantsJson, adminsJson))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Chat not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addMembersToGroup(chatId: String, memberIds: List<String>): Result<Unit> {
        return try {
            val existingChat = chatDao.getChatById(chatId)
            if (existingChat != null) {
                val currentParticipants = gson.fromJson(existingChat.participants, Array<String>::class.java).toList()
                val admins = gson.fromJson(existingChat.admins, Array<String>::class.java).toList()
                
                val updatedParticipants = (currentParticipants + memberIds).distinct()
                val participantsJson = gson.toJson(updatedParticipants)
                val adminsJson = gson.toJson(admins)
                
                val chat = existingChat.toDomain(updatedParticipants, admins)
                chatDao.updateChat(chat.toEntity(participantsJson, adminsJson))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Chat not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeMembersFromGroup(chatId: String, memberIds: List<String>): Result<Unit> {
        return try {
            val existingChat = chatDao.getChatById(chatId)
            if (existingChat != null) {
                val currentParticipants = gson.fromJson(existingChat.participants, Array<String>::class.java).toList()
                val admins = gson.fromJson(existingChat.admins, Array<String>::class.java).toList()
                
                val updatedParticipants = currentParticipants.filterNot { it in memberIds }
                val participantsJson = gson.toJson(updatedParticipants)
                val adminsJson = gson.toJson(admins)
                
                val chat = existingChat.toDomain(updatedParticipants, admins)
                chatDao.updateChat(chat.toEntity(participantsJson, adminsJson))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Chat not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun leaveGroup(chatId: String): Result<Unit> {
        return try {
            // TODO: Remove current user from group participants
            // This will be implemented when user authentication is added
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeChats(): Flow<List<Chat>> {
        return chatDao.observeAllChats().map { entities ->
            entities.mapNotNull { entity ->
                try {
                    val participants = gson.fromJson(entity.participants, Array<String>::class.java)?.toList() ?: emptyList()
                    val admins = gson.fromJson(entity.admins, Array<String>::class.java)?.toList() ?: emptyList()
                    entity.toDomain(participants, admins)
                } catch (e: Exception) {
                    null // Skip invalid entities
                }
            }
        }
    }
    
    override suspend fun saveChat(chat: Chat): Result<Unit> {
        return try {
            val participantsJson = gson.toJson(chat.participants)
            val adminsJson = gson.toJson(chat.admins)
            chatDao.insertChat(chat.toEntity(participantsJson, adminsJson))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            chatDao.deleteChatById(chatId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markChatAsRead(chatId: String): Result<Unit> {
        return try {
            chatDao.updateUnreadCount(chatId, 0)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUnreadMessageCount(chatId: String): Int {
        return try {
            chatDao.getChatById(chatId)?.unreadCount ?: 0
        } catch (e: Exception) {
            0
        }
    }
}