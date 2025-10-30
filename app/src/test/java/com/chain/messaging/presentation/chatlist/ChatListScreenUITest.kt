package com.chain.messaging.presentation.chatlist

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatSettings
import com.chain.messaging.domain.model.ChatType
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.presentation.theme.ChainTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * UI tests for ChatListScreen interactions
 * Tests Requirements: 11.1, 11.3 - chat list with search, filtering, sorting, and swipe actions
 */
@RunWith(AndroidJUnit4::class)
class ChatListScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mockk<ChatListViewModel>(relaxed = true)

    private val sampleChats = listOf(
        Chat(
            id = "1",
            type = ChatType.DIRECT,
            name = "John Doe",
            participants = listOf("user1", "john"),
            lastMessage = Message(
                id = "msg1",
                chatId = "1",
                senderId = "john",
                content = "Hello there!",
                type = MessageType.TEXT,
                timestamp = Date(),
                status = com.chain.messaging.domain.model.MessageStatus.DELIVERED
            ),
            unreadCount = 2,
            createdAt = Date(),
            updatedAt = Date(),
            settings = ChatSettings(isPinned = false)
        ),
        Chat(
            id = "2",
            type = ChatType.GROUP,
            name = "Team Chat",
            participants = listOf("user1", "user2", "user3"),
            lastMessage = Message(
                id = "msg2",
                chatId = "2",
                senderId = "user2",
                content = "Meeting at 3 PM",
                type = MessageType.TEXT,
                timestamp = Date(),
                status = com.chain.messaging.domain.model.MessageStatus.READ
            ),
            unreadCount = 0,
            createdAt = Date(),
            updatedAt = Date(),
            settings = ChatSettings(isPinned = true)
        )
    )

    @Test
    fun chatListScreen_displaysChats() {
        val uiState = ChatListUiState(
            chats = sampleChats,
            filteredChats = sampleChats,
            isLoading = false
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Verify chats are displayed
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Team Chat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hello there!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Meeting at 3 PM").assertIsDisplayed()
    }

    @Test
    fun chatListScreen_showsUnreadBadges() {
        val uiState = ChatListUiState(
            chats = sampleChats,
            filteredChats = sampleChats,
            isLoading = false
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Verify unread badge is shown for chat with unread messages
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        
        // Verify no badge for read chat (Team Chat has 0 unread)
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun chatListScreen_showsPinnedIndicator() {
        val uiState = ChatListUiState(
            chats = sampleChats,
            filteredChats = sampleChats,
            isLoading = false
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Verify pinned indicator is shown for pinned chat
        composeTestRule.onNodeWithContentDescription("Pinned").assertIsDisplayed()
    }

    @Test
    fun chatListScreen_clickOnChatTriggersCallback() {
        val uiState = ChatListUiState(
            chats = sampleChats,
            filteredChats = sampleChats,
            isLoading = false
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        var clickedChatId = ""
        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = { chatId -> clickedChatId = chatId },
                    viewModel = mockViewModel
                )
            }
        }

        // Click on first chat
        composeTestRule.onNodeWithText("John Doe").performClick()
        
        // Verify callback was triggered with correct chat ID
        assert(clickedChatId == "1")
    }

    @Test
    fun chatListScreen_searchFunctionality() {
        val uiState = ChatListUiState(
            chats = sampleChats,
            filteredChats = sampleChats,
            isLoading = false,
            searchQuery = ""
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Click search icon
        composeTestRule.onNodeWithContentDescription("Search chats").performClick()
        
        // Verify search bar appears
        composeTestRule.onNodeWithText("Search chats...").assertIsDisplayed()
        
        // Type in search bar
        composeTestRule.onNodeWithText("Search chats...").performTextInput("John")
        
        // Verify search query was sent to viewModel
        verify { mockViewModel.onSearchQueryChanged("John") }
    }

    @Test
    fun chatListScreen_sortMenuFunctionality() {
        val uiState = ChatListUiState(
            chats = sampleChats,
            filteredChats = sampleChats,
            isLoading = false
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Click sort icon
        composeTestRule.onNodeWithContentDescription("Sort chats").performClick()
        
        // Verify sort options are displayed
        composeTestRule.onNodeWithText("Recent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unread").assertIsDisplayed()
        
        // Click on Name sort option
        composeTestRule.onNodeWithText("Name").performClick()
        
        // Verify sort option was sent to viewModel
        verify { mockViewModel.onSortChanged(ChatSortOption.NAME) }
    }

    @Test
    fun chatListScreen_filterChips() {
        val uiState = ChatListUiState(
            chats = sampleChats,
            filteredChats = sampleChats,
            isLoading = false,
            availableFilters = listOf(ChatFilter.UNREAD, ChatFilter.GROUPS, ChatFilter.PINNED),
            activeFilters = setOf(ChatFilter.UNREAD)
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Verify filter chips are displayed
        composeTestRule.onNodeWithText("Unread").assertIsDisplayed()
        composeTestRule.onNodeWithText("Groups").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pinned").assertIsDisplayed()
        
        // Click on Groups filter
        composeTestRule.onNodeWithText("Groups").performClick()
        
        // Verify filter toggle was sent to viewModel
        verify { mockViewModel.onFilterToggled(ChatFilter.GROUPS) }
    }

    @Test
    fun chatListScreen_showsLoadingState() {
        val uiState = ChatListUiState(
            isLoading = true,
            chats = emptyList(),
            filteredChats = emptyList()
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Verify loading indicator is displayed
        composeTestRule.onNode(hasTestTag("loading") or hasContentDescription("Loading")).assertIsDisplayed()
    }

    @Test
    fun chatListScreen_showsErrorState() {
        val uiState = ChatListUiState(
            isLoading = false,
            chats = emptyList(),
            filteredChats = emptyList(),
            error = "Network error"
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Error: Network error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        
        // Click retry button
        composeTestRule.onNodeWithText("Retry").performClick()
        
        // Verify refresh was called
        verify { mockViewModel.onRefresh() }
    }

    @Test
    fun chatListScreen_showsEmptyState() {
        val uiState = ChatListUiState(
            isLoading = false,
            chats = emptyList(),
            filteredChats = emptyList(),
            error = null
        )
        every { mockViewModel.uiState } returns MutableStateFlow(uiState)

        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Verify empty state message is displayed
        composeTestRule.onNodeWithText("No conversations yet").assertIsDisplayed()
    }
}