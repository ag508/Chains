package com.chain.messaging.core.messaging

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.EncryptedMessage
import com.chain.messaging.core.crypto.SignalEncryptionService
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

class MessagingServiceTest {
    
    private lateinit var messagingService: MessagingService
    private lateinit var messageRepository: MessageRepository
    private lateinit var blockchainManager: BlockchainManager
    private lateinit var encryptionService: SignalEncryptionService
    
    @Before
    fun setup() {
        messageRepository = mockk()
        blockchainManager = mockk()
        encryptionService = mockk()
        
        messagingService = MessagingService(
            messageRepository,
            blockchainManager,
            encryptionService
        )
    }
    
    @Test
    fun `sendTextMessage should create and send message successfully`() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "user123"
        val content = "Hello, World!"
        val encryptedContent = "encrypted_content"
        val txHash = "tx_hash_123"
        
        coEvery { messageRepository.saveMessage(any()) } returns Result.success(Unit)
        coEvery { encryptionService.encryptMessage(content, chatId) } returns Result.success(
            EncryptedMessage(encryptedContent, MessageType.TEXT, "key123", System.currentTimeMillis())
        )
        coEvery { blockchainManager.sendMessage(any(), any(), any(), any()) } returns Result.success(txHash)
        
        // When
        val result = messagingService.sendTextMessage(chatId, senderId, content)
        
        // Then
        assertTrue(result.isSuccess)
        val message = result.getOrNull()
        assertNotNull(message)
        assertEquals(chatId, message?.chatId)
        assertEquals(senderId, message?.senderId)
        assertEquals(content, message?.content)
        assertEquals(MessageType.TEXT, message?.type)
        
        coVerify { messageRepository.saveMessage(any()) }
        coVerify { encryptionService.encryptMessage(content, chatId) }
        coVerify { blockchainManager.sendMessage(senderId, chatId, encryptedContent, MessageType.TEXT) }
    }
    
    @Test
    fun `sendMessage should handle encryption failure`() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "user123"
        val content = "Hello, World!"
        val encryptionError = Exception("Encryption failed")
        
        coEvery { messageRepository.saveMessage(any()) } returns Result.success(Unit)
        coEvery { encryptionService.encryptMessage(content, chatId) } returns Result.failure(encryptionError)
        
        // When
        val result = messagingService.sendTextMessage(chatId, senderId, content)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(encryptionError, result.exceptionOrNull())
        
        coVerify { messageRepository.saveMessage(any()) }
        coVerify { encryptionService.encryptMessage(content, chatId) }
        coVerify(exactly = 0) { blockchainManager.sendMessage(any(), any(), any(), any()) }
    }
    
    @Test
    fun `sendMessage should handle blockchain failure`() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "user123"
        val content = "Hello, World!"
        val encryptedContent = "encrypted_content"
        val blockchainError = Exception("Blockchain failed")
        
        coEvery { messageRepository.saveMessage(any()) } returns Result.success(Unit)
        coEvery { encryptionService.encryptMessage(content, chatId) } returns Result.success(
            EncryptedMessage(encryptedContent, MessageType.TEXT, "key123", System.currentTimeMillis())
        )
        coEvery { blockchainManager.sendMessage(any(), any(), any(), any()) } returns Result.failure(blockchainError)
        
        // When
        val result = messagingService.sendTextMessage(chatId, senderId, content)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(blockchainError, result.exceptionOrNull())
        
        coVerify { messageRepository.saveMessage(any()) }
        coVerify { encryptionService.encryptMessage(content, chatId) }
        coVerify { blockchainManager.sendMessage(senderId, chatId, encryptedContent, MessageType.TEXT) }
    }
    
    @Test
    fun `getMessages should return messages from repository`() = runTest {
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
        
        coEvery { messageRepository.getMessages(chatId, 50, 0) } returns messages
        
        // When
        val result = messagingService.getMessages(chatId)
        
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
        val result = messagingService.observeMessages(chatId).first()
        
        // Then
        assertEquals(messages, result)
    }
    
    @Test
    fun `markMessagesAsRead should update repository and status`() = runTest {
        // Given
        val messageIds = listOf("msg1", "msg2")
        
        coEvery { messageRepository.markMessagesAsRead(messageIds) } returns Result.success(Unit)
        
        // When
        messagingService.markMessagesAsRead(messageIds)
        
        // Then
        coVerify { messageRepository.markMessagesAsRead(messageIds) }
        
        // Verify status updates
        val statusUpdates = messagingService.messageStatusUpdates.value
        messageIds.forEach { messageId ->
            assertEquals(MessageStatus.READ, statusUpdates[messageId])
        }
    }
    
    @Test
    fun `searchMessages should return search results from repository`() = runTest {
        // Given
        val query = "hello"
        val searchResults = listOf(
            Message(
                id = "msg1",
                chatId = "chat1",
                senderId = "user1",
                content = "Hello world",
                type = MessageType.TEXT,
                timestamp = Date(),
                status = MessageStatus.SENT,
                replyTo = null,
                reactions = emptyList(),
                isEncrypted = true
            )
        )
        
        coEvery { messageRepository.searchMessages(query) } returns searchResults
        
        // When
        val result = messagingService.searchMessages(query)
        
        // Then
        assertEquals(searchResults, result)
        coVerify { messageRepository.searchMessages(query) }
    }
    
    @Test
    fun `deleteMessages should delete from repository`() = runTest {
        // Given
        val messageIds = listOf("msg1", "msg2")
        
        coEvery { messageRepository.deleteMessages(messageIds) } returns Result.success(Unit)
        
        // When
        val result = messagingService.deleteMessages(messageIds)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { messageRepository.deleteMessages(messageIds) }
    }
}