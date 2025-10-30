package com.chain.messaging.core.webrtc

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class CallNotificationServiceTest {
    
    private lateinit var callNotificationService: CallNotificationService
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        callNotificationService = CallNotificationService(context)
    }
    
    @Test
    fun `showIncomingCallNotification should create and display notification`() = runTest {
        // Given
        val callId = "call_123"
        val callerName = "John Doe"
        val isVideo = false
        
        // When
        callNotificationService.showIncomingCallNotification(callId, callerName, isVideo)
        
        // Then
        val notification = callNotificationService.getCallNotification(callId)
        assertNotNull(notification)
        assertEquals(callId, notification?.callId)
        assertEquals(callerName, notification?.callerName)
        assertEquals(isVideo, notification?.isVideo)
        assertEquals(CallNotificationType.INCOMING, notification?.type)
        assertEquals("Incoming call", notification?.title)
        assertEquals("From $callerName", notification?.message)
    }
    
    @Test
    fun `showIncomingCallNotification should create video call notification`() = runTest {
        // Given
        val callId = "video_call_123"
        val callerName = "Jane Smith"
        val isVideo = true
        
        // When
        callNotificationService.showIncomingCallNotification(callId, callerName, isVideo)
        
        // Then
        val notification = callNotificationService.getCallNotification(callId)
        assertNotNull(notification)
        assertEquals("Incoming video call", notification?.title)
        assertTrue(notification?.isVideo == true)
    }
    
    @Test
    fun `showOngoingCallNotification should create ongoing call notification`() = runTest {
        // Given
        val callId = "ongoing_call_123"
        val callerName = "Bob Wilson"
        val isVideo = true
        val duration = "02:30"
        
        // When
        callNotificationService.showOngoingCallNotification(callId, callerName, isVideo, duration)
        
        // Then
        val notification = callNotificationService.getCallNotification(callId)
        assertNotNull(notification)
        assertEquals(callId, notification?.callId)
        assertEquals(callerName, notification?.callerName)
        assertEquals(isVideo, notification?.isVideo)
        assertEquals(CallNotificationType.ONGOING, notification?.type)
        assertEquals("Video call in progress", notification?.title)
        assertEquals("$callerName • $duration", notification?.message)
    }
    
    @Test
    fun `showOngoingCallNotification should create audio call notification`() = runTest {
        // Given
        val callId = "audio_call_123"
        val callerName = "Alice Brown"
        val isVideo = false
        val duration = "01:15"
        
        // When
        callNotificationService.showOngoingCallNotification(callId, callerName, isVideo, duration)
        
        // Then
        val notification = callNotificationService.getCallNotification(callId)
        assertNotNull(notification)
        assertEquals("Call in progress", notification?.title)
        assertFalse(notification?.isVideo == true)
    }
    
    @Test
    fun `showMissedCallNotification should create missed call notification`() = runTest {
        // Given
        val callId = "missed_call_123"
        val callerName = "Charlie Davis"
        val isVideo = false
        val timestamp = System.currentTimeMillis()
        
        // When
        callNotificationService.showMissedCallNotification(callId, callerName, isVideo, timestamp)
        
        // Then
        val notification = callNotificationService.getCallNotification(callId)
        assertNotNull(notification)
        assertEquals(callId, notification?.callId)
        assertEquals(callerName, notification?.callerName)
        assertEquals(isVideo, notification?.isVideo)
        assertEquals(CallNotificationType.MISSED, notification?.type)
        assertEquals("Missed call", notification?.title)
        assertEquals("From $callerName", notification?.message)
        assertEquals(timestamp, notification?.timestamp)
    }
    
    @Test
    fun `clearCallNotification should remove notification`() = runTest {
        // Given
        val callId = "call_to_clear_123"
        val callerName = "Test User"
        
        callNotificationService.showIncomingCallNotification(callId, callerName, false)
        
        // Verify notification exists
        assertNotNull(callNotificationService.getCallNotification(callId))
        
        // When
        callNotificationService.clearCallNotification(callId)
        
        // Then
        assertNull(callNotificationService.getCallNotification(callId))
    }
    
    @Test
    fun `clearAllCallNotifications should remove all notifications`() = runTest {
        // Given
        callNotificationService.showIncomingCallNotification("call_1", "User 1", false)
        callNotificationService.showIncomingCallNotification("call_2", "User 2", true)
        callNotificationService.showOngoingCallNotification("call_3", "User 3", false, "01:00")
        
        // Verify notifications exist
        assertEquals(3, callNotificationService.getAllCallNotifications().size)
        
        // When
        callNotificationService.clearAllCallNotifications()
        
        // Then
        assertEquals(0, callNotificationService.getAllCallNotifications().size)
    }
    
    @Test
    fun `updateCallStatus should update existing notification`() = runTest {
        // Given
        val callId = "status_update_call_123"
        val callerName = "Status Test User"
        
        callNotificationService.showIncomingCallNotification(callId, callerName, false)
        
        // When
        callNotificationService.updateCallStatus(callId, CallStatus.CONNECTED, "Call connected")
        
        // Then
        val notification = callNotificationService.getCallNotification(callId)
        assertNotNull(notification)
        assertEquals(CallStatus.CONNECTED, notification?.status)
        assertEquals("Call connected", notification?.message)
    }
    
    @Test
    fun `updateCallStatus should not fail for non-existent notification`() = runTest {
        // Given
        val nonExistentCallId = "non_existent_call_123"
        
        // When & Then - should not throw exception
        callNotificationService.updateCallStatus(nonExistentCallId, CallStatus.FAILED, "Call failed")
        
        // Notification should still not exist
        assertNull(callNotificationService.getCallNotification(nonExistentCallId))
    }
    
    @Test
    fun `getAllCallNotifications should return all active notifications`() = runTest {
        // Given
        callNotificationService.showIncomingCallNotification("call_1", "User 1", false)
        callNotificationService.showOngoingCallNotification("call_2", "User 2", true, "02:00")
        callNotificationService.showMissedCallNotification("call_3", "User 3", false, System.currentTimeMillis())
        
        // When
        val allNotifications = callNotificationService.getAllCallNotifications()
        
        // Then
        assertEquals(3, allNotifications.size)
        assertTrue(allNotifications.containsKey("call_1"))
        assertTrue(allNotifications.containsKey("call_2"))
        assertTrue(allNotifications.containsKey("call_3"))
        
        assertEquals(CallNotificationType.INCOMING, allNotifications["call_1"]?.type)
        assertEquals(CallNotificationType.ONGOING, allNotifications["call_2"]?.type)
        assertEquals(CallNotificationType.MISSED, allNotifications["call_3"]?.type)
    }
    
    @Test
    fun `callNotifications flow should emit updates`() = runTest {
        // Given
        val callId = "flow_test_call_123"
        val callerName = "Flow Test User"
        
        // When
        callNotificationService.showIncomingCallNotification(callId, callerName, false)
        
        // Then
        val notifications = callNotificationService.callNotifications.first()
        assertTrue(notifications.containsKey(callId))
        assertEquals(callerName, notifications[callId]?.callerName)
    }
    
    @Test
    fun `multiple notifications should be handled correctly`() = runTest {
        // Given
        val calls = listOf(
            Triple("call_1", "User 1", false),
            Triple("call_2", "User 2", true),
            Triple("call_3", "User 3", false)
        )
        
        // When
        calls.forEach { (callId, callerName, isVideo) ->
            callNotificationService.showIncomingCallNotification(callId, callerName, isVideo)
        }
        
        // Then
        val allNotifications = callNotificationService.getAllCallNotifications()
        assertEquals(3, allNotifications.size)
        
        calls.forEach { (callId, callerName, isVideo) ->
            val notification = allNotifications[callId]
            assertNotNull(notification)
            assertEquals(callerName, notification?.callerName)
            assertEquals(isVideo, notification?.isVideo)
        }
    }
    
    @Test
    fun `clearCallNotification should handle non-existent call gracefully`() = runTest {
        // Given
        val nonExistentCallId = "non_existent_call_123"
        
        // When & Then - should not throw exception
        callNotificationService.clearCallNotification(nonExistentCallId)
        
        // Should still have no notifications
        assertEquals(0, callNotificationService.getAllCallNotifications().size)
    }
}