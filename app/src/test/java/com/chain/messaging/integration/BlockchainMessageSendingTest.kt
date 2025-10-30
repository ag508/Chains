package com.chain.messaging.integration

import com.chain.messaging.core.auth.AuthenticationService
import com.chain.messaging.core.auth.UserIdentity
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.data.local.dao.MessageDao
import com.chain.messaging.data.repository.MessageRepositoryImpl
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.SignalProtocolAddress
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration test for blockchain message sending functionality
 */
class BlockchainMessageSendingTest {

    private lateinit var messageRepository: MessageRepositoryImpl
    private lateinit var messageDao: MessageDao
    private lateinit var blockchainManager: BlockchainManager
    private lateinit var encryptionService: SignalEncryptionService
    private lateinit var authenticationService: AuthenticationService

    private val testUserId = "test_user_123"
    private val testRecipientId = "recipient_456"
    private val testChatId = "${testUserId}_${testRecipientId}"

    @Before
    fun setup() {
        messageDao = mockk(relaxed = true)
        blockchainManager = mockk(relaxed = true)
        encryptionService = mockk(relaxed = true)
        authenticationService = mockk(relaxed = true)

        messageRepository = MessageRepositoryImpl(
            messageDao = messageDao,
            blockchainManager = blockchainManager,
            encryptionService = encryptionService,
            authenticationService = authenticationService
        )
    }

    @Test
    fun `sendMessage should encrypt and send message through blockchain when connected`() = runTest {
        // Arrange
        val testMessage = createTestMessage()
        val mockUser = createMockUser()
        val mockEncryptedMessage = mockk<com.chain.messaging.core.crypto.EncryptedMessage>()
        val testTransactionHash = "tx_hash_123"

        // Mock authentication service
        coEvery { authenticationService.getCurrentUser() } returns mockUser

        // Mock blockchain connection
        every { blockchainManager.isConnected() } returns true

        // Mock encryption
        every { mockEncryptedMessage.ciphertext } returns "encrypted_content".toByteArray()
        coEvery { 
            encryptionService.encryptMessage(any<SignalProtocolAddress>(), any<ByteArray>()) 
        } returns Result.success(mockEncryptedMessage)

        // Mock blockchain sending
        coEvery { 
            blockchainManager.sendMessage(
                recipientId = testRecipientId,
                encryptedContent = any<String>(),
                messageType = MessageType.TEXT.name
            ) 
        } returns testTransactionHash

        // Act
        val result = messageRepository.sendMessage(testMessage)

        // Assert
        assertTrue(result.isSuccess)

        // Verify message was saved locally with SENDING status
        verify { 
            messageDao.insertMessage(
                match { it.status == MessageStatus.SENDING.name }
            ) 
        }

        // Verify encryption was called
        coVerify { 
            encryptionService.encryptMessage(
                match { it.name == testRecipientId },
                testMessage.content.toByteArray()
            ) 
        }

        // Verify blockchain sending was called
        coVerify { 
            blockchainManager.sendMessage(
                recipientId = testRecipientId,
                encryptedContent = "encrypted_content",
                messageType = MessageType.TEXT.name
            ) 
        }

        // Verify message status was updated to SENT
        verify { 
            messageDao.updateMessage(
                match { it.status == MessageStatus.SENT.name }
            ) 
        }
    }

    @Test
    fun `sendMessage should queue message when blockchain is not connected`() = runTest {
        // Arrange
        val testMessage = createTestMessage()
        val mockUser = createMockUser()

        // Mock authentication service
        coEvery { authenticationService.getCurrentUser() } returns mockUser

        // Mock blockchain not connected
        every { blockchainManager.isConnected() } returns false

        // Act
        val result = messageRepository.sendMessage(testMessage)

        // Assert
        assertTrue(result.isSuccess)

        // Verify message was saved locally with SENDING status (queued)
        verify { 
            messageDao.insertMessage(
                match { it.status == MessageStatus.SENDING.name }
            ) 
        }

        // Verify encryption and blockchain sending were not called
        coVerify(exactly = 0) { encryptionService.encryptMessage(any(), any()) }
        coVerify(exactly = 0) { blockchainManager.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `sendMessage should fail when no authenticated user`() = runTest {
        // Arrange
        val testMessage = createTestMessage()

        // Mock no authenticated user
        coEvery { authenticationService.getCurrentUser() } returns null

        // Act
        val result = messageRepository.sendMessage(testMessage)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("No authenticated user", result.exceptionOrNull()?.message)

        // Verify no operations were performed
        verify(exactly = 0) { messageDao.insertMessage(any()) }
        coVerify(exactly = 0) { encryptionService.encryptMessage(any(), any()) }
        coVerify(exactly = 0) { blockchainManager.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `sendMessage should fail when sender ID mismatch`() = runTest {
        // Arrange
        val testMessage = createTestMessage().copy(senderId = "wrong_user")
        val mockUser = createMockUser()

        // Mock authentication service
        coEvery { authenticationService.getCurrentUser() } returns mockUser

        // Act
        val result = messageRepository.sendMessage(testMessage)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Sender ID mismatch", result.exceptionOrNull()?.message)
    }

    @Test
    fun `sendMessage should handle encryption failure`() = runTest {
        // Arrange
        val testMessage = createTestMessage()
        val mockUser = createMockUser()

        // Mock authentication service
        coEvery { authenticationService.getCurrentUser() } returns mockUser

        // Mock blockchain connection
        every { blockchainManager.isConnected() } returns true

        // Mock encryption failure
        coEvery { 
            encryptionService.encryptMessage(any<SignalProtocolAddress>(), any<ByteArray>()) 
        } returns Result.failure(Exception("Encryption failed"))

        // Act
        val result = messageRepository.sendMessage(testMessage)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Encryption failed", result.exceptionOrNull()?.message)

        // Verify message status was updated to FAILED
        verify { 
            messageDao.updateMessage(
                match { it.status == MessageStatus.FAILED.name }
            ) 
        }
    }

    @Test
    fun `sendMessage should handle blockchain sending failure`() = runTest {
        // Arrange
        val testMessage = createTestMessage()
        val mockUser = createMockUser()
        val mockEncryptedMessage = mockk<com.chain.messaging.core.crypto.EncryptedMessage>()

        // Mock authentication service
        coEvery { authenticationService.getCurrentUser() } returns mockUser

        // Mock blockchain connection
        every { blockchainManager.isConnected() } returns true

        // Mock encryption success
        every { mockEncryptedMessage.ciphertext } returns "encrypted_content".toByteArray()
        coEvery { 
            encryptionService.encryptMessage(any<SignalProtocolAddress>(), any<ByteArray>()) 
        } returns Result.success(mockEncryptedMessage)

        // Mock blockchain sending failure
        coEvery { 
            blockchainManager.sendMessage(any<String>(), any<String>(), any<String>()) 
        } throws Exception("Blockchain error")

        // Act
        val result = messageRepository.sendMessage(testMessage)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Blockchain error", result.exceptionOrNull()?.message)

        // Verify message status was updated to FAILED
        verify { 
            messageDao.updateMessage(
                match { it.status == MessageStatus.FAILED.name }
            ) 
        }
    }

    private fun createTestMessage(): Message {
        return Message(
            id = "msg_123",
            chatId = testChatId,
            senderId = testUserId,
            content = "Test message content",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENDING,
            replyTo = null,
            reactions = emptyList(),
            isEncrypted = true,
            disappearingMessageTimer = null,
            expiresAt = null,
            isDisappearing = false
        )
    }

    private fun createMockUser(): UserIdentity {
        val mockIdentityKey = mockk<IdentityKey>()
        return UserIdentity(
            userId = testUserId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            profilePictureUrl = null,
            blockchainPublicKey = "public_key".toByteArray(),
            signalIdentityKey = mockIdentityKey,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis(),
            isVerified = false
        )
    }
}