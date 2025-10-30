package com.chain.messaging.core.group

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.p2p.P2PManager
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.ChatRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.signal.libsignal.protocol.SignalProtocolAddress
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroupMessageDistributorTest {
    
    private lateinit var chatRepository: ChatRepository
    private lateinit var blockchainManager: BlockchainManager
    private lateinit var encryptionService: SignalEncryptionService
    private lateinit var p2pManager: P2PManager
    private lateinit var distributor: GroupMessageDistributorImpl
    
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
    }
    
    @Test
    fun `test optimal distribution strategy selection`() {
        // Small group
        assertEquals(
            DistributionStrategy.DIRECT,
            distributor.getOptimalDistributionStrategy(50)
        )
        
        // Medium group
        assertEquals(
            DistributionStrategy.BATCHED,
            distributor.getOptimalDistributionStrategy(500)
        )
        
        // Large group
        assertEquals(
            DistributionStrategy.TREE_ROUTING,
            distributor.getOptimalDistributionStrategy(5000)
        )
        
        // Very large group
        assertEquals(
            DistributionStrategy.HYBRID_MESH,
            distributor.getOptimalDistributionStrategy(50000)
        )
    }
    
    @Test
    fun `test direct distribution for small group`() = runTest {
        // Arrange
        val groupId = "test-group"
        val senderId = "sender-1"
        val recipients = (1..50).map { "user-$it" }
        val message = createTestMessage(groupId, senderId)
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Test Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        coEvery { blockchainManager.sendMessage(any()) } returns "tx-hash"
        every { encryptionService.encryptMessage(any(), any()) } returns Result.success(
            mockk {
                every { ciphertext } returns "encrypted-content".toByteArray()
            }
        )
        
        // Act
        val result = distributor.distributeMessage(groupId, message, senderId)
        
        // Assert
        assertTrue(result.isSuccess)
        val distributionResult = result.getOrThrow()
        assertEquals(recipients.size, distributionResult.totalRecipients)
        assertEquals(DistributionStrategy.DIRECT, distributionResult.strategy)
        
        // Verify encryption was called for each recipient
        verify(exactly = recipients.size) {
            encryptionService.encryptMessage(any(), any())
        }
        
        // Verify blockchain sends
        coVerify(exactly = recipients.size) {
            blockchainManager.sendMessage(any())
        }
    }
    
    @Test
    fun `test batched distribution for medium group`() = runTest {
        // Arrange
        val groupId = "test-group"
        val senderId = "sender-1"
        val recipients = (1..500).map { "user-$it" }
        val message = createTestMessage(groupId, senderId)
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Test Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        coEvery { blockchainManager.sendMessage(any()) } returns "tx-hash"
        every { encryptionService.encryptMessage(any(), any()) } returns Result.success(
            mockk {
                every { ciphertext } returns "encrypted-content".toByteArray()
            }
        )
        
        // Act
        val result = distributor.distributeMessage(groupId, message, senderId)
        
        // Assert
        assertTrue(result.isSuccess)
        val distributionResult = result.getOrThrow()
        assertEquals(recipients.size, distributionResult.totalRecipients)
        assertEquals(DistributionStrategy.BATCHED, distributionResult.strategy)
    }
    
    @Test
    fun `test tree routing distribution for large group`() = runTest {
        // Arrange
        val groupId = "test-group"
        val senderId = "sender-1"
        val recipients = (1..5000).map { "user-$it" }
        val message = createTestMessage(groupId, senderId)
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Test Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        coEvery { blockchainManager.sendMessage(any()) } returns "tx-hash"
        every { encryptionService.encryptMessage(any(), any()) } returns Result.success(
            mockk {
                every { ciphertext } returns "encrypted-content".toByteArray()
            }
        )
        
        // Act
        val result = distributor.distributeMessageWithTreeRouting(groupId, message, senderId)
        
        // Assert
        assertTrue(result.isSuccess)
        val distributionResult = result.getOrThrow()
        assertEquals(recipients.size, distributionResult.totalRecipients)
        assertEquals(DistributionStrategy.TREE_ROUTING, distributionResult.strategy)
    }
    
    @Test
    fun `test hybrid mesh distribution for very large group`() = runTest {
        // Arrange
        val groupId = "test-group"
        val senderId = "sender-1"
        val recipients = (1..50000).map { "user-$it" }
        val message = createTestMessage(groupId, senderId)
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Test Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        coEvery { blockchainManager.sendMessage(any()) } returns "tx-hash"
        every { encryptionService.encryptMessage(any(), any()) } returns Result.success(
            mockk {
                every { ciphertext } returns "encrypted-content".toByteArray()
            }
        )
        
        // Act
        val result = distributor.distributeMessage(groupId, message, senderId)
        
        // Assert
        assertTrue(result.isSuccess)
        val distributionResult = result.getOrThrow()
        assertEquals(recipients.size, distributionResult.totalRecipients)
        assertEquals(DistributionStrategy.HYBRID_MESH, distributionResult.strategy)
    }
    
    @Test
    fun `test distribution progress tracking`() = runTest {
        // Arrange
        val groupId = "test-group"
        val senderId = "sender-1"
        val recipients = (1..100).map { "user-$it" }
        val message = createTestMessage(groupId, senderId)
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Test Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        coEvery { blockchainManager.sendMessage(any()) } returns "tx-hash"
        every { encryptionService.encryptMessage(any(), any()) } returns Result.success(
            mockk {
                every { ciphertext } returns "encrypted-content".toByteArray()
            }
        )
        
        // Act
        val result = distributor.distributeMessage(groupId, message, senderId)
        
        // Assert
        assertTrue(result.isSuccess)
        val distributionResult = result.getOrThrow()
        
        // Test progress observation
        val progressFlow = distributor.observeDistributionProgress(distributionResult.distributionId)
        // Note: In a real test, we would collect progress updates during distribution
    }
    
    @Test
    fun `test distribution failure handling`() = runTest {
        // Arrange
        val groupId = "test-group"
        val senderId = "sender-1"
        val recipients = listOf("user-1", "user-2")
        val message = createTestMessage(groupId, senderId)
        
        val chat = Chat(
            id = groupId,
            type = ChatType.GROUP,
            name = "Test Group",
            participants = recipients,
            admins = listOf(senderId),
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(groupId) } returns chat
        every { encryptionService.encryptMessage(any(), any()) } returns Result.failure(
            RuntimeException("Encryption failed")
        )
        
        // Act
        val result = distributor.distributeMessage(groupId, message, senderId)
        
        // Assert
        assertTrue(result.isSuccess)
        val distributionResult = result.getOrThrow()
        assertEquals(0, distributionResult.successfulDeliveries)
        assertEquals(recipients.size, distributionResult.failedDeliveries)
    }
    
    @Test
    fun `test group not found error`() = runTest {
        // Arrange
        val groupId = "non-existent-group"
        val senderId = "sender-1"
        val message = createTestMessage(groupId, senderId)
        
        coEvery { chatRepository.getChatById(groupId) } returns null
        
        // Act & Assert
        val result = distributor.distributeMessage(groupId, message, senderId)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `test cancel distribution`() = runTest {
        // Arrange
        val distributionId = "test-distribution"
        
        // Act
        val result = distributor.cancelDistribution(distributionId)
        
        // Assert
        assertTrue(result.isFailure) // Should fail because distribution doesn't exist
    }
    
    @Test
    fun `test retry failed deliveries`() = runTest {
        // Arrange
        val distributionId = "test-distribution"
        
        // Act
        val result = distributor.retryFailedDeliveries(distributionId)
        
        // Assert
        assertTrue(result.isFailure) // Should fail because distribution doesn't exist
    }
    
    private fun createTestMessage(chatId: String, senderId: String): Message {
        return Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            content = "Test message content",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENDING,
            reactions = emptyList(),
            isEncrypted = true
        )
    }
}