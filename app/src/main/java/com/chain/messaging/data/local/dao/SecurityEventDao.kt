package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.SecurityEventEntity
import java.time.LocalDateTime

data class EventTypeCount(
    val type: String,
    val count: Int
)

@Dao
interface SecurityEventDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SecurityEventEntity)
    
    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    suspend fun getAllEvents(): List<SecurityEventEntity>
    
    @Query("SELECT * FROM security_events WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getEventsInTimeRange(start: LocalDateTime, end: LocalDateTime): List<SecurityEventEntity>
    
    @Query("SELECT * FROM security_events WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getEventsByType(type: String): List<SecurityEventEntity>
    
    @Query("SELECT * FROM security_events WHERE severity = :severity ORDER BY timestamp DESC")
    suspend fun getEventsBySeverity(severity: String): List<SecurityEventEntity>
    
    @Query("UPDATE security_events SET isAcknowledged = 1 WHERE id = :eventId")
    suspend fun acknowledgeEvent(eventId: String)
    
    @Query("DELETE FROM security_events WHERE timestamp < :cutoffDate")
    suspend fun deleteEventsOlderThan(cutoffDate: LocalDateTime)
    
    @Query("SELECT type, COUNT(*) as count FROM security_events GROUP BY type")
    suspend fun getEventCountByType(): List<EventTypeCount>
    
    @Query("SELECT * FROM security_events WHERE severity IN ('HIGH', 'CRITICAL') AND isAcknowledged = 0 ORDER BY timestamp DESC")
    suspend fun getActiveHighSeverityEvents(): List<SecurityEventEntity>
    
    @Query("SELECT COUNT(*) FROM security_events WHERE timestamp > :since")
    suspend fun getEventCountSince(since: LocalDateTime): Int
    
    @Query("DELETE FROM security_events")
    suspend fun deleteAllEvents()
}