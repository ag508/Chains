package com.chain.messaging.data.repository

import com.chain.messaging.core.auth.AuthenticationService
import com.chain.messaging.core.auth.UserIdentity
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.EncryptedMessage
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.data.local.dao.MessageDao
import com.chain.messaging.data.local.entity.MessageEntity
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
 * Unit test for blockchain message sending functionality in MessageRepositoryImpl
 */
class MessageRepositoryBlockchainTest {

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
    fun `sendMessage successfully encrypts and sends through blockchain`() = runTest {
        // Arrange
        val testMessage = createTestMessage()
        val mockUser = createMockUser()
        val mockEncryptedMessage = createMockEncryptedMessage()
        val testTransactionHash = "tx_hash_123"

        // Mock authentication service
        coEvery { authenticationService.getCurrentUser() } returns mockUser

        // Mock blockchain connection
        every { blockchainManager.isConnected() } returns true

        // Mock encryption
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
        assertTrue(result.isSuccess, "Message sending should succeed")

        // Verify message was saved locally with SENDING status first
        verify { 
            messageDao.insertMessage(
                match { entity -> 
                    entity.id == testMessage.id && 
                    entity.status == MessageStatus.SENDING.name 
                }
            ) 
        }

        // Verify encryption was called with correct parameters
        coVerify { 
            encryptionService.encryptMessage(
                match { address -> address.name == testRecipientId },
                testMessage.content.toByteArray()
            ) 
        }

        // Verify blockchain sending was called with encrypted content
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
                match { entity -> 
                    entity.id == testMessage.id && 
                    entity.status == MessageStatus.SENT.name 
                }
            ) 
        }
    }

    @Test
    fun `sendMessage queues message when blockchain not connected`() = runTest {
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
        assertTrue(result.isSuccess, "Message should be queued successfully")

        // Verify message was saved locally with SENDING status (queued)
        verify { 
            messageDao.insertMessage(
                match { entity -> 
                    entity.id == testMessage.id && 
                    entity.status == MessageStatus.SENDING.name 
                }
            ) 
        }

        // Verify encryption and blockchain sending were not called
        coVerify(exactly = 0) { encryptionService.encryptMessage(any(), any()) }
        coVerify(exactly = 0) { blockchainManager.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `sendMessage fails when no authenticated user`() = runTest {
        // Arrange
        val testMessage = createTestMessage()

        // Mock no authenticated user
        coEvery { authenticationService.getCurrentUser() } returns null

        // Act
        val result = messageRepository.sendMessage(testMessage)

        // Assert
        assertTrue(result.isFailure, "Should fail when no authenticated user")
        assertEquals("No authenticated user", result.exceptionOrNull()?.message)

        // Verify no operations were performed
        verify(exactly = 0) { messageDao.insertMessage(any()) }
        coVerify(exactly = 0) { encryptionService.encryptMessage(any(), any()) }
        coVerify(exactly = 0) { blockchainManager.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `sendMessage fails when sender ID mismatch`() = runTest {
        // Arrange
        val testMessage = createTestMessage().copy(senderId = "wrong_user")
        val mockUser = createMockUser()

        // Mock authentication service
        coEvery { authenticationService.getCurrentUser() } returns mockUser

        // Act
        val result = messageRepository.sendMessage(testMessage)

        // Assert
        assertTrue(result.isFailure, "Should fail when sender ID mismatch")
        assertEquals("Sender ID mismatch", result.exceptionOrNull()?.message)
    }

    @Test
    fun `sendMessage handles encryption failure gracefully`() = runTest {
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
        assertTrue(result.isFailure, "Should fail when encryption fails")
        assertEquals("Encryption failed", result.exceptionOrNull()?.message)

        // Verify message status was updated to FAILED
        verify { 
            messageDao.updateMessage(
                match { entity -> 
                    entity.id == testMessage.id && 
                    entity.status == MessageStatus.FAILED.name 
                }
            ) 
        }
    }

    @Test
    fun `sendMessage handles blockchain sending failure gracefully`() = runTest {
        // Arrange
        val testMessage = createTestMessage()
        val mockUser = createMockUser()
        val mockEncryptedMessage = createMockEncryptedMessage()

        // Mock authentication service
        coEvery { authenticationService.getCurrentUser() } returns mockUser

        // Mock blockchain connection
        every { blockchainManager.isConnected() } returns true

        // Mock encryption success
        coEvery { 
            encryptionService.encryptMessage(any<SignalProtocolAddress>(), any<ByteArray>()) 
        } returns Result.success(mockEncryptedMessage)

        // Mock blockchain sending failure
        coEvery { 
            blockchainManager.sendMessage(any<String>(), any<String>(), any<String>()) 
        } throws Exception("Blockchain network error")

        // Act
        val result = messageRepository.sendMessage(testMessage)

        // Assert
        assertTrue(result.isFailure, "Should fail when blockchain sending fails")
        assertEquals("Blockchain network error", result.exceptionOrNull()?.message)

        // Verify message status was updated to FAILED
        verify { 
            messageDao.updateMessage(
                match { entity -> 
                    entity.id == testMessage.id && 
                    entity.status == MessageStatus.FAILED.name 
                }
            ) 
        }
    }

    @Test
    fun `getRecipientId extracts correct recipient from direct chat`() = runTest {
        // This tests the private getRecipientId method indirectly through sendMessage
        val testMessage = createTestMessage()
        val mockUser = createMockUser()
        val mockEncryptedMessage = createMockEncryptedMessage()

        // Mock authentication service
        coEvery { authenticationService.getCurrentUser() } returns mockUser

        // Mock blockchain connection
        every { blockchainManager.isConnected() } returns true

        // Mock encryption
        coEvery { 
            encryptionService.encryptMessage(any<SignalProtocolAddress>(), any<ByteArray>()) 
        } returns Result.success(mockEncryptedMessage)

        // Mock blockchain sending
        coEvery { 
            blockchainManager.sendMessage(any<String>(), any<String>(), any<String>()) 
        } returns "tx_hash"

        // Act
        messageRepository.sendMessage(testMessage)

        // Assert - verify the correct recipient ID was extracted and used
        coVerify { 
            blockchainManager.sendMessage(
                recipientId = testRecipientId, // Should extract the recipient correctly
                encryptedContent = any<String>(),
                messageType = MessageType.TEXT.name
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

    private fun createMockEncryptedMessage(): EncryptedMessage {
        val mockAddress = mockk<SignalProtocolAddress>()
        return EncryptedMessage(
            recipientAddress = mockAddress,
            ciphertext = "encrypted_content".toByteArray(),
            type = EncryptedMessage.Type.SIGNAL
        )
    }
}