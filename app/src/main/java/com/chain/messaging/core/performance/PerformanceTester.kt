package com.chain.messaging.core.performance

import kotlinx.coroutines.flow.Flow

/**
 * Interface for performance testing and benchmarking
 */
interface PerformanceTester {
    
    /**
     * Run message throughput benchmark
     */
    suspend fun benchmarkMessageThroughput(messageCount: Int, concurrentUsers: Int): BenchmarkResult
    
    /**
     * Run memory usage benchmark
     */
    suspend fun benchmarkMemoryUsage(duration: Long): BenchmarkResult
    
    /**
     * Run battery usage benchmark
     */
    suspend fun benchmarkBatteryUsage(duration: Long): BenchmarkResult
    
    /**
     * Run network performance benchmark
     */
    suspend fun benchmarkNetworkPerformance(requestCount: Int): BenchmarkResult
    
    /**
     * Run comprehensive performance test
     */
    suspend fun runComprehensiveTest(): ComprehensiveTestResult
    
    /**
     * Get benchmark progress
     */
    fun getBenchmarkProgress(): Flow<BenchmarkProgress>
    
    /**
     * Generate performance report
     */
    suspend fun generatePerformanceReport(): PerformanceReport
}

/**
 * Benchmark result
 */
data class BenchmarkResult(
    val testName: String,
    val duration: Long,
    val success: Boolean,
    val metrics: Map<String, Double>,
    val errors: List<String>,
    val recommendations: List<String>
)

/**
 * Comprehensive test result
 */
data class ComprehensiveTestResult(
    val overallScore: Double,
    val testResults: List<BenchmarkResult>,
    val summary: String,
    val criticalIssues: List<String>,
    val recommendations: List<String>
)

/**
 * Benchmark progress
 */
data class BenchmarkProgress(
    val testName: String,
    val progress: Float,
    val currentStep: String,
    val estimatedTimeRemaining: Long
)

/**
 * Performance report
 */
data class PerformanceReport(
    val timestamp: Long,
    val deviceInfo: DeviceInfo,
    val testResults: List<BenchmarkResult>,
    val overallScore: Double,
    val performanceGrade: PerformanceGrade,
    val recommendations: List<String>,
    val historicalComparison: HistoricalComparison?
)

/**
 * Device information
 */
data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val apiLevel: Int,
    val totalMemoryMb: Long,
    val availableStorageGb: Long,
    val cpuCores: Int,
    val screenDensity: Int
)

/**
 * Historical performance comparison
 */
data class HistoricalComparison(
    val previousScore: Double,
    val scoreChange: Double,
    val trend: PerformanceTrend,
    val significantChanges: List<String>
)

enum class PerformanceGrade {
    EXCELLENT, GOOD, FAIR, POOR, CRITICAL
}

enum class PerformanceTrend {
    IMPROVING, STABLE, DECLINING
}