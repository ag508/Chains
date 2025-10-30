package com.chain.messaging.core.performance

import kotlinx.coroutines.flow.Flow

/**
 * Interface for monitoring application performance metrics
 */
interface PerformanceMonitor {
    
    /**
     * Start monitoring performance metrics
     */
    suspend fun startMonitoring()
    
    /**
     * Stop monitoring performance metrics
     */
    suspend fun stopMonitoring()
    
    /**
     * Record a message throughput event
     */
    suspend fun recordMessageThroughput(messageCount: Int, timeWindowMs: Long)
    
    /**
     * Record memory usage
     */
    suspend fun recordMemoryUsage(usedMemoryMb: Long, totalMemoryMb: Long)
    
    /**
     * Record battery usage
     */
    suspend fun recordBatteryUsage(batteryLevel: Float, isCharging: Boolean)
    
    /**
     * Record network performance
     */
    suspend fun recordNetworkPerformance(latencyMs: Long, throughputKbps: Long)
    
    /**
     * Get current performance metrics
     */
    fun getPerformanceMetrics(): Flow<PerformanceMetrics>
    
    /**
     * Get performance alerts
     */
    fun getPerformanceAlerts(): Flow<PerformanceAlert>
    
    /**
     * Clear old performance data
     */
    suspend fun clearOldData(olderThanMs: Long)
    
    /**
     * Start periodic performance reports
     */
    suspend fun startPeriodicReports()
    
    /**
     * Get current performance metrics as a map
     */
    fun getCurrentMetrics(): Map<String, Any>
}

/**
 * Performance metrics data class
 */
data class PerformanceMetrics(
    val timestamp: Long,
    val messageThroughput: MessageThroughputMetrics,
    val memoryUsage: MemoryUsageMetrics,
    val batteryUsage: BatteryUsageMetrics,
    val networkPerformance: NetworkPerformanceMetrics,
    val cpuUsage: CpuUsageMetrics
)

data class MessageThroughputMetrics(
    val messagesPerSecond: Double,
    val averageLatencyMs: Long,
    val peakThroughput: Double,
    val totalMessages: Long
)

data class MemoryUsageMetrics(
    val usedMemoryMb: Long,
    val totalMemoryMb: Long,
    val usagePercentage: Float,
    val gcCount: Int,
    val gcTimeMs: Long
)

data class BatteryUsageMetrics(
    val batteryLevel: Float,
    val isCharging: Boolean,
    val batteryDrainRate: Float,
    val estimatedTimeRemaining: Long?
)

data class NetworkPerformanceMetrics(
    val latencyMs: Long,
    val throughputKbps: Long,
    val packetLoss: Float,
    val connectionQuality: NetworkQuality
)

data class CpuUsageMetrics(
    val cpuUsagePercentage: Float,
    val threadCount: Int,
    val activeThreads: Int
)

enum class NetworkQuality {
    EXCELLENT, GOOD, FAIR, POOR, OFFLINE
}

/**
 * Performance alert data class
 */
data class PerformanceAlert(
    val id: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long,
    val metrics: Map<String, Any>
)

enum class AlertType {
    HIGH_MEMORY_USAGE,
    LOW_BATTERY,
    HIGH_CPU_USAGE,
    SLOW_MESSAGE_THROUGHPUT,
    NETWORK_ISSUES,
    MEMORY_LEAK_DETECTED
}

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}