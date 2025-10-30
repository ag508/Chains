package com.chain.messaging.core.offline

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.data.local.dao.QueuedMessageDao
import com.chain.messaging.data.local.entity.QueuedMessageEntity
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class OfflineMessageQueueTest {
    
    private lateinit var offlineMessageQueue: OfflineMessageQueueImpl
    private lateinit var queuedMessageDao: QueuedMessageDao
    private lateinit var messagingService: MessagingService
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var backoffStrategy: BackoffStrategy
    private lateinit var coroutineScope: CoroutineScope
    
    private val testMessage = Message(
        id = "test-message-1",
        chatId = "test-chat",
        senderId = "test-sender",
        content = "Test message content",
        type = MessageType.TEXT,
        timestamp = LocalDateTime.now(),
        status = MessageStatus.SENDING
    )
    
    @Before
    fun setup() {
        queuedMessageDao = mockk()
        messagingService = mockk()
        networkMonitor = mockk()
        backoffStrategy = mockk()
        coroutineScope = CoroutineScope(SupervisorJob())
        
        // Setup default mocks
        every { networkMonitor.isConnected } returns flowOf(true)
        coEvery { queuedMessageDao.getAllQueuedMessages() } returns emptyList()
        coEvery { queuedMessageDao.insertQueuedMessage(any()) } just Runs
        coEvery { queuedMessageDao.deleteQueuedMessage(any()) } just Runs
        coEvery { queuedMessageDao.getQueueSize() } returns 0
        
        offlineMessageQueue = OfflineMessageQueueImpl(
            queuedMessageDao = queuedMessageDao,
            messagingService = messagingService,
            networkMonitor = networkMonitor,
            backoffStrategy = backoffStrategy,
            coroutineScope = coroutineScope
        )
    }
    
    @Test
    fun `queueMessage should save message to database`() = runTest {
        // When
        offlineMessageQueue.queueMessage(testMessage)
        
        // Then
        coVerify { queuedMessageDao.insertQueuedMessage(any()) }
    }
    
    @Test
    fun `getQueuedMessages should return messages from database`() = runTest {
        // Given
        val queuedEntity = QueuedMessageEntity(
            id = "queued-1",
            messageId = testMessage.id,
            chatId = testMessage.chatId,
            senderId = testMessage.senderId,
            content = testMessage.content,
            messageType = testMessage.type.name,
            queuedAt = LocalDateTime.now(),
            retryCount = 0,
            priority = "NORMAL"
        )
        coEvery { queuedMessageDao.getAllQueuedMessages() } returns listOf(queuedEntity)
        
        // When
        val result = offlineMessageQueue.getQueuedMessages()
        
        // Then
        assertEquals(1, result.size)
        assertEquals(testMessage.id, result[0].message.id)
    }
    
    @Test
    fun `removeFromQueue should delete message from database`() = runTest {
        // When
        offlineMessageQueue.removeFromQueue("test-id")
        
        // Then
        coVerify { queuedMessageDao.deleteQueuedMessage("test-id") }
    }
    
    @Test
    fun `clearQueue should delete all messages from database`() = runTest {
        // When
        offlineMessageQueue.clearQueue()
        
        // Then
        coVerify { queuedMessageDao.deleteAllQueuedMessages() }
    }
    
    @Test
    fun `processQueuedMessages should send messages and remove from queue on success`() = runTest {
        // Given
        val queuedMessage = QueuedMessage(
            id = "queued-1",
            message = testMessage,
            queuedAt = LocalDateTime.now(),
            retryCount = 0
        )
        coEvery { queuedMessageDao.getAllQueuedMessages() } returns listOf(queuedMessage.toEntity())
        every { queuedMessage.canRetry(backoffStrategy) } returns true
        coEvery { messagingService.sendMessage(testMessage) } just Runs
        
        // When
        offlineMessageQueue.processQueuedMessages()
        
        // Then
        coVerify { messagingService.sendMessage(testMessage) }
        coVerify { queuedMessageDao.deleteQueuedMessage("queued-1") }
    }
    
    @Test
    fun `processQueuedMessages should increment retry count on failure`() = runTest {
        // Given
        val queuedMessage = QueuedMessage(
            id = "queued-1",
            message = testMessage,
            queuedAt = LocalDateTime.now(),
            retryCount = 0
        )
        coEvery { queuedMessageDao.getAllQueuedMessages() } returns listOf(queuedMessage.toEntity())
        every { queuedMessage.canRetry(backoffStrategy) } returns true
        coEvery { messagingService.sendMessage(testMessage) } throws Exception("Network error")
        coEvery { queuedMessageDao.updateQueuedMessage(any()) } just Runs
        
        // When
        offlineMessageQueue.processQueuedMessages()
        
        // Then
        coVerify { queuedMessageDao.updateQueuedMessage(any()) }
        coVerify(exactly = 0) { queuedMessageDao.deleteQueuedMessage(any()) }
    }
    
    @Test
    fun `processQueuedMessages should remove message after max retries exceeded`() = runTest {
        // Given
        val queuedMessage = QueuedMessage(
            id = "queued-1",
            message = testMessage,
            queuedAt = LocalDateTime.now(),
            retryCount = 5, // Max retries
            maxRetries = 5
        )
        coEvery { queuedMessageDao.getAllQueuedMessages() } returns listOf(queuedMessage.toEntity())
        every { queuedMessage.canRetry(backoffStrategy) } returns true
        coEvery { messagingService.sendMessage(testMessage) } throws Exception("Network error")
        
        // When
        offlineMessageQueue.processQueuedMessages()
        
        // Then
        coVerify { queuedMessageDao.deleteQueuedMessage("queued-1") }
    }
    
    @Test
    fun `getQueueSize should return count from database`() = runTest {
        // Given
        coEvery { queuedMessageDao.getQueueSize() } returns 5
        
        // When
        val result = offlineMessageQueue.getQueueSize()
        
        // Then
        assertEquals(5, result)
    }
}

// Extension function for testing
private fun QueuedMessage.toEntity(): QueuedMessageEntity {
    return QueuedMessageEntity(
        id = id,
        messageId = message.id,
        chatId = message.chatId,
        senderId = message.senderId,
        content = message.content,
        messageType = message.type.name,
        queuedAt = queuedAt,
        retryCount = retryCount,
        lastRetryAt = lastRetryAt,
        priority = priority.name,
        maxRetries = maxRetries
    )
}