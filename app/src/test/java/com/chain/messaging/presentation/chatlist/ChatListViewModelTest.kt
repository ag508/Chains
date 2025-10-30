package com.chain.messaging.presentation.chatlist

import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatSettings
import com.chain.messaging.domain.model.ChatType
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.usecase.ArchiveChatUseCase
import com.chain.messaging.domain.usecase.DeleteChatUseCase
import com.chain.messaging.domain.usecase.GetChatsUseCase
import com.chain.messaging.domain.usecase.PinChatUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for ChatListViewModel
 * Tests Requirements: 11.1, 11.3 - search, filtering, sorting functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getChatsUseCase = mockk<GetChatsUseCase>()
    private val archiveChatUseCase = mockk<ArchiveChatUseCase>()
    private val deleteChatUseCase = mockk<DeleteChatUseCase>()
    private val pinChatUseCase = mockk<PinChatUseCase>()
    private lateinit var viewModel: ChatListViewModel

    private val sampleChats = listOf(
        Chat(
            id = "1",
            type = ChatType.DIRECT,
            name = "Alice Johnson",
            participants = listOf("user1", "alice"),
            lastMessage = Message(
                id = "msg1",
                chatId = "1",
                senderId = "alice",
                content = "Hello there!",
                type = MessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 1000),
                status = MessageStatus.DELIVERED
            ),
            unreadCount = 3,
            createdAt = Date(),
            updatedAt = Date(),
            settings = ChatSettings(isPinned = false)
        ),
        Chat(
            id = "2",
            type = ChatType.GROUP,
            name = "Team Alpha",
            participants = listOf("user1", "user2", "user3"),
            lastMessage = Message(
                id = "msg2",
                chatId = "2",
                senderId = "user2",
                content = "Meeting at 3 PM",
                type = MessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 2000),
                status = MessageStatus.READ
            ),
            unreadCount = 0,
            createdAt = Date(),
            updatedAt = Date(),
            settings = ChatSettings(isPinned = true)
        ),
        Chat(
            id = "3",
            type = ChatType.DIRECT,
            name = "Bob Smith",
            participants = listOf("user1", "bob"),
            lastMessage = Message(
                id = "msg3",
                chatId = "3",
                senderId = "bob",
                content = "See you tomorrow",
                type = MessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - 3000),
                status = MessageStatus.SENT
            ),
            unreadCount = 1,
            createdAt = Date(),
            updatedAt = Date(),
            settings = ChatSettings(isPinned = false, isArchived = true)
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getChatsUseCase.observeChats() } returns flowOf(sampleChats)
        viewModel = ChatListViewModel(getChatsUseCase, archiveChatUseCase, deleteChatUseCase, pinChatUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        val initialState = viewModel.uiState.value
        assertEquals(ChatListUiState(), initialState)
    }

    @Test
    fun `loadChats updates state with chats`() = runTest {
        // Advance time to allow coroutines to complete
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals(sampleChats, currentState.chats)
        assertEquals(sampleChats, currentState.filteredChats)
        assertEquals(false, currentState.isLoading)
        assertEquals(null, currentState.error)
    }

    @Test
    fun `search query filters chats by name`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Alice")
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals("Alice", currentState.searchQuery)
        assertEquals(1, currentState.filteredChats.size)
        assertEquals("Alice Johnson", currentState.filteredChats[0].name)
    }

    @Test
    fun `search query filters chats by message content`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Meeting")
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals("Meeting", currentState.searchQuery)
        assertEquals(1, currentState.filteredChats.size)
        assertEquals("Team Alpha", currentState.filteredChats[0].name)
    }

    @Test
    fun `sort by recent orders chats by last message timestamp`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onSortChanged(ChatSortOption.RECENT)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals(ChatSortOption.RECENT, currentState.sortOption)
        
        // Should be ordered by most recent message first
        assertEquals("Alice Johnson", currentState.filteredChats[0].name) // Most recent
        assertEquals("Team Alpha", currentState.filteredChats[1].name)
        assertEquals("Bob Smith", currentState.filteredChats[2].name) // Oldest
    }

    @Test
    fun `sort by name orders chats alphabetically`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onSortChanged(ChatSortOption.NAME)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals(ChatSortOption.NAME, currentState.sortOption)
        
        // Should be ordered alphabetically
        assertEquals("Alice Johnson", currentState.filteredChats[0].name)
        assertEquals("Bob Smith", currentState.filteredChats[1].name)
        assertEquals("Team Alpha", currentState.filteredChats[2].name)
    }

    @Test
    fun `sort by unread prioritizes unread chats`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onSortChanged(ChatSortOption.UNREAD)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals(ChatSortOption.UNREAD, currentState.sortOption)
        
        // Unread chats should come first, ordered by unread count
        val filteredChats = currentState.filteredChats
        assertTrue(filteredChats[0].unreadCount > 0) // Alice (3 unread)
        assertTrue(filteredChats[1].unreadCount > 0) // Bob (1 unread)
        assertEquals(0, filteredChats[2].unreadCount) // Team Alpha (0 unread)
    }

    @Test
    fun `filter by unread shows only unread chats`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onFilterToggled(ChatFilter.UNREAD)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue(currentState.activeFilters.contains(ChatFilter.UNREAD))
        assertEquals(2, currentState.filteredChats.size) // Alice and Bob have unread messages
        assertTrue(currentState.filteredChats.all { it.unreadCount > 0 })
    }

    @Test
    fun `filter by groups shows only group chats`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onFilterToggled(ChatFilter.GROUPS)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue(currentState.activeFilters.contains(ChatFilter.GROUPS))
        assertEquals(1, currentState.filteredChats.size)
        assertEquals(ChatType.GROUP, currentState.filteredChats[0].type)
        assertEquals("Team Alpha", currentState.filteredChats[0].name)
    }

    @Test
    fun `filter by direct shows only direct chats`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onFilterToggled(ChatFilter.DIRECT)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue(currentState.activeFilters.contains(ChatFilter.DIRECT))
        assertEquals(2, currentState.filteredChats.size)
        assertTrue(currentState.filteredChats.all { it.type == ChatType.DIRECT })
    }

    @Test
    fun `filter by pinned shows only pinned chats`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onFilterToggled(ChatFilter.PINNED)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue(currentState.activeFilters.contains(ChatFilter.PINNED))
        assertEquals(1, currentState.filteredChats.size)
        assertTrue(currentState.filteredChats[0].settings.isPinned)
        assertEquals("Team Alpha", currentState.filteredChats[0].name)
    }

    @Test
    fun `filter by archived shows only archived chats`() = runTest {
        testScheduler.advanceUntilIdle()

        viewModel.onFilterToggled(ChatFilter.ARCHIVED)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue(currentState.activeFilters.contains(ChatFilter.ARCHIVED))
        assertEquals(1, currentState.filteredChats.size)
        assertTrue(currentState.filteredChats[0].settings.isArchived)
        assertEquals("Bob Smith", currentState.filteredChats[0].name)
    }

    @Test
    fun `multiple filters work together`() = runTest {
        testScheduler.advanceUntilIdle()

        // Apply both UNREAD and DIRECT filters
        viewModel.onFilterToggled(ChatFilter.UNREAD)
        viewModel.onFilterToggled(ChatFilter.DIRECT)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue(currentState.activeFilters.contains(ChatFilter.UNREAD))
        assertTrue(currentState.activeFilters.contains(ChatFilter.DIRECT))
        
        // Should show only direct chats with unread messages
        assertEquals(2, currentState.filteredChats.size)
        assertTrue(currentState.filteredChats.all { 
            it.type == ChatType.DIRECT && it.unreadCount > 0 
        })
    }

    @Test
    fun `toggling filter twice removes it`() = runTest {
        testScheduler.advanceUntilIdle()

        // Add filter
        viewModel.onFilterToggled(ChatFilter.UNREAD)
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.activeFilters.contains(ChatFilter.UNREAD))

        // Remove filter
        viewModel.onFilterToggled(ChatFilter.UNREAD)
        testScheduler.advanceUntilIdle()
        assertTrue(!viewModel.uiState.value.activeFilters.contains(ChatFilter.UNREAD))
        assertEquals(sampleChats.size, viewModel.uiState.value.filteredChats.size)
    }

    @Test
    fun `search and filters work together`() = runTest {
        testScheduler.advanceUntilIdle()

        // Apply search and filter
        viewModel.onSearchQueryChanged("Alice")
        viewModel.onFilterToggled(ChatFilter.UNREAD)
        testScheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals("Alice", currentState.searchQuery)
        assertTrue(currentState.activeFilters.contains(ChatFilter.UNREAD))
        
        // Should show Alice's chat (matches search and has unread messages)
        assertEquals(1, currentState.filteredChats.size)
        assertEquals("Alice Johnson", currentState.filteredChats[0].name)
    }

    @Test
    fun `onRefresh calls loadChats`() = runTest {
        testScheduler.advanceUntilIdle()

        // Clear current state
        viewModel.onRefresh()
        testScheduler.advanceUntilIdle()

        // Should reload chats
        val currentState = viewModel.uiState.value
        assertEquals(sampleChats, currentState.chats)
    }

    @Test
    fun `onArchiveChat archives unarchived chat successfully`() = runTest {
        testScheduler.advanceUntilIdle()
        
        // Mock successful archive operation
        coEvery { archiveChatUseCase.execute("1", true) } returns Result.success(Unit)
        
        // Archive Alice's chat (currently unarchived)
        viewModel.onArchiveChat("1")
        testScheduler.advanceUntilIdle()
        
        // Verify the use case was called with correct parameters
        coVerify { archiveChatUseCase.execute("1", true) }
    }

    @Test
    fun `onArchiveChat unarchives archived chat successfully`() = runTest {
        testScheduler.advanceUntilIdle()
        
        // Mock successful unarchive operation
        coEvery { archiveChatUseCase.execute("3", false) } returns Result.success(Unit)
        
        // Unarchive Bob's chat (currently archived)
        viewModel.onArchiveChat("3")
        testScheduler.advanceUntilIdle()
        
        // Verify the use case was called with correct parameters
        coVerify { archiveChatUseCase.execute("3", false) }
    }

    @Test
    fun `onArchiveChat handles failure gracefully`() = runTest {
        testScheduler.advanceUntilIdle()
        
        // Mock failed archive operation
        val error = Exception("Archive failed")
        coEvery { archiveChatUseCase.execute("1", true) } returns Result.failure(error)
        
        // Archive Alice's chat
        viewModel.onArchiveChat("1")
        testScheduler.advanceUntilIdle()
        
        // Verify error is handled
        val currentState = viewModel.uiState.value
        assertEquals("Failed to archive chat: Archive failed", currentState.error)
        
        coVerify { archiveChatUseCase.execute("1", true) }
    }

    @Test
    fun `onArchiveChat does nothing for non-existent chat`() = runTest {
        testScheduler.advanceUntilIdle()
        
        // Try to archive non-existent chat
        viewModel.onArchiveChat("non-existent")
        testScheduler.advanceUntilIdle()
        
        // Verify use case was not called
        coVerify(exactly = 0) { archiveChatUseCase.execute(any(), any()) }
    }

    @Test
    fun `onDeleteChat deletes chat successfully`() = runTest {
        testScheduler.advanceUntilIdle()
        
        // Mock successful delete operation
        coEvery { deleteChatUseCase.execute("1") } returns Result.success(Unit)
        
        // Delete Alice's chat
        viewModel.onDeleteChat("1")
        testScheduler.advanceUntilIdle()
        
        // Verify the use case was called with correct parameters
        coVerify { deleteChatUseCase.execute("1") }
    }

    @Test
    fun `onDeleteChat handles failure gracefully`() = runTest {
        testScheduler.advanceUntilIdle()
        
        // Mock failed delete operation
        val error = Exception("Delete failed")
        coEvery { deleteChatUseCase.execute("1") } returns Result.failure(error)
        
        // Delete Alice's chat
        viewModel.onDeleteChat("1")
        testScheduler.advanceUntilIdle()
        
        // Verify error is handled
        val currentState = viewModel.uiState.value
        assertEquals("Failed to delete chat: Delete failed", currentState.error)
        
        coVerify { deleteChatUseCase.execute("1") }
    }

    @Test
    fun `onChatSelected handles chat selection correctly`() = runTest {
        testScheduler.advanceUntilIdle()
        
        // Select a chat with unread messages
        viewModel.onChatSelected("1")
        testScheduler.advanceUntilIdle()
        
        // The method should complete without errors
        // Navigation is handled by the UI layer, so we just verify no exceptions occur
        val currentState = viewModel.uiState.value
        assertEquals(null, currentState.error)
    }

    @Test
    fun `onChatSelected with non-existent chat handles gracefully`() = runTest {
        testScheduler.advanceUntilIdle()
        
        // Select a non-existent chat
        viewModel.onChatSelected("non-existent")
        testScheduler.advanceUntilIdle()
        
        // Should handle gracefully without errors
        val currentState = viewModel.uiState.value
        assertEquals(null, currentState.error)
    }
}