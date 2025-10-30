package com.chain.messaging.core.group

import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.ChatRepository
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroupManagerTest {
    
    private val chatRepository = mockk<ChatRepository>()
    private val messageRepository = mockk<MessageRepository>()
    private val inviteLinkGenerator = mockk<InviteLinkGenerator>()
    
    private lateinit var groupManager: GroupManager
    
    @BeforeEach
    fun setup() {
        clearAllMocks()
        groupManager = GroupManagerImpl(chatRepository, messageRepository, inviteLinkGenerator)
    }
    
    @Test
    fun `createGroup should create group successfully`() = runTest {
        // Given
        val groupName = "Test Group"
        val description = "Test Description"
        val memberIds = listOf("user1", "user2", "user3")
        val adminIds = listOf("user1")
        val permissions = GroupPermissions()
        
        coEvery { chatRepository.insertChat(any()) } just Runs
        coEvery { messageRepository.insertMessage(any()) } just Runs
        
        // When
        val result = groupManager.createGroup(groupName, description, memberIds, adminIds, permissions)
        
        // Then
        assertTrue(result.isSuccess)
        val groupChat = result.getOrNull()!!
        assertEquals(groupName, groupChat.chat.name)
        assertEquals(description, groupChat.description)
        assertEquals(memberIds, groupChat.chat.participants)
        assertEquals(adminIds, groupChat.chat.admins)
        assertEquals(ChatType.GROUP, groupChat.chat.type)
        
        coVerify { chatRepository.insertChat(any()) }
        coVerify { messageRepository.insertMessage(any()) }
    }
    
    @Test
    fun `createGroup should fail with empty name`() = runTest {
        // Given
        val groupName = ""
        val memberIds = listOf("user1", "user2")
        val adminIds = listOf("user1")
        
        // When
        val result = groupManager.createGroup(groupName, null, memberIds, adminIds)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Group name cannot be empty", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `createGroup should fail with empty members`() = runTest {
        // Given
        val groupName = "Test Group"
        val memberIds = emptyList<String>()
        val adminIds = emptyList<String>()
        
        // When
        val result = groupManager.createGroup(groupName, null, memberIds, adminIds)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Group must have at least one member", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `createGroup should fail when admin is not a member`() = runTest {
        // Given
        val groupName = "Test Group"
        val memberIds = listOf("user1", "user2")
        val adminIds = listOf("user3") // user3 is not in memberIds
        
        // When
        val result = groupManager.createGroup(groupName, null, memberIds, adminIds)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("All admins must be members of the group", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `addMembers should add members successfully`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2"), listOf("user1"))
        val newMemberIds = listOf("user3", "user4")
        val invitedBy = "user1"
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        coEvery { chatRepository.updateChat(any()) } just Runs
        coEvery { messageRepository.insertMessage(any()) } just Runs
        
        // When
        val result = groupManager.addMembers(groupId, newMemberIds, invitedBy)
        
        // Then
        assertTrue(result.isSuccess)
        
        val expectedParticipants = listOf("user1", "user2", "user3", "user4")
        coVerify { 
            chatRepository.updateChat(
                match { chat -> 
                    chat.participants.containsAll(expectedParticipants) && 
                    expectedParticipants.containsAll(chat.participants)
                }
            ) 
        }
        coVerify { messageRepository.insertMessage(any()) }
    }
    
    @Test
    fun `addMembers should fail when user has no permission`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2"), listOf("user1"))
        val newMemberIds = listOf("user3")
        val invitedBy = "user2" // user2 is not an admin
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        
        // When
        val result = groupManager.addMembers(groupId, newMemberIds, invitedBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
    
    @Test
    fun `removeMembers should remove members successfully`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2", "user3"), listOf("user1"))
        val memberIdsToRemove = listOf("user3")
        val removedBy = "user1"
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        coEvery { chatRepository.updateChat(any()) } just Runs
        coEvery { messageRepository.insertMessage(any()) } just Runs
        
        // When
        val result = groupManager.removeMembers(groupId, memberIdsToRemove, removedBy)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { 
            chatRepository.updateChat(
                match { chat -> 
                    !chat.participants.contains("user3") &&
                    chat.participants.contains("user1") &&
                    chat.participants.contains("user2")
                }
            ) 
        }
        coVerify { messageRepository.insertMessage(any()) }
    }
    
    @Test
    fun `promoteToAdmin should promote members successfully`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2", "user3"), listOf("user1"))
        val memberIdsToPromote = listOf("user2")
        val promotedBy = "user1"
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        coEvery { chatRepository.updateChat(any()) } just Runs
        coEvery { messageRepository.insertMessage(any()) } just Runs
        
        // When
        val result = groupManager.promoteToAdmin(groupId, memberIdsToPromote, promotedBy)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { 
            chatRepository.updateChat(
                match { chat -> 
                    chat.admins.contains("user1") &&
                    chat.admins.contains("user2")
                }
            ) 
        }
        coVerify { messageRepository.insertMessage(any()) }
    }
    
    @Test
    fun `demoteFromAdmin should demote admin successfully`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2", "user3"), listOf("user1", "user2"))
        val adminIdsToDemote = listOf("user2")
        val demotedBy = "user1"
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        coEvery { chatRepository.updateChat(any()) } just Runs
        coEvery { messageRepository.insertMessage(any()) } just Runs
        
        // When
        val result = groupManager.demoteFromAdmin(groupId, adminIdsToDemote, demotedBy)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { 
            chatRepository.updateChat(
                match { chat -> 
                    chat.admins.contains("user1") &&
                    !chat.admins.contains("user2")
                }
            ) 
        }
        coVerify { messageRepository.insertMessage(any()) }
    }
    
    @Test
    fun `demoteFromAdmin should fail when trying to remove last admin`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2"), listOf("user1"))
        val adminIdsToDemote = listOf("user1") // Only admin
        val demotedBy = "user1"
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        
        // When
        val result = groupManager.demoteFromAdmin(groupId, adminIdsToDemote, demotedBy)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Group must have at least one admin", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `leaveGroup should remove user from group`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2", "user3"), listOf("user1", "user2"))
        val userId = "user3"
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        coEvery { chatRepository.updateChat(any()) } just Runs
        coEvery { messageRepository.insertMessage(any()) } just Runs
        
        // When
        val result = groupManager.leaveGroup(groupId, userId)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { 
            chatRepository.updateChat(
                match { chat -> 
                    !chat.participants.contains("user3") &&
                    !chat.admins.contains("user3")
                }
            ) 
        }
        coVerify { messageRepository.insertMessage(any()) }
    }
    
    @Test
    fun `leaveGroup should promote new admin when last admin leaves`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2", "user3"), listOf("user1"))
        val userId = "user1" // Only admin leaving
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        coEvery { chatRepository.updateChat(any()) } just Runs
        coEvery { messageRepository.insertMessage(any()) } just Runs
        
        // When
        val result = groupManager.leaveGroup(groupId, userId)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { 
            chatRepository.updateChat(
                match { chat -> 
                    !chat.participants.contains("user1") &&
                    !chat.admins.contains("user1") &&
                    chat.admins.isNotEmpty() // Someone else should be promoted
                }
            ) 
        }
    }
    
    @Test
    fun `generateInviteLink should generate link successfully`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2"), listOf("user1"))
        val generatedBy = "user1"
        val expectedLink = "https://chain.app/invite/abc123"
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        every { inviteLinkGenerator.generateInviteLink(groupId) } returns expectedLink
        
        // When
        val result = groupManager.generateInviteLink(groupId, generatedBy)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedLink, result.getOrNull())
        
        verify { inviteLinkGenerator.generateInviteLink(groupId) }
    }
    
    @Test
    fun `joinGroupByInvite should add user to group`() = runTest {
        // Given
        val inviteLink = "https://chain.app/invite/abc123"
        val groupId = "group1"
        val userId = "user3"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2"), listOf("user1"))
        
        every { inviteLinkGenerator.validateInviteLink(inviteLink) } returns groupId
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        coEvery { chatRepository.updateChat(any()) } just Runs
        coEvery { messageRepository.insertMessage(any()) } just Runs
        
        // When
        val result = groupManager.joinGroupByInvite(inviteLink, userId)
        
        // Then
        assertTrue(result.isSuccess)
        val groupChat = result.getOrNull()!!
        assertTrue(groupChat.chat.participants.contains(userId))
        
        coVerify { 
            chatRepository.updateChat(
                match { chat -> chat.participants.contains(userId) }
            ) 
        }
        coVerify { messageRepository.insertMessage(any()) }
    }
    
    @Test
    fun `hasPermission should return correct permissions for admin`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2"), listOf("user1"))
        val userId = "user1" // Admin
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        
        // When & Then
        assertTrue(groupManager.hasPermission(groupId, userId, GroupAction.ADD_MEMBERS))
        assertTrue(groupManager.hasPermission(groupId, userId, GroupAction.REMOVE_MEMBERS))
        assertTrue(groupManager.hasPermission(groupId, userId, GroupAction.EDIT_GROUP_INFO))
        assertTrue(groupManager.hasPermission(groupId, userId, GroupAction.PROMOTE_ADMIN))
        assertTrue(groupManager.hasPermission(groupId, userId, GroupAction.DEMOTE_ADMIN))
        assertTrue(groupManager.hasPermission(groupId, userId, GroupAction.GENERATE_INVITE_LINK))
        assertTrue(groupManager.hasPermission(groupId, userId, GroupAction.REVOKE_INVITE_LINK))
    }
    
    @Test
    fun `hasPermission should return false for non-admin`() = runTest {
        // Given
        val groupId = "group1"
        val existingChat = createTestGroupChat(groupId, listOf("user1", "user2"), listOf("user1"))
        val userId = "user2" // Not admin
        
        coEvery { chatRepository.getChatById(groupId) } returns existingChat
        
        // When & Then
        assertFalse(groupManager.hasPermission(groupId, userId, GroupAction.ADD_MEMBERS))
        assertFalse(groupManager.hasPermission(groupId, userId, GroupAction.REMOVE_MEMBERS))
        assertFalse(groupManager.hasPermission(groupId, userId, GroupAction.EDIT_GROUP_INFO))
        assertFalse(groupManager.hasPermission(groupId, userId, GroupAction.PROMOTE_ADMIN))
        assertFalse(groupManager.hasPermission(groupId, userId, GroupAction.DEMOTE_ADMIN))
        assertFalse(groupManager.hasPermission(groupId, userId, GroupAction.GENERATE_INVITE_LINK))
        assertFalse(groupManager.hasPermission(groupId, userId, GroupAction.REVOKE_INVITE_LINK))
    }
    
    private fun createTestGroupChat(
        groupId: String,
        participants: List<String>,
        admins: List<String>
    ): Chat {
        return Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Test Group",
            participants = participants,
            admins = admins,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}