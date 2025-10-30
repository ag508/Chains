package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing performance metrics in the database
 */
@Entity(tableName = "performance_metrics")
data class PerformanceMetricsEntity(
    @PrimaryKey
    val timestamp: Long,
    
    // Message throughput metrics
    val messagesPerSecond: Double,
    val averageLatencyMs: Long,
    val totalMessages: Long,
    
    // Memory usage metrics
    val usedMemoryMb: Long,
    val totalMemoryMb: Long,
    val memoryUsagePercentage: Float,
    val gcCount: Int,
    val gcTimeMs: Long,
    
    // Battery usage metrics
    val batteryLevel: Float,
    val isCharging: Boolean,
    val batteryDrainRate: Float,
    
    // Network performance metrics
    val networkLatencyMs: Long,
    val networkThroughputKbps: Long,
    val networkQuality: String,
    
    // CPU usage metrics
    val cpuUsagePercentage: Float,
    val threadCount: Int
)