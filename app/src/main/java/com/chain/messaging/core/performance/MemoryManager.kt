package com.chain.messaging.core.performance

import kotlinx.coroutines.flow.Flow

/**
 * Interface for memory management and garbage collection optimization
 */
interface MemoryManager {
    
    /**
     * Start memory monitoring and optimization
     */
    suspend fun startMemoryManagement()
    
    /**
     * Stop memory monitoring
     */
    suspend fun stopMemoryManagement()
    
    /**
     * Perform garbage collection optimization
     */
    suspend fun optimizeGarbageCollection()
    
    /**
     * Clear memory caches
     */
    suspend fun clearCaches()
    
    /**
     * Optimize memory usage
     */
    suspend fun optimizeMemoryUsage()
    
    /**
     * Get memory usage statistics
     */
    fun getMemoryStats(): Flow<MemoryStats>
    
    /**
     * Get memory optimization recommendations
     */
    suspend fun getMemoryRecommendations(): List<MemoryRecommendation>
    
    /**
     * Force garbage collection
     */
    suspend fun forceGarbageCollection()
    
    /**
     * Check for memory leaks
     */
    suspend fun checkForMemoryLeaks(): List<MemoryLeak>
}

/**
 * Memory usage statistics
 */
data class MemoryStats(
    val timestamp: Long,
    val usedMemoryMb: Long,
    val totalMemoryMb: Long,
    val availableMemoryMb: Long,
    val usagePercentage: Float,
    val gcCount: Int,
    val gcTimeMs: Long,
    val cacheSize: Long,
    val heapSize: Long,
    val nativeHeapSize: Long
)

/**
 * Memory optimization recommendation
 */
data class MemoryRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val impact: MemoryImpact,
    val action: String,
    val estimatedSavingMb: Long
)

/**
 * Memory leak detection result
 */
data class MemoryLeak(
    val id: String,
    val type: LeakType,
    val description: String,
    val severity: LeakSeverity,
    val estimatedLeakSizeMb: Long,
    val suggestedFix: String
)

enum class MemoryImpact {
    LOW, MEDIUM, HIGH
}

enum class LeakType {
    ACTIVITY_LEAK,
    FRAGMENT_LEAK,
    BITMAP_LEAK,
    LISTENER_LEAK,
    STATIC_REFERENCE_LEAK,
    THREAD_LEAK
}

enum class LeakSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}