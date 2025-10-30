package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class GetMessagesUseCaseTest {
    
    private lateinit var getMessagesUseCase: GetMessagesUseCase
    private lateinit var messageRepository: MessageRepository
    
    @Before
    fun setup() {
        messageRepository = mockk()
        getMessagesUseCase = GetMessagesUseCase(messageRepository)
    }
    
    @Test
    fun `execute should return messages from repository`() = runTest {
        // Given
        val chatId = "chat123"
        val limit = 20
        val offset = 10
        
        val messages = listOf(
            Message(
                id = "msg1",
                chatId = chatId,
                senderId = "user1",
                content = "Hello",
                type = MessageType.TEXT,
                timestamp = Date(),
                status = MessageStatus.SENT,
                replyTo = null,
                reactions = emptyList(),
                isEncrypted = true
            ),
            Message(
                id = "msg2",
                chatId = chatId,
                senderId = "user2",
                content = "Hi there",
                type = MessageType.TEXT,
                timestamp = Date(),
                status = MessageStatus.DELIVERED,
                replyTo = null,
                reactions = emptyList(),
                isEncrypted = true
            )
        )
        
        coEvery { messageRepository.getMessages(chatId, limit, offset) } returns messages
        
        // When
        val result = getMessagesUseCase.execute(chatId, limit, offset)
        
        // Then
        assertEquals(messages, result)
        coVerify { messageRepository.getMessages(chatId, limit, offset) }
    }
    
    @Test
    fun `execute with default parameters should use default limit and offset`() = runTest {
        // Given
        val chatId = "chat123"
        val messages = emptyList<Message>()
        
        coEvery { messageRepository.getMessages(chatId, 50, 0) } returns messages
        
        // When
        val result = getMessagesUseCase.execute(chatId)
        
        // Then
        assertEquals(messages, result)
        coVerify { messageRepository.getMessages(chatId, 50, 0) }
    }
    
    @Test
    fun `observeMessages should return flow from repository`() = runTest {
        // Given
        val chatId = "chat123"
        val messages = listOf(
            Message(
                id = "msg1",
                chatId = chatId,
                senderId = "user1",
                content = "Hello",
                type = MessageType.TEXT,
                timestamp = Date(),
                status = MessageStatus.SENT,
                replyTo = null,
                reactions = emptyList(),
                isEncrypted = true
            )
        )
        
        every { messageRepository.observeMessages(chatId) } returns flowOf(messages)
        
        // When
        val result = getMessagesUseCase.observeMessages(chatId).first()
        
        // Then
        assertEquals(messages, result)
    }
    
    @Test
    fun `execute should handle empty result from repository`() = runTest {
        // Given
        val chatId = "chat123"
        val emptyMessages = emptyList<Message>()
        
        coEvery { messageRepository.getMessages(chatId, 50, 0) } returns emptyMessages
        
        // When
        val result = getMessagesUseCase.execute(chatId)
        
        // Then
        assertEquals(emptyMessages, result)
        assertTrue(result.isEmpty())
        coVerify { messageRepository.getMessages(chatId, 50, 0) }
    }
}