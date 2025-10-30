package com.chain.messaging.core.security

import com.chain.messaging.domain.model.SecurityAlert
import com.chain.messaging.domain.model.SecurityRecommendation
import com.chain.messaging.domain.model.SecurityStatus
import com.chain.messaging.domain.model.SecurityMetrics
import com.chain.messaging.domain.model.SecurityLevel
import com.chain.messaging.domain.model.SecurityEvent
import com.chain.messaging.domain.model.SecurityEventType
import com.chain.messaging.domain.model.SecuritySeverity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

/**
 * Manages security monitoring and threat detection for the Chain messaging platform.
 * Monitors various security events and provides alerts for suspicious activities.
 */
interface SecurityMonitoringManager {
    
    /**
     * Starts security monitoring services
     */
    suspend fun startMonitoring()
    
    /**
     * Stops security monitoring services
     */
    suspend fun stopMonitoring()
    
    /**
     * Reports a security event for analysis
     */
    suspend fun reportSecurityEvent(event: SecurityEvent)
    
    /**
     * Gets current security status
     */
    fun getSecurityStatusFlow(): StateFlow<SecurityStatus>
    
    /**
     * Gets stream of security alerts
     */
    fun getSecurityAlerts(): Flow<SecurityAlert>
    
    /**
     * Gets security recommendations based on current threats
     */
    suspend fun getSecurityRecommendations(): List<SecurityRecommendation>
    
    /**
     * Acknowledges a security alert
     */
    suspend fun acknowledgeAlert(alertId: String)
    
    /**
     * Gets security metrics and statistics
     */
    suspend fun getSecurityMetrics(): SecurityMetrics
    
    /**
     * Initialize security monitoring
     */
    suspend fun initialize()
    
    /**
     * Get security status as string
     */
    fun getSecurityStatus(): String
}



