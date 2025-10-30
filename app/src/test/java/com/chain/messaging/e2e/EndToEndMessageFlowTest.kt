package com.chain.messaging.e2e

import com.chain.messaging.core.auth.AuthenticationService
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.p2p.P2PManager
import com.chain.messaging.data.local.ChainDatabase
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.model.User
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import javax.inject.Inject

/**
 * End-to-end tests for complete message flows from sender to recipient
 * Tests the entire message journey through encryption, blockchain, and P2P delivery
 */
@HiltAndroidTest
class EndToEndMessageFlowTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var authService: AuthenticationService

    @Inject
    lateinit var messagingService: MessagingService

    @Inject
    lateinit var encryptionService: SignalEncryptionService

    @Inject
    lateinit var blockchainManager: BlockchainManager

    @Inject
    lateinit var p2pManager: P2PManager

    @Inject
    lateinit var database: ChainDatabase

    private lateinit var senderUser: User
    private lateinit var recipientUser: User

    @Before
    fun setup() {
        hiltRule.inject()
        
        runBlocking {
            // Setup test users
            senderUser = User(
                id = "sender_${UUID.randomUUID()}",
                publicKey = "sender_public_key",
                displayName = "Test Sender",
                status = "online"
            )
            
            recipientUser = User(
                id = "recipient_${UUID.randomUUID()}",
                publicKey = "recipient_public_key", 
                displayName = "Test Recipient",
                status = "online"
            )

            // Initialize encryption sessions
            encryptionService.initializeSession(recipientUser.id, mockPreKeyBundle())
            encryptionService.initializeSession(senderUser.id, mockPreKeyBundle())
        }
    }

    @After
    fun cleanup() {
        runBlocking {
            database.clearAllTables()
        }
    }

    @Test
    fun testCompleteTextMessageFlow() = runTest {
        // Given: A text message to send
        val messageContent = "Hello, this is an end-to-end test message!"
        val chatId = "test_chat_${UUID.randomUUID()}"

        // When: Sender sends a message
        val sentMessage = messagingService.sendMessage(
            chatId = chatId,
            recipientId = recipientUser.id,
            content = messageContent,
            type = MessageType.TEXT
        )

        // Then: Message should be created with SENDING status
        assertEquals(MessageStatus.SENDING, sentMessage.status)
        assertEquals(messageContent, sentMessage.content)
        assertEquals(senderUser.id, sentMessage.senderId)

        // Wait for blockchain processing
        delay(2000)

        // Verify message was encrypted and sent to blockchain
        val blockchainMessages = blockchainManager.getMessagesForUser(recipientUser.id)
        assertTrue("Message should be on blockchain", blockchainMessages.isNotEmpty())

        // Verify message status updated to SENT
        val updatedMessage = messagingService.getMessage(sentMessage.id)
        assertEquals(MessageStatus.SENT, updatedMessage?.status)

        // Simulate recipient receiving the message
        val receivedMessages = messagingService.getMessagesForChat(chatId).first()
        val receivedMessage = receivedMessages.find { it.id == sentMessage.id }

        assertNotNull("Message should be received", receivedMessage)
        assertEquals(messageContent, receivedMessage?.content)
        assertEquals(MessageStatus.DELIVERED, receivedMessage?.status)
    }

    @Test
    fun testCompleteMediaMessageFlow() = runTest {
        // Given: A media message to send
        val chatId = "test_chat_${UUID.randomUUID()}"
        val mediaUrl = "content://test/image.jpg"
        val mediaCaption = "Test image caption"

        // When: Sender sends a media message
        val sentMessage = messagingService.sendMediaMessage(
            chatId = chatId,
            recipientId = recipientUser.id,
            mediaUrl = mediaUrl,
            caption = mediaCaption,
            type = MessageType.IMAGE
        )

        // Then: Media message should be processed
        assertEquals(MessageStatus.SENDING, sentMessage.status)
        assertEquals(MessageType.IMAGE, sentMessage.type)
        assertEquals(mediaCaption, sentMessage.content)

        // Wait for media processing and blockchain transmission
        delay(3000)

        // Verify media was encrypted and uploaded
        val updatedMessage = messagingService.getMessage(sentMessage.id)
        assertEquals(MessageStatus.SENT, updatedMessage?.status)
        assertNotNull("Media URL should be set", updatedMessage?.mediaUrl)

        // Verify recipient can receive and decrypt media
        val receivedMessages = messagingService.getMessagesForChat(chatId).first()
        val receivedMessage = receivedMessages.find { it.id == sentMessage.id }

        assertNotNull("Media message should be received", receivedMessage)
        assertEquals(MessageType.IMAGE, receivedMessage?.type)
        assertEquals(mediaCaption, receivedMessage?.content)
    }

    @Test
    fun testGroupMessageFlow() = runTest {
        // Given: A group chat with multiple recipients
        val groupId = "test_group_${UUID.randomUUID()}"
        val recipient2 = User(
            id = "recipient2_${UUID.randomUUID()}",
            publicKey = "recipient2_public_key",
            displayName = "Test Recipient 2",
            status = "online"
        )

        val groupMembers = listOf(senderUser.id, recipientUser.id, recipient2.id)
        
        // Initialize group encryption
        encryptionService.initializeGroupSession(groupId, groupMembers)

        // When: Sender sends a group message
        val messageContent = "Hello group! This is an E2E test."
        val sentMessage = messagingService.sendGroupMessage(
            groupId = groupId,
            content = messageContent,
            type = MessageType.TEXT
        )

        // Then: Message should be sent to all group members
        assertEquals(MessageStatus.SENDING, sentMessage.status)
        assertEquals(messageContent, sentMessage.content)

        // Wait for group message distribution
        delay(4000)

        // Verify all recipients received the message
        val groupMessages = messagingService.getMessagesForChat(groupId).first()
        val receivedMessage = groupMessages.find { it.id == sentMessage.id }

        assertNotNull("Group message should be received", receivedMessage)
        assertEquals(messageContent, receivedMessage?.content)
        assertEquals(MessageStatus.DELIVERED, receivedMessage?.status)
    }

    @Test
    fun testMessageDeliveryWithNetworkFailure() = runTest {
        // Given: A message to send with simulated network failure
        val messageContent = "Test message with network failure"
        val chatId = "test_chat_${UUID.randomUUID()}"

        // Simulate network disconnection
        p2pManager.simulateNetworkDisconnection()

        // When: Sender attempts to send a message
        val sentMessage = messagingService.sendMessage(
            chatId = chatId,
            recipientId = recipientUser.id,
            content = messageContent,
            type = MessageType.TEXT
        )

        // Then: Message should be queued locally
        assertEquals(MessageStatus.PENDING, sentMessage.status)

        // Simulate network reconnection
        delay(1000)
        p2pManager.simulateNetworkReconnection()

        // Wait for message retry and delivery
        delay(3000)

        // Verify message was eventually delivered
        val updatedMessage = messagingService.getMessage(sentMessage.id)
        assertEquals(MessageStatus.DELIVERED, updatedMessage?.status)
    }

    @Test
    fun testMessageEncryptionIntegrity() = runTest {
        // Given: A sensitive message
        val sensitiveContent = "This is a confidential message with sensitive data"
        val chatId = "test_chat_${UUID.randomUUID()}"

        // When: Message is sent
        val sentMessage = messagingService.sendMessage(
            chatId = chatId,
            recipientId = recipientUser.id,
            content = sensitiveContent,
            type = MessageType.TEXT
        )

        delay(2000)

        // Then: Verify message is encrypted on blockchain
        val blockchainMessages = blockchainManager.getMessagesForUser(recipientUser.id)
        val blockchainMessage = blockchainMessages.find { it.messageId == sentMessage.id }

        assertNotNull("Message should be on blockchain", blockchainMessage)
        assertNotEquals("Message should be encrypted", sensitiveContent, blockchainMessage?.encryptedContent)
        assertTrue("Encrypted content should not contain original text", 
            !blockchainMessage?.encryptedContent?.contains(sensitiveContent)!!)

        // Verify recipient can decrypt the message
        val receivedMessages = messagingService.getMessagesForChat(chatId).first()
        val receivedMessage = receivedMessages.find { it.id == sentMessage.id }

        assertEquals("Decrypted message should match original", sensitiveContent, receivedMessage?.content)
    }

    @Test
    fun testMessageStatusProgression() = runTest {
        // Given: A message to track through all status stages
        val messageContent = "Status tracking test message"
        val chatId = "test_chat_${UUID.randomUUID()}"

        // When: Message is sent
        val sentMessage = messagingService.sendMessage(
            chatId = chatId,
            recipientId = recipientUser.id,
            content = messageContent,
            type = MessageType.TEXT
        )

        // Then: Track status progression
        assertEquals("Initial status should be SENDING", MessageStatus.SENDING, sentMessage.status)

        // Wait for blockchain confirmation
        delay(1500)
        var updatedMessage = messagingService.getMessage(sentMessage.id)
        assertEquals("Status should progress to SENT", MessageStatus.SENT, updatedMessage?.status)

        // Wait for delivery confirmation
        delay(2000)
        updatedMessage = messagingService.getMessage(sentMessage.id)
        assertEquals("Status should progress to DELIVERED", MessageStatus.DELIVERED, updatedMessage?.status)

        // Simulate recipient reading the message
        messagingService.markMessageAsRead(sentMessage.id, recipientUser.id)
        delay(500)

        updatedMessage = messagingService.getMessage(sentMessage.id)
        assertEquals("Status should progress to READ", MessageStatus.READ, updatedMessage?.status)
    }

    private fun mockPreKeyBundle() = mapOf(
        "identityKey" to "mock_identity_key",
        "signedPreKey" to "mock_signed_pre_key",
        "oneTimePreKey" to "mock_one_time_pre_key",
        "signature" to "mock_signature"
    )
}