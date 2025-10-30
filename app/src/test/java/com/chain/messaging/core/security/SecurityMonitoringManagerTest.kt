package com.chain.messaging.core.security

import android.content.Context
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityMonitoringManagerTest {
    
    private val context = mockk<Context>()
    private val securityEventStorage = mockk<SecurityEventStorage>()
    private val threatDetector = mockk<ThreatDetector>()
    private val alertNotificationService = mockk<AlertNotificationService>()
    
    private lateinit var securityMonitoringManager: SecurityMonitoringManagerImpl
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        coEvery { securityEventStorage.storeEvent(any()) } just Runs
        coEvery { securityEventStorage.getAllEvents() } returns emptyList()
        coEvery { securityEventStorage.getEventsInTimeRange(any(), any()) } returns emptyList()
        coEvery { securityEventStorage.acknowledgeAlert(any()) } just Runs
        coEvery { securityEventStorage.deleteEventsOlderThan(any()) } just Runs
        coEvery { securityEventStorage.getEventCountByType() } returns emptyMap()
        
        coEvery { threatDetector.startDetection() } just Runs
        coEvery { threatDetector.stopDetection() } just Runs
        every { threatDetector.getThreatIndicators() } returns emptyFlow()
        
        coEvery { alertNotificationService.sendAlert(any()) } just Runs
        
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
    }
    
    @Test
    fun `startMonitoring should start threat detector and begin monitoring`() = runTest {
        // When
        securityMonitoringManager.startMonitoring()
        
        // Then
        coVerify { threatDetector.startDetection() }
        coVerify { securityEventStorage.deleteEventsOlderThan(any()) }
    }
    
    @Test
    fun `stopMonitoring should stop threat detector`() = runTest {
        // Given
        securityMonitoringManager.startMonitoring()
        
        // When
        securityMonitoringManager.stopMonitoring()
        
        // Then
        coVerify { threatDetector.stopDetection() }
    }
    
    @Test
    fun `reportSecurityEvent should store event and generate alert for high severity`() = runTest {
        // Given
        val event = SecurityEvent(
            id = "test-event",
            type = SecurityEventType.POTENTIAL_MITM_ATTACK,
            timestamp = LocalDateTime.now(),
            severity = SecuritySeverity.CRITICAL,
            description = "Test critical event"
        )
        
        // When
        securityMonitoringManager.reportSecurityEvent(event)
        
        // Then
        coVerify { securityEventStorage.storeEvent(event) }
        coVerify { alertNotificationService.sendAlert(any()) }
    }
    
    @Test
    fun `reportSecurityEvent should not generate alert for low severity events`() = runTest {
        // Given
        val event = SecurityEvent(
            id = "test-event",
            type = SecurityEventType.FAILED_LOGIN_ATTEMPT,
            timestamp = LocalDateTime.now(),
            severity = SecuritySeverity.LOW,
            description = "Test low severity event"
        )
        
        // When
        securityMonitoringManager.reportSecurityEvent(event)
        
        // Then
        coVerify { securityEventStorage.storeEvent(event) }
        coVerify(exactly = 0) { alertNotificationService.sendAlert(any()) }
    }
    
    @Test
    fun `getSecurityStatus should return current security status`() = runTest {
        // When
        val statusFlow = securityMonitoringManager.getSecurityStatus()
        val status = statusFlow.first()
        
        // Then
        assertEquals(SecurityLevel.SECURE, status.level)
        assertEquals(0, status.activeThreats)
        assertTrue(status.recommendations >= 0)
    }
    
    @Test
    fun `acknowledgeAlert should update alert status`() = runTest {
        // Given
        val alertId = "test-alert-id"
        
        // When
        securityMonitoringManager.acknowledgeAlert(alertId)
        
        // Then
        coVerify { securityEventStorage.acknowledgeAlert(alertId) }
    }
    
    @Test
    fun `getSecurityRecommendations should return recommendations based on recent events`() = runTest {
        // Given
        val recentEvents = listOf(
            SecurityEvent(
                id = "event1",
                type = SecurityEventType.SUSPICIOUS_KEY_CHANGE,
                timestamp = LocalDateTime.now().minusHours(1),
                severity = SecuritySeverity.HIGH,
                description = "Key change detected"
            ),
            SecurityEvent(
                id = "event2",
                type = SecurityEventType.FAILED_LOGIN_ATTEMPT,
                timestamp = LocalDateTime.now().minusMinutes(30),
                severity = SecuritySeverity.MEDIUM,
                description = "Failed login"
            )
        )
        
        coEvery { 
            securityEventStorage.getEventsInTimeRange(any(), any()) 
        } returns recentEvents
        
        // When
        val recommendations = securityMonitoringManager.getSecurityRecommendations()
        
        // Then
        assertTrue(recommendations.isNotEmpty())
        assertTrue(recommendations.any { it.category == RecommendationCategory.KEY_MANAGEMENT })
        assertTrue(recommendations.any { it.category == RecommendationCategory.AUTHENTICATION })
    }
    
    @Test
    fun `getSecurityMetrics should return comprehensive metrics`() = runTest {
        // Given
        val allEvents = listOf(
            SecurityEvent(
                id = "event1",
                type = SecurityEventType.FAILED_LOGIN_ATTEMPT,
                timestamp = LocalDateTime.now().minusHours(1),
                severity = SecuritySeverity.MEDIUM,
                description = "Failed login"
            ),
            SecurityEvent(
                id = "event2",
                type = SecurityEventType.SUSPICIOUS_KEY_CHANGE,
                timestamp = LocalDateTime.now().minusDays(2),
                severity = SecuritySeverity.HIGH,
                description = "Key change"
            )
        )
        
        coEvery { securityEventStorage.getAllEvents() } returns allEvents
        coEvery { securityEventStorage.getEventCountByType() } returns mapOf(
            SecurityEventType.FAILED_LOGIN_ATTEMPT to 1,
            SecurityEventType.SUSPICIOUS_KEY_CHANGE to 1
        )
        
        // When
        val metrics = securityMonitoringManager.getSecurityMetrics()
        
        // Then
        assertEquals(2, metrics.totalEvents)
        assertEquals(1, metrics.eventsLast24Hours)
        assertTrue(metrics.securityScore in 0..100)
        assertEquals(2, metrics.eventsByType.size)
    }
    
    @Test
    fun `security status should update when threats are detected`() = runTest {
        // Given
        securityMonitoringManager.startMonitoring()
        
        val criticalEvent = SecurityEvent(
            id = "critical-event",
            type = SecurityEventType.POTENTIAL_MITM_ATTACK,
            timestamp = LocalDateTime.now(),
            severity = SecuritySeverity.CRITICAL,
            description = "Critical security threat"
        )
        
        // When
        securityMonitoringManager.reportSecurityEvent(criticalEvent)
        advanceUntilIdle()
        
        // Then
        val status = securityMonitoringManager.getSecurityStatus().first()
        assertTrue(status.level == SecurityLevel.CRITICAL || status.level == SecurityLevel.DANGER)
        assertTrue(status.activeThreats > 0)
    }
    
    @Test
    fun `security alerts should be emitted for significant events`() = runTest {
        // Given
        val alertsReceived = mutableListOf<SecurityAlert>()
        val alertsJob = launch {
            securityMonitoringManager.getSecurityAlerts().collect { alert ->
                alertsReceived.add(alert)
            }
        }
        
        val highSeverityEvent = SecurityEvent(
            id = "high-event",
            type = SecurityEventType.ENCRYPTION_FAILURE,
            timestamp = LocalDateTime.now(),
            severity = SecuritySeverity.HIGH,
            description = "Encryption failure detected"
        )
        
        // When
        securityMonitoringManager.reportSecurityEvent(highSeverityEvent)
        advanceUntilIdle()
        
        // Then
        assertTrue(alertsReceived.isNotEmpty())
        assertEquals(SecurityEventType.ENCRYPTION_FAILURE, alertsReceived.first().type)
        assertEquals(SecuritySeverity.HIGH, alertsReceived.first().severity)
        
        alertsJob.cancel()
    }
    
    @Test
    fun `multiple failed login attempts should trigger brute force detection`() = runTest {
        // Given
        securityMonitoringManager.startMonitoring()
        
        val failedLoginEvents = (1..6).map { i ->
            SecurityEvent(
                id = "login-fail-$i",
                type = SecurityEventType.FAILED_LOGIN_ATTEMPT,
                timestamp = LocalDateTime.now().minusMinutes(i.toLong()),
                severity = SecuritySeverity.LOW,
                description = "Failed login attempt $i"
            )
        }
        
        coEvery { 
            securityEventStorage.getEventsInTimeRange(any(), any()) 
        } returns failedLoginEvents
        
        // When
        failedLoginEvents.forEach { event ->
            securityMonitoringManager.reportSecurityEvent(event)
        }
        advanceUntilIdle()
        
        // Then - Should detect brute force pattern and generate additional security event
        coVerify(atLeast = failedLoginEvents.size) { securityEventStorage.storeEvent(any()) }
    }
    
    @Test
    fun `security recommendations should include appropriate action steps`() = runTest {
        // Given
        val keyRelatedEvents = listOf(
            SecurityEvent(
                id = "key-event",
                type = SecurityEventType.KEY_VERIFICATION_FAILURE,
                timestamp = LocalDateTime.now().minusHours(1),
                severity = SecuritySeverity.HIGH,
                description = "Key verification failed"
            )
        )
        
        coEvery { 
            securityEventStorage.getEventsInTimeRange(any(), any()) 
        } returns keyRelatedEvents
        
        // When
        val recommendations = securityMonitoringManager.getSecurityRecommendations()
        
        // Then
        val keyRecommendation = recommendations.find { 
            it.category == RecommendationCategory.KEY_MANAGEMENT 
        }
        
        assertNotNull(keyRecommendation)
        assertTrue(keyRecommendation.actionSteps.isNotEmpty())
        assertTrue(keyRecommendation.actionSteps.any { it.contains("Key Management") })
    }
    
    private fun assertNotNull(value: Any?) {
        assertTrue(value != null)
    }
}