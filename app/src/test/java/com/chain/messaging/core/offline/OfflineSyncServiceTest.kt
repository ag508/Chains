package com.chain.messaging.core.offline

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class OfflineSyncServiceTest {
    
    private lateinit var offlineSyncService: OfflineSyncServiceImpl
    private lateinit var offlineMessageQueue: OfflineMessageQueue
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var messagingService: MessagingService
    private lateinit var messageRepository: MessageRepository
    private lateinit var conflictResolver: ConflictResolver
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
        offlineMessageQueue = mockk()
        networkMonitor = mockk()
        messagingService = mockk()
        messageRepository = mockk()
        conflictResolver = mockk()
        coroutineScope = CoroutineScope(SupervisorJob())
        
        // Setup default mocks
        every { networkMonitor.isConnected } returns flowOf(true)
        every { offlineMessageQueue.getQueuedMessagesFlow() } returns flowOf(emptyList())
        coEvery { networkMonitor.isNetworkAvailable() } returns true
        coEvery { offlineMessageQueue.getQueuedMessages() } returns emptyList()
        coEvery { messageRepository.getRecentMessages(any()) } returns emptyList()
        coEvery { messagingService.getRecentMessages(any()) } returns emptyList()
        coEvery { conflictResolver.resolveConflicts(any(), any()) } returns ConflictResolution(
            resolvedMessages = emptyList(),
            conflictsFound = 0,
            conflictsResolved = 0,
            unresolvedConflicts = emptyList()
        )
        
        offlineSyncService = OfflineSyncServiceImpl(
            offlineMessageQueue = offlineMessageQueue,
            networkMonitor = networkMonitor,
            messagingService = messagingService,
            messageRepository = messageRepository,
            conflictResolver = conflictResolver,
            coroutineScope = coroutineScope
        )
    }
    
    @Test
    fun `handleOfflineMessage should queue message`() = runTest {
        // Given
        coEvery { offlineMessageQueue.queueMessage(any()) } just Runs
        
        // When
        offlineSyncService.handleOfflineMessage(testMessage)
        
        // Then
        coVerify { offlineMessageQueue.queueMessage(testMessage) }
    }
    
    @Test
    fun `syncPendingMessages should return failure when network unavailable`() = runTest {
        // Given
        coEvery { networkMonitor.isNetworkAvailable() } returns false
        
        // When
        val result = offlineSyncService.syncPendingMessages()
        
        // Then
        assertFalse(result.success)
        assertEquals(0, result.messagesSynced)
        assertTrue(result.errors.isNotEmpty())
        assertEquals("Network not available", result.errors[0].error)
    }
    
    @Test
    fun `syncPendingMessages should process queued messages when network available`() = runTest {
        // Given
        val queuedMessage = QueuedMessage(
            id = "queued-1",
            message = testMessage,
            queuedAt = LocalDateTime.now()
        )
        coEvery { offlineMessageQueue.getQueuedMessages() } returns listOf(queuedMessage)
        coEvery { messagingService.sendMessage(testMessage) } just Runs
        coEvery { offlineMessageQueue.removeFromQueue("queued-1") } just Runs
        
        // When
        val result = offlineSyncService.syncPendingMessages()
        
        // Then
        assertTrue(result.success)
        assertEquals(1, result.messagesSynced)
        assertTrue(result.errors.isEmpty())
        coVerify { messagingService.sendMessage(testMessage) }
        coVerify { offlineMessageQueue.removeFromQueue("queued-1") }
    }
    
    @Test
    fun `syncPendingMessages should handle send failures`() = runTest {
        // Given
        val queuedMessage = QueuedMessage(
            id = "queued-1",
            message = testMessage,
            queuedAt = LocalDateTime.now()
        )
        coEvery { offlineMessageQueue.getQueuedMessages() } returns listOf(queuedMessage)
        coEvery { messagingService.sendMessage(testMessage) } throws Exception("Send failed")
        
        // When
        val result = offlineSyncService.syncPendingMessages()
        
        // Then
        assertFalse(result.success)
        assertEquals(0, result.messagesSynced)
        assertEquals(1, result.errors.size)
        assertEquals("Send failed", result.errors[0].error)
        coVerify(exactly = 0) { offlineMessageQueue.removeFromQueue(any()) }
    }
    
    @Test
    fun `syncPendingMessages should resolve conflicts`() = runTest {
        // Given
        val localMessages = listOf(testMessage)
        val remoteMessages = listOf(testMessage.copy(content = "Remote content"))
        val conflictResolution = ConflictResolution(
            resolvedMessages = listOf(testMessage.copy(content = "Resolved content")),
            conflictsFound = 1,
            conflictsResolved = 1,
            unresolvedConflicts = emptyList()
        )
        
        coEvery { messageRepository.getRecentMessages(100) } returns localMessages
        coEvery { messagingService.getRecentMessages(100) } returns remoteMessages
        coEvery { conflictResolver.resolveConflicts(localMessages, remoteMessages) } returns conflictResolution
        coEvery { messageRepository.updateMessage(any()) } returns Result.success(Unit)
        
        // When
        val result = offlineSyncService.syncPendingMessages()
        
        // Then
        assertEquals(1, result.conflictsResolved)
        coVerify { messageRepository.updateMessage(any()) }
    }
    
    @Test
    fun `getOfflineStats should return correct statistics`() = runTest {
        // Given
        val queuedMessages = listOf(
            QueuedMessage(
                id = "1",
                message = testMessage,
                queuedAt = LocalDateTime.now().minusMinutes(10),
                retryCount = 2
            ),
            QueuedMessage(
                id = "2",
                message = testMessage.copy(id = "2"),
                queuedAt = LocalDateTime.now().minusMinutes(5),
                retryCount = 6, // Exceeded max retries
                maxRetries = 5
            )
        )
        coEvery { offlineMessageQueue.getQueuedMessages() } returns queuedMessages
        
        // When
        val stats = offlineSyncService.getOfflineStats()
        
        // Then
        assertEquals(2, stats.totalQueuedMessages)
        assertEquals(1, stats.failedMessages)
        assertEquals(4.0, stats.averageRetryCount, 0.1) // (2 + 6) / 2 = 4
        assertNotNull(stats.oldestQueuedMessage)
    }
    
    @Test
    fun `clearOfflineData should clear queue and reset state`() = runTest {
        // Given
        coEvery { offlineMessageQueue.clearQueue() } just Runs
        
        // When
        offlineSyncService.clearOfflineData()
        
        // Then
        coVerify { offlineMessageQueue.clearQueue() }
    }
    
    @Test
    fun `forcSync should call syncPendingMessages`() = runTest {
        // Given
        coEvery { offlineMessageQueue.getQueuedMessages() } returns emptyList()
        
        // When
        val result = offlineSyncService.forcSync()
        
        // Then
        assertNotNull(result)
        assertTrue(result.success)
    }
}