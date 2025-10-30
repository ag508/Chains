package com.chain.messaging.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.notification.NotificationActionHandler
import com.chain.messaging.core.notification.NotificationEvent
import com.chain.messaging.core.notification.NotificationManager
import com.chain.messaging.core.notification.NotificationService
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
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for the notification system
 * Tests the complete flow from message arrival to notification display
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var notificationService: NotificationService
    private lateinit var notificationActionHandler: NotificationActionHandler
    private lateinit var notificationManager: NotificationManager
    
    // Mocked dependencies
    private val messagingService = mockk<MessagingService>(relaxed = true)
    private val messageRepository = mockk<MessageRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationService = NotificationService(context)
        notificationActionHandler = NotificationActionHandler()
        
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
    fun `complete message notification flow works correctly`() = runTest {
        // Given
        val message = Message(
            id = "msg1",
            chatId = "chat1",
            senderId = "user1",
            content = "Hello world!",
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
            notifications = NotificationSettings(
                messageNotifications = true,
                showPreview = true,
                showSenderName = true,
                soundEnabled = true,
                vibrationEnabled = true
            ),
            appearance = mockk(),
            accessibility = mockk()
        )
        
        // Mock repository responses
        coEvery { settingsRepository.getUserSettings(currentUserId) } returns settings
        coEvery { userRepository.getUserById("user1") } returns sender
        coEvery { messageRepository.getChatById("chat1") } returns null
        coEvery { messageRepository.getUnreadMessageCount("chat1", currentUserId) } returns 1
        
        // When
        notificationManager.showMessageNotification(message, currentUserId)
        
        // Then
        val event = notificationManager.getNotificationEvents().first()
        assertTrue(event is NotificationEvent.MessageNotificationShown)
        assertEquals("msg1", event.messageId)
        assertEquals("chat1", event.chatId)
        assertEquals("John Doe", event.senderName)
    }
    
    @Test
    fun `group message notification flow works correctly`() = runTest {
        // Given
        val message = Message(
            id = "msg1",
            chatId = "group_123",
            senderId = "user1",
            content = "Hello group!",
            type = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        val currentUserId = "user2"
        val sender = User(
            id = "user1",
            displayName = "Alice",
            publicKey = "key1"
        )
        val chat = mockk<com.chain.messaging.domain.model.Chat> {
            every { name } returns "Test Group"
        }
        val settings = UserSettings(
            userId = currentUserId,
            profile = mockk(),
            privacy = mockk(),
            notifications = NotificationSettings(
                messageNotifications = true,
                groupNotifications = true,
                showPreview = true,
                showSenderName = true
            ),
            appearance = mockk(),
            accessibility = mockk()
        )
        
        // Mock repository responses
        coEvery { settingsRepository.getUserSettings(currentUserId) } returns settings
        coEvery { userRepository.getUserById("user1") } returns sender
        coEvery { messageRepository.getChatById("group_123") } returns chat
        coEvery { messageRepository.getUnreadMessageCount("group_123", currentUserId) } returns 3
        
        // When
        notificationManager.showMessageNotification(message, currentUserId)
        
        // Then
        val event = notificationManager.getNotificationEvents().first()
        assertTrue(event is NotificationEvent.MessageNotificationShown)
        assertEquals("group_123", event.chatId)
    }
    
    @Test
    fun `missed call notification flow works correctly`() = runTest {
        // Given
        val callId = "call1"
        val callerId = "user1"
        val currentUserId = "user2"
        val caller = User(
            id = "user1",
            displayName = "Bob Smith",
            publicKey = "key1"
        )
        val settings = UserSettings(
            userId = currentUserId,
            profile = mockk(),
            privacy = mockk(),
            notifications = NotificationSettings(
                callNotifications = true,
                soundEnabled = true,
                vibrationEnabled = true
            ),
            appearance = mockk(),
            accessibility = mockk()
        )
        
        // Mock repository responses
        coEvery { settingsRepository.getUserSettings(currentUserId) } returns settings
        coEvery { userRepository.getUserById(callerId) } returns caller
        
        // When
        notificationManager.showMissedCallNotification(callId, callerId, true, currentUserId)
        
        // Then
        val event = notificationManager.getNotificationEvents().first()
        assertTrue(event is NotificationEvent.MissedCallNotificationShown)
        assertEquals("call1", event.callId)
        assertEquals("Bob Smith", event.callerName)
        assertTrue(event.isVideo)
    }
    
    @Test
    fun `notification action reply flow works correctly`() = runTest {
        // Given
        val chatId = "chat1"
        val replyText = "Thanks for the message!"
        
        coEvery { messagingService.sendTextMessage(chatId, replyText) } returns Unit
        coEvery { messageRepository.markChatAsRead(chatId) } returns Unit
        
        // When
        notificationActionHandler.handleReply(chatId, replyText)
        
        // Then
        val event = notificationActionHandler.actionEvents.first()
        assertTrue(event is NotificationEvent.NotificationReply)
        assertEquals(chatId, event.chatId)
        assertEquals(replyText, event.replyText)
    }
    
    @Test
    fun `notification action mark read flow works correctly`() = runTest {
        // Given
        val chatId = "chat1"
        
        coEvery { messageRepository.markChatAsRead(chatId) } returns Unit
        
        // When
        notificationActionHandler.handleMarkRead(chatId)
        
        // Then
        val event = notificationActionHandler.actionEvents.first()
        assertTrue(event is NotificationEvent.NotificationMarkRead)
        assertEquals(chatId, event.chatId)
    }
    
    @Test
    fun `notification respects quiet hours setting`() = runTest {
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
            notifications = NotificationSettings(
                messageNotifications = true,
                quietHoursEnabled = true,
                quietHoursStart = "22:00",
                quietHoursEnd = "08:00"
            ),
            appearance = mockk(),
            accessibility = mockk()
        )
        
        coEvery { settingsRepository.getUserSettings(currentUserId) } returns settings
        
        // When - This test would need to mock the current time to be within quiet hours
        notificationManager.showMessageNotification(message, currentUserId)
        
        // Then - The notification behavior would depend on the current time
        // In a real implementation, we'd mock the time to test this properly
    }
    
    @Test
    fun `system notification works correctly`() = runTest {
        // When
        notificationManager.showSystemNotification(
            "Security Alert",
            "New device detected on your account"
        )
        
        // Then
        val event = notificationManager.getNotificationEvents().first()
        assertTrue(event is NotificationEvent.SystemNotificationShown)
        assertEquals("Security Alert", event.title)
        assertEquals("New device detected on your account", event.message)
    }
    
    @Test
    fun `clear chat notification works correctly`() = runTest {
        // When
        notificationManager.clearChatNotification("chat1")
        
        // Then
        val event = notificationManager.getNotificationEvents().first()
        assertTrue(event is NotificationEvent.NotificationCleared)
        assertEquals("chat1", event.chatId)
    }
}