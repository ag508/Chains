package com.chain.messaging.core.security

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.ecc.Curve
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SecurityAlertManagerTest {
    
    private lateinit var securityAlertManager: SecurityAlertManager
    private val testIdentityKey1 = IdentityKey(Curve.generateKeyPair().publicKey)
    private val testIdentityKey2 = IdentityKey(Curve.generateKeyPair().publicKey)
    
    @Before
    fun setup() {
        securityAlertManager = SecurityAlertManager()
    }
    
    @Test
    fun `addAlert should add alert to list and update threat level`() = runTest {
        // Given
        val alert = SecurityAlert.SuspiciousActivity("user1", "login", "Multiple failed attempts")
        
        // When
        securityAlertManager.addAlert(alert)
        
        // Then
        val alerts = securityAlertManager.alerts.first()
        assertEquals(1, alerts.size)
        assertEquals(alert, alerts.first())
        
        val threatLevel = securityAlertManager.threatLevel.first()
        assertEquals(ThreatLevel.LOW, threatLevel)
    }
    
    @Test
    fun `addAlert should update threat level to MEDIUM for key change alerts`() = runTest {
        // Given
        val alert = SecurityAlert.IdentityKeyChanged(
            userId = "user1", 
            displayName = "User 1", 
            oldKey = testIdentityKey1, 
            newKey = testIdentityKey2, 
            changeTimestamp = System.currentTimeMillis()
        )
        
        // When
        securityAlertManager.addAlert(alert)
        
        // Then
        val threatLevel = securityAlertManager.threatLevel.first()
        assertEquals(ThreatLevel.MEDIUM, threatLevel)
    }
    
    @Test
    fun `addAlert should update threat level to HIGH for key mismatch alerts`() = runTest {
        // Given
        val alert = SecurityAlert.KeyMismatch("user1", "User 1", testIdentityKey1, testIdentityKey2)
        
        // When
        securityAlertManager.addAlert(alert)
        
        // Then
        val threatLevel = securityAlertManager.threatLevel.first()
        assertEquals(ThreatLevel.HIGH, threatLevel)
    }
    
    @Test
    fun `removeAlert should remove alert from list and update threat level`() = runTest {
        // Given
        val alert = SecurityAlert.SuspiciousActivity("user1", "login", "Multiple failed attempts")
        securityAlertManager.addAlert(alert)
        
        // When
        securityAlertManager.removeAlert(alert.id)
        
        // Then
        val alerts = securityAlertManager.alerts.first()
        assertTrue(alerts.isEmpty())
        
        val threatLevel = securityAlertManager.threatLevel.first()
        assertEquals(ThreatLevel.LOW, threatLevel)
    }
    
    @Test
    fun `clearAllAlerts should remove all alerts and reset threat level`() = runTest {
        // Given
        val alert1 = SecurityAlert.SuspiciousActivity("user1", "login", "Failed attempts")
        val alert2 = SecurityAlert.KeyMismatch("user2", "User 2", testIdentityKey1, testIdentityKey2)
        securityAlertManager.addAlert(alert1)
        securityAlertManager.addAlert(alert2)
        
        // When
        securityAlertManager.clearAllAlerts()
        
        // Then
        val alerts = securityAlertManager.alerts.first()
        assertTrue(alerts.isEmpty())
        
        val threatLevel = securityAlertManager.threatLevel.first()
        assertEquals(ThreatLevel.LOW, threatLevel)
    }
    
    @Test
    fun `getAlertsByType should return alerts of specified type`() = runTest {
        // Given
        val keyChangeAlert = SecurityAlert.IdentityKeyChanged(
            userId = "user1", 
            displayName = "User 1", 
            oldKey = testIdentityKey1, 
            newKey = testIdentityKey2, 
            changeTimestamp = System.currentTimeMillis()
        )
        val suspiciousAlert = SecurityAlert.SuspiciousActivity("user2", "login", "Failed attempts")
        val keyMismatchAlert = SecurityAlert.KeyMismatch("user3", "User 3", testIdentityKey1, testIdentityKey2)
        
        securityAlertManager.addAlert(keyChangeAlert)
        securityAlertManager.addAlert(suspiciousAlert)
        securityAlertManager.addAlert(keyMismatchAlert)
        
        // When
        val keyChangeAlerts = securityAlertManager.getAlertsByType(AlertType.KEY_CHANGE)
        val suspiciousAlerts = securityAlertManager.getAlertsByType(AlertType.SUSPICIOUS_ACTIVITY)
        val keyMismatchAlerts = securityAlertManager.getAlertsByType(AlertType.KEY_MISMATCH)
        
        // Then
        assertEquals(1, keyChangeAlerts.size)
        assertEquals(keyChangeAlert, keyChangeAlerts.first())
        
        assertEquals(1, suspiciousAlerts.size)
        assertEquals(suspiciousAlert, suspiciousAlerts.first())
        
        assertEquals(1, keyMismatchAlerts.size)
        assertEquals(keyMismatchAlert, keyMismatchAlerts.first())
    }
    
    @Test
    fun `getRecentAlerts should return alerts from last 24 hours`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentAlert = SecurityAlert.IdentityKeyChanged(
            userId = "user1", 
            displayName = "User 1", 
            oldKey = testIdentityKey1, 
            newKey = testIdentityKey2, 
            changeTimestamp = currentTime
        )
        val oldAlert = SecurityAlert.IdentityKeyChanged(
            userId = "user2", 
            displayName = "User 2", 
            oldKey = testIdentityKey1, 
            newKey = testIdentityKey2, 
            changeTimestamp = currentTime - (25 * 60 * 60 * 1000) // 25 hours ago
        )
        
        securityAlertManager.addAlert(recentAlert)
        securityAlertManager.addAlert(oldAlert)
        
        // When
        val recentAlerts = securityAlertManager.getRecentAlerts()
        
        // Then
        assertEquals(1, recentAlerts.size)
        assertEquals(recentAlert, recentAlerts.first())
    }
    
    @Test
    fun `analyzeSecurityThreats should return comprehensive analysis`() = runTest {
        // Given
        val keyChangeAlert = SecurityAlert.IdentityKeyChanged(
            userId = "user1", 
            displayName = "User 1", 
            oldKey = testIdentityKey1, 
            newKey = testIdentityKey2, 
            changeTimestamp = System.currentTimeMillis()
        )
        val keyMismatchAlert = SecurityAlert.KeyMismatch("user2", "User 2", testIdentityKey1, testIdentityKey2)
        val suspiciousAlert = SecurityAlert.SuspiciousActivity("user3", "login", "Failed attempts")
        
        securityAlertManager.addAlert(keyChangeAlert)
        securityAlertManager.addAlert(keyMismatchAlert)
        securityAlertManager.addAlert(suspiciousAlert)
        
        // When
        val analysis = securityAlertManager.analyzeSecurityThreats()
        
        // Then
        assertEquals(3, analysis.totalAlerts)
        assertEquals(3, analysis.recentAlerts) // All alerts are recent
        assertEquals(ThreatLevel.HIGH, analysis.threatLevel) // Due to key mismatch
        assertTrue(analysis.recommendations.isNotEmpty())
        
        // Check that recommendations include expected types
        val recommendationTypes = analysis.recommendations.map { it::class }
        assertTrue(recommendationTypes.contains(SecurityRecommendation.ReviewKeyChanges::class))
        assertTrue(recommendationTypes.contains(SecurityRecommendation.VerifyIdentities::class))
        assertTrue(recommendationTypes.contains(SecurityRecommendation.ReviewSuspiciousActivity::class))
    }
    
    @Test
    fun `createKeyChangeAlert should create proper alert`() {
        // When
        val alert = securityAlertManager.createKeyChangeAlert(
            "user1", "User 1", testIdentityKey1, testIdentityKey2
        )
        
        // Then
        assertTrue(alert is SecurityAlert.IdentityKeyChanged)
        assertEquals("user1", alert.userId)
        assertEquals("User 1", alert.displayName)
        assertEquals(testIdentityKey1, alert.oldKey)
        assertEquals(testIdentityKey2, alert.newKey)
    }
    
    @Test
    fun `createKeyMismatchAlert should create proper alert`() {
        // When
        val alert = securityAlertManager.createKeyMismatchAlert(
            "user1", "User 1", testIdentityKey1, testIdentityKey2
        )
        
        // Then
        assertTrue(alert is SecurityAlert.KeyMismatch)
        assertEquals("user1", alert.userId)
        assertEquals("User 1", alert.displayName)
        assertEquals(testIdentityKey1, alert.expectedKey)
        assertEquals(testIdentityKey2, alert.receivedKey)
    }
    
    @Test
    fun `createSuspiciousActivityAlert should create proper alert`() {
        // When
        val alert = securityAlertManager.createSuspiciousActivityAlert(
            "user1", "login", "Multiple failed attempts"
        )
        
        // Then
        assertTrue(alert is SecurityAlert.SuspiciousActivity)
        assertEquals("user1", alert.userId)
        assertEquals("login", alert.activityType)
        assertEquals("Multiple failed attempts", alert.details)
    }
    
    @Test
    fun `getSecurityScore should calculate score based on alerts and verification ratio`() {
        // Given
        val keyChangeAlert = SecurityAlert.IdentityKeyChanged(
            userId = "user1", 
            displayName = "User 1", 
            oldKey = testIdentityKey1, 
            newKey = testIdentityKey2, 
            changeTimestamp = System.currentTimeMillis()
        )
        securityAlertManager.addAlert(keyChangeAlert)
        
        // When
        val securityScore = securityAlertManager.getSecurityScore(
            verifiedContactsCount = 8,
            totalContactsCount = 10
        )
        
        // Then
        assertTrue(securityScore.score in 0..100)
        assertTrue(securityScore.factors.isNotEmpty())
        
        // Score should be reduced due to key change alert but increased due to high verification ratio
        val expectedScore = 100 - 15 + (0.8f * 20).toInt() // Base - key change penalty + verification bonus
        assertEquals(expectedScore, securityScore.score)
    }
    
    @Test
    fun `acknowledgeAlert should not remove alert but could track acknowledgment`() = runTest {
        // Given
        val alert = SecurityAlert.SuspiciousActivity("user1", "login", "Failed attempts")
        securityAlertManager.addAlert(alert)
        
        // When
        securityAlertManager.acknowledgeAlert(alert.id)
        
        // Then
        val alerts = securityAlertManager.alerts.first()
        assertEquals(1, alerts.size) // Alert should still be there
        // In a real implementation, you might track acknowledged status
    }
    
    @Test
    fun `threat level should escalate with multiple recent alerts`() = runTest {
        // Given - Add multiple recent alerts
        repeat(6) { i ->
            val alert = SecurityAlert.SuspiciousActivity("user$i", "activity", "details")
            securityAlertManager.addAlert(alert)
        }
        
        // When
        val threatLevel = securityAlertManager.threatLevel.first()
        
        // Then
        assertEquals(ThreatLevel.HIGH, threatLevel)
    }
}