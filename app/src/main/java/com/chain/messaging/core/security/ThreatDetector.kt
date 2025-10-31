package com.chain.messaging.core.security

import com.chain.messaging.domain.model.SecuritySeverity
import com.chain.messaging.domain.model.ThreatIndicator
import com.chain.messaging.domain.model.ThreatType
import kotlinx.coroutines.flow.Flow

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