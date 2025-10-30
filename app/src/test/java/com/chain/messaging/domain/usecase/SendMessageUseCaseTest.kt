package com.chain.messaging.domain.usecase

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class SendMessageUseCaseTest {
    
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var messagingService: MessagingService
    
    @Before
    fun setup() {
        messagingService = mockk()
        sendMessageUseCase = SendMessageUseCase(messagingService)
    }
    
    @Test
    fun `execute should send message through messaging service`() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "user123"
        val content = "Hello, World!"
        val type = MessageType.TEXT
        val replyTo = "msg456"
        
        val expectedMessage = Message(
            id = "msg123",
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = type,
            timestamp = Date(),
            status = MessageStatus.SENT,
            replyTo = replyTo,
            reactions = emptyList(),
            isEncrypted = true
        )
        
        coEvery { 
            messagingService.sendMessage(chatId, senderId, content, type, replyTo) 
        } returns Result.success(expectedMessage)
        
        // When
        val result = sendMessageUseCase.execute(chatId, senderId, content, type, replyTo)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedMessage, result.getOrNull())
        
        coVerify { messagingService.sendMessage(chatId, senderId, content, type, replyTo) }
    }
    
    @Test
    fun `execute should handle messaging service failure`() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "user123"
        val content = "Hello, World!"
        val error = Exception("Messaging failed")
        
        coEvery { 
            messagingService.sendMessage(chatId, senderId, content, MessageType.TEXT, null) 
        } returns Result.failure(error)
        
        // When
        val result = sendMessageUseCase.execute(chatId, senderId, content)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        
        coVerify { messagingService.sendMessage(chatId, senderId, content, MessageType.TEXT, null) }
    }
    
    @Test
    fun `sendTextMessage should send text message through messaging service`() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "user123"
        val content = "Hello, World!"
        val replyTo = "msg456"
        
        val expectedMessage = Message(
            id = "msg123",
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENT,
            replyTo = replyTo,
            reactions = emptyList(),
            isEncrypted = true
        )
        
        coEvery { 
            messagingService.sendTextMessage(chatId, senderId, content, replyTo) 
        } returns Result.success(expectedMessage)
        
        // When
        val result = sendMessageUseCase.sendTextMessage(chatId, senderId, content, replyTo)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedMessage, result.getOrNull())
        
        coVerify { messagingService.sendTextMessage(chatId, senderId, content, replyTo) }
    }
    
    @Test
    fun `execute with default parameters should use TEXT type and null replyTo`() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "user123"
        val content = "Hello, World!"
        
        val expectedMessage = Message(
            id = "msg123",
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENT,
            replyTo = null,
            reactions = emptyList(),
            isEncrypted = true
        )
        
        coEvery { 
            messagingService.sendMessage(chatId, senderId, content, MessageType.TEXT, null) 
        } returns Result.success(expectedMessage)
        
        // When
        val result = sendMessageUseCase.execute(chatId, senderId, content)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedMessage, result.getOrNull())
        
        coVerify { messagingService.sendMessage(chatId, senderId, content, MessageType.TEXT, null) }
    }
}