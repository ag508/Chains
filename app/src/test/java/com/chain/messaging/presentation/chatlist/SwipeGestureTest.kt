package com.chain.messaging.presentation.chatlist

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatType
import com.chain.messaging.domain.model.ChatSettings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Test for swipe gesture functionality in ChatListScreen
 * Verifies that the swipe library integration is working correctly
 */
@RunWith(AndroidJUnit4::class)
class SwipeGestureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun swipeGestureLibrary_isIntegrated() {
        // Create a test chat
        val testChat = Chat(
            id = "test-chat-1",
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            admins = listOf("user1"),
            settings = ChatSettings(
                isPinned = false,
                isArchived = false,
                isMuted = false,
                disappearingMessageTimer = null,
                wallpaper = null
            ),
            lastMessage = null,
            unreadCount = 0,
            createdAt = Date(),
            updatedAt = Date()
        )

        var pinClicked = false
        var archiveClicked = false
        var deleteClicked = false

        composeTestRule.setContent {
            SwipeableChatListItem(
                chat = testChat,
                onClick = { },
                onArchive = { archiveClicked = true },
                onDelete = { deleteClicked = true },
                onPin = { pinClicked = true }
            )
        }

        // Verify that the chat item is displayed
        composeTestRule
            .onNodeWithText("Test Chat")
            .assertExists()

        // Note: Actual swipe gesture testing would require more complex setup
        // This test primarily verifies that the SwipeableActionsBox component
        // can be composed without errors, confirming the library integration
        composeTestRule
            .onNodeWithText("Test Chat")
            .assertIsDisplayed()
    }
}