package com.chain.messaging.core.security

import com.chain.messaging.data.local.dao.SecurityEventDao
import com.chain.messaging.data.local.entity.SecurityEventEntity
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityEventStorageImpl @Inject constructor(
    private val securityEventDao: SecurityEventDao
) : SecurityEventStorage {
    
    override suspend fun storeEvent(event: SecurityEvent) {
        val entity = SecurityEventEntity(
            id = event.id,
            type = event.type.name,
            timestamp = event.timestamp,
            severity = event.severity.name,
            description = event.description,
            metadata = event.metadata.toString(),
            userId = event.userId,
            deviceId = event.deviceId,
            ipAddress = event.ipAddress,
            isAcknowledged = false
        )
        securityEventDao.insertEvent(entity)
    }
    
    override suspend fun getAllEvents(): List<SecurityEvent> {
        return securityEventDao.getAllEvents().map { it.toDomainModel() }
    }
    
    override suspend fun getEventsInTimeRange(start: LocalDateTime, end: LocalDateTime): List<SecurityEvent> {
        return securityEventDao.getEventsInTimeRange(start, end).map { it.toDomainModel() }
    }
    
    override suspend fun getEventsByType(type: SecurityEventType): List<SecurityEvent> {
        return securityEventDao.getEventsByType(type.name).map { it.toDomainModel() }
    }
    
    override suspend fun getEventsBySeverity(severity: SecuritySeverity): List<SecurityEvent> {
        return securityEventDao.getEventsBySeverity(severity.name).map { it.toDomainModel() }
    }
    
    override suspend fun acknowledgeAlert(alertId: String) {
        securityEventDao.acknowledgeEvent(alertId)
    }
    
    override suspend fun deleteEventsOlderThan(cutoffDate: LocalDateTime) {
        securityEventDao.deleteEventsOlderThan(cutoffDate)
    }
    
    override suspend fun getEventCountByType(): Map<SecurityEventType, Int> {
        return securityEventDao.getEventCountByType()
            .mapKeys { SecurityEventType.valueOf(it.key) }
    }
    
    private fun SecurityEventEntity.toDomainModel(): SecurityEvent {
        return SecurityEvent(
            id = id,
            type = SecurityEventType.valueOf(type),
            timestamp = timestamp,
            severity = SecuritySeverity.valueOf(severity),
            description = description,
            metadata = parseMetadata(metadata),
            userId = userId,
            deviceId = deviceId,
            ipAddress = ipAddress
        )
    }
    
    private fun parseMetadata(metadataString: String): Map<String, Any> {
        // Simple parsing - in production, use proper JSON serialization
        return try {
            if (metadataString.isBlank()) emptyMap()
            else mapOf("raw" to metadataString)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}