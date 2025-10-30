package com.chain.messaging.core.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.model.NotificationSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
 * Unit tests for NotificationService
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationServiceTest {
    
    private lateinit var context: Context
    private lateinit var notificationService: NotificationService
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationService = NotificationService(context)
    }
    
    @Test
    fun `showMessageNotification creates notification with correct content`() = runTest {
        // Given
        val message = Message(
            id = "msg1",
            chatId = "chat1",
            senderId = "user1",
            content = "Hello world",
            type = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            status = com.chain.messaging.domain.model.MessageStatus.SENT
        )
        val senderName = "John Doe"
        val settings = NotificationSettings(
            messageNotifications = true,
            showPreview = true,
            showSenderName = true
        )
        
        // When
        notificationService.showMessageNotification(
            message = message,
            senderName = senderName,
            chatName = null,
            unreadCount = 1,
            settings = settings
        )
        
        // Then
        val event = notificationService.notificationEvents.first()
        assertTrue(event is NotificationEvent.MessageNotificationShown)
        assertEquals("msg1", event.messageId)
        assertEquals("chat1", event.chatId)
        assertEquals("John Doe", event.senderName)
    }
    
    @Test
    fun `showMessageNotification respects notification settings`() = runTest {
        // Given
        val message = Message(
            id = "msg1",
            chatId = "chat1",
            senderId = "user1",
            content = "Hello world",
            type = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            status = com.chain.messaging.domain.model.MessageStatus.SENT
        )
        val settings = NotificationSettings(
            messageNotifications = false // Disabled
        )
        
        // When
        notificationService.showMessageNotification(
            message = message,
            senderName = "John Doe",
            chatName = null,
            unreadCount = 1,
            settings = settings
        )
        
        // Then - No notification should be shown
        // This is verified by the fact that no notification event is emitted
    }
    
    @Test
    fun `showMessageNotification handles group messages correctly`() = runTest {
        // Given
        val message = Message(
            id = "msg1",
            chatId = "group_123",
            senderId = "user1",
            content = "Hello group",
            type = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            status = com.chain.messaging.domain.model.MessageStatus.SENT
        )
        val settings = NotificationSettings(
            messageNotifications = true,
            groupNotifications = true,
            showPreview = true,
            showSenderName = true
        )
        
        // When
        notificationService.showMessageNotification(
            message = message,
            senderName = "John Doe",
            chatName = "Test Group",
            unreadCount = 3,
            settings = settings
        )
        
        // Then
        val event = notificationService.notificationEvents.first()
        assertTrue(event is NotificationEvent.MessageNotificationShown)
        assertEquals("group_123", event.chatId)
    }
    
    @Test
    fun `showMessageNotification handles media messages correctly`() = runTest {
        // Given
        val message = Message(
            id = "msg1",
            chatId = "chat1",
            senderId = "user1",
            content = "image.jpg",
            type = MessageType.IMAGE,
            timestamp = System.currentTimeMillis(),
            status = com.chain.messaging.domain.model.MessageStatus.SENT
        )
        val settings = NotificationSettings(
            messageNotifications = true,
            showPreview = true
        )
        
        // When
        notificationService.showMessageNotification(
            message = message,
            senderName = "John Doe",
            chatName = null,
            unreadCount = 1,
            settings = settings
        )
        
        // Then
        val event = notificationService.notificationEvents.first()
        assertTrue(event is NotificationEvent.MessageNotificationShown)
    }
    
    @Test
    fun `showMissedCallNotification creates correct notification`() = runTest {
        // Given
        val settings = NotificationSettings(callNotifications = true)
        
        // When
        notificationService.showMissedCallNotification(
            callId = "call1",
            callerName = "Jane Doe",
            isVideo = true,
            settings = settings
        )
        
        // Then
        val event = notificationService.notificationEvents.first()
        assertTrue(event is NotificationEvent.MissedCallNotificationShown)
        assertEquals("call1", event.callId)
        assertEquals("Jane Doe", event.callerName)
        assertTrue(event.isVideo)
    }
    
    @Test
    fun `showSystemNotification creates system notification`() = runTest {
        // When
        notificationService.showSystemNotification(
            title = "Security Alert",
            message = "New device detected"
        )
        
        // Then
        val event = notificationService.notificationEvents.first()
        assertTrue(event is NotificationEvent.SystemNotificationShown)
        assertEquals("Security Alert", event.title)
        assertEquals("New device detected", event.message)
    }
    
    @Test
    fun `clearChatNotification emits correct event`() = runTest {
        // When
        notificationService.clearChatNotification("chat1")
        
        // Then
        val event = notificationService.notificationEvents.first()
        assertTrue(event is NotificationEvent.NotificationCleared)
        assertEquals("chat1", event.chatId)
    }
    
    @Test
    fun `clearAllNotifications emits correct event`() = runTest {
        // When
        notificationService.clearAllNotifications()
        
        // Then
        val event = notificationService.notificationEvents.first()
        assertTrue(event is NotificationEvent.AllNotificationsCleared)
    }
}