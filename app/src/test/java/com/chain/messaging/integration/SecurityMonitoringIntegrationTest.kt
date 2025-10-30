package com.chain.messaging.integration

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.core.security.*
import com.chain.messaging.data.local.dao.SecurityEventDao
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SecurityMonitoringIntegrationTest {
    
    private lateinit var context: Context
    private val securityEventDao = mockk<SecurityEventDao>()
    private val connectivityManager = mockk<ConnectivityManager>()
    private val notificationManager = mockk<NotificationManagerCompat>()
    
    private lateinit var securityEventStorage: SecurityEventStorage
    private lateinit var threatDetector: ThreatDetector
    private lateinit var alertNotificationService: AlertNotificationService
    private lateinit var securityMonitoringManager: SecurityMonitoringManager
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        
        // Mock DAO operations
        coEvery { securityEventDao.insertEvent(any()) } just Runs
        coEvery { securityEventDao.getAllEvents() } returns emptyList()
        coEvery { securityEventDao.getEventsInTimeRange(any(), any()) } returns emptyList()
        coEvery { securityEventDao.getEventsByType(any()) } returns emptyList()
        coEvery { securityEventDao.getEventsBySeverity(any()) } returns emptyList()
        coEvery { securityEventDao.acknowledgeEvent(any()) } just Runs
        coEvery { securityEventDao.deleteEventsOlderThan(any()) } just Runs
        coEvery { securityEventDao.getEventCountByType() } returns emptyMap()
        
        // Mock notification manager
        every { notificationManager.notify(any(), any()) } just Runs
        every { notificationManager.cancel(any()) } just Runs
        
        // Mock connectivity manager
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.getNetworkCapabilities(any()) } returns null
        
        // Create components
        securityEventStorage = SecurityEventStorageImpl(securityEventDao)
        threatDetector = ThreatDetectorImpl(context, connectivityManager)
        alertNotificationService = AlertNotificationServiceImpl(context, notificationManager)
        securityMonitoringManager = SecurityMonitoringManagerImpl(
            context,
            securityEventStorage,
            threatDetector,
            alertNotificationService
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    
    @Test
    fun `complete security monitoring workflow should work end-to-end`() = runTest {
        // Given - Start monitoring
        securityMonitoringManager.startMonitoring()
        advanceUntilIdle()
        
        // When - Report a critical security event
        val criticalEvent = SecurityEvent(
            id = "critical-test-event",
            type = SecurityEventType.POTENTIAL_MITM_ATTACK,
            timestamp = LocalDateTime.now(),
            severity = SecuritySeverity.CRITICAL,
            description = "Potential man-in-the-middle attack detected during integration test",
            metadata = mapOf("test" to "integration"),
            userId = "test-user",
            deviceId = "test-device",
            ipAddress = "192.168.1.100"
        )
        
        securityMonitoringManager.reportSecurityEvent(criticalEvent)
        advanceUntilIdle()
        
        // Then - Verify event was stored
        coVerify { securityEventDao.insertEvent(any()) }
        
        // And - Verify notification was sent
        verify { notificationManager.notify(any(), any()) }
        
        // And - Verify security status reflects the threat
        val securityStatus = securityMonitoringManager.getSecurityStatus().first()
        assertTrue(securityStatus.activeThreats > 0)
        assertTrue(securityStatus.level != SecurityLevel.SECURE)
    }
    
    @Test
    fun `security alert flow should work from detection to acknowledgment`() = runTest {
        // Given - Start monitoring and collect alerts
        val alertsReceived = mutableListOf<SecurityAlert>()
        val alertsJob = launch {
            securityMonitoringManager.getSecurityAlerts().collect { alert ->
                alertsReceived.add(alert)
            }
        }
        
        securityMonitoringManager.startMonitoring()
        advanceUntilIdle()
        
        // When - Report high severity event
        val highSeverityEvent = SecurityEvent(
            id = "high-severity-event",
            type = SecurityEventType.ENCRYPTION_FAILURE,
            timestamp = LocalDateTime.now(),
            severity = SecuritySeverity.HIGH,
            description = "Message encryption failed during transmission"
        )
        
        securityMonitoringManager.reportSecurityEvent(highSeverityEvent)
        advanceUntilIdle()
        
        // Then - Verify alert was generated
        assertTrue(alertsReceived.isNotEmpty())
        val alert = alertsReceived.first()
        assertEquals(SecurityEventType.ENCRYPTION_FAILURE, alert.type)
        assertEquals(SecuritySeverity.HIGH, alert.severity)
        
        // When - Acknowledge the alert
        securityMonitoringManager.acknowledgeAlert(alert.id)
        advanceUntilIdle()
        
        // Then - Verify acknowledgment was processed
        coVerify { securityEventDao.acknowledgeEvent(alert.id) }
        
        alertsJob.cancel()
    }
    
    @Test
    fun `security recommendations should be generated based on event patterns`() = runTest {
        // Given - Mock recent events with key-related issues
        val keyRelatedEvents = listOf(
            mockk<com.chain.messaging.data.local.entity.SecurityEventEntity>().apply {
                every { toDomainModel() } returns SecurityEvent(
                    id = "key-event-1",
                    type = SecurityEventType.SUSPICIOUS_KEY_CHANGE,
                    timestamp = LocalDateTime.now().minusHours(2),
                    severity = SecuritySeverity.HIGH,
                    description = "Suspicious key change detected"
                )
            },
            mockk<com.chain.messaging.data.local.entity.SecurityEventEntity>().apply {
                every { toDomainModel() } returns SecurityEvent(
                    id = "key-event-2",
                    type = SecurityEventType.KEY_VERIFICATION_FAILURE,
                    timestamp = LocalDateTime.now().minusHours(1),
                    severity = SecuritySeverity.MEDIUM,
                    description = "Key verification failed"
                )
            }
        )
        
        coEvery { securityEventDao.getEventsInTimeRange(any(), any()) } returns keyRelatedEvents
        
        // When - Get security recommendations
        val recommendations = securityMonitoringManager.getSecurityRecommendations()
        
        // Then - Verify key management recommendation is included
        assertTrue(recommendations.isNotEmpty())
        val keyRecommendation = recommendations.find { 
            it.category == RecommendationCategory.KEY_MANAGEMENT 
        }
        assertTrue(keyRecommendation != null)
        assertTrue(keyRecommendation.actionSteps.isNotEmpty())
    }
    
    @Test
    fun `threat detection should integrate with security monitoring`() = runTest {
        // Given - Start monitoring
        securityMonitoringManager.startMonitoring()
        advanceUntilIdle()
        
        // When - Simulate authentication failures (this would normally come from auth service)
        val threatDetectorImpl = threatDetector as ThreatDetectorImpl
        repeat(6) {
            threatDetectorImpl.reportAuthenticationFailure()
        }
        
        // And - Trigger authentication pattern monitoring
        val authIndicators = threatDetector.monitorAuthenticationPatterns()
        
        // Then - Verify brute force attack is detected
        val bruteForceIndicator = authIndicators.find { 
            it.type == ThreatType.AUTHENTICATION_ANOMALY &&
            it.description.contains("Excessive authentication failures")
        }
        assertTrue(bruteForceIndicator != null)
        assertEquals(SecuritySeverity.HIGH, bruteForceIndicator.severity)
    }
    
    @Test
    fun `security metrics should reflect system state accurately`() = runTest {
        // Given - Mock events for metrics calculation
        val mockEvents = listOf(
            mockk<com.chain.messaging.data.local.entity.SecurityEventEntity>().apply {
                every { toDomainModel() } returns SecurityEvent(
                    id = "metric-event-1",
                    type = SecurityEventType.FAILED_LOGIN_ATTEMPT,
                    timestamp = LocalDateTime.now().minusHours(12),
                    severity = SecuritySeverity.MEDIUM,
                    description = "Failed login attempt"
                )
            },
            mockk<com.chain.messaging.data.local.entity.SecurityEventEntity>().apply {
                every { toDomainModel() } returns SecurityEvent(
                    id = "metric-event-2",
                    type = SecurityEventType.SUSPICIOUS_NETWORK_ACTIVITY,
                    timestamp = LocalDateTime.now().minusDays(2),
                    severity = SecuritySeverity.LOW,
                    description = "Suspicious network activity"
                )
            }
        )
        
        coEvery { securityEventDao.getAllEvents() } returns mockEvents
        coEvery { securityEventDao.getEventCountByType() } returns mapOf(
            "FAILED_LOGIN_ATTEMPT" to 1,
            "SUSPICIOUS_NETWORK_ACTIVITY" to 1
        )
        
        // When - Get security metrics
        val metrics = securityMonitoringManager.getSecurityMetrics()
        
        // Then - Verify metrics are calculated correctly
        assertEquals(2, metrics.totalEvents)
        assertEquals(1, metrics.eventsLast24Hours)
        assertTrue(metrics.securityScore in 0..100)
        assertEquals(2, metrics.eventsByType.size)
    }
    
    @Test
    fun `notification preferences should affect alert delivery`() = runTest {
        // Given - Set notification preferences to only critical alerts
        val preferences = AlertNotificationPreferences(
            enablePushNotifications = true,
            minimumSeverity = SecuritySeverity.CRITICAL
        )
        alertNotificationService.updateNotificationPreferences(preferences)
        
        // When - Send medium severity alert
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
        
        alertNotificationService.sendAlert(mediumAlert)
        
        // Then - Verify no notification was sent (below threshold)
        verify(exactly = 0) { notificationManager.notify(any(), any()) }
        
        // When - Send critical alert
        val criticalAlert = SecurityAlert(
            id = "critical-alert",
            type = SecurityEventType.BLOCKCHAIN_TAMPERING,
            severity = SecuritySeverity.CRITICAL,
            title = "Critical Alert",
            message = "Critical security breach",
            timestamp = LocalDateTime.now(),
            actionRequired = true,
            recommendedActions = listOf("Immediate action required")
        )
        
        alertNotificationService.sendAlert(criticalAlert)
        
        // Then - Verify notification was sent
        verify { notificationManager.notify(any(), any()) }
    }
    
    @Test
    fun `system should handle concurrent security events properly`() = runTest {
        // Given - Start monitoring
        securityMonitoringManager.startMonitoring()
        advanceUntilIdle()
        
        // When - Report multiple concurrent events
        val events = (1..5).map { i ->
            SecurityEvent(
                id = "concurrent-event-$i",
                type = SecurityEventType.values()[i % SecurityEventType.values().size],
                timestamp = LocalDateTime.now().minusMinutes(i.toLong()),
                severity = SecuritySeverity.values()[i % SecuritySeverity.values().size],
                description = "Concurrent security event $i"
            )
        }
        
        // Report all events concurrently
        events.forEach { event ->
            launch {
                securityMonitoringManager.reportSecurityEvent(event)
            }
        }
        advanceUntilIdle()
        
        // Then - Verify all events were processed
        coVerify(exactly = events.size) { securityEventDao.insertEvent(any()) }
        
        // And - Verify system state is updated
        val securityStatus = securityMonitoringManager.getSecurityStatus().first()
        assertTrue(securityStatus.lastScanTime != null)
    }
    
    @Test
    fun `cleanup should remove old security events`() = runTest {
        // Given - Start monitoring (which triggers cleanup)
        securityMonitoringManager.startMonitoring()
        advanceUntilIdle()
        
        // Then - Verify cleanup was called
        coVerify { securityEventDao.deleteEventsOlderThan(any()) }
    }
}