package com.chain.messaging.core.security

import android.content.Context
import android.util.Log
import com.chain.messaging.domain.model.SecurityAlert
import com.chain.messaging.domain.model.SecurityEvent
import com.chain.messaging.domain.model.SecurityEventType
import com.chain.messaging.domain.model.SecurityLevel
import com.chain.messaging.domain.model.SecurityMetrics
import com.chain.messaging.domain.model.SecurityRecommendation
import com.chain.messaging.domain.model.SecuritySeverity
import com.chain.messaging.domain.model.SecurityStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityMonitoringManagerImpl @Inject constructor(
    private val context: Context,
    private val securityEventStorage: SecurityEventStorage,
    private val threatDetector: ThreatDetector,
    private val alertNotificationService: AlertNotificationService
) : SecurityMonitoringManager {
    
    companion object {
        private const val TAG = "SecurityMonitoring"
        private const val MONITORING_INTERVAL_MS = 30_000L // 30 seconds
        private const val EVENT_RETENTION_DAYS = 30L
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val SUSPICIOUS_ACTIVITY_THRESHOLD = 10
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    
    private val _securityStatus = MutableStateFlow(
        SecurityStatus(
            overallLevel = SecurityLevel.SECURE,
            activeThreats = 0,
            verifiedContacts = 0,
            totalContacts = 0,
            lastSecurityCheck = System.currentTimeMillis(),
            isMonitoringActive = false,
            level = SecurityLevel.SECURE,
            lastScanTime = LocalDateTime.now(),
            recommendations = 0
        )
    )
    
    private val _securityAlerts = MutableSharedFlow<SecurityAlert>()
    private val activeAlerts = ConcurrentHashMap<String, SecurityAlert>()
    private val eventCounters = ConcurrentHashMap<SecurityEventType, Int>()
    
    override suspend fun startMonitoring() {
        Log.d(TAG, "Starting security monitoring")
        
        monitoringJob?.cancel()
        monitoringJob = scope.launch {
            while (isActive) {
                try {
                    performSecurityScan()
                    delay(MONITORING_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error during security monitoring", e)
                    delay(MONITORING_INTERVAL_MS * 2) // Back off on error
                }
            }
        }
        
        // Start threat detection
        threatDetector.startDetection()
        
        // Clean up old events
        cleanupOldEvents()
    }
    
    override suspend fun stopMonitoring() {
        Log.d(TAG, "Stopping security monitoring")
        monitoringJob?.cancel()
        threatDetector.stopDetection()
    }
    
    override suspend fun reportSecurityEvent(event: SecurityEvent) {
        Log.d(TAG, "Security event reported: ${event.type} - ${event.severity}")
        
        // Store the event
        securityEventStorage.storeEvent(event)
        
        // Update counters
        eventCounters[event.type] = (eventCounters[event.type] ?: 0) + 1
        
        // Analyze threat level
        val threatLevel = analyzeThreatLevel(event)
        
        // Generate alert if necessary
        if (shouldGenerateAlert(event, threatLevel)) {
            val alert = createSecurityAlert(event, threatLevel)
            activeAlerts[alert.id] = alert
            _securityAlerts.emit(alert)
            
            // Send notification
            alertNotificationService.sendAlert(alert)
        }
        
        // Update security status
        updateSecurityStatus()
    }
    
    override fun getSecurityStatusFlow(): StateFlow<SecurityStatus> = _securityStatus.asStateFlow()
    
    override suspend fun initialize() {
        // Initialize security monitoring components
        threatDetector.initialize()
        alertNotificationService.initialize()
        
        // Start monitoring
        startMonitoring()
    }
    
    override fun getSecurityStatus(): String {
        val status = _securityStatus.value
        return when (status.level) {
            SecurityLevel.SECURE -> "Secure - No active threats detected"
            SecurityLevel.LOW -> "Low - Minimal security concerns"
            SecurityLevel.MEDIUM -> "Medium - Some security issues detected"
            SecurityLevel.WARNING -> "Warning - ${status.activeThreats} potential threats detected"
            SecurityLevel.HIGH -> "High - Elevated security risk detected"
            SecurityLevel.DANGER -> "Danger - ${status.activeThreats} active threats require attention"
            SecurityLevel.CRITICAL -> "Critical - Immediate action required for ${status.activeThreats} threats"
        }
    }
    
    override fun getSecurityAlerts(): Flow<SecurityAlert> = _securityAlerts.asSharedFlow()
    
    override suspend fun getSecurityRecommendations(): List<SecurityRecommendation> {
        val recommendations = mutableListOf<SecurityRecommendation>()
        
        // Analyze recent events for recommendations
        val recentEvents = securityEventStorage.getEventsInTimeRange(
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now()
        )
        
        // Key management recommendations
        if (hasKeyRelatedIssues(recentEvents)) {
            recommendations.add(SecurityRecommendation.UpdateKeys)
        }
        
        // Authentication recommendations
        if (hasAuthenticationIssues(recentEvents)) {
            recommendations.add(SecurityRecommendation.EnableTwoFactor)
        }
        
        // Network security recommendations
        if (hasNetworkSecurityIssues(recentEvents)) {
            recommendations.add(SecurityRecommendation.IncreaseSecurityMeasures)
        }
        
        return recommendations
    }
    
    override suspend fun acknowledgeAlert(alertId: String) {
        activeAlerts[alertId]?.let { alert ->
            val acknowledgedAlert = alert.copy(isAcknowledged = true)
            activeAlerts[alertId] = acknowledgedAlert
            securityEventStorage.acknowledgeAlert(alertId)
        }
        updateSecurityStatus()
    }
    
    override suspend fun getSecurityMetrics(): SecurityMetrics {
        val now = LocalDateTime.now()
        val last24Hours = now.minusDays(1)
        val last7Days = now.minusDays(7)

        val allEvents = securityEventStorage.getAllEvents()
        val events24h = allEvents.filter { it.timestamp.isAfter(last24Hours) }
        val events7d = allEvents.filter { it.timestamp.isAfter(last7Days) }

        val lastBreachAttempt = allEvents
            .filter { it.severity == SecuritySeverity.CRITICAL }
            .maxByOrNull { it.timestamp }
            ?.timestamp

        val securityScore = calculateSecurityScore(allEvents)

        // Calculate verification rate (percentage of verified contacts)
        val verifiedCount = _securityStatus.value.verifiedContacts
        val totalCount = _securityStatus.value.totalContacts
        val verificationRate = if (totalCount > 0) {
            verifiedCount.toFloat() / totalCount.toFloat()
        } else {
            1.0f
        }

        // Determine threat level based on critical alerts and recent events
        val criticalCount = activeAlerts.values.count {
            it.severity == SecuritySeverity.CRITICAL && !it.isAcknowledged
        }
        val threatLevel = when {
            criticalCount > 0 || events24h.size > 20 -> com.chain.messaging.domain.model.ThreatLevel.HIGH
            events24h.size > 10 -> com.chain.messaging.domain.model.ThreatLevel.MEDIUM
            else -> com.chain.messaging.domain.model.ThreatLevel.LOW
        }

        return SecurityMetrics(
            totalAlerts = allEvents.size,
            alertsLast24h = events24h.size,
            alertsLast7d = events7d.size,
            verificationRate = verificationRate,
            threatLevel = threatLevel,
            securityScore = securityScore,
            totalEvents = allEvents.size,
            eventsLast24Hours = events24h.size,
            criticalAlertsActive = criticalCount,
            averageResponseTime = calculateAverageResponseTime(),
            lastBreachAttempt = lastBreachAttempt,
            eventsByType = eventCounters.toMap()
        )
    }
    
    private suspend fun performSecurityScan() {
        val now = LocalDateTime.now()
        
        // Check for suspicious patterns
        val recentEvents = securityEventStorage.getEventsInTimeRange(
            now.minusHours(1),
            now
        )
        
        // Detect potential attacks
        detectBruteForceAttacks(recentEvents)
        detectUnusualActivity(recentEvents)
        detectKeyCompromise(recentEvents)
        
        // Update scan time
        _securityStatus.value = _securityStatus.value.copy(lastScanTime = now)
    }
    
    private suspend fun detectBruteForceAttacks(events: List<SecurityEvent>) {
        val failedAttempts = events.count { it.type == SecurityEventType.FAILED_LOGIN_ATTEMPT }
        
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            val event = SecurityEvent(
                id = UUID.randomUUID().toString(),
                type = SecurityEventType.UNAUTHORIZED_ACCESS_ATTEMPT,
                timestamp = LocalDateTime.now(),
                severity = SecuritySeverity.HIGH,
                description = "Multiple failed login attempts detected ($failedAttempts attempts)",
                metadata = mapOf("failed_attempts" to failedAttempts)
            )
            reportSecurityEvent(event)
        }
    }
    
    private suspend fun detectUnusualActivity(events: List<SecurityEvent>) {
        if (events.size > SUSPICIOUS_ACTIVITY_THRESHOLD) {
            val event = SecurityEvent(
                id = UUID.randomUUID().toString(),
                type = SecurityEventType.SUSPICIOUS_NETWORK_ACTIVITY,
                timestamp = LocalDateTime.now(),
                severity = SecuritySeverity.MEDIUM,
                description = "Unusual activity pattern detected (${events.size} events in last hour)",
                metadata = mapOf("event_count" to events.size)
            )
            reportSecurityEvent(event)
        }
    }
    
    private suspend fun detectKeyCompromise(events: List<SecurityEvent>) {
        val keyEvents = events.filter { 
            it.type == SecurityEventType.SUSPICIOUS_KEY_CHANGE || 
            it.type == SecurityEventType.KEY_VERIFICATION_FAILURE 
        }
        
        if (keyEvents.size >= 3) {
            val event = SecurityEvent(
                id = UUID.randomUUID().toString(),
                type = SecurityEventType.POTENTIAL_MITM_ATTACK,
                timestamp = LocalDateTime.now(),
                severity = SecuritySeverity.CRITICAL,
                description = "Potential key compromise detected",
                metadata = mapOf("key_events" to keyEvents.size)
            )
            reportSecurityEvent(event)
        }
    }
    
    private fun analyzeThreatLevel(event: SecurityEvent): SecuritySeverity {
        return when (event.type) {
            SecurityEventType.POTENTIAL_MITM_ATTACK,
            SecurityEventType.BLOCKCHAIN_TAMPERING -> SecuritySeverity.CRITICAL
            
            SecurityEventType.SUSPICIOUS_KEY_CHANGE,
            SecurityEventType.UNAUTHORIZED_ACCESS_ATTEMPT,
            SecurityEventType.ENCRYPTION_FAILURE -> SecuritySeverity.HIGH
            
            SecurityEventType.UNUSUAL_DEVICE_ACCESS,
            SecurityEventType.SUSPICIOUS_NETWORK_ACTIVITY,
            SecurityEventType.EXCESSIVE_MESSAGE_REQUESTS -> SecuritySeverity.MEDIUM
            
            else -> SecuritySeverity.LOW
        }
    }
    
    private fun shouldGenerateAlert(event: SecurityEvent, threatLevel: SecuritySeverity): Boolean {
        return threatLevel >= SecuritySeverity.MEDIUM || 
               event.type in listOf(
                   SecurityEventType.POTENTIAL_MITM_ATTACK,
                   SecurityEventType.BLOCKCHAIN_TAMPERING,
                   SecurityEventType.ENCRYPTION_FAILURE
               )
    }
    
    private fun createSecurityAlert(event: SecurityEvent, threatLevel: SecuritySeverity): SecurityAlert {
        return when (event.type) {
            SecurityEventType.SUSPICIOUS_KEY_CHANGE -> {
                val userId = event.userId ?: "unknown"
                val displayName = event.metadata["displayName"]?.toString() ?: "Unknown User"
                // For this case, we'll create a generic suspicious activity alert since we don't have the actual keys
                SecurityAlert.SuspiciousActivity(
                    userId = userId,
                    activityType = "KEY_CHANGE",
                    details = "Unexpected changes to encryption keys detected. This could indicate a security issue.",
                    acknowledged = false
                )
            }
            
            SecurityEventType.UNAUTHORIZED_ACCESS_ATTEMPT -> {
                val userId = event.userId ?: "system"
                SecurityAlert.SuspiciousActivity(
                    userId = userId,
                    activityType = "UNAUTHORIZED_ACCESS",
                    details = "Multiple failed login attempts detected. Someone may be trying to access your account.",
                    acknowledged = false
                )
            }
            
            SecurityEventType.ENCRYPTION_FAILURE -> {
                val userId = event.userId ?: "system"
                SecurityAlert.SuspiciousActivity(
                    userId = userId,
                    activityType = "ENCRYPTION_FAILURE",
                    details = "Message encryption/decryption failed. This could indicate tampering or key issues.",
                    acknowledged = false
                )
            }
            
            SecurityEventType.POTENTIAL_MITM_ATTACK -> {
                val userId = event.userId ?: "system"
                SecurityAlert.SuspiciousActivity(
                    userId = userId,
                    activityType = "MITM_ATTACK",
                    details = "A potential man-in-the-middle attack has been detected. Your communications may be compromised.",
                    acknowledged = false
                )
            }
            
            else -> {
                val userId = event.userId ?: "system"
                SecurityAlert.SuspiciousActivity(
                    userId = userId,
                    activityType = event.type.name,
                    details = event.description,
                    acknowledged = false
                )
            }
        }
    }
    
    private suspend fun updateSecurityStatus() {
        val activeThreats = activeAlerts.values.count { !it.isAcknowledged }
        val criticalThreats = activeAlerts.values.count { 
            it.severity == SecuritySeverity.CRITICAL && !it.isAcknowledged 
        }
        val highThreats = activeAlerts.values.count { 
            it.severity == SecuritySeverity.HIGH && !it.isAcknowledged 
        }
        
        val level = when {
            criticalThreats > 0 -> SecurityLevel.CRITICAL
            highThreats > 0 -> SecurityLevel.DANGER
            activeThreats > 0 -> SecurityLevel.WARNING
            else -> SecurityLevel.SECURE
        }
        
        val recommendations = getSecurityRecommendations().size
        
        _securityStatus.value = SecurityStatus(
            overallLevel = level,
            activeThreats = activeThreats,
            verifiedContacts = _securityStatus.value.verifiedContacts,
            totalContacts = _securityStatus.value.totalContacts,
            lastSecurityCheck = System.currentTimeMillis(),
            isMonitoringActive = true,
            level = level,
            lastScanTime = LocalDateTime.now(),
            recommendations = recommendations
        )
    }
    
    private suspend fun cleanupOldEvents() {
        val cutoffDate = LocalDateTime.now().minusDays(EVENT_RETENTION_DAYS)
        securityEventStorage.deleteEventsOlderThan(cutoffDate)
    }
    
    private fun hasKeyRelatedIssues(events: List<SecurityEvent>): Boolean {
        return events.any { 
            it.type in listOf(
                SecurityEventType.SUSPICIOUS_KEY_CHANGE,
                SecurityEventType.KEY_VERIFICATION_FAILURE,
                SecurityEventType.ENCRYPTION_FAILURE
            )
        }
    }
    
    private fun hasAuthenticationIssues(events: List<SecurityEvent>): Boolean {
        return events.count { it.type == SecurityEventType.FAILED_LOGIN_ATTEMPT } >= 3
    }
    
    private fun hasNetworkSecurityIssues(events: List<SecurityEvent>): Boolean {
        return events.any { 
            it.type in listOf(
                SecurityEventType.SUSPICIOUS_NETWORK_ACTIVITY,
                SecurityEventType.POTENTIAL_MITM_ATTACK
            )
        }
    }
    
    private fun calculateSecurityScore(events: List<SecurityEvent>): Int {
        val recentEvents = events.filter { 
            it.timestamp.isAfter(LocalDateTime.now().minusDays(7)) 
        }
        
        var score = 100
        
        // Deduct points based on recent security events
        recentEvents.forEach { event ->
            score -= when (event.severity) {
                SecuritySeverity.CRITICAL -> 20
                SecuritySeverity.HIGH -> 10
                SecuritySeverity.MEDIUM -> 5
                SecuritySeverity.LOW -> 1
            }
        }
        
        return maxOf(0, score)
    }
    
    private fun calculateAverageResponseTime(): Long {
        // Placeholder - would calculate based on actual response times
        return 300L // 5 minutes average
    }
}