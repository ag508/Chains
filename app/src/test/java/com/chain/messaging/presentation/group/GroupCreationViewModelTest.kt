package com.chain.messaging.presentation.group

import com.chain.messaging.core.group.GroupManager
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.UserRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class GroupCreationViewModelTest {
    
    private val groupManager = mockk<GroupManager>()
    private val userRepository = mockk<UserRepository>()
    
    private lateinit var viewModel: GroupCreationViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        clearAllMocks()
        
        // Mock default behavior
        coEvery { userRepository.getAllUsers() } returns createTestUsers()
        
        viewModel = GroupCreationViewModel(groupManager, userRepository)
    }
    
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be correct`() = runTest {
        // Given - ViewModel is initialized
        
        // When
        val initialState = viewModel.uiState.value
        
        // Then
        assertEquals("", initialState.groupName)
        assertEquals("", initialState.groupDescription)
        assertTrue(initialState.selectedMembers.isEmpty())
        assertFalse(initialState.canCreateGroup)
        assertFalse(initialState.isCreatingGroup)
        assertNull(initialState.error)
        assertNull(initialState.createdGroupId)
    }
    
    @Test
    fun `updateGroupName should update state correctly`() = runTest {
        // Given
        val groupName = "Test Group"
        
        // When
        viewModel.updateGroupName(groupName)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(groupName, state.groupName)
        assertNull(state.groupNameError)
    }
    
    @Test
    fun `updateGroupName with empty name should show error`() = runTest {
        // Given
        val emptyName = ""
        
        // When
        viewModel.updateGroupName(emptyName)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(emptyName, state.groupName)
        assertEquals("Group name is required", state.groupNameError)
    }
    
    @Test
    fun `updateGroupDescription should update state correctly`() = runTest {
        // Given
        val description = "Test Description"
        
        // When
        viewModel.updateGroupDescription(description)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(description, state.groupDescription)
    }
    
    @Test
    fun `addMember should add member to selection`() = runTest {
        // Given
        val memberId = "user1"
        
        // When
        viewModel.addMember(memberId)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.selectedMembers.contains(memberId))
    }
    
    @Test
    fun `removeMember should remove member from selection`() = runTest {
        // Given
        val memberId = "user1"
        viewModel.addMember(memberId)
        
        // When
        viewModel.removeMember(memberId)
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.selectedMembers.contains(memberId))
    }
    
    @Test
    fun `canCreateGroup should be true when name and members are provided`() = runTest {
        // Given
        viewModel.updateGroupName("Test Group")
        viewModel.addMember("user1")
        
        // When
        val state = viewModel.uiState.value
        
        // Then
        assertTrue(state.canCreateGroup)
    }
    
    @Test
    fun `canCreateGroup should be false when name is empty`() = runTest {
        // Given
        viewModel.updateGroupName("")
        viewModel.addMember("user1")
        
        // When
        val state = viewModel.uiState.value
        
        // Then
        assertFalse(state.canCreateGroup)
    }
    
    @Test
    fun `canCreateGroup should be false when no members selected`() = runTest {
        // Given
        viewModel.updateGroupName("Test Group")
        // No members added
        
        // When
        val state = viewModel.uiState.value
        
        // Then
        assertFalse(state.canCreateGroup)
    }
    
    @Test
    fun `createGroup should succeed with valid input`() = runTest {
        // Given
        val groupName = "Test Group"
        val description = "Test Description"
        val memberId = "user1"
        val createdGroupId = "group123"
        
        val mockGroupChat = GroupChat(
            chat = Chat(
                id = createdGroupId,
                type = ChatType.GROUP,
                name = groupName,
                participants = listOf("current_user_id", memberId),
                admins = listOf("current_user_id"),
                createdAt = Date(),
                updatedAt = Date()
            ),
            description = description
        )
        
        coEvery { 
            groupManager.createGroup(any(), any(), any(), any(), any()) 
        } returns Result.success(mockGroupChat)
        
        viewModel.updateGroupName(groupName)
        viewModel.updateGroupDescription(description)
        viewModel.addMember(memberId)
        
        // When
        viewModel.createGroup()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCreatingGroup)
        assertEquals(createdGroupId, state.createdGroupId)
        assertNull(state.error)
        
        coVerify { 
            groupManager.createGroup(
                name = groupName,
                description = description,
                memberIds = listOf(memberId, "current_user_id"),
                adminIds = listOf("current_user_id"),
                permissions = any()
            )
        }
    }
    
    @Test
    fun `createGroup should fail with empty name`() = runTest {
        // Given
        viewModel.updateGroupName("")
        viewModel.addMember("user1")
        
        // When
        viewModel.createGroup()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCreatingGroup)
        assertEquals("Group name is required", state.groupNameError)
        assertNull(state.createdGroupId)
    }
    
    @Test
    fun `createGroup should fail with no members`() = runTest {
        // Given
        viewModel.updateGroupName("Test Group")
        // No members added
        
        // When
        viewModel.createGroup()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCreatingGroup)
        assertEquals("Please select at least one member", state.error)
        assertNull(state.createdGroupId)
    }
    
    @Test
    fun `createGroup should handle failure from GroupManager`() = runTest {
        // Given
        val errorMessage = "Failed to create group"
        coEvery { 
            groupManager.createGroup(any(), any(), any(), any(), any()) 
        } returns Result.failure(Exception(errorMessage))
        
        viewModel.updateGroupName("Test Group")
        viewModel.addMember("user1")
        
        // When
        viewModel.createGroup()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCreatingGroup)
        assertEquals(errorMessage, state.error)
        assertNull(state.createdGroupId)
    }
    
    @Test
    fun `loadContacts should populate available contacts`() = runTest {
        // Given - contacts are loaded in init
        
        // When
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingContacts)
        assertEquals(2, state.availableContacts.size)
        
        val contact1 = state.availableContacts.find { it.id == "user1" }
        assertNotNull(contact1)
        assertEquals("User 1", contact1.name)
        assertEquals("Online", contact1.status)
        
        val contact2 = state.availableContacts.find { it.id == "user2" }
        assertNotNull(contact2)
        assertEquals("User 2", contact2.name)
        assertEquals("Last seen recently", contact2.status)
    }
    
    @Test
    fun `loadContacts should handle repository failure`() = runTest {
        // Given
        coEvery { userRepository.getAllUsers() } throws Exception("Network error")
        
        // When
        val newViewModel = GroupCreationViewModel(groupManager, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = newViewModel.uiState.value
        assertFalse(state.isLoadingContacts)
        assertTrue(state.availableContacts.isEmpty())
        assertEquals("Failed to load contacts", state.error)
    }
    
    private fun createTestUsers(): List<User> {
        return listOf(
            User(
                id = "current_user_id",
                publicKey = "current_user_key",
                displayName = "Current User",
                status = UserStatus.ONLINE
            ),
            User(
                id = "user1",
                publicKey = "user1_key",
                displayName = "User 1",
                status = UserStatus.ONLINE
            ),
            User(
                id = "user2",
                publicKey = "user2_key",
                displayName = "User 2",
                status = UserStatus.OFFLINE
            )
        )
    }
}