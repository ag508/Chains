package com.chain.messaging.core.performance

import kotlinx.coroutines.flow.Flow

/**
 * Interface for battery usage optimization
 */
interface BatteryOptimizer {
    
    /**
     * Start battery optimization
     */
    suspend fun startOptimization()
    
    /**
     * Stop battery optimization
     */
    suspend fun stopOptimization()
    
    /**
     * Optimize background operations based on battery level
     */
    suspend fun optimizeBackgroundOperations(batteryLevel: Float, isCharging: Boolean)
    
    /**
     * Reduce CPU intensive operations
     */
    suspend fun reduceCpuIntensiveOperations()
    
    /**
     * Optimize network operations
     */
    suspend fun optimizeNetworkOperations()
    
    /**
     * Get battery optimization status
     */
    fun getBatteryOptimizationStatus(): Flow<BatteryOptimizationStatus>
    
    /**
     * Get battery usage recommendations
     */
    suspend fun getBatteryRecommendations(): List<BatteryRecommendation>
}

/**
 * Battery optimization status
 */
data class BatteryOptimizationStatus(
    val isOptimizing: Boolean,
    val currentMode: OptimizationMode,
    val batteryLevel: Float,
    val estimatedTimeRemaining: Long?,
    val activeOptimizations: List<String>
)

/**
 * Battery optimization recommendation
 */
data class BatteryRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val impact: BatteryImpact,
    val action: String
)

enum class OptimizationMode {
    NORMAL,
    POWER_SAVER,
    ULTRA_POWER_SAVER,
    CHARGING
}

enum class BatteryImpact {
    LOW, MEDIUM, HIGH
}