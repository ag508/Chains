package com.chain.messaging.integration

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.core.offline.*
import com.chain.messaging.data.local.dao.QueuedMessageDao
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Integration test for offline messaging functionality
 */
class OfflineMessagingIntegrationTest {
    
    private lateinit var offlineSyncService: OfflineSyncService
    private lateinit var offlineMessageQueue: OfflineMessageQueue
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var messagingService: MessagingService
    private lateinit var messageRepository: MessageRepository
    private lateinit var conflictResolver: ConflictResolver
    private lateinit var queuedMessageDao: QueuedMessageDao
    private lateinit var backoffStrategy: BackoffStrategy
    
    private val networkConnectedFlow = MutableStateFlow(true)
    
    private val testMessage = Message(
        id = "test-message-1",
        chatId = "test-chat",
        senderId = "test-sender",
        content = "Test offline message",
        type = MessageType.TEXT,
        timestamp = LocalDateTime.now(),
        status = MessageStatus.SENDING
    )
    
    @Before
    fun setup() {
        // Create real implementations where possible
        conflictResolver = ConflictResolverImpl()
        backoffStrategy = ExponentialBackoffStrategy(baseDelaySeconds = 1, maxDelaySeconds = 5)
        
        // Mock external dependencies
        queuedMessageDao = mockk()
        messagingService = mockk()
        messageRepository = mockk()
        networkMonitor = mockk()
        
        // Setup network monitor
        every { networkMonitor.isConnected } returns networkConnectedFlow
        coEvery { networkMonitor.isNetworkAvailable() } answers { networkConnectedFlow.value }
        
        // Setup message repository
        coEvery { messageRepository.getRecentMessages(any()) } returns emptyList()
        coEvery { messageRepository.updateMessage(any()) } returns Result.success(Unit)
        
        // Setup messaging service
        coEvery { messagingService.getRecentMessages(any()) } returns emptyList()
        
        // Setup queued message dao
        coEvery { queuedMessageDao.getAllQueuedMessages() } returns emptyList()
        coEvery { queuedMessageDao.insertQueuedMessage(any()) } just Runs
        coEvery { queuedMessageDao.deleteQueuedMessage(any()) } just Runs
        coEvery { queuedMessageDao.deleteAllQueuedMessages() } just Runs
        coEvery { queuedMessageDao.getQueueSize() } returns 0
        
        // Create real offline message queue
        offlineMessageQueue = OfflineMessageQueueImpl(
            queuedMessageDao = queuedMessageDao,
            messagingService = messagingService,
            networkMonitor = networkMonitor,
            backoffStrategy = backoffStrategy,
            coroutineScope = CoroutineScope(SupervisorJob())
        )
        
        // Create real offline sync service
        offlineSyncService = OfflineSyncServiceImpl(
            offlineMessageQueue = offlineMessageQueue,
            networkMonitor = networkMonitor,
            messagingService = messagingService,
            messageRepository = messageRepository,
            conflictResolver = conflictResolver,
            coroutineScope = CoroutineScope(SupervisorJob())
        )
    }
    
    @Test
    fun `complete offline to online flow should work correctly`() = runTest {
        // Initialize the service
        offlineSyncService.initialize()
        
        // Step 1: Go offline
        networkConnectedFlow.value = false
        
        // Step 2: Queue a message while offline
        offlineSyncService.handleOfflineMessage(testMessage)
        
        // Verify message was queued
        coVerify { queuedMessageDao.insertQueuedMessage(any()) }
        
        // Step 3: Verify sync status shows offline
        val offlineStatus = offlineSyncService.getSyncStatus().first()
        assertFalse(offlineStatus.isOnline)
        
        // Step 4: Go back online
        networkConnectedFlow.value = true
        coEvery { messagingService.sendMessage(testMessage) } just Runs
        
        // Step 5: Sync should happen automatically, but let's force it
        val syncResult = offlineSyncService.syncPendingMessages()
        
        // Verify sync was successful
        assertTrue(syncResult.success)
        assertEquals(0, syncResult.errors.size) // No errors since we mocked successful send
        
        // Verify message was sent
        coVerify { messagingService.sendMessage(testMessage) }
        
        // Step 6: Verify sync status shows online
        val onlineStatus = offlineSyncService.getSyncStatus().first()
        assertTrue(onlineStatus.isOnline)
        assertFalse(onlineStatus.isSyncing)
    }
    
    @Test
    fun `conflict resolution during sync should work correctly`() = runTest {
        // Initialize the service
        offlineSyncService.initialize()
        
        // Setup conflict scenario
        val localMessage = testMessage.copy(content = "Local version")
        val remoteMessage = testMessage.copy(content = "Remote version")
        
        coEvery { messageRepository.getRecentMessages(100) } returns listOf(localMessage)
        coEvery { messagingService.getRecentMessages(100) } returns listOf(remoteMessage)
        
        // Sync should resolve conflicts
        val syncResult = offlineSyncService.syncPendingMessages()
        
        // Verify conflict was resolved
        assertTrue(syncResult.success)
        assertEquals(1, syncResult.conflictsResolved)
        
        // Verify resolved message was saved (should prefer remote version)
        coVerify { messageRepository.updateMessage(match { it.content == "Remote version" }) }
    }
    
    @Test
    fun `retry mechanism should work with exponential backoff`() = runTest {
        // Setup queue with a message
        val queuedMessage = QueuedMessage(
            id = "queued-1",
            message = testMessage,
            queuedAt = LocalDateTime.now().minusSeconds(10),
            retryCount = 2
        )
        coEvery { queuedMessageDao.getAllQueuedMessages() } returns listOf(queuedMessage.toEntity())
        
        // Mock send failure
        coEvery { messagingService.sendMessage(testMessage) } throws Exception("Network error")
        coEvery { queuedMessageDao.updateQueuedMessage(any()) } just Runs
        
        // Process queue
        offlineMessageQueue.processQueuedMessages()
        
        // Verify retry count was incremented
        coVerify { queuedMessageDao.updateQueuedMessage(match { 
            it.retryCount == 3 // Should be incremented from 2 to 3
        }) }
    }
    
    @Test
    fun `message should be removed after max retries exceeded`() = runTest {
        // Setup queue with a message that has exceeded max retries
        val queuedMessage = QueuedMessage(
            id = "queued-1",
            message = testMessage,
            queuedAt = LocalDateTime.now().minusMinutes(10),
            retryCount = 5, // Max retries
            maxRetries = 5
        )
        coEvery { queuedMessageDao.getAllQueuedMessages() } returns listOf(queuedMessage.toEntity())
        
        // Mock send failure
        coEvery { messagingService.sendMessage(testMessage) } throws Exception("Network error")
        
        // Process queue
        offlineMessageQueue.processQueuedMessages()
        
        // Verify message was removed from queue
        coVerify { queuedMessageDao.deleteQueuedMessage("queued-1") }
    }
    
    @Test
    fun `offline statistics should be accurate`() = runTest {
        // Setup queue with multiple messages
        val queuedMessages = listOf(
            QueuedMessage(
                id = "1",
                message = testMessage,
                queuedAt = LocalDateTime.now().minusMinutes(10),
                retryCount = 1
            ),
            QueuedMessage(
                id = "2",
                message = testMessage.copy(id = "2"),
                queuedAt = LocalDateTime.now().minusMinutes(5),
                retryCount = 3
            ),
            QueuedMessage(
                id = "3",
                message = testMessage.copy(id = "3"),
                queuedAt = LocalDateTime.now().minusMinutes(2),
                retryCount = 6, // Failed
                maxRetries = 5
            )
        )
        coEvery { offlineMessageQueue.getQueuedMessages() } returns queuedMessages
        
        // Get statistics
        val stats = offlineSyncService.getOfflineStats()
        
        // Verify statistics
        assertEquals(3, stats.totalQueuedMessages)
        assertEquals(1, stats.failedMessages) // Only one exceeded max retries
        assertEquals(3.33, stats.averageRetryCount, 0.1) // (1 + 3 + 6) / 3
        assertNotNull(stats.oldestQueuedMessage)
    }
    
    @Test
    fun `clear offline data should reset everything`() = runTest {
        // Clear offline data
        offlineSyncService.clearOfflineData()
        
        // Verify queue was cleared
        coVerify { queuedMessageDao.deleteAllQueuedMessages() }
        
        // Verify sync status was reset
        val status = offlineSyncService.getSyncStatus().first()
        assertEquals(0, status.pendingMessages)
        assertNull(status.lastSyncTime)
    }
}

// Extension function for testing
private fun QueuedMessage.toEntity() = com.chain.messaging.data.local.entity.QueuedMessageEntity(
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