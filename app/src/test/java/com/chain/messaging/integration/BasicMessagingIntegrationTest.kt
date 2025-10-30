package com.chain.messaging.integration

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.messaging.MessageStatusTracker
import com.chain.messaging.core.messaging.TypingIndicatorService
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.usecase.GetMessagesUseCase
import com.chain.messaging.domain.usecase.MessageStatusUseCase
import com.chain.messaging.domain.usecase.SendMessageUseCase
import com.chain.messaging.domain.usecase.TypingIndicatorUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Integration test for basic messaging functionality
 * Tests the complete flow from sending a message to status tracking
 */
class BasicMessagingIntegrationTest {
    
    @Test
    fun `complete messaging flow should work end to end`() = runTest {
        // Given - Mock dependencies
        val messagingService = mockk<MessagingService>()
        val typingIndicatorService = TypingIndicatorService()
        val messageStatusTracker = MessageStatusTracker()
        
        // Create use cases
        val sendMessageUseCase = SendMessageUseCase(messagingService)
        val typingIndicatorUseCase = TypingIndicatorUseCase(typingIndicatorService)
        val messageStatusUseCase = MessageStatusUseCase(messageStatusTracker)
        
        // Mock successful message sending
        val chatId = "chat123"
        val senderId = "user123"
        val content = "Hello, World!"
        
        val mockMessage = mockk<com.chain.messaging.domain.model.Message> {
            coEvery { id } returns "msg123"
            coEvery { this@mockk.chatId } returns chatId
            coEvery { this@mockk.senderId } returns senderId
            coEvery { this@mockk.content } returns content
            coEvery { type } returns MessageType.TEXT
            coEvery { status } returns MessageStatus.SENT
        }
        
        coEvery { messagingService.sendTextMessage(chatId, senderId, content, null) } returns Result.success(mockMessage)
        
        // When - Start typing
        typingIndicatorUseCase.startTyping(chatId, senderId)
        
        // Then - User should be typing
        assertTrue(typingIndicatorUseCase.getTypingUsers(chatId).contains(senderId))
        
        // When - Send message
        val result = sendMessageUseCase.sendTextMessage(chatId, senderId, content)
        
        // Then - Message should be sent successfully
        assertTrue(result.isSuccess)
        assertEquals(mockMessage, result.getOrNull())
        
        // When - Stop typing
        typingIndicatorUseCase.stopTyping(chatId, senderId)
        
        // Then - User should no longer be typing
        assertFalse(typingIndicatorUseCase.getTypingUsers(chatId).contains(senderId))
        
        // When - Update message status
        messageStatusUseCase.markAsDelivered("msg123", chatId)
        
        // Then - Status should be updated
        assertEquals(MessageStatus.DELIVERED, messageStatusUseCase.getMessageStatus("msg123"))
        
        // When - Mark as read
        messageStatusUseCase.markAsRead("msg123", chatId)
        
        // Then - Status should be read
        assertEquals(MessageStatus.READ, messageStatusUseCase.getMessageStatus("msg123"))
    }
    
    @Test
    fun `typing indicators should work correctly`() = runTest {
        // Given
        val typingIndicatorService = TypingIndicatorService()
        val typingIndicatorUseCase = TypingIndicatorUseCase(typingIndicatorService)
        
        val chatId = "chat123"
        val user1 = "user1"
        val user2 = "user2"
        
        // When - Multiple users start typing
        typingIndicatorUseCase.startTyping(chatId, user1)
        typingIndicatorUseCase.startTyping(chatId, user2)
        
        // Then - Both users should be typing
        val typingUsers = typingIndicatorUseCase.getTypingUsers(chatId)
        assertTrue(typingUsers.contains(user1))
        assertTrue(typingUsers.contains(user2))
        assertEquals(2, typingUsers.size)
        
        // When - One user stops typing
        typingIndicatorUseCase.stopTyping(chatId, user1)
        
        // Then - Only one user should be typing
        val remainingTypingUsers = typingIndicatorUseCase.getTypingUsers(chatId)
        assertFalse(remainingTypingUsers.contains(user1))
        assertTrue(remainingTypingUsers.contains(user2))
        assertEquals(1, remainingTypingUsers.size)
        
        // When - Set user online status
        typingIndicatorUseCase.setUserOnline(user1, true)
        
        // Then - User should be online
        assertTrue(typingIndicatorUseCase.isUserOnline(user1))
        
        // Observe online users
        val onlineUsers = typingIndicatorUseCase.observeOnlineUsers().first()
        assertEquals(true, onlineUsers[user1])
    }
    
    @Test
    fun `message status tracking should work correctly`() = runTest {
        // Given
        val messageStatusTracker = MessageStatusTracker()
        val messageStatusUseCase = MessageStatusUseCase(messageStatusTracker)
        
        val messageId = "msg123"
        val chatId = "chat123"
        
        // When - Update message status through different states
        messageStatusTracker.updateMessageStatus(messageId, MessageStatus.SENDING)
        assertEquals(MessageStatus.SENDING, messageStatusUseCase.getMessageStatus(messageId))
        
        messageStatusTracker.markAsSent(messageId)
        assertEquals(MessageStatus.SENT, messageStatusUseCase.getMessageStatus(messageId))
        
        messageStatusUseCase.markAsDelivered(messageId, chatId)
        assertEquals(MessageStatus.DELIVERED, messageStatusUseCase.getMessageStatus(messageId))
        
        messageStatusUseCase.markAsRead(messageId, chatId)
        assertEquals(MessageStatus.READ, messageStatusUseCase.getMessageStatus(messageId))
        
        // Then - Status updates should be observable
        val statusUpdates = messageStatusUseCase.observeStatusUpdates().first()
        assertEquals(MessageStatus.READ, statusUpdates[messageId])
        
        // And - Delivery and read receipts should be tracked
        val deliveryReceipts = messageStatusUseCase.observeDeliveryReceipts().first()
        assertNotNull(deliveryReceipts[chatId]?.get(messageId))
        
        val readReceipts = messageStatusUseCase.observeReadReceipts().first()
        assertNotNull(readReceipts[chatId]?.get(messageId))
    }
    
    @Test
    fun `message status should handle failure cases`() = runTest {
        // Given
        val messageStatusTracker = MessageStatusTracker()
        val messageStatusUseCase = MessageStatusUseCase(messageStatusTracker)
        
        val messageId = "msg123"
        
        // When - Mark message as failed
        messageStatusUseCase.markAsFailed(messageId)
        
        // Then - Status should be FAILED
        assertEquals(MessageStatus.FAILED, messageStatusUseCase.getMessageStatus(messageId))
        
        // When - Try to get status for unknown message
        val unknownStatus = messageStatusUseCase.getMessageStatus("unknown_msg")
        
        // Then - Should return null
        assertNull(unknownStatus)
    }
}