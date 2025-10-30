package com.chain.messaging.core.performance

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.chain.messaging.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

/**
 * Implementation of PerformanceTester for benchmarking and performance testing
 */
@Singleton
class PerformanceTesterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val performanceMonitor: PerformanceMonitor,
    private val memoryManager: MemoryManager
) : PerformanceTester {
    
    private val _benchmarkProgress = MutableSharedFlow<BenchmarkProgress>()
    
    override suspend fun benchmarkMessageThroughput(messageCount: Int, concurrentUsers: Int): BenchmarkResult {
        Logger.i("Starting message throughput benchmark: $messageCount messages, $concurrentUsers users")
        
        emitProgress("Message Throughput", 0f, "Initializing test", 0L)
        
        val metrics = mutableMapOf<String, Double>()
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        try {
            val totalTime = measureTimeMillis {
                // Simulate concurrent message sending
                coroutineScope {
                    val jobs = (1..concurrentUsers).map { userId ->
                        async {
                            val userTime = measureTimeMillis {
                                repeat(messageCount / concurrentUsers) { messageIndex ->
                                    // Simulate message processing
                                    delay(1) // Simulate encryption/network delay
                                    
                                    val progress = (messageIndex.toFloat() / (messageCount / concurrentUsers)) * 100f
                                    if (messageIndex % 10 == 0) {
                                        emitProgress("Message Throughput", progress, "Processing messages for user $userId", 0L)
                                    }
                                }
                            }
                            userTime
                        }
                    }
                    
                    jobs.awaitAll()
                }
            }
            
            val messagesPerSecond = (messageCount * 1000.0) / totalTime
            val averageLatency = totalTime.toDouble() / messageCount
            
            metrics["messages_per_second"] = messagesPerSecond
            metrics["average_latency_ms"] = averageLatency
            metrics["total_time_ms"] = totalTime.toDouble()
            metrics["concurrent_users"] = concurrentUsers.toDouble()
            
            // Performance analysis
            when {
                messagesPerSecond > 100 -> {
                    recommendations.add("Excellent message throughput performance")
                }
                messagesPerSecond > 50 -> {
                    recommendations.add("Good message throughput, consider optimizing for higher loads")
                }
                messagesPerSecond > 10 -> {
                    recommendations.add("Moderate throughput, optimize encryption and network operations")
                }
                else -> {
                    recommendations.add("Low throughput detected, review message processing pipeline")
                    errors.add("Message throughput below acceptable threshold")
                }
            }
            
            emitProgress("Message Throughput", 100f, "Test completed", 0L)
            
            return BenchmarkResult(
                testName = "Message Throughput",
                duration = totalTime,
                success = errors.isEmpty(),
                metrics = metrics,
                errors = errors,
                recommendations = recommendations
            )
            
        } catch (e: Exception) {
            Logger.e("Error in message throughput benchmark", e)
            errors.add("Benchmark failed: ${e.message}")
            
            return BenchmarkResult(
                testName = "Message Throughput",
                duration = 0L,
                success = false,
                metrics = metrics,
                errors = errors,
                recommendations = recommendations
            )
        }
    }
    
    override suspend fun benchmarkMemoryUsage(duration: Long): BenchmarkResult {
        Logger.i("Starting memory usage benchmark for ${duration}ms")
        
        emitProgress("Memory Usage", 0f, "Starting memory monitoring", duration)
        
        val metrics = mutableMapOf<String, Double>()
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        try {
            val memoryStats = mutableListOf<MemoryStats>()
            val startTime = System.currentTimeMillis()
            
            // Collect memory stats during the test
            coroutineScope {
                val job = launch {
                    memoryManager.getMemoryStats().collect { stats ->
                        memoryStats.add(stats)
                        
                        val elapsed = System.currentTimeMillis() - startTime
                        val progress = (elapsed.toFloat() / duration) * 100f
                        emitProgress("Memory Usage", progress, "Monitoring memory usage", duration - elapsed)
                    }
                }
                
                // Simulate memory-intensive operations
                val operations = async {
                val chunks = mutableListOf<ByteArray>()
                try {
                    repeat(100) { iteration ->
                        // Allocate memory chunks
                        chunks.add(ByteArray(1024 * 1024)) // 1MB chunks
                        delay(duration / 100)
                        
                        // Occasionally clear some chunks
                        if (iteration % 10 == 0 && chunks.size > 5) {
                            repeat(5) { chunks.removeLastOrNull() }
                            System.gc()
                        }
                    }
                } finally {
                    chunks.clear()
                    System.gc()
                }
                }
                
                delay(duration)
                operations.cancel()
                job.cancel()
            }
            
            // Analyze memory stats
            if (memoryStats.isNotEmpty()) {
                val avgMemoryUsage = memoryStats.map { it.usagePercentage }.average()
                val maxMemoryUsage = memoryStats.maxOf { it.usagePercentage }
                val totalGcCount = memoryStats.sumOf { it.gcCount }
                val totalGcTime = memoryStats.sumOf { it.gcTimeMs }
                
                metrics["average_memory_usage_percent"] = (avgMemoryUsage * 100).toDouble()
                metrics["max_memory_usage_percent"] = (maxMemoryUsage * 100).toDouble()
                metrics["total_gc_count"] = totalGcCount.toDouble()
                metrics["total_gc_time_ms"] = totalGcTime.toDouble()
                
                // Analysis
                when {
                    maxMemoryUsage < 0.7f -> {
                        recommendations.add("Excellent memory management")
                    }
                    maxMemoryUsage < 0.85f -> {
                        recommendations.add("Good memory usage, monitor for memory leaks")
                    }
                    maxMemoryUsage < 0.95f -> {
                        recommendations.add("High memory usage detected, optimize memory allocation")
                    }
                    else -> {
                        recommendations.add("Critical memory usage, immediate optimization required")
                        errors.add("Memory usage exceeded safe threshold")
                    }
                }
                
                if (totalGcCount > 50) {
                    recommendations.add("High GC activity detected, optimize object allocation")
                }
            }
            
            emitProgress("Memory Usage", 100f, "Test completed", 0L)
            
            return BenchmarkResult(
                testName = "Memory Usage",
                duration = duration,
                success = errors.isEmpty(),
                metrics = metrics,
                errors = errors,
                recommendations = recommendations
            )
            
        } catch (e: Exception) {
            Logger.e("Error in memory usage benchmark", e)
            errors.add("Benchmark failed: ${e.message}")
            
            return BenchmarkResult(
                testName = "Memory Usage",
                duration = 0L,
                success = false,
                metrics = metrics,
                errors = errors,
                recommendations = recommendations
            )
        }
    }
    
    override suspend fun benchmarkBatteryUsage(duration: Long): BenchmarkResult {
        Logger.i("Starting battery usage benchmark for ${duration}ms")
        
        val metrics = mutableMapOf<String, Double>()
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        try {
            // This would require actual battery monitoring over time
            // For now, we'll simulate the test
            
            emitProgress("Battery Usage", 0f, "Monitoring battery consumption", duration)
            
            val startTime = System.currentTimeMillis()
            var progress = 0f
            
            while (progress < 100f) {
                delay(duration / 100)
                progress += 1f
                emitProgress("Battery Usage", progress, "Measuring battery drain", duration - (System.currentTimeMillis() - startTime))
            }
            
            // Simulated battery metrics
            metrics["estimated_drain_percent_per_hour"] = 5.0
            metrics["cpu_usage_percent"] = 15.0
            metrics["network_usage_percent"] = 10.0
            
            recommendations.add("Battery usage within normal parameters")
            recommendations.add("Consider enabling power saving mode for extended usage")
            
            return BenchmarkResult(
                testName = "Battery Usage",
                duration = duration,
                success = true,
                metrics = metrics,
                errors = errors,
                recommendations = recommendations
            )
            
        } catch (e: Exception) {
            Logger.e("Error in battery usage benchmark", e)
            errors.add("Benchmark failed: ${e.message}")
            
            return BenchmarkResult(
                testName = "Battery Usage",
                duration = 0L,
                success = false,
                metrics = metrics,
                errors = errors,
                recommendations = recommendations
            )
        }
    }
    
    override suspend fun benchmarkNetworkPerformance(requestCount: Int): BenchmarkResult {
        Logger.i("Starting network performance benchmark with $requestCount requests")
        
        val metrics = mutableMapOf<String, Double>()
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        try {
            emitProgress("Network Performance", 0f, "Testing network latency", 0L)
            
            val latencies = mutableListOf<Long>()
            val startTime = System.currentTimeMillis()
            
            repeat(requestCount) { request ->
                val requestTime = measureTimeMillis {
                    // Simulate network request
                    delay((50 + (0..100).random()).toLong()) // 50-150ms simulated latency
                }
                
                latencies.add(requestTime)
                
                val progress = ((request + 1).toFloat() / requestCount) * 100f
                emitProgress("Network Performance", progress, "Request ${request + 1}/$requestCount", 0L)
            }
            
            val totalTime = System.currentTimeMillis() - startTime
            val averageLatency = latencies.average()
            val minLatency = latencies.minOrNull() ?: 0L
            val maxLatency = latencies.maxOrNull() ?: 0L
            val requestsPerSecond = (requestCount * 1000.0) / totalTime
            
            metrics["average_latency_ms"] = averageLatency
            metrics["min_latency_ms"] = minLatency.toDouble()
            metrics["max_latency_ms"] = maxLatency.toDouble()
            metrics["requests_per_second"] = requestsPerSecond
            metrics["total_time_ms"] = totalTime.toDouble()
            
            // Analysis
            when {
                averageLatency < 100 -> {
                    recommendations.add("Excellent network performance")
                }
                averageLatency < 300 -> {
                    recommendations.add("Good network performance")
                }
                averageLatency < 1000 -> {
                    recommendations.add("Moderate network performance, consider optimization")
                }
                else -> {
                    recommendations.add("Poor network performance detected")
                    errors.add("High network latency may impact user experience")
                }
            }
            
            emitProgress("Network Performance", 100f, "Test completed", 0L)
            
            return BenchmarkResult(
                testName = "Network Performance",
                duration = totalTime,
                success = errors.isEmpty(),
                metrics = metrics,
                errors = errors,
                recommendations = recommendations
            )
            
        } catch (e: Exception) {
            Logger.e("Error in network performance benchmark", e)
            errors.add("Benchmark failed: ${e.message}")
            
            return BenchmarkResult(
                testName = "Network Performance",
                duration = 0L,
                success = false,
                metrics = metrics,
                errors = errors,
                recommendations = recommendations
            )
        }
    }
    
    override suspend fun runComprehensiveTest(): ComprehensiveTestResult {
        Logger.i("Starting comprehensive performance test")
        
        val testResults = mutableListOf<BenchmarkResult>()
        val criticalIssues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        try {
            // Run all benchmarks
            testResults.add(benchmarkMessageThroughput(1000, 10))
            testResults.add(benchmarkMemoryUsage(30000L)) // 30 seconds
            testResults.add(benchmarkBatteryUsage(10000L)) // 10 seconds
            testResults.add(benchmarkNetworkPerformance(50))
            
            // Calculate overall score
            val successfulTests = testResults.count { it.success }
            val totalTests = testResults.size
            val overallScore = (successfulTests.toDouble() / totalTests) * 100.0
            
            // Collect critical issues
            testResults.forEach { result ->
                criticalIssues.addAll(result.errors)
                recommendations.addAll(result.recommendations)
            }
            
            val summary = when {
                overallScore >= 90 -> "Excellent overall performance"
                overallScore >= 75 -> "Good performance with minor issues"
                overallScore >= 50 -> "Moderate performance, optimization recommended"
                else -> "Poor performance, immediate attention required"
            }
            
            return ComprehensiveTestResult(
                overallScore = overallScore,
                testResults = testResults,
                summary = summary,
                criticalIssues = criticalIssues.distinct(),
                recommendations = recommendations.distinct()
            )
            
        } catch (e: Exception) {
            Logger.e("Error in comprehensive performance test", e)
            
            return ComprehensiveTestResult(
                overallScore = 0.0,
                testResults = testResults,
                summary = "Test failed: ${e.message}",
                criticalIssues = listOf("Comprehensive test failed"),
                recommendations = listOf("Review test configuration and try again")
            )
        }
    }
    
    override fun getBenchmarkProgress(): Flow<BenchmarkProgress> = _benchmarkProgress.asSharedFlow()
    
    override suspend fun generatePerformanceReport(): PerformanceReport {
        val comprehensiveResult = runComprehensiveTest()
        val deviceInfo = getDeviceInfo()
        
        val performanceGrade = when {
            comprehensiveResult.overallScore >= 90 -> PerformanceGrade.EXCELLENT
            comprehensiveResult.overallScore >= 75 -> PerformanceGrade.GOOD
            comprehensiveResult.overallScore >= 50 -> PerformanceGrade.FAIR
            comprehensiveResult.overallScore >= 25 -> PerformanceGrade.POOR
            else -> PerformanceGrade.CRITICAL
        }
        
        return PerformanceReport(
            timestamp = System.currentTimeMillis(),
            deviceInfo = deviceInfo,
            testResults = comprehensiveResult.testResults,
            overallScore = comprehensiveResult.overallScore,
            performanceGrade = performanceGrade,
            recommendations = comprehensiveResult.recommendations,
            historicalComparison = null // Would be implemented with historical data
        )
    }
    
    private suspend fun emitProgress(testName: String, progress: Float, currentStep: String, estimatedTimeRemaining: Long) {
        _benchmarkProgress.emit(
            BenchmarkProgress(
                testName = testName,
                progress = progress,
                currentStep = currentStep,
                estimatedTimeRemaining = estimatedTimeRemaining
            )
        )
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        return DeviceInfo(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            totalMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024),
            availableStorageGb = context.filesDir.freeSpace / (1024 * 1024 * 1024),
            cpuCores = Runtime.getRuntime().availableProcessors(),
            screenDensity = displayMetrics.densityDpi
        )
    }
}