package com.chain.messaging.core.group

import com.chain.messaging.core.util.TimeUtils
import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatType
import com.chain.messaging.domain.model.GroupChat
import com.chain.messaging.domain.model.GroupPermissions
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.repository.ChatRepository
import com.chain.messaging.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GroupManager that handles group chat operations.
 */
@Singleton
class GroupManagerImpl @Inject constructor(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val inviteLinkGenerator: InviteLinkGenerator
) : GroupManager {
    
    override suspend fun createGroup(
        name: String,
        description: String?,
        memberIds: List<String>,
        adminIds: List<String>,
        permissions: GroupPermissions
    ): Result<GroupChat> {
        return try {
            val groupId = UUID.randomUUID().toString()
            val now = TimeUtils.getCurrentDate()
            
            // Validate input
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Group name cannot be empty"))
            }
            
            if (memberIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("Group must have at least one member"))
            }
            
            if (adminIds.any { !memberIds.contains(it) }) {
                return Result.failure(IllegalArgumentException("All admins must be members of the group"))
            }
            
            // Create the base chat
            val chat = Chat(
                id = groupId,
                type = ChatType.GROUP,
                name = name,
                participants = memberIds,
                admins = adminIds,
                createdAt = now,
                updatedAt = now
            )
            
            // Create the group chat
            val groupChat = GroupChat(
                chat = chat,
                description = description,
                permissions = permissions
            )
            
            // Save to repository
            chatRepository.saveChat(chat)
            
            // Send system message about group creation
            val systemMessage = Message(
                id = UUID.randomUUID().toString(),
                chatId = groupId,
                senderId = "system",
                content = "Group created",
                type = MessageType.SYSTEM,
                timestamp = now,
                status = MessageStatus.SENT
            )
            messageRepository.saveMessage(systemMessage)
            
            Result.success(groupChat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateGroupInfo(
        groupId: String,
        name: String?,
        description: String?,
        permissions: GroupPermissions?
    ): Result<GroupChat> {
        return try {
            val existingChat = chatRepository.getChatById(groupId)
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            if (existingChat.type != ChatType.GROUP) {
                return Result.failure(IllegalArgumentException("Chat is not a group"))
            }
            
            val updatedChat = existingChat.copy(
                name = name ?: existingChat.name,
                updatedAt = TimeUtils.getCurrentTimestamp()
            )
            
            chatRepository.saveChat(updatedChat)
            
            val groupChat = GroupChat(
                chat = updatedChat,
                description = description,
                permissions = permissions ?: GroupPermissions()
            )
            
            Result.success(groupChat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addMembers(
        groupId: String,
        memberIds: List<String>,
        invitedBy: String
    ): Result<Unit> {
        return try {
            val chat = chatRepository.getChatById(groupId)
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            if (chat.type != ChatType.GROUP) {
                return Result.failure(IllegalArgumentException("Chat is not a group"))
            }
            
            // Check if inviter has permission
            if (!hasPermission(groupId, invitedBy, GroupAction.ADD_MEMBERS)) {
                return Result.failure(SecurityException("User does not have permission to add members"))
            }
            
            val newParticipants = (chat.participants + memberIds).distinct()
            val updatedChat = chat.copy(
                participants = newParticipants,
                updatedAt = TimeUtils.getCurrentTimestamp()
            )
            
            chatRepository.saveChat(updatedChat)
            
            // Send system message
            val systemMessage = Message(
                id = UUID.randomUUID().toString(),
                chatId = groupId,
                senderId = "system",
                content = "${memberIds.size} member(s) added to the group",
                type = MessageType.SYSTEM,
                timestamp = TimeUtils.getCurrentDate(),
                status = MessageStatus.SENT
            )
            messageRepository.saveMessage(systemMessage)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeMembers(
        groupId: String,
        memberIds: List<String>,
        removedBy: String
    ): Result<Unit> {
        return try {
            val chat = chatRepository.getChatById(groupId)
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            if (chat.type != ChatType.GROUP) {
                return Result.failure(IllegalArgumentException("Chat is not a group"))
            }
            
            // Check if remover has permission
            if (!hasPermission(groupId, removedBy, GroupAction.REMOVE_MEMBERS)) {
                return Result.failure(SecurityException("User does not have permission to remove members"))
            }
            
            val newParticipants = chat.participants.filterNot { memberIds.contains(it) }
            val newAdmins = chat.admins.filterNot { memberIds.contains(it) }
            
            val updatedChat = chat.copy(
                participants = newParticipants,
                admins = newAdmins,
                updatedAt = TimeUtils.getCurrentTimestamp()
            )
            
            chatRepository.saveChat(updatedChat)
            
            // Send system message
            val systemMessage = Message(
                id = UUID.randomUUID().toString(),
                chatId = groupId,
                senderId = "system",
                content = "${memberIds.size} member(s) removed from the group",
                type = MessageType.SYSTEM,
                timestamp = TimeUtils.getCurrentDate(),
                status = MessageStatus.SENT
            )
            messageRepository.saveMessage(systemMessage)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun promoteToAdmin(
        groupId: String,
        memberIds: List<String>,
        promotedBy: String
    ): Result<Unit> {
        return try {
            val chat = chatRepository.getChatById(groupId)
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            if (chat.type != ChatType.GROUP) {
                return Result.failure(IllegalArgumentException("Chat is not a group"))
            }
            
            // Check if promoter has permission
            if (!hasPermission(groupId, promotedBy, GroupAction.PROMOTE_ADMIN)) {
                return Result.failure(SecurityException("User does not have permission to promote admins"))
            }
            
            // Validate all members exist in the group
            if (!memberIds.all { chat.participants.contains(it) }) {
                return Result.failure(IllegalArgumentException("Some users are not members of the group"))
            }
            
            val newAdmins = (chat.admins + memberIds).distinct()
            val updatedChat = chat.copy(
                admins = newAdmins,
                updatedAt = TimeUtils.getCurrentTimestamp()
            )
            
            chatRepository.saveChat(updatedChat)
            
            // Send system message
            val systemMessage = Message(
                id = UUID.randomUUID().toString(),
                chatId = groupId,
                senderId = "system",
                content = "${memberIds.size} member(s) promoted to admin",
                type = MessageType.SYSTEM,
                timestamp = TimeUtils.getCurrentDate(),
                status = MessageStatus.SENT
            )
            messageRepository.saveMessage(systemMessage)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun demoteFromAdmin(
        groupId: String,
        adminIds: List<String>,
        demotedBy: String
    ): Result<Unit> {
        return try {
            val chat = chatRepository.getChatById(groupId)
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            if (chat.type != ChatType.GROUP) {
                return Result.failure(IllegalArgumentException("Chat is not a group"))
            }
            
            // Check if demoter has permission
            if (!hasPermission(groupId, demotedBy, GroupAction.DEMOTE_ADMIN)) {
                return Result.failure(SecurityException("User does not have permission to demote admins"))
            }
            
            val newAdmins = chat.admins.filterNot { adminIds.contains(it) }
            
            // Ensure at least one admin remains
            if (newAdmins.isEmpty()) {
                return Result.failure(IllegalArgumentException("Group must have at least one admin"))
            }
            
            val updatedChat = chat.copy(
                admins = newAdmins,
                updatedAt = TimeUtils.getCurrentTimestamp()
            )
            
            chatRepository.saveChat(updatedChat)
            
            // Send system message
            val systemMessage = Message(
                id = UUID.randomUUID().toString(),
                chatId = groupId,
                senderId = "system",
                content = "${adminIds.size} admin(s) demoted to member",
                type = MessageType.SYSTEM,
                timestamp = TimeUtils.getCurrentDate(),
                status = MessageStatus.SENT
            )
            messageRepository.saveMessage(systemMessage)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun leaveGroup(
        groupId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val chat = chatRepository.getChatById(groupId)
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            if (chat.type != ChatType.GROUP) {
                return Result.failure(IllegalArgumentException("Chat is not a group"))
            }
            
            if (!chat.participants.contains(userId)) {
                return Result.failure(IllegalArgumentException("User is not a member of the group"))
            }
            
            val newParticipants = chat.participants.filterNot { it == userId }
            val newAdmins = chat.admins.filterNot { it == userId }
            
            // If this was the last admin, promote someone else
            val finalAdmins = if (newAdmins.isEmpty() && newParticipants.isNotEmpty()) {
                listOf(newParticipants.first())
            } else {
                newAdmins
            }
            
            val updatedChat = chat.copy(
                participants = newParticipants,
                admins = finalAdmins,
                updatedAt = TimeUtils.getCurrentTimestamp()
            )
            
            chatRepository.saveChat(updatedChat)
            
            // Send system message
            val systemMessage = Message(
                id = UUID.randomUUID().toString(),
                chatId = groupId,
                senderId = "system",
                content = "User left the group",
                type = MessageType.SYSTEM,
                timestamp = TimeUtils.getCurrentDate(),
                status = MessageStatus.SENT
            )
            messageRepository.saveMessage(systemMessage)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun generateInviteLink(
        groupId: String,
        generatedBy: String
    ): Result<String> {
        return try {
            val chat = chatRepository.getChatById(groupId)
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            if (chat.type != ChatType.GROUP) {
                return Result.failure(IllegalArgumentException("Chat is not a group"))
            }
            
            // Check if generator has permission
            if (!hasPermission(groupId, generatedBy, GroupAction.GENERATE_INVITE_LINK)) {
                return Result.failure(SecurityException("User does not have permission to generate invite links"))
            }
            
            val inviteLink = inviteLinkGenerator.generateInviteLink(groupId)
            
            Result.success(inviteLink)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun revokeInviteLink(
        groupId: String,
        revokedBy: String,
        generateNew: Boolean
    ): Result<String?> {
        return try {
            val chat = chatRepository.getChatById(groupId)
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            if (chat.type != ChatType.GROUP) {
                return Result.failure(IllegalArgumentException("Chat is not a group"))
            }
            
            // Check if revoker has permission
            if (!hasPermission(groupId, revokedBy, GroupAction.REVOKE_INVITE_LINK)) {
                return Result.failure(SecurityException("User does not have permission to revoke invite links"))
            }
            
            inviteLinkGenerator.revokeInviteLink(groupId)
            
            val newLink = if (generateNew) {
                inviteLinkGenerator.generateInviteLink(groupId)
            } else {
                null
            }
            
            Result.success(newLink)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun joinGroupByInvite(
        inviteLink: String,
        userId: String
    ): Result<GroupChat> {
        return try {
            val groupId = inviteLinkGenerator.validateInviteLink(inviteLink)
                ?: return Result.failure(IllegalArgumentException("Invalid invite link"))
            
            val chat = chatRepository.getChatById(groupId)
                ?: return Result.failure(IllegalArgumentException("Group not found"))
            
            if (chat.type != ChatType.GROUP) {
                return Result.failure(IllegalArgumentException("Chat is not a group"))
            }
            
            if (chat.participants.contains(userId)) {
                return Result.failure(IllegalArgumentException("User is already a member of the group"))
            }
            
            val newParticipants = chat.participants + userId
            val updatedChat = chat.copy(
                participants = newParticipants,
                updatedAt = TimeUtils.getCurrentTimestamp()
            )
            
            chatRepository.saveChat(updatedChat)
            
            // Send system message
            val systemMessage = Message(
                id = UUID.randomUUID().toString(),
                chatId = groupId,
                senderId = "system",
                content = "User joined the group via invite link",
                type = MessageType.SYSTEM,
                timestamp = TimeUtils.getCurrentDate(),
                status = MessageStatus.SENT
            )
            messageRepository.saveMessage(systemMessage)
            
            val groupChat = GroupChat(
                chat = updatedChat,
                permissions = GroupPermissions()
            )
            
            Result.success(groupChat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGroup(groupId: String): Result<GroupChat?> {
        return try {
            val chat = chatRepository.getChatById(groupId)
            if (chat?.type == ChatType.GROUP) {
                val groupChat = GroupChat(
                    chat = chat,
                    permissions = GroupPermissions()
                )
                Result.success(groupChat)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeGroup(groupId: String): Flow<GroupChat?> {
        return chatRepository.observeChats().map { chats ->
            val chat = chats.find { it.id == groupId }
            if (chat?.type == ChatType.GROUP) {
                GroupChat(
                    chat = chat,
                    permissions = GroupPermissions()
                )
            } else {
                null
            }
        }
    }
    
    override suspend fun hasPermission(
        groupId: String,
        userId: String,
        action: GroupAction
    ): Boolean {
        return try {
            val chat = chatRepository.getChatById(groupId) ?: return false
            
            if (chat.type != ChatType.GROUP) return false
            if (!chat.participants.contains(userId)) return false
            
            val isAdmin = chat.admins.contains(userId)
            
            when (action) {
                GroupAction.ADD_MEMBERS -> isAdmin
                GroupAction.REMOVE_MEMBERS -> isAdmin
                GroupAction.EDIT_GROUP_INFO -> isAdmin
                GroupAction.PROMOTE_ADMIN -> isAdmin
                GroupAction.DEMOTE_ADMIN -> isAdmin
                GroupAction.GENERATE_INVITE_LINK -> isAdmin
                GroupAction.REVOKE_INVITE_LINK -> isAdmin
            }
        } catch (e: Exception) {
            false
        }
    }
}