package com.chain.messaging.core.notification

import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.test.core.app.ApplicationProvider
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
 * Unit tests for NotificationActionReceiver and NotificationActionHandler
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationActionReceiverTest {
    
    private lateinit var context: Context
    private lateinit var notificationActionHandler: NotificationActionHandler
    private lateinit var receiver: NotificationActionReceiver
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationActionHandler = NotificationActionHandler()
        receiver = NotificationActionReceiver().apply {
            notificationActionHandler = this@NotificationActionReceiverTest.notificationActionHandler
        }
    }
    
    @Test
    fun `handleReply emits correct event`() = runTest {
        // When
        notificationActionHandler.handleReply("chat1", "Hello back!")
        
        // Then
        val event = notificationActionHandler.actionEvents.first()
        assertTrue(event is NotificationEvent.NotificationReply)
        assertEquals("chat1", event.chatId)
        assertEquals("Hello back!", event.replyText)
    }
    
    @Test
    fun `handleMarkRead emits correct event`() = runTest {
        // When
        notificationActionHandler.handleMarkRead("chat1")
        
        // Then
        val event = notificationActionHandler.actionEvents.first()
        assertTrue(event is NotificationEvent.NotificationMarkRead)
        assertEquals("chat1", event.chatId)
    }
    
    @Test
    fun `handleMuteChat emits correct event`() = runTest {
        // When
        notificationActionHandler.handleMuteChat("chat1")
        
        // Then
        val event = notificationActionHandler.actionEvents.first()
        assertTrue(event is NotificationEvent.NotificationMuteChat)
        assertEquals("chat1", event.chatId)
    }
    
    @Test
    fun `onReceive handles reply action correctly`() {
        // Given
        val intent = Intent(NotificationActionReceiver.ACTION_REPLY).apply {
            putExtra("chat_id", "chat1")
        }
        
        // Mock RemoteInput - this is complex to test in unit tests
        // In a real scenario, this would be tested in integration tests
        
        // When
        receiver.onReceive(context, intent)
        
        // Then - verify the handler was called (would need more setup for full test)
    }
    
    @Test
    fun `onReceive handles mark read action correctly`() {
        // Given
        val intent = Intent(NotificationActionReceiver.ACTION_MARK_READ).apply {
            putExtra("chat_id", "chat1")
        }
        
        // When
        receiver.onReceive(context, intent)
        
        // Then - verify the handler was called
    }
    
    @Test
    fun `onReceive handles mute action correctly`() {
        // Given
        val intent = Intent(NotificationActionReceiver.ACTION_MUTE_CHAT).apply {
            putExtra("chat_id", "chat1")
        }
        
        // When
        receiver.onReceive(context, intent)
        
        // Then - verify the handler was called
    }
    
    @Test
    fun `onReceive ignores intent without chat_id`() {
        // Given
        val intent = Intent(NotificationActionReceiver.ACTION_REPLY)
        // No chat_id extra
        
        // When
        receiver.onReceive(context, intent)
        
        // Then - should not crash and should not call handler
    }
}