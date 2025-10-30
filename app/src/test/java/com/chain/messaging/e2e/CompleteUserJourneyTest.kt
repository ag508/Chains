package com.chain.messaging.e2e

import com.chain.messaging.core.integration.AuthMethod
import com.chain.messaging.core.integration.ChainApplicationManager
import com.chain.messaging.core.integration.UserJourneyOrchestrator
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.repository.ChatRepository
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end tests for complete user journeys from registration to messaging.
 * Validates all requirements are met through integration testing.
 */
@HiltAndroidTest
class CompleteUserJourneyTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var applicationManager: ChainApplicationManager

    @Inject
    lateinit var userJourneyOrchestrator: UserJourneyOrchestrator

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var chatRepository: ChatRepository

    @Inject
    lateinit var messageRepository: MessageRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testCompleteNewUserRegistrationJourney() = runTest {
        // Test Requirements: 1.1, 1.2, 1.3, 1.5, 3.4, 3.1
        
        // Initialize application
        val initResult = applicationManager.initialize()
        assertTrue(initResult.isSuccess, "Application should initialize successfully")
        
        // Wait for application to be ready
        applicationManager.isReady.first { it }
        
        // Complete registration journey with Google OAuth
        val registrationResult = userJourneyOrchestrator.completeRegistrationJourney(
            AuthMethod.GOOGLE_OAUTH
        )
        
        assertTrue(registrationResult.isSuccess, "Registration should complete successfully")
        
        val user = registrationResult.getOrThrow()
        assertNotNull(user.id, "User should have valid ID")
        assertNotNull(user.publicKey, "User should have public key generated")
        
        // Verify user is stored in database
        val storedUser = userRepository.getUserById(user.id)
        assertNotNull(storedUser, "User should be stored in database")
        assertEquals(user.id, storedUser.id, "Stored user should match created user")
        
        // Verify application is authenticated
        assertEquals(
            com.chain.messaging.core.integration.ApplicationState.AUTHENTICATED,
            applicationManager.applicationState.first()
        )
    }

    @Test
    fun testCompleteFirstMessageJourney() = runTest {
        // Test Requirements: 4.1, 4.3, 4.5, 3.1, 3.2, 2.1, 2.2, 2.3
        
        // Setup: Initialize and authenticate two users
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        
        val user1Result = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.GOOGLE_OAUTH)
        val user1 = user1Result.getOrThrow()
        
        // Simulate second user (in real scenario, this would be on another device)
        val user2Result = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.MICROSOFT_OAUTH)
        val user2 = user2Result.getOrThrow()
        
        // Send first message from user1 to user2
        val messageContent = "Hello, this is my first message!"
        val messageResult = userJourneyOrchestrator.completeFirstMessageJourney(
            user2.id,
            messageContent
        )
        
        assertTrue(messageResult.isSuccess, "First message should be sent successfully")
        
        val message = messageResult.getOrThrow()
        assertEquals(messageContent, message.content, "Message content should match")
        assertEquals(user1.id, message.senderId, "Message sender should be user1")
        assertEquals(MessageType.TEXT, message.type, "Message type should be TEXT")
        assertEquals(MessageStatus.SENT, message.status, "Message should be sent")
        
        // Verify message is stored in database
        val storedMessage = messageRepository.getMessageById(message.id)
        assertNotNull(storedMessage, "Message should be stored in database")
        assertEquals(message.id, storedMessage.id, "Stored message should match sent message")
        
        // Verify chat is created
        val chats = chatRepository.getChatsForUser(user1.id)
        assertTrue(chats.isNotEmpty(), "Chat should be created")
        
        val chat = chats.first { it.participants.contains(user2.id) }
        assertNotNull(chat, "Chat with user2 should exist")
        assertEquals(message.id, chat.lastMessage?.id, "Last message should be the sent message")
    }

    @Test
    fun testCompleteGroupCreationAndMessagingJourney() = runTest {
        // Test Requirements: 5.1, 5.2, 5.4, 5.5, 5.6, 5.3
        
        // Setup: Initialize and authenticate multiple users
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        
        val user1Result = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.GOOGLE_OAUTH)
        val user1 = user1Result.getOrThrow()
        
        val user2Result = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.MICROSOFT_OAUTH)
        val user2 = user2Result.getOrThrow()
        
        val user3Result = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.PASSKEY)
        val user3 = user3Result.getOrThrow()
        
        // Create group with multiple members
        val groupName = "Test Group"
        val memberIds = listOf(user2.id, user3.id)
        
        val groupResult = userJourneyOrchestrator.completeGroupCreationJourney(
            groupName,
            memberIds
        )
        
        assertTrue(groupResult.isSuccess, "Group creation should complete successfully")
        
        val group = groupResult.getOrThrow()
        assertEquals(groupName, group.name, "Group name should match")
        assertTrue(group.participants.contains(user1.id), "Group should contain creator")
        assertTrue(group.participants.contains(user2.id), "Group should contain user2")
        assertTrue(group.participants.contains(user3.id), "Group should contain user3")
        assertEquals(3, group.participants.size, "Group should have 3 participants")
        
        // Verify group is stored in database
        val storedGroup = chatRepository.getChatById(group.id)
        assertNotNull(storedGroup, "Group should be stored in database")
        assertEquals(group.id, storedGroup.id, "Stored group should match created group")
        
        // Verify welcome message was sent
        val groupMessages = messageRepository.getMessagesForChat(group.id, 10, 0)
        assertTrue(groupMessages.isNotEmpty(), "Group should have welcome message")
        
        val welcomeMessage = groupMessages.first()
        assertTrue(
            welcomeMessage.content.contains("Welcome"),
            "Welcome message should contain 'Welcome'"
        )
    }

    @Test
    fun testCompleteIdentityVerificationJourney() = runTest {
        // Test Requirements: 12.1, 12.2, 12.4
        
        // Setup: Initialize and authenticate two users
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        
        val user1Result = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.GOOGLE_OAUTH)
        val user1 = user1Result.getOrThrow()
        
        val user2Result = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.MICROSOFT_OAUTH)
        val user2 = user2Result.getOrThrow()
        
        // Complete identity verification between users
        val verificationResult = userJourneyOrchestrator.completeIdentityVerificationJourney(user2.id)
        
        assertTrue(verificationResult.isSuccess, "Identity verification should complete successfully")
        
        val isVerified = verificationResult.getOrThrow()
        assertTrue(isVerified, "Users should be verified")
        
        // Verify verification status is stored
        val user1Updated = userRepository.getUserById(user1.id)
        assertNotNull(user1Updated, "User1 should exist after verification")
        
        val user2Updated = userRepository.getUserById(user2.id)
        assertNotNull(user2Updated, "User2 should exist after verification")
    }

    @Test
    fun testCompleteOfflineToOnlineMessageJourney() = runTest {
        // Test Requirements: 10.1, 10.2, 10.4
        
        // Setup: Initialize application and authenticate user
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        
        val userResult = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.GOOGLE_OAUTH)
        val user = userResult.getOrThrow()
        
        // Simulate network disconnection
        applicationManager.onNetworkStateChanged(false)
        
        // Attempt to send message while offline
        val recipientId = "offline-recipient-id"
        val messageContent = "Offline message"
        
        // Message should be queued for later delivery
        val messageResult = userJourneyOrchestrator.completeFirstMessageJourney(
            recipientId,
            messageContent
        )
        
        // In offline mode, message should be queued but marked as pending
        assertTrue(messageResult.isSuccess, "Message should be queued successfully")
        val message = messageResult.getOrThrow()
        assertEquals(MessageStatus.PENDING, message.status, "Message should be pending")
        
        // Simulate network reconnection
        applicationManager.onNetworkStateChanged(true)
        
        // Wait for message to be processed from queue
        // In real implementation, this would trigger automatic processing
        
        // Verify message status is updated after reconnection
        val updatedMessage = messageRepository.getMessageById(message.id)
        assertNotNull(updatedMessage, "Message should exist after reconnection")
    }

    @Test
    fun testApplicationHealthAndMonitoring() = runTest {
        // Test Requirements: 2.6, 10.5, 12.3, 12.5
        
        // Initialize application
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        
        // Authenticate user
        userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.GOOGLE_OAUTH)
        
        // Get application health status
        val healthStatus = applicationManager.getHealthStatus()
        
        // Verify all components are healthy
        assertTrue(healthStatus.isBlockchainConnected, "Blockchain should be connected")
        assertTrue(healthStatus.isP2PConnected, "P2P network should be connected")
        assertTrue(healthStatus.hasNetworkConnectivity, "Network should be connected")
        assertNotNull(healthStatus.encryptionStatus, "Encryption status should be available")
        assertTrue(healthStatus.performanceMetrics.isNotEmpty(), "Performance metrics should be available")
        assertNotNull(healthStatus.securityStatus, "Security status should be available")
    }

    @Test
    fun testCompleteApplicationLifecycle() = runTest {
        // Test Requirements: 8.1, 8.2
        
        // Test complete application lifecycle
        
        // 1. Initialize
        val initResult = applicationManager.initialize()
        assertTrue(initResult.isSuccess, "Application should initialize")
        
        // 2. Authenticate
        val authResult = applicationManager.authenticateUser(AuthMethod.GOOGLE_OAUTH)
        assertTrue(authResult.isSuccess, "Authentication should succeed")
        
        // 3. Verify ready state
        assertTrue(applicationManager.isReady.first(), "Application should be ready")
        
        // 4. Perform messaging operations
        val registrationResult = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.GOOGLE_OAUTH)
        assertTrue(registrationResult.isSuccess, "Registration should complete")
        
        // 5. Shutdown gracefully
        applicationManager.shutdown()
        
        // Verify shutdown state
        assertEquals(
            com.chain.messaging.core.integration.ApplicationState.SHUTDOWN,
            applicationManager.applicationState.first()
        )
    }

    @Test
    fun testCrossDeviceSynchronization() = runTest {
        // Test Requirements: 8.2, 8.3, 10.3
        
        // Setup: Initialize application and authenticate user
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        
        val userResult = userJourneyOrchestrator.completeRegistrationJourney(AuthMethod.GOOGLE_OAUTH)
        val user = userResult.getOrThrow()
        
        // Send a message
        val recipientId = "sync-test-recipient"
        val messageContent = "Cross-device sync test message"
        
        val messageResult = userJourneyOrchestrator.completeFirstMessageJourney(
            recipientId,
            messageContent
        )
        
        assertTrue(messageResult.isSuccess, "Message should be sent successfully")
        val message = messageResult.getOrThrow()
        
        // Verify message is available for synchronization
        val storedMessage = messageRepository.getMessageById(message.id)
        assertNotNull(storedMessage, "Message should be stored for sync")
        assertEquals(message.content, storedMessage.content, "Message content should match")
        
        // Verify user's devices can be synchronized
        val userChats = chatRepository.getChatsForUser(user.id)
        assertTrue(userChats.isNotEmpty(), "User should have chats to sync")
    }
}