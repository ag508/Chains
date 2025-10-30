package com.chain.messaging.presentation.chat

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.domain.model.*
import com.chain.messaging.presentation.theme.ChainTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Integration tests for message display and conversation UI
 * Tests Requirements: 4.6, 4.7, 4.4
 */
@RunWith(AndroidJUnit4::class)
class MessageDisplayIntegrationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun messageDisplay_showsCompleteConversationFlow() {
        val messages = listOf(
            createTestMessage(
                id = "1",
                content = "Hello! How are you?",
                senderId = "other_user",
                timestamp = Date(System.currentTimeMillis() - 60000) // 1 minute ago
            ),
            createTestMessage(
                id = "2",
                content = "I'm doing great, thanks for asking!",
                senderId = "current_user",
                timestamp = Date(System.currentTimeMillis() - 30000) // 30 seconds ago
            ),
            createTestMessage(
                id = "3",
                content = "That's wonderful to hear! 😊",
                senderId = "other_user",
                timestamp = Date(),
                reactions = listOf(
                    Reaction("current_user", "❤️", Date())
                )
            )
        )
        
        composeTestRule.setContent {
            ChainTheme {
                ChatScreen(
                    chatId = "test_chat",
                    chatName = "John Doe",
                    onBackClick = {},
                    viewModel = createMockChatViewModel(
                        ChatUiState(
                            messages = messages,
                            currentUserId = "current_user"
                        )
                    )
                )
            }
        }
        
        // Verify all messages are displayed
        composeTestRule
            .onNodeWithText("Hello! How are you?")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("I'm doing great, thanks for asking!")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("That's wonderful to hear! 😊")
            .assertIsDisplayed()
        
        // Verify reaction is displayed
        composeTestRule
            .onNodeWithText("❤️")
            .assertIsDisplayed()
        
        // Verify message input is present
        composeTestRule
            .onNodeWithText("Type a message...")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("Send message")
            .assertIsDisplayed()
    }
    
    @Test
    fun messageReactions_workEndToEnd() {
        val message = createTestMessage(
            content = "This is a test message",
            senderId = "other_user"
        )
        
        var addedReaction: String? = null
        
        composeTestRule.setContent {
            ChainTheme {
                MessageBubble(
                    message = message,
                    isOwnMessage = false,
                    onReactionClick = { emoji -> addedReaction = emoji },
                    onReplyClick = {}
                )
            }
        }
        
        // Click on message to show reaction picker
        composeTestRule
            .onNodeWithText("This is a test message")
            .performClick()
        
        // Verify reaction picker appears
        composeTestRule
            .onNodeWithText("👍")
            .assertIsDisplayed()
        
        // Click on thumbs up reaction
        composeTestRule
            .onNodeWithText("👍")
            .performClick()
        
        // Verify reaction callback was triggered
        assert(addedReaction == "👍")
    }
    
    @Test
    fun messageReply_workEndToEnd() {
        val originalMessage = createTestMessage(
            content = "What time is the meeting?",
            senderId = "other_user"
        )
        
        var replyToMessage: Message? = null
        var sentMessage: String? = null
        
        composeTestRule.setContent {
            ChainTheme {
                Column {
                    MessageBubble(
                        message = originalMessage,
                        isOwnMessage = false,
                        onReactionClick = {},
                        onReplyClick = { replyToMessage = originalMessage }
                    )
                    
                    MessageInput(
                        replyToMessage = replyToMessage,
                        onSendMessage = { message -> sentMessage = message },
                        onClearReply = { replyToMessage = null }
                    )
                }
            }
        }
        
        // Click on message to show reaction picker
        composeTestRule
            .onNodeWithText("What time is the meeting?")
            .performClick()
        
        // Click on reply button (arrow emoji)
        composeTestRule
            .onNodeWithText("↩️")
            .performClick()
        
        // Verify reply was set
        assert(replyToMessage == originalMessage)
        
        // Verify reply preview appears
        composeTestRule
            .onNodeWithText("Replying to")
            .assertIsDisplayed()
        
        // Type a reply
        composeTestRule
            .onNodeWithText("Reply...")
            .performTextInput("It's at 3 PM")
        
        // Send the reply
        composeTestRule
            .onNodeWithContentDescription("Send message")
            .performClick()
        
        // Verify message was sent
        assert(sentMessage == "It's at 3 PM")
    }
    
    @Test
    fun messageTypes_displayCorrectly() {
        val messages = listOf(
            createTestMessage(
                content = "Hello there!",
                type = MessageType.TEXT
            ),
            createTestMessage(
                content = "Check out this photo",
                type = MessageType.IMAGE
            ),
            createTestMessage(
                content = "Here's a video",
                type = MessageType.VIDEO
            ),
            createTestMessage(
                content = "",
                type = MessageType.AUDIO
            ),
            createTestMessage(
                content = "document.pdf",
                type = MessageType.DOCUMENT
            )
        )
        
        composeTestRule.setContent {
            ChainTheme {
                Column {
                    messages.forEach { message ->
                        MessageBubble(
                            message = message,
                            isOwnMessage = false,
                            onReactionClick = {},
                            onReplyClick = {}
                        )
                    }
                }
            }
        }
        
        // Verify text message
        composeTestRule
            .onNodeWithText("Hello there!")
            .assertIsDisplayed()
        
        // Verify image message
        composeTestRule
            .onNodeWithText("📷 Image")
            .assertIsDisplayed()
        
        // Verify video message
        composeTestRule
            .onNodeWithText("🎥 Video")
            .assertIsDisplayed()
        
        // Verify audio message
        composeTestRule
            .onNodeWithText("Voice message")
            .assertIsDisplayed()
        
        // Verify document message
        composeTestRule
            .onNodeWithText("document.pdf")
            .assertIsDisplayed()
    }
    
    @Test
    fun messageStatus_displaysCorrectIcons() {
        val statuses = listOf(
            MessageStatus.SENDING,
            MessageStatus.SENT,
            MessageStatus.DELIVERED,
            MessageStatus.READ,
            MessageStatus.FAILED
        )
        
        composeTestRule.setContent {
            ChainTheme {
                Column {
                    statuses.forEachIndexed { index, status ->
                        MessageBubble(
                            message = createTestMessage(
                                id = "msg_$index",
                                content = "Message $index",
                                senderId = "current_user",
                                status = status
                            ),
                            isOwnMessage = true,
                            onReactionClick = {},
                            onReplyClick = {}
                        )
                    }
                }
            }
        }
        
        // Verify status icons are displayed
        composeTestRule
            .onNodeWithContentDescription("Sending")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("Sent")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("Delivered")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("Read")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("Failed")
            .assertIsDisplayed()
    }
    
    private fun createMockChatViewModel(uiState: ChatUiState): ChatViewModel {
        return object : ChatViewModel(
            getMessagesUseCase = io.mockk.mockk(),
            sendMessageUseCase = io.mockk.mockk()
        ) {
            override val uiState = kotlinx.coroutines.flow.MutableStateFlow(uiState)
        }
    }
    
    private fun createTestMessage(
        id: String = "msg_1",
        content: String = "Test message",
        senderId: String = "user_1",
        type: MessageType = MessageType.TEXT,
        status: MessageStatus = MessageStatus.DELIVERED,
        reactions: List<Reaction> = emptyList(),
        timestamp: Date = Date()
    ): Message {
        return Message(
            id = id,
            chatId = "chat_1",
            senderId = senderId,
            content = content,
            type = type,
            timestamp = timestamp,
            status = status,
            reactions = reactions
        )
    }
}