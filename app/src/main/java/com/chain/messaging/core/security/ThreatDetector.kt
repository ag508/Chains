package com.chain.messaging.core.security

import com.chain.messaging.domain.model.SecuritySeverity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Interface for threat detection and security monitoring
 */
interface ThreatDetector {
    
    /**
     * Starts threat detection
     */
    suspend fun startDetection()
    
    /**
     * Stops threat detection
     */
    suspend fun stopDetection()
    
    /**
     * Initializes the threat detector
     */
    suspend fun initialize() {
        // Default implementation - can be overridden
    }
    
    /**
     * Analyzes network traffic for anomalies
     */
    suspend fun analyzeNetworkTraffic(): List<ThreatIndicator>
    
    /**
     * Detects potential MITM attacks
     */
    suspend fun detectMITMAttacks(): List<ThreatIndicator>
    
    /**
     * Monitors authentication patterns for suspicious activity
     */
    suspend fun monitorAuthenticationPatterns(): List<ThreatIndicator>
    
    /**
     * Detects blockchain tampering attempts
     */
    suspend fun detectBlockchainTampering(): List<ThreatIndicator>
    
    /**
     * Checks device security status
     */
    suspend fun checkDeviceSecurity(): List<ThreatIndicator>
    
    /**
     * Gets flow of threat indicators
     */
    fun getThreatIndicators(): Flow<ThreatIndicator>
}

/**
 * Represents a threat indicator detected by the system
 */
data class ThreatIndicator(
    val id: String,
    val type: ThreatType,
    val severity: SecuritySeverity,
    val description: String,
    val confidence: Float, // 0.0 to 1.0
    val evidence: Map<String, Any>,
    val timestamp: LocalDateTime,
    val source: String
)

/**
 * Types of threats that can be detected
 */
enum class ThreatType {
    NETWORK_ANOMALY,
    ENCRYPTION_COMPROMISE,
    AUTHENTICATION_ANOMALY,
    BLOCKCHAIN_ATTACK,
    DEVICE_COMPROMISE,
    MALWARE_ACTIVITY,
    DATA_BREACH,
    SOCIAL_ENGINEERING
}