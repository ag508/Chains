package com.chain.messaging.core.messaging

import com.chain.messaging.domain.model.MessageStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MessageStatusTrackerTest {
    
    private lateinit var messageStatusTracker: MessageStatusTracker
    
    @Before
    fun setup() {
        messageStatusTracker = MessageStatusTracker()
    }
    
    @Test
    fun `updateMessageStatus should update status and notify observers`() = runTest {
        // Given
        val messageId = "msg123"
        val status = MessageStatus.SENT
        
        // When
        messageStatusTracker.updateMessageStatus(messageId, status)
        
        // Then
        assertEquals(status, messageStatusTracker.getMessageStatus(messageId))
        
        val statusUpdates = messageStatusTracker.statusUpdates.first()
        assertEquals(status, statusUpdates[messageId])
    }
    
    @Test
    fun `getMessageStatus should return null for unknown message`() {
        // Given
        val messageId = "unknown_msg"
        
        // When
        val status = messageStatusTracker.getMessageStatus(messageId)
        
        // Then
        assertNull(status)
    }
    
    @Test
    fun `markAsSent should update status to SENT`() = runTest {
        // Given
        val messageId = "msg123"
        
        // When
        messageStatusTracker.markAsSent(messageId)
        
        // Then
        assertEquals(MessageStatus.SENT, messageStatusTracker.getMessageStatus(messageId))
    }
    
    @Test
    fun `markAsDelivered should update status and delivery receipt`() = runTest {
        // Given
        val messageId = "msg123"
        val chatId = "chat123"
        val timestamp = System.currentTimeMillis()
        
        // When
        messageStatusTracker.markAsDelivered(messageId, chatId, timestamp)
        
        // Then
        assertEquals(MessageStatus.DELIVERED, messageStatusTracker.getMessageStatus(messageId))
        assertEquals(timestamp, messageStatusTracker.getDeliveryReceipt(messageId, chatId))
        
        val deliveryReceipts = messageStatusTracker.deliveryReceipts.first()
        assertEquals(timestamp, deliveryReceipts[chatId]?.get(messageId))
    }
    
    @Test
    fun `markAsRead should update status and read receipt`() = runTest {
        // Given
        val messageId = "msg123"
        val chatId = "chat123"
        val timestamp = System.currentTimeMillis()
        
        // When
        messageStatusTracker.markAsRead(messageId, chatId, timestamp)
        
        // Then
        assertEquals(MessageStatus.READ, messageStatusTracker.getMessageStatus(messageId))
        assertEquals(timestamp, messageStatusTracker.getReadReceipt(messageId, chatId))
        
        val readReceipts = messageStatusTracker.readReceipts.first()
        assertEquals(timestamp, readReceipts[chatId]?.get(messageId))
    }
    
    @Test
    fun `markAsFailed should update status to FAILED`() = runTest {
        // Given
        val messageId = "msg123"
        
        // When
        messageStatusTracker.markAsFailed(messageId)
        
        // Then
        assertEquals(MessageStatus.FAILED, messageStatusTracker.getMessageStatus(messageId))
    }
    
    @Test
    fun `processDeliveryReceipt should mark as delivered`() = runTest {
        // Given
        val messageId = "msg123"
        val chatId = "chat123"
        val timestamp = System.currentTimeMillis()
        
        // When
        messageStatusTracker.processDeliveryReceipt(messageId, chatId, timestamp)
        
        // Then
        assertEquals(MessageStatus.DELIVERED, messageStatusTracker.getMessageStatus(messageId))
        assertEquals(timestamp, messageStatusTracker.getDeliveryReceipt(messageId, chatId))
    }
    
    @Test
    fun `processReadReceipt should mark as read`() = runTest {
        // Given
        val messageId = "msg123"
        val chatId = "chat123"
        val timestamp = System.currentTimeMillis()
        
        // When
        messageStatusTracker.processReadReceipt(messageId, chatId, timestamp)
        
        // Then
        assertEquals(MessageStatus.READ, messageStatusTracker.getMessageStatus(messageId))
        assertEquals(timestamp, messageStatusTracker.getReadReceipt(messageId, chatId))
    }
    
    @Test
    fun `getDeliveryReceipt should return null for unknown message`() {
        // Given
        val messageId = "unknown_msg"
        val chatId = "chat123"
        
        // When
        val receipt = messageStatusTracker.getDeliveryReceipt(messageId, chatId)
        
        // Then
        assertNull(receipt)
    }
    
    @Test
    fun `getReadReceipt should return null for unknown message`() {
        // Given
        val messageId = "unknown_msg"
        val chatId = "chat123"
        
        // When
        val receipt = messageStatusTracker.getReadReceipt(messageId, chatId)
        
        // Then
        assertNull(receipt)
    }
    
    @Test
    fun `multiple messages in same chat should track separately`() = runTest {
        // Given
        val msg1 = "msg1"
        val msg2 = "msg2"
        val chatId = "chat123"
        val timestamp1 = System.currentTimeMillis()
        val timestamp2 = timestamp1 + 1000
        
        // When
        messageStatusTracker.markAsDelivered(msg1, chatId, timestamp1)
        messageStatusTracker.markAsRead(msg2, chatId, timestamp2)
        
        // Then
        assertEquals(MessageStatus.DELIVERED, messageStatusTracker.getMessageStatus(msg1))
        assertEquals(MessageStatus.READ, messageStatusTracker.getMessageStatus(msg2))
        
        assertEquals(timestamp1, messageStatusTracker.getDeliveryReceipt(msg1, chatId))
        assertEquals(timestamp2, messageStatusTracker.getReadReceipt(msg2, chatId))
    }
    
    @Test
    fun `cleanup should maintain current messages`() = runTest {
        // Given
        val messageId = "msg123"
        val status = MessageStatus.SENT
        
        messageStatusTracker.updateMessageStatus(messageId, status)
        
        // When
        messageStatusTracker.cleanup(System.currentTimeMillis())
        
        // Then - message should still be tracked (cleanup logic is basic in current implementation)
        assertEquals(status, messageStatusTracker.getMessageStatus(messageId))
    }
}