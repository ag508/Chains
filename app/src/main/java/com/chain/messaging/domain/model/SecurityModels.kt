package com.chain.messaging.domain.model

import org.signal.libsignal.protocol.IdentityKey
import java.time.LocalDateTime

/**
 * Shared security model classes to avoid redeclarations
 */

/**
 * Security alerts for various threats
 */
sealed class SecurityAlert(
    val id: String, 
    val timestamp: Long, 
    val isAcknowledged: Boolean = false,
    open val type: SecurityEventType? = null,
    open val severity: SecuritySeverity? = null,
    open val title: String = "",
    open val message: String = "",
    open val actionRequired: Boolean = false,
    open val recommendedActions: List<String> = emptyList(),
    open val displayName: String = "",
    open val activityType: String = "",
    open val details: String = ""
) {
    data class KeyMismatch(
        val userId: String,
        override val displayName: String,
        val expectedKey: IdentityKey,
        val receivedKey: IdentityKey,
        val acknowledged: Boolean = false
    ) : SecurityAlert(
        id = "key_mismatch_$userId", 
        timestamp = System.currentTimeMillis(), 
        isAcknowledged = acknowledged,
        type = SecurityEventType.KEY_VERIFICATION_FAILURE,
        severity = SecuritySeverity.HIGH,
        title = "Key Mismatch Detected",
        message = "Identity key mismatch detected for $displayName",
        actionRequired = true,
        recommendedActions = listOf("Verify contact identity", "Check safety numbers"),
        displayName = displayName,
        activityType = "KEY_MISMATCH",
        details = "Expected key differs from received key"
    ) {
        fun copy(isAcknowledged: Boolean = this.acknowledged): KeyMismatch {
            return KeyMismatch(userId, displayName, expectedKey, receivedKey, isAcknowledged)
        }
    }
    
    data class IdentityKeyChanged(
        val userId: String,
        override val displayName: String,
        val oldKey: IdentityKey,
        val newKey: IdentityKey,
        val changeTimestamp: Long = System.currentTimeMillis(),
        val acknowledged: Boolean = false
    ) : SecurityAlert(
        id = "key_changed_$userId", 
        timestamp = changeTimestamp, 
        isAcknowledged = acknowledged,
        type = SecurityEventType.SUSPICIOUS_KEY_CHANGE,
        severity = SecuritySeverity.CRITICAL,
        title = "Identity Key Changed",
        message = "Identity key has changed for $displayName",
        actionRequired = true,
        recommendedActions = listOf("Verify new identity", "Check with contact directly"),
        displayName = displayName,
        activityType = "IDENTITY_KEY_CHANGED",
        details = "Contact's identity key has been updated"
    ) {
        fun copy(isAcknowledged: Boolean = this.acknowledged): IdentityKeyChanged {
            return IdentityKeyChanged(userId, displayName, oldKey, newKey, changeTimestamp, isAcknowledged)
        }
    }
    
    data class SuspiciousActivity(
        val userId: String,
        override val activityType: String,
        override val details: String,
        val acknowledged: Boolean = false
    ) : SecurityAlert(
        id = "suspicious_${userId}_${System.currentTimeMillis()}", 
        timestamp = System.currentTimeMillis(), 
        isAcknowledged = acknowledged,
        type = SecurityEventType.SUSPICIOUS_NETWORK_ACTIVITY,
        severity = SecuritySeverity.MEDIUM,
        title = "Suspicious Activity Detected",
        message = "Unusual activity detected: $activityType",
        actionRequired = false,
        recommendedActions = listOf("Review activity details", "Monitor for further incidents"),
        displayName = "System",
        activityType = activityType,
        details = details
    ) {
        fun copy(isAcknowledged: Boolean = this.acknowledged): SuspiciousActivity {
            return SuspiciousActivity(userId, activityType, details, isAcknowledged)
        }
    }
    
    data class PolicyViolation(
        val policyType: String,
        val description: String,
        val securityLevel: SecurityLevel,
        val acknowledged: Boolean = false
    ) : SecurityAlert(
        id = "policy_violation_${System.currentTimeMillis()}", 
        timestamp = System.currentTimeMillis(), 
        isAcknowledged = acknowledged,
        type = SecurityEventType.UNAUTHORIZED_ACCESS_ATTEMPT,
        severity = when(securityLevel) {
            SecurityLevel.LOW -> SecuritySeverity.LOW
            SecurityLevel.MEDIUM -> SecuritySeverity.MEDIUM
            SecurityLevel.HIGH -> SecuritySeverity.HIGH
            SecurityLevel.CRITICAL -> SecuritySeverity.CRITICAL
            SecurityLevel.DANGER -> SecuritySeverity.HIGH
            SecurityLevel.WARNING -> SecuritySeverity.MEDIUM
            SecurityLevel.SECURE -> SecuritySeverity.LOW
        },
        title = "Policy Violation",
        message = "Security policy violation: $policyType",
        actionRequired = true,
        recommendedActions = listOf("Review security policies", "Take corrective action"),
        displayName = "Security System",
        activityType = "POLICY_VIOLATION",
        details = description
    ) {
        fun copy(isAcknowledged: Boolean = this.acknowledged): PolicyViolation {
            return PolicyViolation(policyType, description, securityLevel, isAcknowledged)
        }
    }
}

/**
 * Security recommendations for users
 */
sealed class SecurityRecommendation(
    val id: String,
    val type: SecurityRecommendationType,
    val priority: SecurityLevel,
    val title: String,
    val description: String,
    val actionRequired: Boolean = false
) {
    data class VerifyContacts(val count: Int) : SecurityRecommendation(
        id = "verify_contacts",
        type = SecurityRecommendationType.VERIFY_CONTACTS,
        priority = SecurityLevel.HIGH,
        title = "Verify Contact Identity",
        description = "Verify the identity of $count contacts using safety numbers",
        actionRequired = true
    )
    
    data class ReviewSecurityAlerts(val count: Int) : SecurityRecommendation(
        id = "review_alerts",
        type = SecurityRecommendationType.REVIEW_SECURITY_ALERTS,
        priority = SecurityLevel.HIGH,
        title = "Review Security Alerts",
        description = "Review $count pending security alerts",
        actionRequired = true
    )
    
    data class ReviewKeyChanges(val count: Int) : SecurityRecommendation(
        id = "review_key_changes",
        type = SecurityRecommendationType.VERIFY_CONTACTS,
        priority = SecurityLevel.MEDIUM,
        title = "Review Key Changes",
        description = "Review $count recent key changes",
        actionRequired = false
    )
    
    data class VerifyIdentities(val count: Int) : SecurityRecommendation(
        id = "verify_identities",
        type = SecurityRecommendationType.VERIFY_CONTACTS,
        priority = SecurityLevel.HIGH,
        title = "Verify Identities",
        description = "Verify $count unverified contact identities",
        actionRequired = true
    )
    
    data class ReviewSuspiciousActivity(val count: Int) : SecurityRecommendation(
        id = "review_suspicious",
        type = SecurityRecommendationType.REVIEW_SECURITY_ALERTS,
        priority = SecurityLevel.MEDIUM,
        title = "Review Suspicious Activity",
        description = "Review $count suspicious activity reports",
        actionRequired = false
    )
    
    object UpdateKeys : SecurityRecommendation(
        id = "update_keys",
        type = SecurityRecommendationType.UPDATE_KEYS,
        priority = SecurityLevel.MEDIUM,
        title = "Update Encryption Keys",
        description = "Update your encryption keys for enhanced security",
        actionRequired = false
    )
    
    object EnableTwoFactor : SecurityRecommendation(
        id = "enable_2fa",
        type = SecurityRecommendationType.ENABLE_TWO_FACTOR,
        priority = SecurityLevel.HIGH,
        title = "Enable Two-Factor Authentication",
        description = "Enable two-factor authentication for additional security",
        actionRequired = true
    )
    
    object IncreaseSecurityMeasures : SecurityRecommendation(
        id = "increase_security",
        type = SecurityRecommendationType.REVIEW_SECURITY_ALERTS,
        priority = SecurityLevel.MEDIUM,
        title = "Increase Security Measures",
        description = "Consider implementing additional security measures",
        actionRequired = false
    )
}

/**
 * Security levels
 */
enum class SecurityLevel {
    CRITICAL, DANGER, WARNING, SECURE, HIGH, MEDIUM, LOW
}

/**
 * Security monitoring status states
 */
enum class MonitoringStatus {
    ACTIVE,
    INACTIVE,
    SCANNING,
    ERROR,
    INITIALIZING
}

/**
 * Security severity levels
 */
enum class SecuritySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Security event types
 */
enum class SecurityEventType {
    FAILED_LOGIN_ATTEMPT,
    SUSPICIOUS_KEY_CHANGE,
    UNUSUAL_DEVICE_ACCESS,
    POTENTIAL_MITM_ATTACK,
    ENCRYPTION_FAILURE,
    BLOCKCHAIN_TAMPERING,
    EXCESSIVE_MESSAGE_REQUESTS,
    UNAUTHORIZED_ACCESS_ATTEMPT,
    SUSPICIOUS_NETWORK_ACTIVITY,
    KEY_VERIFICATION_FAILURE
}

/**
 * Security alert types
 */
enum class SecurityAlertType {
    IDENTITY_KEY_CHANGED,
    KEY_MISMATCH,
    SUSPICIOUS_ACTIVITY,
    POLICY_VIOLATION
}

/**
 * Security recommendation types
 */
enum class SecurityRecommendationType {
    VERIFY_CONTACTS,
    REVIEW_SECURITY_ALERTS,
    UPDATE_KEYS,
    ENABLE_TWO_FACTOR
}

/**
 * Categories of security recommendations
 */
enum class RecommendationCategory {
    KEY_MANAGEMENT,
    DEVICE_SECURITY,
    NETWORK_SECURITY,
    PRIVACY_SETTINGS,
    AUTHENTICATION,
    BACKUP_RECOVERY
}

/**
 * Threat levels
 */
enum class ThreatLevel {
    LOW, MEDIUM, HIGH
}

/**
 * Alert types
 */
enum class AlertType {
    KEY_CHANGE, KEY_MISMATCH, SUSPICIOUS_ACTIVITY, POLICY_VIOLATION
}

/**
 * Security analysis result
 */
data class SecurityAnalysis(
    val totalAlerts: Int,
    val recentAlerts: Int,
    val threatLevel: ThreatLevel,
    val recommendations: List<SecurityRecommendation>
)

/**
 * Security score with factors
 */
data class SecurityScore(
    val score: Int,
    val level: SecurityLevel,
    val factors: List<SecurityScoreFactor>
)

/**
 * Factors affecting security score
 */
sealed class SecurityScoreFactor {
    data class ActiveAlerts(val count: Int) : SecurityScoreFactor()
    data class VerificationRatio(val ratio: Float) : SecurityScoreFactor()
    object RecentActivity : SecurityScoreFactor()
}

/**
 * Security status information
 */
data class SecurityStatus(
    val overallLevel: SecurityLevel,
    val activeThreats: Int,
    val verifiedContacts: Int,
    val totalContacts: Int,
    val lastSecurityCheck: Long,
    val isMonitoringActive: Boolean,
    val level: SecurityLevel = overallLevel,
    val lastScanTime: LocalDateTime = LocalDateTime.now(),
    val recommendations: Int = 0
)

/**
 * Security event data
 */
data class SecurityEvent(
    val id: String,
    val type: SecurityEventType,
    val timestamp: LocalDateTime,
    val severity: SecuritySeverity,
    val description: String,
    val metadata: Map<String, Any> = emptyMap(),
    val userId: String? = null,
    val deviceId: String? = null,
    val ipAddress: String? = null
)

/**
 * Security metrics for monitoring
 */
data class SecurityMetrics(
    val totalAlerts: Int,
    val alertsLast24h: Int,
    val alertsLast7d: Int,
    val verificationRate: Float,
    val threatLevel: ThreatLevel,
    val securityScore: Int,
    val totalEvents: Int = totalAlerts,
    val eventsLast24Hours: Int = alertsLast24h,
    val criticalAlertsActive: Int = 0,
    val averageResponseTime: Long = 0L,
    val lastBreachAttempt: LocalDateTime? = null,
    val eventsByType: Map<SecurityEventType, Int> = emptyMap()
)

/**
 * Exception for security-related operations
 */
class SecurityException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Verification state for user identity verification
 */
data class VerificationState(
    val userId: String,
    val isVerified: Boolean,
    val verificationMethod: VerificationMethod,
    val verificationTimestamp: Long,
    val safetyNumber: String? = null,
    val qrCodeData: String? = null
)

/**
 * Methods used for verification
 */
enum class VerificationMethod {
    QR_CODE,
    SAFETY_NUMBER,
    MANUAL,
    NONE
}

/**
 * Result of identity verification operations
 */
sealed class VerificationResult {
    data class Success(
        val userId: String,
        val displayName: String,
        val publicKey: String,
        val timestamp: Long
    ) : VerificationResult()
    
    data class KeyMismatch(
        val userId: String,
        val displayName: String,
        val expectedKey: String,
        val receivedKey: String
    ) : VerificationResult()
    
    data class InvalidData(
        val reason: String
    ) : VerificationResult()
}

/**
 * QR code scan result
 */
sealed class ScanResult {
    data class Success(
        val userId: String,
        val displayName: String,
        val publicKey: String,
        val timestamp: Long
    ) : ScanResult()
    
    data class KeyMismatch(
        val userId: String,
        val displayName: String,
        val expectedKey: String,
        val receivedKey: String
    ) : ScanResult()
    
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ScanResult()
}

/**
 * Network quality levels for calls
 */
enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    POOR,
    BAD
}

/**
 * Exception for security-related operations
 */
class SecurityException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception for cryptographic operations
 */
class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)