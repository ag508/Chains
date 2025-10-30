package com.chain.messaging.core.performance

import com.chain.messaging.data.local.dao.PerformanceDao
import com.chain.messaging.data.local.entity.PerformanceAlertEntity
import com.chain.messaging.data.local.entity.PerformanceMetricsEntity
import com.chain.messaging.core.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PerformanceStorage using Room database
 */
@Singleton
class PerformanceStorageImpl @Inject constructor(
    private val performanceDao: PerformanceDao
) : PerformanceStorage {
    
    override suspend fun storeMetrics(metrics: PerformanceMetrics) {
        try {
            val entity = PerformanceMetricsEntity(
                timestamp = metrics.timestamp,
                messagesPerSecond = metrics.messageThroughput.messagesPerSecond,
                averageLatencyMs = metrics.messageThroughput.averageLatencyMs,
                totalMessages = metrics.messageThroughput.totalMessages,
                usedMemoryMb = metrics.memoryUsage.usedMemoryMb,
                totalMemoryMb = metrics.memoryUsage.totalMemoryMb,
                memoryUsagePercentage = metrics.memoryUsage.usagePercentage,
                gcCount = metrics.memoryUsage.gcCount,
                gcTimeMs = metrics.memoryUsage.gcTimeMs,
                batteryLevel = metrics.batteryUsage.batteryLevel,
                isCharging = metrics.batteryUsage.isCharging,
                batteryDrainRate = metrics.batteryUsage.batteryDrainRate,
                networkLatencyMs = metrics.networkPerformance.latencyMs,
                networkThroughputKbps = metrics.networkPerformance.throughputKbps,
                networkQuality = metrics.networkPerformance.connectionQuality.name,
                cpuUsagePercentage = metrics.cpuUsage.cpuUsagePercentage,
                threadCount = metrics.cpuUsage.threadCount
            )
            performanceDao.insertMetrics(entity)
        } catch (e: Exception) {
            Logger.e("Failed to store performance metrics", e)
        }
    }
    
    override suspend fun storeAlert(alert: PerformanceAlert) {
        try {
            val entity = PerformanceAlertEntity(
                id = alert.id,
                type = alert.type.name,
                severity = alert.severity.name,
                message = alert.message,
                timestamp = alert.timestamp,
                metricsJson = alert.metrics.toString() // Simple serialization
            )
            performanceDao.insertAlert(entity)
        } catch (e: Exception) {
            Logger.e("Failed to store performance alert", e)
        }
    }
    
    override suspend fun storeMemoryMetric(usedMemoryMb: Long, totalMemoryMb: Long) {
        // This is handled as part of storeMetrics, but can be used for immediate storage
        Logger.d("Memory usage: ${usedMemoryMb}MB / ${totalMemoryMb}MB")
    }
    
    override suspend fun storeBatteryMetric(batteryLevel: Float, isCharging: Boolean) {
        // This is handled as part of storeMetrics, but can be used for immediate storage
        Logger.d("Battery: ${(batteryLevel * 100).toInt()}%, charging: $isCharging")
    }
    
    override suspend fun storeNetworkMetric(latencyMs: Long, throughputKbps: Long) {
        // This is handled as part of storeMetrics, but can be used for immediate storage
        Logger.d("Network: ${latencyMs}ms latency, ${throughputKbps}kbps throughput")
    }
    
    override suspend fun getMetrics(fromTimestamp: Long, toTimestamp: Long): List<PerformanceMetrics> {
        return try {
            performanceDao.getMetrics(fromTimestamp, toTimestamp).map { entity ->
                PerformanceMetrics(
                    timestamp = entity.timestamp,
                    messageThroughput = MessageThroughputMetrics(
                        messagesPerSecond = entity.messagesPerSecond,
                        averageLatencyMs = entity.averageLatencyMs,
                        peakThroughput = entity.messagesPerSecond, // Simplified
                        totalMessages = entity.totalMessages
                    ),
                    memoryUsage = MemoryUsageMetrics(
                        usedMemoryMb = entity.usedMemoryMb,
                        totalMemoryMb = entity.totalMemoryMb,
                        usagePercentage = entity.memoryUsagePercentage,
                        gcCount = entity.gcCount,
                        gcTimeMs = entity.gcTimeMs
                    ),
                    batteryUsage = BatteryUsageMetrics(
                        batteryLevel = entity.batteryLevel,
                        isCharging = entity.isCharging,
                        batteryDrainRate = entity.batteryDrainRate,
                        estimatedTimeRemaining = null // Not stored
                    ),
                    networkPerformance = NetworkPerformanceMetrics(
                        latencyMs = entity.networkLatencyMs,
                        throughputKbps = entity.networkThroughputKbps,
                        packetLoss = 0f, // Not stored
                        connectionQuality = NetworkQuality.valueOf(entity.networkQuality)
                    ),
                    cpuUsage = CpuUsageMetrics(
                        cpuUsagePercentage = entity.cpuUsagePercentage,
                        threadCount = entity.threadCount,
                        activeThreads = entity.threadCount // Simplified
                    )
                )
            }
        } catch (e: Exception) {
            Logger.e("Failed to get performance metrics", e)
            emptyList()
        }
    }
    
    override suspend fun getAlerts(fromTimestamp: Long, toTimestamp: Long): List<PerformanceAlert> {
        return try {
            performanceDao.getAlerts(fromTimestamp, toTimestamp).map { entity ->
                PerformanceAlert(
                    id = entity.id,
                    type = AlertType.valueOf(entity.type),
                    severity = AlertSeverity.valueOf(entity.severity),
                    message = entity.message,
                    timestamp = entity.timestamp,
                    metrics = emptyMap() // Simplified - would need proper JSON parsing
                )
            }
        } catch (e: Exception) {
            Logger.e("Failed to get performance alerts", e)
            emptyList()
        }
    }
    
    override suspend fun clearOldData(olderThanMs: Long) {
        try {
            val cutoffTime = System.currentTimeMillis() - olderThanMs
            performanceDao.deleteOldMetrics(cutoffTime)
            performanceDao.deleteOldAlerts(cutoffTime)
            Logger.d("Cleared performance data older than $cutoffTime")
        } catch (e: Exception) {
            Logger.e("Failed to clear old performance data", e)
        }
    }
}