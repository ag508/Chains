package com.chain.messaging.core.security

import com.chain.messaging.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.signal.libsignal.protocol.IdentityKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages security alerts and threat detection
 */
@Singleton
class SecurityAlertManager @Inject constructor() {
    
    private val _alerts = MutableStateFlow<List<SecurityAlert>>(emptyList())
    val alerts: StateFlow<List<SecurityAlert>> = _alerts.asStateFlow()
    
    private val _threatLevel = MutableStateFlow(ThreatLevel.LOW)
    val threatLevel: StateFlow<ThreatLevel> = _threatLevel.asStateFlow()
    
    /**
     * Add a new security alert
     */
    fun addAlert(alert: SecurityAlert) {
        val currentAlerts = _alerts.value.toMutableList()
        currentAlerts.add(alert)
        _alerts.value = currentAlerts
        
        updateThreatLevel()
    }
    
    /**
     * Remove a security alert
     */
    fun removeAlert(alertId: String) {
        val currentAlerts = _alerts.value.toMutableList()
        currentAlerts.removeAll { it.id == alertId }
        _alerts.value = currentAlerts
        
        updateThreatLevel()
    }
    
    /**
     * Clear all alerts
     */
    fun clearAllAlerts() {
        _alerts.value = emptyList()
        _threatLevel.value = ThreatLevel.LOW
    }
    
    /**
     * Get alerts by type
     */
    fun getAlertsByType(type: AlertType): List<SecurityAlert> {
        return _alerts.value.filter { getAlertType(it) == type }
    }
    
    /**
     * Get recent alerts (within last 24 hours)
     */
    fun getRecentAlerts(): List<SecurityAlert> {
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return _alerts.value.filter { it.timestamp > oneDayAgo }
    }
    
    /**
     * Check for potential security threats
     */
    fun analyzeSecurityThreats(): SecurityAnalysis {
        val alerts = _alerts.value
        val recentAlerts = getRecentAlerts()
        
        val keyChangeCount = alerts.count { it is SecurityAlert.IdentityKeyChanged }
        val mismatchCount = alerts.count { it is SecurityAlert.KeyMismatch }
        val suspiciousCount = alerts.count { it is SecurityAlert.SuspiciousActivity }
        
        val recommendations = mutableListOf<SecurityRecommendation>()
        
        // Analyze key changes
        if (keyChangeCount > 0) {
            recommendations.add(SecurityRecommendation.ReviewKeyChanges(keyChangeCount))
        }
        
        // Analyze key mismatches
        if (mismatchCount > 0) {
            recommendations.add(SecurityRecommendation.VerifyIdentities(mismatchCount))
        }
        
        // Analyze suspicious activity
        if (suspiciousCount > 0) {
            recommendations.add(SecurityRecommendation.ReviewSuspiciousActivity(suspiciousCount))
        }
        
        // Check for patterns
        if (recentAlerts.size > 3) {
            recommendations.add(SecurityRecommendation.IncreaseSecurityMeasures)
        }
        
        return SecurityAnalysis(
            totalAlerts = alerts.size,
            recentAlerts = recentAlerts.size,
            threatLevel = _threatLevel.value,
            recommendations = recommendations
        )
    }
    
    /**
     * Create alert for identity key change
     */
    fun createKeyChangeAlert(
        userId: String,
        displayName: String,
        oldKey: IdentityKey,
        newKey: IdentityKey
    ): SecurityAlert.IdentityKeyChanged {
        return SecurityAlert.IdentityKeyChanged(
            userId = userId,
            displayName = displayName,
            oldKey = oldKey,
            newKey = newKey,
            changeTimestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Create alert for key mismatch
     */
    fun createKeyMismatchAlert(
        userId: String,
        displayName: String,
        expectedKey: IdentityKey,
        receivedKey: IdentityKey
    ): SecurityAlert.KeyMismatch {
        return SecurityAlert.KeyMismatch(
            userId = userId,
            displayName = displayName,
            expectedKey = expectedKey,
            receivedKey = receivedKey
        )
    }
    
    /**
     * Create alert for suspicious activity
     */
    fun createSuspiciousActivityAlert(
        userId: String,
        activityType: String,
        details: String
    ): SecurityAlert.SuspiciousActivity {
        return SecurityAlert.SuspiciousActivity(
            userId = userId,
            activityType = activityType,
            details = details
        )
    }
    
    /**
     * Mark alert as acknowledged
     */
    fun acknowledgeAlert(alertId: String) {
        val currentAlerts = _alerts.value.toMutableList()
        val alertIndex = currentAlerts.indexOfFirst { it.id == alertId }
        
        if (alertIndex != -1) {
            val alert = currentAlerts[alertIndex]
            // In a real implementation, you might want to track acknowledged status
            // For now, we'll just keep the alert but could add an acknowledged flag
        }
    }
    
    /**
     * Get security score based on current alerts and verification status
     */
    fun getSecurityScore(verifiedContactsCount: Int, totalContactsCount: Int): SecurityScore {
        val alerts = _alerts.value
        val recentAlerts = getRecentAlerts()
        
        var score = 100
        
        // Deduct points for alerts
        score -= alerts.count { it is SecurityAlert.IdentityKeyChanged } * 15
        score -= alerts.count { it is SecurityAlert.KeyMismatch } * 20
        score -= alerts.count { it is SecurityAlert.SuspiciousActivity } * 10
        
        // Deduct points for recent activity
        score -= recentAlerts.size * 5
        
        // Add points for verified contacts
        if (totalContactsCount > 0) {
            val verificationRatio = verifiedContactsCount.toFloat() / totalContactsCount
            score += (verificationRatio * 20).toInt()
        }
        
        // Ensure score is between 0 and 100
        score = score.coerceIn(0, 100)
        
        return SecurityScore(
            score = score,
            level = when {
                score >= 80 -> SecurityLevel.HIGH
                score >= 60 -> SecurityLevel.MEDIUM
                else -> SecurityLevel.LOW
            },
            factors = getScoreFactors(alerts, verifiedContactsCount, totalContactsCount)
        )
    }
    
    // Private helper methods
    
    private fun updateThreatLevel() {
        val alerts = _alerts.value
        val recentAlerts = getRecentAlerts()
        
        _threatLevel.value = when {
            recentAlerts.size > 5 || alerts.any { it is SecurityAlert.KeyMismatch } -> ThreatLevel.HIGH
            recentAlerts.size > 2 || alerts.any { it is SecurityAlert.IdentityKeyChanged } -> ThreatLevel.MEDIUM
            else -> ThreatLevel.LOW
        }
    }
    
    private fun getAlertType(alert: SecurityAlert): AlertType {
        return when (alert) {
            is SecurityAlert.IdentityKeyChanged -> AlertType.KEY_CHANGE
            is SecurityAlert.KeyMismatch -> AlertType.KEY_MISMATCH
            is SecurityAlert.SuspiciousActivity -> AlertType.SUSPICIOUS_ACTIVITY
            is SecurityAlert.PolicyViolation -> AlertType.POLICY_VIOLATION
        }
    }
    
    private fun getScoreFactors(
        alerts: List<SecurityAlert>,
        verifiedContactsCount: Int,
        totalContactsCount: Int
    ): List<SecurityScoreFactor> {
        val factors = mutableListOf<SecurityScoreFactor>()
        
        if (alerts.isNotEmpty()) {
            factors.add(SecurityScoreFactor.ActiveAlerts(alerts.size))
        }
        
        if (totalContactsCount > 0) {
            val verificationRatio = verifiedContactsCount.toFloat() / totalContactsCount
            factors.add(SecurityScoreFactor.VerificationRatio(verificationRatio))
        }
        
        return factors
    }
}

