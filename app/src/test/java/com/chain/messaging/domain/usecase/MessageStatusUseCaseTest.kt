package com.chain.messaging.domain.usecase

import com.chain.messaging.core.messaging.MessageStatusTracker
import com.chain.messaging.domain.model.MessageStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MessageStatusUseCaseTest {
    
    private lateinit var messageStatusUseCase: MessageStatusUseCase
    private lateinit var messageStatusTracker: MessageStatusTracker
    
    @Before
    fun setup() {
        messageStatusTracker = mockk()
        messageStatusUseCase = MessageStatusUseCase(messageStatusTracker)
    }
    
    @Test
    fun `getMessageStatus should return status from tracker`() {
        // Given
        val messageId = "msg123"
        val status = MessageStatus.SENT
        
        every { messageStatusTracker.getMessageStatus(messageId) } returns status
        
        // When
        val result = messageStatusUseCase.getMessageStatus(messageId)
        
        // Then
        assertEquals(status, result)
        verify { messageStatusTracker.getMessageStatus(messageId) }
    }
    
    @Test
    fun `getMessageStatus should return null for unknown message`() {
        // Given
        val messageId = "unknown_msg"
        
        every { messageStatusTracker.getMessageStatus(messageId) } returns null
        
        // When
        val result = messageStatusUseCase.getMessageStatus(messageId)
        
        // Then
        assertNull(result)
        verify { messageStatusTracker.getMessageStatus(messageId) }
    }
    
    @Test
    fun `markAsDelivered should call tracker markAsDelivered`() {
        // Given
        val messageId = "msg123"
        val chatId = "chat123"
        
        every { messageStatusTracker.markAsDelivered(messageId, chatId) } returns Unit
        
        // When
        messageStatusUseCase.markAsDelivered(messageId, chatId)
        
        // Then
        verify { messageStatusTracker.markAsDelivered(messageId, chatId) }
    }
    
    @Test
    fun `markAsRead should call tracker markAsRead`() {
        // Given
        val messageId = "msg123"
        val chatId = "chat123"
        
        every { messageStatusTracker.markAsRead(messageId, chatId) } returns Unit
        
        // When
        messageStatusUseCase.markAsRead(messageId, chatId)
        
        // Then
        verify { messageStatusTracker.markAsRead(messageId, chatId) }
    }
    
    @Test
    fun `markAsFailed should call tracker markAsFailed`() {
        // Given
        val messageId = "msg123"
        
        every { messageStatusTracker.markAsFailed(messageId) } returns Unit
        
        // When
        messageStatusUseCase.markAsFailed(messageId)
        
        // Then
        verify { messageStatusTracker.markAsFailed(messageId) }
    }
    
    @Test
    fun `observeStatusUpdates should return flow from tracker`() = runTest {
        // Given
        val statusUpdates = mapOf("msg1" to MessageStatus.SENT, "msg2" to MessageStatus.DELIVERED)
        val statusFlow = MutableStateFlow(statusUpdates)
        
        every { messageStatusTracker.statusUpdates } returns statusFlow
        
        // When
        val result = messageStatusUseCase.observeStatusUpdates().first()
        
        // Then
        assertEquals(statusUpdates, result)
    }
    
    @Test
    fun `observeDeliveryReceipts should return flow from tracker`() = runTest {
        // Given
        val deliveryReceipts = mapOf("chat123" to mapOf("msg1" to 123456789L))
        val receiptsFlow = MutableStateFlow(deliveryReceipts)
        
        every { messageStatusTracker.deliveryReceipts } returns receiptsFlow
        
        // When
        val result = messageStatusUseCase.observeDeliveryReceipts().first()
        
        // Then
        assertEquals(deliveryReceipts, result)
    }
    
    @Test
    fun `observeReadReceipts should return flow from tracker`() = runTest {
        // Given
        val readReceipts = mapOf("chat123" to mapOf("msg1" to 123456789L))
        val receiptsFlow = MutableStateFlow(readReceipts)
        
        every { messageStatusTracker.readReceipts } returns receiptsFlow
        
        // When
        val result = messageStatusUseCase.observeReadReceipts().first()
        
        // Then
        assertEquals(readReceipts, result)
    }
}