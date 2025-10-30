package com.chain.messaging.core.security

import com.chain.messaging.domain.model.SecurityEvent
import com.chain.messaging.domain.model.SecurityEventType
import com.chain.messaging.domain.model.SecuritySeverity
import java.time.LocalDateTime

/**
 * Interface for storing and retrieving security events
 */
interface SecurityEventStorage {
    
    /**
     * Stores a security event
     */
    suspend fun storeEvent(event: SecurityEvent)
    
    /**
     * Gets all security events
     */
    suspend fun getAllEvents(): List<SecurityEvent>
    
    /**
     * Gets events within a time range
     */
    suspend fun getEventsInTimeRange(start: LocalDateTime, end: LocalDateTime): List<SecurityEvent>
    
    /**
     * Gets events by type
     */
    suspend fun getEventsByType(type: SecurityEventType): List<SecurityEvent>
    
    /**
     * Gets events by severity
     */
    suspend fun getEventsBySeverity(severity: SecuritySeverity): List<SecurityEvent>
    
    /**
     * Acknowledges an alert
     */
    suspend fun acknowledgeAlert(alertId: String)
    
    /**
     * Deletes events older than the cutoff date
     */
    suspend fun deleteEventsOlderThan(cutoffDate: LocalDateTime)
    
    /**
     * Gets event count by type
     */
    suspend fun getEventCountByType(): Map<SecurityEventType, Int>
}