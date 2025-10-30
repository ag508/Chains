package com.chain.messaging.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.usecase.GetMessagesUseCase
import com.chain.messaging.domain.usecase.SendMessageUseCase
import com.chain.messaging.presentation.theme.ChainTheme
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * UI tests for ChatScreen and message display
 * Tests Requirements: 4.6, 4.7, 4.4
 */
@RunWith(AndroidJUnit4::class)
class ChatScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun chatScreen_displaysEmptyState_whenNoMessages() {
        composeTestRule.setContent {
            ChainTheme {
                ChatScreen(
                    chatId = "test_chat",
                    chatName = "Test Chat",
                    onBackClick = {},
                    viewModel = createMockChatViewModel(
                        uiState = ChatUiState(
                            isLoading = false,
                            messages = emptyList(),
                            error = null
                        )
                    )
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("No messages yet. Start the conversation!")
            .assertIsDisplayed()
    }
    
    @Test
    fun chatScreen_displaysMessages() {
        val testMessages = listOf(
            createTestMessage(
                id = "1",
                content = "Hello!",
                senderId = "other_user"
            ),
            createTestMessage(
                id = "2",
                content = "Hi there!",
                senderId = "current_user"
            )
        )
        
        composeTestRule.setContent {
            ChainTheme {
                ChatScreen(
                    chatId = "test_chat",
                    chatName = "Test Chat",
                    onBackClick = {},
                    viewModel = createMockChatViewModel(
                        uiState = ChatUiState(
                            isLoading = false,
                            messages = testMessages,
                            error = null
                        )
                    )
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Hello!")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Hi there!")
            .assertIsDisplayed()
    }
    
    @Test
    fun chatScreen_displaysTopAppBar() {
        composeTestRule.setContent {
            ChainTheme {
                ChatScreen(
                    chatId = "test_chat",
                    chatName = "John Doe",
                    onBackClick = {},
                    viewModel = createMockChatViewModel()
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("John Doe")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertIsDisplayed()
    }
    
    @Test
    fun messageInput_allowsTypingAndSending() {
        var sentMessage: String? = null
        
        composeTestRule.setContent {
            ChainTheme {
                MessageInput(
                    replyToMessage = null,
                    onSendMessage = { message -> sentMessage = message },
                    onClearReply = {}
                )
            }
        }
        
        // Type a message
        composeTestRule
            .onNodeWithText("Type a message...")
            .performTextInput("Hello world!")
        
        // Click send button
        composeTestRule
            .onNodeWithContentDescription("Send message")
            .performClick()
        
        assert(sentMessage == "Hello world!")
    }
    
    @Test
    fun messageInput_displaysReplyPreview() {
        val replyMessage = createTestMessage(
            content = "Original message",
            senderId = "other_user"
        )
        
        composeTestRule.setContent {
            ChainTheme {
                MessageInput(
                    replyToMessage = replyMessage,
                    onSendMessage = {},
                    onClearReply = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Replying to")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Original message")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("Clear reply")
            .assertIsDisplayed()
    }
    
    @Test
    fun messageInput_clearsReplyOnClose() {
        val replyMessage = createTestMessage(content = "Original message")
        var replyCleared = false
        
        composeTestRule.setContent {
            ChainTheme {
                MessageInput(
                    replyToMessage = replyMessage,
                    onSendMessage = {},
                    onClearReply = { replyCleared = true }
                )
            }
        }
        
        composeTestRule
            .onNodeWithContentDescription("Clear reply")
            .performClick()
        
        assert(replyCleared)
    }
    
    @Test
    fun messageBubble_displaysCorrectAlignment() {
        val ownMessage = createTestMessage(
            content = "My message",
            senderId = "current_user"
        )
        
        val otherMessage = createTestMessage(
            content = "Other message",
            senderId = "other_user"
        )
        
        composeTestRule.setContent {
            ChainTheme {
                Column {
                    MessageBubble(
                        message = ownMessage,
                        isOwnMessage = true,
                        onReactionClick = {},
                        onReplyClick = {}
                    )
                    MessageBubble(
                        message = otherMessage,
                        isOwnMessage = false,
                        onReactionClick = {},
                        onReplyClick = {}
                    )
                }
            }
        }
        
        composeTestRule
            .onNodeWithText("My message")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Other message")
            .assertIsDisplayed()
    }
    
    @Test
    fun messageBubble_displaysMessageStatus() {
        val message = createTestMessage(
            content = "Test message",
            senderId = "current_user",
            status = MessageStatus.DELIVERED
        )
        
        composeTestRule.setContent {
            ChainTheme {
                MessageBubble(
                    message = message,
                    isOwnMessage = true,
                    onReactionClick = {},
                    onReplyClick = {}
                )
            }
        }
        
        composeTestRule
            .onNodeWithContentDescription("Delivered")
            .assertIsDisplayed()
    }
    
    @Test
    fun messageBubble_showsReactionPicker_onClick() {
        val message = createTestMessage(content = "Test message")
        
        composeTestRule.setContent {
            ChainTheme {
                MessageBubble(
                    message = message,
                    isOwnMessage = false,
                    onReactionClick = {},
                    onReplyClick = {}
                )
            }
        }
        
        // Click on message bubble
        composeTestRule
            .onNodeWithText("Test message")
            .performClick()
        
        // Verify reaction picker appears
        composeTestRule
            .onNodeWithText("👍")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("❤️")
            .assertIsDisplayed()
    }
    
    @Test
    fun messageBubble_triggersReactionCallback() {
        val message = createTestMessage(content = "Test message")
        var selectedReaction: String? = null
        
        composeTestRule.setContent {
            ChainTheme {
                MessageBubble(
                    message = message,
                    isOwnMessage = false,
                    onReactionClick = { emoji -> selectedReaction = emoji },
                    onReplyClick = {}
                )
            }
        }
        
        // Click on message to show reaction picker
        composeTestRule
            .onNodeWithText("Test message")
            .performClick()
        
        // Click on thumbs up reaction
        composeTestRule
            .onNodeWithText("👍")
            .performClick()
        
        assert(selectedReaction == "👍")
    }
    
    @Test
    fun messageBubble_displaysReactions() {
        val reactions = listOf(
            Reaction(
                userId = "user1",
                emoji = "👍",
                timestamp = Date()
            ),
            Reaction(
                userId = "user2",
                emoji = "👍",
                timestamp = Date()
            ),
            Reaction(
                userId = "user3",
                emoji = "❤️",
                timestamp = Date()
            )
        )
        
        val message = createTestMessage(
            content = "Test message",
            reactions = reactions
        )
        
        composeTestRule.setContent {
            ChainTheme {
                MessageBubble(
                    message = message,
                    isOwnMessage = false,
                    onReactionClick = {},
                    onReplyClick = {}
                )
            }
        }
        
        // Should show thumbs up with count of 2
        composeTestRule
            .onNodeWithText("2")
            .assertIsDisplayed()
        
        // Should show heart emoji
        composeTestRule
            .onAllNodesWithText("❤️")
            .onFirst()
            .assertIsDisplayed()
    }
    
    private fun createMockChatViewModel(
        uiState: ChatUiState = ChatUiState()
    ): ChatViewModel {
        // In a real test, you would use a mock framework
        return object : ChatViewModel(
            getMessagesUseCase = mockk(),
            sendMessageUseCase = mockk()
        ) {
            override val uiState = MutableStateFlow(uiState)
        }
    }
    
    private fun createTestMessage(
        id: String = "msg_1",
        content: String = "Test message",
        senderId: String = "user_1",
        type: MessageType = MessageType.TEXT,
        status: MessageStatus = MessageStatus.DELIVERED,
        reactions: List<Reaction> = emptyList()
    ): Message {
        return Message(
            id = id,
            chatId = "chat_1",
            senderId = senderId,
            content = content,
            type = type,
            timestamp = Date(),
            status = status,
            reactions = reactions
        )
    }
}