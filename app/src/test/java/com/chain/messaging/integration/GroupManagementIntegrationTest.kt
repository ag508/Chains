package com.chain.messaging.integration

import com.chain.messaging.core.group.GroupManager
import com.chain.messaging.core.group.GroupManagerImpl
import com.chain.messaging.core.group.InviteLinkGenerator
import com.chain.messaging.core.group.InviteLinkGeneratorImpl
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.ChatRepository
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.*

/**
 * Integration tests for group management functionality.
 * Tests the complete flow from group creation to member management.
 */
class GroupManagementIntegrationTest {
    
    private val chatRepository = mockk<ChatRepository>()
    private val messageRepository = mockk<MessageRepository>()
    private val inviteLinkGenerator: InviteLinkGenerator = InviteLinkGeneratorImpl()
    
    private lateinit var groupManager: GroupManager
    
    @BeforeEach
    fun setup() {
        clearAllMocks()
        groupManager = GroupManagerImpl(chatRepository, messageRepository, inviteLinkGenerator)
        
        // Setup default mock behaviors
        coEvery { chatRepository.insertChat(any()) } just Runs
        coEvery { chatRepository.updateChat(any()) } just Runs
        coEvery { messageRepository.insertMessage(any()) } just Runs
    }
    
    @Test
    fun `complete group lifecycle - create, add members, manage admins, leave`() = runTest {
        // Step 1: Create a group
        val groupName = "Test Group"
        val description = "Integration test group"
        val initialMembers = listOf("user1", "user2")
        val initialAdmins = listOf("user1")
        
        val createResult = groupManager.createGroup(
            name = groupName,
            description = description,
            memberIds = initialMembers,
            adminIds = initialAdmins
        )
        
        assertTrue(createResult.isSuccess)
        val createdGroup = createResult.getOrNull()!!
        val groupId = createdGroup.chat.id
        
        // Verify group creation
        assertEquals(groupName, createdGroup.chat.name)
        assertEquals(description, createdGroup.description)
        assertEquals(initialMembers, createdGroup.chat.participants)
        assertEquals(initialAdmins, createdGroup.chat.admins)
        
        // Step 2: Mock the created group for subsequent operations
        coEvery { chatRepository.getChatById(groupId) } returns createdGroup.chat
        
        // Step 3: Add new members
        val newMembers = listOf("user3", "user4")
        val addMembersResult = groupManager.addMembers(groupId, newMembers, "user1")
        
        assertTrue(addMembersResult.isSuccess)
        
        // Update mock to reflect new members
        val updatedChatAfterAdd = createdGroup.chat.copy(
            participants = initialMembers + newMembers,
            updatedAt = Date()
        )
        coEvery { chatRepository.getChatById(groupId) } returns updatedChatAfterAdd
        
        // Step 4: Promote a member to admin
        val promoteResult = groupManager.promoteToAdmin(groupId, listOf("user2"), "user1")
        assertTrue(promoteResult.isSuccess)
        
        // Update mock to reflect new admin
        val updatedChatAfterPromotion = updatedChatAfterAdd.copy(
            admins = initialAdmins + "user2",
            updatedAt = Date()
        )
        coEvery { chatRepository.getChatById(groupId) } returns updatedChatAfterPromotion
        
        // Step 5: Generate invite link
        val inviteLinkResult = groupManager.generateInviteLink(groupId, "user1")
        assertTrue(inviteLinkResult.isSuccess)
        val inviteLink = inviteLinkResult.getOrNull()!!
        assertTrue(inviteLink.startsWith("https://chain.app/invite/"))
        
        // Step 6: Someone joins via invite link
        val newUserId = "user5"
        val joinResult = groupManager.joinGroupByInvite(inviteLink, newUserId)
        assertTrue(joinResult.isSuccess)
        
        val joinedGroup = joinResult.getOrNull()!!
        assertTrue(joinedGroup.chat.participants.contains(newUserId))
        
        // Update mock to reflect joined user
        val updatedChatAfterJoin = updatedChatAfterPromotion.copy(
            participants = updatedChatAfterPromotion.participants + newUserId,
            updatedAt = Date()
        )
        coEvery { chatRepository.getChatById(groupId) } returns updatedChatAfterJoin
        
        // Step 7: Remove a member
        val removeResult = groupManager.removeMembers(groupId, listOf("user3"), "user1")
        assertTrue(removeResult.isSuccess)
        
        // Update mock to reflect removed member
        val updatedChatAfterRemoval = updatedChatAfterJoin.copy(
            participants = updatedChatAfterJoin.participants.filterNot { it == "user3" },
            updatedAt = Date()
        )
        coEvery { chatRepository.getChatById(groupId) } returns updatedChatAfterRemoval
        
        // Step 8: Demote an admin
        val demoteResult = groupManager.demoteFromAdmin(groupId, listOf("user2"), "user1")
        assertTrue(demoteResult.isSuccess)
        
        // Update mock to reflect demoted admin
        val updatedChatAfterDemotion = updatedChatAfterRemoval.copy(
            admins = updatedChatAfterRemoval.admins.filterNot { it == "user2" },
            updatedAt = Date()
        )
        coEvery { chatRepository.getChatById(groupId) } returns updatedChatAfterDemotion
        
        // Step 9: User leaves group
        val leaveResult = groupManager.leaveGroup(groupId, "user4")
        assertTrue(leaveResult.isSuccess)
        
        // Verify all operations were called with correct parameters
        coVerify { chatRepository.insertChat(any()) }
        coVerify(atLeast = 6) { chatRepository.updateChat(any()) }
        coVerify(atLeast = 6) { messageRepository.insertMessage(any()) }
    }
    
    @Test
    fun `invite link workflow - generate, validate, revoke`() = runTest {
        // Step 1: Create a group
        val groupId = "test-group"
        val testChat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Test Group",
            participants = listOf("user1", "user2"),
            admins = listOf("user1"),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns testChat
        
        // Step 2: Generate invite link
        val generateResult = groupManager.generateInviteLink(groupId, "user1")
        assertTrue(generateResult.isSuccess)
        val inviteLink = generateResult.getOrNull()!!
        
        // Step 3: Validate the link works
        val validateResult = inviteLinkGenerator.validateInviteLink(inviteLink)
        assertEquals(groupId, validateResult)
        
        // Step 4: Join using the link
        val joinResult = groupManager.joinGroupByInvite(inviteLink, "user3")
        assertTrue(joinResult.isSuccess)
        
        // Step 5: Revoke the link
        val revokeResult = groupManager.revokeInviteLink(groupId, "user1", generateNew = false)
        assertTrue(revokeResult.isSuccess)
        assertNull(revokeResult.getOrNull())
        
        // Step 6: Verify link is no longer valid
        val invalidValidateResult = inviteLinkGenerator.validateInviteLink(inviteLink)
        assertNull(invalidValidateResult)
        
        // Step 7: Generate new link after revocation
        val newLinkResult = groupManager.revokeInviteLink(groupId, "user1", generateNew = true)
        assertTrue(newLinkResult.isSuccess)
        val newLink = newLinkResult.getOrNull()!!
        assertNotEquals(inviteLink, newLink)
        
        // Step 8: Verify new link works
        val newValidateResult = inviteLinkGenerator.validateInviteLink(newLink)
        assertEquals(groupId, newValidateResult)
    }
    
    @Test
    fun `permission system integration test`() = runTest {
        // Setup: Create group with specific admin structure
        val groupId = "permission-test-group"
        val testChat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Permission Test Group",
            participants = listOf("admin1", "admin2", "member1", "member2"),
            admins = listOf("admin1", "admin2"),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns testChat
        
        // Test 1: Admin can perform all actions
        assertTrue(groupManager.hasPermission(groupId, "admin1", GroupAction.ADD_MEMBERS))
        assertTrue(groupManager.hasPermission(groupId, "admin1", GroupAction.REMOVE_MEMBERS))
        assertTrue(groupManager.hasPermission(groupId, "admin1", GroupAction.EDIT_GROUP_INFO))
        assertTrue(groupManager.hasPermission(groupId, "admin1", GroupAction.PROMOTE_ADMIN))
        assertTrue(groupManager.hasPermission(groupId, "admin1", GroupAction.DEMOTE_ADMIN))
        assertTrue(groupManager.hasPermission(groupId, "admin1", GroupAction.GENERATE_INVITE_LINK))
        assertTrue(groupManager.hasPermission(groupId, "admin1", GroupAction.REVOKE_INVITE_LINK))
        
        // Test 2: Regular member cannot perform admin actions
        assertFalse(groupManager.hasPermission(groupId, "member1", GroupAction.ADD_MEMBERS))
        assertFalse(groupManager.hasPermission(groupId, "member1", GroupAction.REMOVE_MEMBERS))
        assertFalse(groupManager.hasPermission(groupId, "member1", GroupAction.EDIT_GROUP_INFO))
        assertFalse(groupManager.hasPermission(groupId, "member1", GroupAction.PROMOTE_ADMIN))
        assertFalse(groupManager.hasPermission(groupId, "member1", GroupAction.DEMOTE_ADMIN))
        assertFalse(groupManager.hasPermission(groupId, "member1", GroupAction.GENERATE_INVITE_LINK))
        assertFalse(groupManager.hasPermission(groupId, "member1", GroupAction.REVOKE_INVITE_LINK))
        
        // Test 3: Non-member has no permissions
        assertFalse(groupManager.hasPermission(groupId, "outsider", GroupAction.ADD_MEMBERS))
        
        // Test 4: Admin actions are blocked for non-admins
        val addMembersResult = groupManager.addMembers(groupId, listOf("user3"), "member1")
        assertTrue(addMembersResult.isFailure)
        assertTrue(addMembersResult.exceptionOrNull() is SecurityException)
        
        val removeMembersResult = groupManager.removeMembers(groupId, listOf("member2"), "member1")
        assertTrue(removeMembersResult.isFailure)
        assertTrue(removeMembersResult.exceptionOrNull() is SecurityException)
        
        val promoteResult = groupManager.promoteToAdmin(groupId, listOf("member1"), "member2")
        assertTrue(promoteResult.isFailure)
        assertTrue(promoteResult.exceptionOrNull() is SecurityException)
    }
    
    @Test
    fun `error handling integration test`() = runTest {
        // Test 1: Operations on non-existent group
        coEvery { chatRepository.getChatById("non-existent") } returns null
        
        val addResult = groupManager.addMembers("non-existent", listOf("user1"), "admin")
        assertTrue(addResult.isFailure)
        assertEquals("Group not found", addResult.exceptionOrNull()?.message)
        
        // Test 2: Operations on direct chat (not group)
        val directChat = Chat(
            id = "direct-chat",
            type = ChatType.DIRECT,
            name = "Direct Chat",
            participants = listOf("user1", "user2"),
            createdAt = Date(),
            updatedAt = Date()
        )
        coEvery { chatRepository.getChatById("direct-chat") } returns directChat
        
        val promoteResult = groupManager.promoteToAdmin("direct-chat", listOf("user2"), "user1")
        assertTrue(promoteResult.isFailure)
        assertEquals("Chat is not a group", promoteResult.exceptionOrNull()?.message)
        
        // Test 3: Repository failures
        coEvery { chatRepository.updateChat(any()) } throws Exception("Database error")
        
        val testChat = Chat(
            id = "test-group",
            type = ChatType.GROUP,
            name = "Test Group",
            participants = listOf("user1", "user2"),
            admins = listOf("user1"),
            createdAt = Date(),
            updatedAt = Date()
        )
        coEvery { chatRepository.getChatById("test-group") } returns testChat
        
        val updateResult = groupManager.updateGroupInfo("test-group", "New Name")
        assertTrue(updateResult.isFailure)
        assertEquals("Database error", updateResult.exceptionOrNull()?.message)
    }
}