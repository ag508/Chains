package com.chain.messaging.core.notification

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.model.NotificationSettings
import com.chain.messaging.domain.model.User
import com.chain.messaging.domain.model.UserSettings
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.domain.repository.SettingsRepository
import com.chain.messaging.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for NotificationManager
 */
class NotificationManagerTest {
    
    private val notificationService = mockk<NotificationService>(relaxed = true)
    private val notificationActionHandler = mockk<NotificationActionHandler>(relaxed = true)
    private val messagingService = mockk<MessagingService>(relaxed = true)
    private val messageRepository = mockk<MessageRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    
    private lateinit var notificationManager: NotificationManager
    
    @Before
    fun setup() {
        notificationManager = NotificationManager(
            notificationService,
            notificationActionHandler,
            messagingService,
            messageRepository,
            userRepository,
            settingsRepository
        )
    }
    
    @Test
    fun `showMessageNotification shows notification when conditions are met`() = runTest {
        // Given
        val message = Message(
            id = "msg1",
            chatId = "chat1",
            senderId = "user1",
            content = "Hello",
            type = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        val currentUserId = "user2"
        val sender = User(
            id = "user1",
            displayName = "John Doe",
            publicKey = "key1"
        )
        val settings = UserSettings(
            userId = currentUserId,
            profile = mockk(),
            privacy = mockk(),
            notifications = NotificationSettings(messageNotifications = true),
            appearance = mockk(),
            accessibility = mockk()
        )
        
        coEvery { settingsRepository.getUserSettings(currentUserId) } returns settings
        coEvery { userRepository.getUserById("user1") } returns sender
        coEvery { messageRepository.getChatById("chat1") } returns null
        coEvery { messageRepository.getUnreadMessageCount("chat1", currentUserId) } returns 1
        
        // When
        notificationManager.showMessageNotification(message, currentUserId)
        
        // Then
        verify {
            notificationService.showMessageNotification(
                message = message,
                senderName = "John Doe",
                chatName = null,
                unreadCount = 1,
                settings = settings.notifications
            )
        }
    }
    
    @Test
    fun `showMessageNotification does not show notification for own messages`() = runTest {
        // Given
        val message = Message(
            id = "msg1",
            chatId = "chat1",
            senderId = "user1",
            content = "Hello",
            type = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        val currentUserId = "user1" // Same as sender
        
        // When
        notificationManager.showMessageNotification(message, currentUserId)
        
        // Then
        verify(exactly = 0) {
            notificationService.showMessageNotification(any(), any(), any(), any(), any())
        }
    }
    
    @Test
    fun `showMessageNotification does not show notification when disabled`() = runTest {
        // Given
        val message = Message(
            id = "msg1",
            chatId = "chat1",
            senderId = "user1",
            content = "Hello",
            type = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        val currentUserId = "user2"
        val settings = UserSettings(
            userId = currentUserId,
            profile = mockk(),
            privacy = mockk(),
            notifications = NotificationSettings(messageNotifications = false), // Disabled
            appearance = mockk(),
            accessibility = mockk()
        )
        
        coEvery { settingsRepository.getUserSettings(currentUserId) } returns settings
        
        // When
        notificationManager.showMessageNotification(message, currentUserId)
        
        // Then
        verify(exactly = 0) {
            notificationService.showMessageNotification(any(), any(), any(), any(), any())
        }
    }
    
    @Test
    fun `showMissedCallNotification shows notification when conditions are met`() = runTest {
        // Given
        val callId = "call1"
        val callerId = "user1"
        val currentUserId = "user2"
        val caller = User(
            id = "user1",
            displayName = "Jane Doe",
            publicKey = "key1"
        )
        val settings = UserSettings(
            userId = currentUserId,
            profile = mockk(),
            privacy = mockk(),
            notifications = NotificationSettings(callNotifications = true),
            appearance = mockk(),
            accessibility = mockk()
        )
        
        coEvery { settingsRepository.getUserSettings(currentUserId) } returns settings
        coEvery { userRepository.getUserById(callerId) } returns caller
        
        // When
        notificationManager.showMissedCallNotification(callId, callerId, true, currentUserId)
        
        // Then
        verify {
            notificationService.showMissedCallNotification(
                callId = callId,
                callerName = "Jane Doe",
                isVideo = true,
                settings = settings.notifications
            )
        }
    }
    
    @Test
    fun `showSystemNotification delegates to service`() {
        // When
        notificationManager.showSystemNotification("Title", "Message")
        
        // Then
        verify {
            notificationService.showSystemNotification("Title", "Message", any())
        }
    }
    
    @Test
    fun `clearChatNotification delegates to service`() {
        // When
        notificationManager.clearChatNotification("chat1")
        
        // Then
        verify {
            notificationService.clearChatNotification("chat1")
        }
    }
    
    @Test
    fun `areNotificationsEnabled delegates to service`() {
        // Given
        every { notificationService.areNotificationsEnabled() } returns true
        
        // When
        val result = notificationManager.areNotificationsEnabled()
        
        // Then
        assertTrue(result)
        verify { notificationService.areNotificationsEnabled() }
    }
    
    @Test
    fun `shouldRequestNotificationPermission delegates to service`() {
        // Given
        every { notificationService.shouldRequestNotificationPermission() } returns false
        
        // When
        val result = notificationManager.shouldRequestNotificationPermission()
        
        // Then
        assertFalse(result)
        verify { notificationService.shouldRequestNotificationPermission() }
    }
}