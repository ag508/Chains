package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.PerformanceAlertEntity
import com.chain.messaging.data.local.entity.PerformanceMetricsEntity

/**
 * DAO for performance metrics and alerts
 */
@Dao
interface PerformanceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetrics(metrics: PerformanceMetricsEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PerformanceAlertEntity)
    
    @Query("SELECT * FROM performance_metrics WHERE timestamp BETWEEN :fromTimestamp AND :toTimestamp ORDER BY timestamp DESC")
    suspend fun getMetrics(fromTimestamp: Long, toTimestamp: Long): List<PerformanceMetricsEntity>
    
    @Query("SELECT * FROM performance_alerts WHERE timestamp BETWEEN :fromTimestamp AND :toTimestamp ORDER BY timestamp DESC")
    suspend fun getAlerts(fromTimestamp: Long, toTimestamp: Long): List<PerformanceAlertEntity>
    
    @Query("SELECT * FROM performance_metrics ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMetrics(limit: Int): List<PerformanceMetricsEntity>
    
    @Query("SELECT * FROM performance_alerts ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentAlerts(limit: Int): List<PerformanceAlertEntity>
    
    @Query("DELETE FROM performance_metrics WHERE timestamp < :cutoffTime")
    suspend fun deleteOldMetrics(cutoffTime: Long)
    
    @Query("DELETE FROM performance_alerts WHERE timestamp < :cutoffTime")
    suspend fun deleteOldAlerts(cutoffTime: Long)
    
    @Query("SELECT COUNT(*) FROM performance_metrics")
    suspend fun getMetricsCount(): Int
    
    @Query("SELECT COUNT(*) FROM performance_alerts")
    suspend fun getAlertsCount(): Int
    
    @Query("SELECT AVG(memoryUsagePercentage) FROM performance_metrics WHERE timestamp > :since")
    suspend fun getAverageMemoryUsage(since: Long): Float?
    
    @Query("SELECT AVG(cpuUsagePercentage) FROM performance_metrics WHERE timestamp > :since")
    suspend fun getAverageCpuUsage(since: Long): Float?
    
    @Query("SELECT AVG(messagesPerSecond) FROM performance_metrics WHERE timestamp > :since")
    suspend fun getAverageMessageThroughput(since: Long): Double?
}