package com.chain.messaging.presentation.chatlist

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.usecase.GetChatsUseCase
import com.chain.messaging.presentation.theme.ChainTheme
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * UI tests for ChatListScreen
 * Tests Requirements: 4.6, 4.7, 4.4
 */
@RunWith(AndroidJUnit4::class)
class ChatListScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun chatListScreen_displaysEmptyState_whenNoChats() {
        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = createMockViewModel(
                        uiState = ChatListUiState(
                            isLoading = false,
                            chats = emptyList(),
                            error = null
                        )
                    )
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("No conversations yet")
            .assertIsDisplayed()
    }
    
    @Test
    fun chatListScreen_displaysLoadingState() {
        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = createMockViewModel(
                        uiState = ChatListUiState(
                            isLoading = true,
                            chats = emptyList(),
                            error = null
                        )
                    )
                )
            }
        }
        
        composeTestRule
            .onNode(hasTestTag("loading_indicator") or hasContentDescription("Loading"))
            .assertIsDisplayed()
    }
    
    @Test
    fun chatListScreen_displaysErrorState() {
        val errorMessage = "Network error"
        
        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = createMockViewModel(
                        uiState = ChatListUiState(
                            isLoading = false,
                            chats = emptyList(),
                            error = errorMessage
                        )
                    )
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Error: $errorMessage")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
    }
    
    @Test
    fun chatListScreen_displaysChatItems() {
        val testChats = listOf(
            createTestChat(
                id = "1",
                name = "John Doe",
                type = ChatType.DIRECT,
                lastMessage = createTestMessage(content = "Hello there!")
            ),
            createTestChat(
                id = "2",
                name = "Team Group",
                type = ChatType.GROUP,
                lastMessage = createTestMessage(content = "Meeting at 3 PM"),
                unreadCount = 5
            )
        )
        
        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = createMockViewModel(
                        uiState = ChatListUiState(
                            isLoading = false,
                            chats = testChats,
                            error = null
                        )
                    )
                )
            }
        }
        
        // Verify chat names are displayed
        composeTestRule
            .onNodeWithText("John Doe")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Team Group")
            .assertIsDisplayed()
        
        // Verify last messages are displayed
        composeTestRule
            .onNodeWithText("Hello there!")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Meeting at 3 PM")
            .assertIsDisplayed()
        
        // Verify unread count badge
        composeTestRule
            .onNodeWithText("5")
            .assertIsDisplayed()
    }
    
    @Test
    fun chatListItem_triggersClickCallback() {
        val testChat = createTestChat(
            id = "test_chat_1",
            name = "Test Chat"
        )
        
        var clickedChatId: String? = null
        
        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = { chatId -> clickedChatId = chatId },
                    viewModel = createMockViewModel(
                        uiState = ChatListUiState(
                            isLoading = false,
                            chats = listOf(testChat),
                            error = null
                        )
                    )
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Test Chat")
            .performClick()
        
        assert(clickedChatId == "test_chat_1")
    }
    
    @Test
    fun chatListScreen_displaysCorrectAvatarIcons() {
        val testChats = listOf(
            createTestChat(
                id = "1",
                name = "Direct Chat",
                type = ChatType.DIRECT
            ),
            createTestChat(
                id = "2",
                name = "Group Chat",
                type = ChatType.GROUP
            )
        )
        
        composeTestRule.setContent {
            ChainTheme {
                ChatListScreen(
                    onChatClick = {},
                    viewModel = createMockViewModel(
                        uiState = ChatListUiState(
                            isLoading = false,
                            chats = testChats,
                            error = null
                        )
                    )
                )
            }
        }
        
        // Verify avatar icons are present (content descriptions)
        composeTestRule
            .onNodeWithContentDescription("Direct Chat")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("Group Chat")
            .assertIsDisplayed()
    }
    
    private fun createMockViewModel(uiState: ChatListUiState): ChatListViewModel {
        // In a real test, you would use a mock framework like Mockito
        // For this example, we'll create a simple mock
        return object : ChatListViewModel(
            getChatsUseCase = mockk()
        ) {
            override val uiState = MutableStateFlow(uiState)
        }
    }
    
    private fun createTestChat(
        id: String,
        name: String,
        type: ChatType = ChatType.DIRECT,
        lastMessage: Message? = null,
        unreadCount: Int = 0
    ): Chat {
        return Chat(
            id = id,
            type = type,
            name = name,
            participants = listOf("user1", "user2"),
            lastMessage = lastMessage,
            unreadCount = unreadCount,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
    
    private fun createTestMessage(
        id: String = "msg_1",
        content: String = "Test message",
        type: MessageType = MessageType.TEXT
    ): Message {
        return Message(
            id = id,
            chatId = "chat_1",
            senderId = "user_1",
            content = content,
            type = type,
            timestamp = Date(),
            status = MessageStatus.DELIVERED
        )
    }
}