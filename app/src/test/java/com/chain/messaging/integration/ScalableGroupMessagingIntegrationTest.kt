package com.chain.messaging.integration

import com.chain.messaging.core.group.*
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.p2p.P2PManager
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.ChatRepository
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for the complete scalable group messaging system.
 * Tests end-to-end functionality including distribution, optimization, and history management.
 */
class ScalableGroupMessagingIntegrationTest {
    
    private lateinit var chatRepository: ChatRepository
    private lateinit var messageRepository: MessageRepository
    private lateinit var blockchainManager: BlockchainManager
    private lateinit var encryptionService: SignalEncryptionService
    private lateinit var p2pManager: P2PManager
    
    private lateinit var distributor: GroupMessageDistributor
    private lateinit var optimizer: MessageDeliveryOptimizer
    private lateinit var historyManager: GroupHistoryManager
    
    @BeforeEach
    fun setup() {
        chatRepository = mockk()
        messageRepository = mockk()
        blockchainManager = mockk()
        encryptionService = mockk()
        p2pManager = mockk()
        
        distributor = GroupMessageDistributorImpl(
            chatRepository,
            blockchainManager,
            encryptionService,
            p2pManager
        )
        
        optimizer = MessageDeliveryOptimizerImpl(p2pManager)
        historyManager = GroupHistoryManagerImpl(messageRepository)
        
        setupMocks()
    }
    
    private fun setupMocks() {
        coEvery { blockchainManager.sendMessage(any()) } returns "tx-hash-${UUID.randomUUID()}"
        every { encryptionService.encryptMessage(any(), any()) } returns Result.success(
            mockk {
                every { ciphertext } returns "encrypted-content-${UUID.randomUUID()}".toByteArray()
            }
        )
        
        coEvery { messageRepository.saveMessage(any()) } returns Result.success(Unit)
        coEvery { messageRepository.insertMessage(any()) } returns Unit
        coEvery { messageRepository.deleteMessages(any()) } returns Result.success(Unit)
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    fun `test complete workflow for large group message distribution`() = runTest {
        // Arrange - Create a large group with 10K members
        val memberCount = 10000
        val groupId = "integration-test-group"
        val senderId = "admin-user"
        val recipients = (1..memberCount).map { "user-$it" }
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Integration Test Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        
        val message = Message(
            id = UUID.randomUUID().toString(),
            chatId = groupId,
            senderId = senderId,
            content = "Integration test message for large group",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENDING,
            reactions = emptyList(),
            isEncrypted = true
        )
        
        // Act - Execute complete workflow
        val totalTime = measureTimeMillis {
            // Step 1: Optimize delivery order
            val optimizedRecipients = optimizer.optimizeDeliveryOrder(recipients, message)
            assertEquals(memberCount, optimizedRecipients.size)
            
            // Step 2: Calculate optimal batch size
            val networkConditions = NetworkConditions(
                bandwidth = 10_000_000L, // 10 Mbps
                latency = 50L,
                packetLoss = 0.005f,
                connectionStability = 0.95f,
                peerCount = 200
            )
            
            val batchSize = optimizer.calculateOptimalBatchSize(memberCount, networkConditions)
            assertTrue(batchSize > 0)
            
            // Step 3: Apply rate limiting
            val throttling = optimizer.applyRateLimiting(150f, 0.4f)
            assertTrue(throttling.maxConcurrentDeliveries > 0)
            
            // Step 4: Distribute message
            val distributionResult = distributor.distributeMessage(groupId, message, senderId)
            assertTrue(distributionResult.isSuccess)
            
            val result = distributionResult.getOrThrow()
            assertEquals(memberCount, result.totalRecipients)
            assertEquals(DistributionStrategy.TREE_ROUTING, result.strategy)
            
            // Step 5: Track distribution progress
            val progressFlow = distributor.observeDistributionProgress(result.distributionId)
            // In a real scenario, we would collect progress updates
        }
        
        println("Complete workflow time for $memberCount members: ${totalTime}ms")
        assertTrue(totalTime < 45000, "Complete workflow should finish within 45 seconds")
    }
    
    @Test
    fun `test history synchronization for new member in large group`() = runTest {
        // Arrange
        val groupId = "history-sync-group"
        val newUserId = "new-member-123"
        val messageCount = 50000
        
        // Create mock message history
        val messages = (1..messageCount).map { index ->
            Message(
                id = "msg-$index",
                chatId = groupId,
                senderId = "user-${index % 1000}",
                content = "Historical message $index",
                type = MessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - (messageCount - index) * 1000L),
                status = MessageStatus.SENT,
                reactions = emptyList(),
                isEncrypted = true
            )
        }
        
        coEvery { messageRepository.getMessages(any(), any(), any()) } returns messages
        
        // Act
        val syncTime = measureTimeMillis {
            val syncResult = historyManager.synchronizeHistoryForNewMember(
                groupId = groupId,
                userId = newUserId,
                fromTimestamp = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000) // 7 days ago
            )
            
            assertTrue(syncResult.isSuccess)
            val result = syncResult.getOrThrow()
            assertTrue(result.messagesSynced > 0)
            assertTrue(result.bytesTransferred > 0)
        }
        
        println("History sync time for $messageCount messages: ${syncTime}ms")
        assertTrue(syncTime < 30000, "History sync should complete within 30 seconds")
    }
    
    @Test
    fun `test message distribution with delivery optimization`() = runTest {
        // Arrange
        val memberCount = 5000
        val groupId = "optimized-delivery-group"
        val senderId = "sender-1"
        val recipients = (1..memberCount).map { "user-$it" }
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Optimized Delivery Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        
        val message = Message(
            id = UUID.randomUUID().toString(),
            chatId = groupId,
            senderId = senderId,
            content = "Optimized delivery test message",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENDING,
            reactions = emptyList(),
            isEncrypted = true
        )
        
        // Act - Test with optimization
        val optimizedTime = measureTimeMillis {
            // Pre-optimize delivery order
            val optimizedRecipients = optimizer.optimizeDeliveryOrder(recipients, message)
            
            // Cache encrypted message to avoid re-encryption
            val encryptedContent = "cached-encrypted-content".toByteArray()
            optimizer.cacheEncryptedMessage(message.id, encryptedContent, optimizedRecipients)
            
            // Distribute with optimizations
            val result = distributor.distributeMessage(groupId, message, senderId)
            assertTrue(result.isSuccess)
        }
        
        println("Optimized delivery time for $memberCount members: ${optimizedTime}ms")
        assertTrue(optimizedTime < 20000, "Optimized delivery should complete within 20 seconds")
    }
    
    @Test
    fun `test group history management with pruning`() = runTest {
        // Arrange
        val groupId = "pruning-test-group"
        val messageCount = 100000
        val retentionPeriod = 30L * 24 * 60 * 60 * 1000 // 30 days
        
        // Create messages with varying ages
        val now = System.currentTimeMillis()
        val messages = (1..messageCount).map { index ->
            val age = (index.toDouble() / messageCount) * (60L * 24 * 60 * 60 * 1000) // Up to 60 days old
            Message(
                id = "msg-$index",
                chatId = groupId,
                senderId = "user-${index % 1000}",
                content = if (index % 100 == 0) "!important Important message $index" else "Regular message $index",
                type = if (index % 50 == 0) MessageType.SYSTEM else MessageType.TEXT,
                timestamp = Date(now - age.toLong()),
                status = MessageStatus.SENT,
                reactions = emptyList(),
                isEncrypted = true
            )
        }
        
        coEvery { messageRepository.getMessages(groupId, Int.MAX_VALUE, 0) } returns messages
        
        // Act
        val pruningTime = measureTimeMillis {
            val pruningResult = historyManager.pruneGroupHistory(
                groupId = groupId,
                retentionPeriodMs = retentionPeriod,
                keepImportantMessages = true
            )
            
            assertTrue(pruningResult.isSuccess)
            val result = pruningResult.getOrThrow()
            assertTrue(result.messagesRemoved > 0)
            assertTrue(result.importantMessagesKept >= 0)
            assertTrue(result.bytesFreed > 0)
        }
        
        println("Pruning time for $messageCount messages: ${pruningTime}ms")
        assertTrue(pruningTime < 10000, "Pruning should complete within 10 seconds")
    }
    
    @Test
    fun `test snapshot-based history synchronization`() = runTest {
        // Arrange
        val groupId = "snapshot-sync-group"
        val userId = "sync-user"
        val snapshotTimestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 1 day ago
        
        val messages = (1..10000).map { index ->
            Message(
                id = "msg-$index",
                chatId = groupId,
                senderId = "user-${index % 100}",
                content = "Snapshot message $index",
                type = MessageType.TEXT,
                timestamp = Date(snapshotTimestamp - (10000 - index) * 1000L),
                status = MessageStatus.SENT,
                reactions = emptyList(),
                isEncrypted = true
            )
        }
        
        coEvery { messageRepository.getMessages(any(), any(), any()) } returns messages
        
        // Act
        val snapshotTime = measureTimeMillis {
            // Create snapshot
            val snapshotResult = historyManager.createHistorySnapshot(groupId, snapshotTimestamp)
            assertTrue(snapshotResult.isSuccess)
            
            val snapshot = snapshotResult.getOrThrow()
            assertTrue(snapshot.messageCount > 0)
            assertTrue(snapshot.compressedSize > 0)
            
            // Sync from snapshot
            val syncResult = historyManager.syncFromSnapshot(groupId, userId, snapshot.snapshotId)
            assertTrue(syncResult.isSuccess)
            
            val sync = syncResult.getOrThrow()
            assertEquals(snapshot.messageCount, sync.messagesSynced)
        }
        
        println("Snapshot creation and sync time: ${snapshotTime}ms")
        assertTrue(snapshotTime < 15000, "Snapshot operations should complete within 15 seconds")
    }
    
    @Test
    fun `test concurrent operations on large group`() = runTest {
        // Arrange
        val memberCount = 8000
        val groupId = "concurrent-ops-group"
        val senderId = "admin-user"
        val recipients = (1..memberCount).map { "user-$it" }
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Concurrent Operations Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        
        // Mock message history for concurrent operations
        val messages = (1..5000).map { index ->
            Message(
                id = "msg-$index",
                chatId = groupId,
                senderId = "user-${index % 100}",
                content = "Concurrent test message $index",
                type = MessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - (5000 - index) * 1000L),
                status = MessageStatus.SENT,
                reactions = emptyList(),
                isEncrypted = true
            )
        }
        
        coEvery { messageRepository.getMessages(any(), any(), any()) } returns messages
        
        // Act - Perform concurrent operations
        val concurrentTime = measureTimeMillis {
            // Concurrent message distribution
            val message1 = createTestMessage(groupId, senderId, "Concurrent message 1")
            val message2 = createTestMessage(groupId, senderId, "Concurrent message 2")
            
            val distribution1 = distributor.distributeMessage(groupId, message1, senderId)
            val distribution2 = distributor.distributeMessage(groupId, message2, senderId)
            
            assertTrue(distribution1.isSuccess)
            assertTrue(distribution2.isSuccess)
            
            // Concurrent history operations
            val historyStats = historyManager.getHistoryStatistics(groupId)
            assertTrue(historyStats.totalMessages > 0)
            
            val newUserSync = historyManager.synchronizeHistoryForNewMember(
                groupId = groupId,
                userId = "new-concurrent-user"
            )
            assertTrue(newUserSync.isSuccess)
        }
        
        println("Concurrent operations time for $memberCount members: ${concurrentTime}ms")
        assertTrue(concurrentTime < 40000, "Concurrent operations should complete within 40 seconds")
    }
    
    @Test
    fun `test system resilience with partial failures`() = runTest {
        // Arrange
        val memberCount = 2000
        val groupId = "resilience-test-group"
        val senderId = "sender-1"
        val recipients = (1..memberCount).map { "user-$it" }
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Resilience Test Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        
        // Simulate partial failures in encryption
        var encryptionCallCount = 0
        every { encryptionService.encryptMessage(any(), any()) } answers {
            encryptionCallCount++
            if (encryptionCallCount % 10 == 0) {
                // Fail every 10th encryption
                Result.failure(RuntimeException("Simulated encryption failure"))
            } else {
                Result.success(mockk {
                    every { ciphertext } returns "encrypted-content".toByteArray()
                })
            }
        }
        
        val message = createTestMessage(groupId, senderId, "Resilience test message")
        
        // Act
        val result = distributor.distributeMessage(groupId, message, senderId)
        
        // Assert - System should handle partial failures gracefully
        assertTrue(result.isSuccess)
        val distributionResult = result.getOrThrow()
        
        // Should have some successful deliveries despite failures
        assertTrue(distributionResult.successfulDeliveries > 0)
        assertTrue(distributionResult.failedDeliveries > 0)
        assertEquals(memberCount, distributionResult.totalRecipients)
        
        println("Resilience test - Successful: ${distributionResult.successfulDeliveries}, Failed: ${distributionResult.failedDeliveries}")
    }
    
    private fun createTestMessage(chatId: String, senderId: String, content: String): Message {
        return Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENDING,
            reactions = emptyList(),
            isEncrypted = true
        )
    }
}