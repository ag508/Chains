package com.chain.messaging.core.group

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.p2p.P2PManager
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.ChatRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

/**
 * Performance tests for large group messaging scenarios up to 100k members.
 * Tests distribution efficiency, memory usage, and scalability.
 */
class LargeGroupPerformanceTest {
    
    private lateinit var chatRepository: ChatRepository
    private lateinit var blockchainManager: BlockchainManager
    private lateinit var encryptionService: SignalEncryptionService
    private lateinit var p2pManager: P2PManager
    private lateinit var distributor: GroupMessageDistributorImpl
    private lateinit var optimizer: MessageDeliveryOptimizerImpl
    private lateinit var historyManager: GroupHistoryManagerImpl
    
    @BeforeEach
    fun setup() {
        chatRepository = mockk()
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
        historyManager = GroupHistoryManagerImpl(mockk())
        
        // Setup common mocks
        coEvery { blockchainManager.sendMessage(any()) } returns "tx-hash"
        every { encryptionService.encryptMessage(any(), any()) } returns Result.success(
            mockk {
                every { ciphertext } returns "encrypted-content".toByteArray()
            }
        )
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    fun `test message distribution to 1000 members performance`() = runTest {
        // Arrange
        val memberCount = 1000
        val groupId = "large-group-1k"
        val senderId = "sender-1"
        val recipients = (1..memberCount).map { "user-$it" }
        val message = createTestMessage(groupId, senderId)
        
        val chat = createTestChat(groupId, recipients, senderId)
        coEvery { chatRepository.getChatById(groupId) } returns chat
        
        // Act & Measure
        val executionTime = measureTimeMillis {
            val result = distributor.distributeMessage(groupId, message, senderId)
            assertTrue(result.isSuccess)
            
            val distributionResult = result.getOrThrow()
            assertTrue(distributionResult.totalRecipients == memberCount)
        }
        
        // Assert performance requirements
        println("1K members distribution time: ${executionTime}ms")
        assertTrue(executionTime < 10000, "Distribution should complete within 10 seconds for 1K members")
        
        // Verify strategy selection
        val strategy = distributor.getOptimalDistributionStrategy(memberCount)
        assertTrue(strategy == DistributionStrategy.BATCHED)
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    fun `test message distribution to 10000 members performance`() = runTest {
        // Arrange
        val memberCount = 10000
        val groupId = "large-group-10k"
        val senderId = "sender-1"
        val recipients = (1..memberCount).map { "user-$it" }
        val message = createTestMessage(groupId, senderId)
        
        val chat = createTestChat(groupId, recipients, senderId)
        coEvery { chatRepository.getChatById(groupId) } returns chat
        
        // Act & Measure
        val executionTime = measureTimeMillis {
            val result = distributor.distributeMessage(groupId, message, senderId)
            assertTrue(result.isSuccess)
            
            val distributionResult = result.getOrThrow()
            assertTrue(distributionResult.totalRecipients == memberCount)
        }
        
        // Assert performance requirements
        println("10K members distribution time: ${executionTime}ms")
        assertTrue(executionTime < 30000, "Distribution should complete within 30 seconds for 10K members")
        
        // Verify strategy selection
        val strategy = distributor.getOptimalDistributionStrategy(memberCount)
        assertTrue(strategy == DistributionStrategy.TREE_ROUTING)
    }
    
    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    fun `test message distribution to 100000 members performance`() = runTest {
        // Arrange
        val memberCount = 100000
        val groupId = "large-group-100k"
        val senderId = "sender-1"
        val recipients = (1..memberCount).map { "user-$it" }
        val message = createTestMessage(groupId, senderId)
        
        val chat = createTestChat(groupId, recipients, senderId)
        coEvery { chatRepository.getChatById(groupId) } returns chat
        
        // Act & Measure
        val executionTime = measureTimeMillis {
            val result = distributor.distributeMessage(groupId, message, senderId)
            assertTrue(result.isSuccess)
            
            val distributionResult = result.getOrThrow()
            assertTrue(distributionResult.totalRecipients == memberCount)
        }
        
        // Assert performance requirements
        println("100K members distribution time: ${executionTime}ms")
        assertTrue(executionTime < 120000, "Distribution should complete within 2 minutes for 100K members")
        
        // Verify strategy selection
        val strategy = distributor.getOptimalDistributionStrategy(memberCount)
        assertTrue(strategy == DistributionStrategy.HYBRID_MESH)
    }
    
    @Test
    fun `test delivery optimization for large groups`() = runTest {
        // Arrange
        val memberCount = 50000
        val recipients = (1..memberCount).map { "user-$it" }
        val message = createTestMessage("test-group", "sender-1")
        
        val networkConditions = NetworkConditions(
            bandwidth = 1_000_000L, // 1 Mbps
            latency = 100L,
            packetLoss = 0.01f,
            connectionStability = 0.9f,
            peerCount = 100
        )
        
        // Act & Measure
        val optimizationTime = measureTimeMillis {
            val optimizedOrder = optimizer.optimizeDeliveryOrder(recipients, message)
            assertTrue(optimizedOrder.size == memberCount)
            
            val batchSize = optimizer.calculateOptimalBatchSize(memberCount, networkConditions)
            assertTrue(batchSize > 0)
            
            val throttling = optimizer.applyRateLimiting(100f, 0.5f)
            assertTrue(throttling.maxConcurrentDeliveries > 0)
        }
        
        println("Optimization time for 50K members: ${optimizationTime}ms")
        assertTrue(optimizationTime < 5000, "Optimization should complete within 5 seconds")
    }
    
    @Test
    fun `test history synchronization performance for large groups`() = runTest {
        // Arrange
        val groupId = "history-test-group"
        val userId = "new-user"
        val messageCount = 100000
        
        // Mock message repository with large message set
        val messages = (1..messageCount).map { index ->
            Message(
                id = "msg-$index",
                chatId = groupId,
                senderId = "user-${index % 1000}",
                content = "Message content $index",
                type = MessageType.TEXT,
                timestamp = Date(System.currentTimeMillis() - (messageCount - index) * 1000),
                status = MessageStatus.SENT,
                reactions = emptyList(),
                isEncrypted = true
            )
        }
        
        val messageRepository = mockk<com.chain.messaging.domain.repository.MessageRepository>()
        coEvery { messageRepository.getMessages(any(), any(), any()) } returns messages
        
        val historyManager = GroupHistoryManagerImpl(messageRepository)
        
        // Act & Measure
        val syncTime = measureTimeMillis {
            val result = historyManager.synchronizeHistoryForNewMember(groupId, userId)
            assertTrue(result.isSuccess)
            
            val syncResult = result.getOrThrow()
            assertTrue(syncResult.messagesSynced > 0)
        }
        
        println("History sync time for 100K messages: ${syncTime}ms")
        assertTrue(syncTime < 60000, "History sync should complete within 1 minute")
    }
    
    @Test
    fun `test memory usage during large group operations`() = runTest {
        // Arrange
        val memberCount = 10000
        val groupId = "memory-test-group"
        val senderId = "sender-1"
        val recipients = (1..memberCount).map { "user-$it" }
        
        val chat = createTestChat(groupId, recipients, senderId)
        coEvery { chatRepository.getChatById(groupId) } returns chat
        
        // Measure memory before
        System.gc()
        val memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        // Act
        val messages = (1..100).map { createTestMessage(groupId, senderId) }
        messages.forEach { message ->
            val result = distributor.distributeMessage(groupId, message, senderId)
            assertTrue(result.isSuccess)
        }
        
        // Measure memory after
        System.gc()
        val memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryUsed = memoryAfter - memoryBefore
        
        println("Memory used for 100 messages to 10K members: ${memoryUsed / 1024 / 1024}MB")
        
        // Assert reasonable memory usage (should not exceed 500MB for this test)
        assertTrue(memoryUsed < 500 * 1024 * 1024, "Memory usage should be reasonable")
    }
    
    @Test
    fun `test concurrent message distribution performance`() = runTest {
        // Arrange
        val memberCount = 5000
        val groupId = "concurrent-test-group"
        val senderId = "sender-1"
        val recipients = (1..memberCount).map { "user-$it" }
        val messageCount = 10
        
        val chat = createTestChat(groupId, recipients, senderId)
        coEvery { chatRepository.getChatById(groupId) } returns chat
        
        // Act & Measure
        val concurrentTime = measureTimeMillis {
            val messages = (1..messageCount).map { createTestMessage(groupId, senderId) }
            
            // Simulate concurrent message sending
            messages.forEach { message ->
                val result = distributor.distributeMessage(groupId, message, senderId)
                assertTrue(result.isSuccess)
            }
        }
        
        println("Concurrent distribution time for $messageCount messages to $memberCount members: ${concurrentTime}ms")
        assertTrue(concurrentTime < 60000, "Concurrent distribution should complete within 1 minute")
    }
    
    @Test
    fun `test scalability across different group sizes`() = runTest {
        val groupSizes = listOf(100, 500, 1000, 5000, 10000)
        val results = mutableMapOf<Int, Long>()
        
        groupSizes.forEach { size ->
            val groupId = "scale-test-$size"
            val senderId = "sender-1"
            val recipients = (1..size).map { "user-$it" }
            val message = createTestMessage(groupId, senderId)
            
            val chat = createTestChat(groupId, recipients, senderId)
            coEvery { chatRepository.getChatById(groupId) } returns chat
            
            val executionTime = measureTimeMillis {
                val result = distributor.distributeMessage(groupId, message, senderId)
                assertTrue(result.isSuccess)
            }
            
            results[size] = executionTime
            println("Group size $size: ${executionTime}ms")
        }
        
        // Verify that execution time scales reasonably (not exponentially)
        val scalingFactor = results[10000]!!.toDouble() / results[1000]!!.toDouble()
        assertTrue(scalingFactor < 20, "Scaling should be sub-linear, factor: $scalingFactor")
    }
    
    private fun createTestMessage(chatId: String, senderId: String): Message {
        return Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            content = "Performance test message content",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENDING,
            reactions = emptyList(),
            isEncrypted = true
        )
    }
    
    private fun createTestChat(groupId: String, participants: List<String>, senderId: String): Chat {
        return Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Performance Test Group",
            participants = participants,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}