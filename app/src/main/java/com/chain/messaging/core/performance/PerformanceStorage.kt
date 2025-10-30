package com.chain.messaging.core.performance

/**
 * Interface for storing performance metrics and alerts
 */
interface PerformanceStorage {
    
    /**
     * Store performance metrics
     */
    suspend fun storeMetrics(metrics: PerformanceMetrics)
    
    /**
     * Store performance alert
     */
    suspend fun storeAlert(alert: PerformanceAlert)
    
    /**
     * Store memory usage metric
     */
    suspend fun storeMemoryMetric(usedMemoryMb: Long, totalMemoryMb: Long)
    
    /**
     * Store battery usage metric
     */
    suspend fun storeBatteryMetric(batteryLevel: Float, isCharging: Boolean)
    
    /**
     * Store network performance metric
     */
    suspend fun storeNetworkMetric(latencyMs: Long, throughputKbps: Long)
    
    /**
     * Get historical metrics
     */
    suspend fun getMetrics(fromTimestamp: Long, toTimestamp: Long): List<PerformanceMetrics>
    
    /**
     * Get historical alerts
     */
    suspend fun getAlerts(fromTimestamp: Long, toTimestamp: Long): List<PerformanceAlert>
    
    /**
     * Clear old data
     */
    suspend fun clearOldData(olderThanMs: Long)
}