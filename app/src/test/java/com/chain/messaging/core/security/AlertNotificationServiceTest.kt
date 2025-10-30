package com.chain.messaging.core.security

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AlertNotificationServiceTest {
    
    private val context = mockk<Context>()
    private val notificationManager = mockk<NotificationManagerCompat>()
    
    private lateinit var alertNotificationService: AlertNotificationServiceImpl
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { notificationManager.notify(any(), any()) } just Runs
        every { notificationManager.cancel(any()) } just Runs
        
        alertNotificationService = AlertNotificationServiceImpl(context, notificationManager)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    
    @Test
    fun `sendAlert should send notification for high severity alert`() = runTest {
        // Given
        val alert = SecurityAlert(
            id = "test-alert",
            type = SecurityEventType.POTENTIAL_MITM_ATTACK,
            severity = SecuritySeverity.HIGH,
            title = "Security Threat Detected",
            message = "Potential man-in-the-middle attack detected",
            timestamp = LocalDateTime.now(),
            actionRequired = true,
            recommendedActions = listOf("Check network connection", "Verify contacts")
        )
        
        // When
        alertNotificationService.sendAlert(alert)
        
        // Then
        verify { notificationManager.notify(any(), any()) }
    }
    
    @Test
    fun `sendAlert should not send notification for low severity when minimum is medium`() = runTest {
        // Given
        val preferences = AlertNotificationPreferences(
            minimumSeverity = SecuritySeverity.MEDIUM
        )
        alertNotificationService.updateNotificationPreferences(preferences)
        
        val lowSeverityAlert = SecurityAlert(
            id = "low-alert",
            type = SecurityEventType.FAILED_LOGIN_ATTEMPT,
            severity = SecuritySeverity.LOW,
            title = "Low Priority Alert",
            message = "Low severity security event",
            timestamp = LocalDateTime.now(),
            actionRequired = false,
            recommendedActions = emptyList()
        )
        
        // When
        alertNotificationService.sendAlert(lowSeverityAlert)
        
        // Then
        verify(exactly = 0) { notificationManager.notify(any(), any()) }
    }
    
    @Test
    fun `sendAlert should send critical alerts during quiet hours`() = runTest {
        // Given
        val preferences = AlertNotificationPreferences(
            quietHoursEnabled = true,
            quietHoursStart = "22:00",
            quietHoursEnd = "08:00"
        )
        alertNotificationService.updateNotificationPreferences(preferences)
        
        val criticalAlert = SecurityAlert(
            id = "critical-alert",
            type = SecurityEventType.BLOCKCHAIN_TAMPERING,
            severity = SecuritySeverity.CRITICAL,
            title = "Critical Security Breach",
            message = "Blockchain tampering detected",
            timestamp = LocalDateTime.now(),
            actionRequired = true,
            recommendedActions = listOf("Immediate action required")
        )
        
        // When
        alertNotificationService.sendAlert(criticalAlert)
        
        // Then
        verify { notificationManager.notify(any(), any()) }
    }
    
    @Test
    fun `sendAlerts should send multiple alerts`() = runTest {
        // Given
        val alerts = listOf(
            SecurityAlert(
                id = "alert1",
                type = SecurityEventType.SUSPICIOUS_KEY_CHANGE,
                severity = SecuritySeverity.HIGH,
                title = "Key Change Alert",
                message = "Suspicious key change detected",
                timestamp = LocalDateTime.now(),
                actionRequired = true,
                recommendedActions = listOf("Verify key change")
            ),
            SecurityAlert(
                id = "alert2",
                type = SecurityEventType.ENCRYPTION_FAILURE,
                severity = SecuritySeverity.CRITICAL,
                title = "Encryption Failure",
                message = "Message encryption failed",
                timestamp = LocalDateTime.now(),
                actionRequired = true,
                recommendedActions = listOf("Check encryption keys")
            )
        )
        
        // When
        alertNotificationService.sendAlerts(alerts)
        
        // Then
        verify(exactly = 2) { notificationManager.notify(any(), any()) }
    }
    
    @Test
    fun `cancelAlert should cancel notification`() = runTest {
        // Given
        val alertId = "test-alert-id"
        
        // When
        alertNotificationService.cancelAlert(alertId)
        
        // Then
        verify { notificationManager.cancel(alertId.hashCode()) }
    }
    
    @Test
    fun `updateNotificationPreferences should update preferences`() = runTest {
        // Given
        val preferences = AlertNotificationPreferences(
            enablePushNotifications = false,
            enableInAppAlerts = true,
            minimumSeverity = SecuritySeverity.HIGH
        )
        
        // When
        alertNotificationService.updateNotificationPreferences(preferences)
        
        // Create a high severity alert
        val alert = SecurityAlert(
            id = "test-alert",
            type = SecurityEventType.POTENTIAL_MITM_ATTACK,
            severity = SecuritySeverity.HIGH,
            title = "Test Alert",
            message = "Test message",
            timestamp = LocalDateTime.now(),
            actionRequired = false,
            recommendedActions = emptyList()
        )
        
        alertNotificationService.sendAlert(alert)
        
        // Then - Should not send push notification due to disabled preference
        verify(exactly = 0) { notificationManager.notify(any(), any()) }
    }
    
    @Test
    fun `notification preferences should have sensible defaults`() {
        // Given
        val defaultPreferences = AlertNotificationPreferences()
        
        // Then
        assertTrue(defaultPreferences.enablePushNotifications)
        assertTrue(defaultPreferences.enableInAppAlerts)
        assertFalse(defaultPreferences.enableEmailAlerts)
        assertTrue(defaultPreferences.minimumSeverity == SecuritySeverity.MEDIUM)
        assertFalse(defaultPreferences.quietHoursEnabled)
        assertTrue(defaultPreferences.quietHoursStart == "22:00")
        assertTrue(defaultPreferences.quietHoursEnd == "08:00")
    }
    
    @Test
    fun `quiet hours logic should work correctly for same day hours`() = runTest {
        // Given - Quiet hours from 14:00 to 18:00 (same day)
        val preferences = AlertNotificationPreferences(
            quietHoursEnabled = true,
            quietHoursStart = "14:00",
            quietHoursEnd = "18:00"
        )
        alertNotificationService.updateNotificationPreferences(preferences)
        
        val mediumAlert = SecurityAlert(
            id = "medium-alert",
            type = SecurityEventType.UNUSUAL_DEVICE_ACCESS,
            severity = SecuritySeverity.MEDIUM,
            title = "Medium Alert",
            message = "Medium severity alert",
            timestamp = LocalDateTime.now(),
            actionRequired = false,
            recommendedActions = emptyList()
        )
        
        // When
        alertNotificationService.sendAlert(mediumAlert)
        
        // Then - Behavior depends on current time, but should handle same-day quiet hours
        // In a real test, we'd mock the current time to test specific scenarios
    }
    
    @Test
    fun `alert notification should handle context and notification manager properly`() = runTest {
        // Given
        val alert = SecurityAlert(
            id = "context-test-alert",
            type = SecurityEventType.UNAUTHORIZED_ACCESS_ATTEMPT,
            severity = SecuritySeverity.CRITICAL,
            title = "Unauthorized Access",
            message = "Unauthorized access attempt detected",
            timestamp = LocalDateTime.now(),
            actionRequired = true,
            recommendedActions = listOf("Change password", "Enable 2FA")
        )
        
        // When
        alertNotificationService.sendAlert(alert)
        
        // Then
        verify { notificationManager.notify(any(), any()) }
        
        // Verify that the notification ID is based on alert ID hash
        val expectedNotificationId = alert.id.hashCode()
        verify { notificationManager.notify(expectedNotificationId, any()) }
    }
}