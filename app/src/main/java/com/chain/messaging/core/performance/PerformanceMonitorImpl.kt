package com.chain.messaging.core.performance

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Debug
import com.chain.messaging.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Implementation of PerformanceMonitor for monitoring application performance
 */
@Singleton
class PerformanceMonitorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val performanceStorage: PerformanceStorage
) : PerformanceMonitor {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isMonitoring = false
    
    private val _performanceMetrics = MutableSharedFlow<PerformanceMetrics>(replay = 1)
    private val _performanceAlerts = MutableSharedFlow<PerformanceAlert>()
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    
    // Metrics tracking
    private val messageCount = AtomicLong(0)
    private val messageTimes = ConcurrentHashMap<Long, Long>()
    private var lastGcCount = 0
    private var lastGcTime = 0L
    private var lastBatteryLevel = 0f
    private var lastBatteryTime = 0L
    
    // Thresholds for alerts
    private companion object {
        const val HIGH_MEMORY_THRESHOLD = 0.85f // 85% memory usage
        const val LOW_BATTERY_THRESHOLD = 0.15f // 15% battery
        const val HIGH_CPU_THRESHOLD = 0.80f // 80% CPU usage
        const val SLOW_THROUGHPUT_THRESHOLD = 1.0 // 1 message per second
        const val MONITORING_INTERVAL_MS = 5000L // 5 seconds
        const val METRICS_RETENTION_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    override suspend fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        Logger.i("Starting performance monitoring")
        
        scope.launch {
            while (isMonitoring) {
                try {
                    collectAndEmitMetrics()
                    delay(MONITORING_INTERVAL_MS)
                } catch (e: Exception) {
                    Logger.e("Error collecting performance metrics", e)
                }
            }
        }
        
        // Start background cleanup
        scope.launch {
            while (isMonitoring) {
                delay(60 * 60 * 1000L) // Every hour
                clearOldData(METRICS_RETENTION_MS)
            }
        }
    }
    
    override suspend fun stopMonitoring() {
        isMonitoring = false
        Logger.i("Stopping performance monitoring")
    }
    
    override suspend fun recordMessageThroughput(messageCount: Int, timeWindowMs: Long) {
        val currentTime = System.currentTimeMillis()
        this.messageCount.addAndGet(messageCount.toLong())
        messageTimes[currentTime] = messageCount.toLong()
        
        // Clean old message times (keep only last 5 minutes)
        val cutoffTime = currentTime - 5 * 60 * 1000L
        messageTimes.entries.removeIf { it.key < cutoffTime }
    }
    
    override suspend fun recordMemoryUsage(usedMemoryMb: Long, totalMemoryMb: Long) {
        performanceStorage.storeMemoryMetric(usedMemoryMb, totalMemoryMb)
        
        val usagePercentage = usedMemoryMb.toFloat() / totalMemoryMb.toFloat()
        if (usagePercentage > HIGH_MEMORY_THRESHOLD) {
            emitAlert(
                AlertType.HIGH_MEMORY_USAGE,
                AlertSeverity.HIGH,
                "High memory usage detected: ${(usagePercentage * 100).toInt()}%",
                mapOf("usedMemoryMb" to usedMemoryMb, "totalMemoryMb" to totalMemoryMb)
            )
        }
    }
    
    override suspend fun recordBatteryUsage(batteryLevel: Float, isCharging: Boolean) {
        performanceStorage.storeBatteryMetric(batteryLevel, isCharging)
        
        if (batteryLevel < LOW_BATTERY_THRESHOLD && !isCharging) {
            emitAlert(
                AlertType.LOW_BATTERY,
                AlertSeverity.MEDIUM,
                "Low battery level: ${(batteryLevel * 100).toInt()}%",
                mapOf("batteryLevel" to batteryLevel, "isCharging" to isCharging)
            )
        }
    }
    
    override suspend fun recordNetworkPerformance(latencyMs: Long, throughputKbps: Long) {
        performanceStorage.storeNetworkMetric(latencyMs, throughputKbps)
        
        if (latencyMs > 5000) { // 5 seconds latency
            emitAlert(
                AlertType.NETWORK_ISSUES,
                AlertSeverity.MEDIUM,
                "High network latency detected: ${latencyMs}ms",
                mapOf("latencyMs" to latencyMs, "throughputKbps" to throughputKbps)
            )
        }
    }
    
    override fun getPerformanceMetrics(): Flow<PerformanceMetrics> = _performanceMetrics.asSharedFlow()
    
    override fun getPerformanceAlerts(): Flow<PerformanceAlert> = _performanceAlerts.asSharedFlow()
    
    override suspend fun clearOldData(olderThanMs: Long) {
        performanceStorage.clearOldData(olderThanMs)
        Logger.d("Cleared performance data older than ${olderThanMs}ms")
    }
    
    override suspend fun startPeriodicReports() {
        Logger.i("Starting periodic performance reports")
        
        scope.launch {
            while (isMonitoring) {
                try {
                    generatePerformanceReport()
                    delay(15 * 60 * 1000L) // Generate report every 15 minutes
                } catch (e: Exception) {
                    Logger.e("Error generating performance report", e)
                }
            }
        }
    }
    
    override fun getCurrentMetrics(): Map<String, Any> {
        val memoryMetrics = collectMemoryMetrics()
        val batteryMetrics = collectBatteryMetrics()
        val cpuMetrics = collectCpuMetrics()
        val throughputMetrics = collectThroughputMetrics()
        
        return mapOf(
            "memoryUsagePercentage" to memoryMetrics.usagePercentage,
            "usedMemoryMb" to memoryMetrics.usedMemoryMb,
            "batteryLevel" to batteryMetrics.batteryLevel,
            "isCharging" to batteryMetrics.isCharging,
            "cpuUsagePercentage" to cpuMetrics.cpuUsagePercentage,
            "threadCount" to cpuMetrics.threadCount,
            "messagesPerSecond" to throughputMetrics.messagesPerSecond,
            "totalMessages" to throughputMetrics.totalMessages,
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    private suspend fun generatePerformanceReport() {
        val currentMetrics = getCurrentMetrics()
        Logger.i("Performance Report: $currentMetrics")
        
        // Store the report
        performanceStorage.storeMetrics(currentMetrics)
        
        // Check if we need to emit any summary alerts
        val memoryUsage = currentMetrics["memoryUsagePercentage"] as Float
        val batteryLevel = currentMetrics["batteryLevel"] as Float
        
        if (memoryUsage > 0.9f || batteryLevel < 0.1f) {
            emitAlert(
                AlertType.HIGH_MEMORY_USAGE,
                AlertSeverity.CRITICAL,
                "Critical performance issues detected in periodic report",
                currentMetrics
            )
        }
    }
    
    private suspend fun collectAndEmitMetrics() {
        val currentTime = System.currentTimeMillis()
        
        val memoryMetrics = collectMemoryMetrics()
        val batteryMetrics = collectBatteryMetrics()
        val networkMetrics = collectNetworkMetrics()
        val cpuMetrics = collectCpuMetrics()
        val throughputMetrics = collectThroughputMetrics()
        
        val metrics = PerformanceMetrics(
            timestamp = currentTime,
            messageThroughput = throughputMetrics,
            memoryUsage = memoryMetrics,
            batteryUsage = batteryMetrics,
            networkPerformance = networkMetrics,
            cpuUsage = cpuMetrics
        )
        
        _performanceMetrics.emit(metrics)
        performanceStorage.storeMetrics(metrics)
        
        // Check for performance issues and emit alerts
        checkPerformanceThresholds(metrics)
    }
    
    private fun collectMemoryMetrics(): MemoryUsageMetrics {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val totalMemory = memoryInfo.totalMem / (1024 * 1024)
        val usagePercentage = usedMemory.toFloat() / totalMemory.toFloat()
        
        // Get GC info
        val currentGcCount = Debug.getGlobalGcInvocationCount()
        val currentGcTime = System.currentTimeMillis() // Use current time as approximation
        val gcCountDelta = max(0, currentGcCount - lastGcCount)
        val gcTimeDelta = max(0L, currentGcTime - lastGcTime)
        
        lastGcCount = currentGcCount
        lastGcTime = currentGcTime
        
        return MemoryUsageMetrics(
            usedMemoryMb = usedMemory,
            totalMemoryMb = totalMemory,
            usagePercentage = usagePercentage,
            gcCount = gcCountDelta,
            gcTimeMs = gcTimeDelta
        )
    }
    
    private fun collectBatteryMetrics(): BatteryUsageMetrics {
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) / 100f
        val isCharging = batteryManager.isCharging
        
        // Calculate battery drain rate
        val currentTime = System.currentTimeMillis()
        val drainRate = if (lastBatteryTime > 0 && !isCharging) {
            val timeDelta = (currentTime - lastBatteryTime) / 1000f / 3600f // hours
            val levelDelta = lastBatteryLevel - batteryLevel
            if (timeDelta > 0) levelDelta / timeDelta else 0f
        } else 0f
        
        val estimatedTimeRemaining = if (drainRate > 0 && !isCharging) {
            ((batteryLevel / drainRate) * 3600 * 1000).toLong() // milliseconds
        } else null
        
        lastBatteryLevel = batteryLevel
        lastBatteryTime = currentTime
        
        return BatteryUsageMetrics(
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            batteryDrainRate = drainRate,
            estimatedTimeRemaining = estimatedTimeRemaining
        )
    }
    
    private fun collectNetworkMetrics(): NetworkPerformanceMetrics {
        // This would typically involve actual network measurements
        // For now, we'll return default values
        return NetworkPerformanceMetrics(
            latencyMs = 50L,
            throughputKbps = 1000L,
            packetLoss = 0f,
            connectionQuality = NetworkQuality.GOOD
        )
    }
    
    private fun collectCpuMetrics(): CpuUsageMetrics {
        val threadCount = Thread.activeCount()
        
        // Get CPU usage (simplified approach)
        val cpuUsage = try {
            val runtime = Runtime.getRuntime()
            val processors = runtime.availableProcessors()
            // This is a simplified CPU usage calculation
            val memoryInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(memoryInfo)
            val loadAverage = memoryInfo.totalPss.toFloat()
            (loadAverage / (processors * 1000)).coerceIn(0f, 1f)
        } catch (e: Exception) {
            0f
        }
        
        return CpuUsageMetrics(
            cpuUsagePercentage = cpuUsage,
            threadCount = threadCount,
            activeThreads = threadCount
        )
    }
    
    private fun collectThroughputMetrics(): MessageThroughputMetrics {
        val currentTime = System.currentTimeMillis()
        val recentMessages = messageTimes.filterKeys { it > currentTime - 60000L } // Last minute
        
        val messagesPerSecond = if (recentMessages.isNotEmpty()) {
            recentMessages.values.sum().toDouble() / 60.0
        } else 0.0
        
        val totalMessages = messageCount.get()
        
        return MessageThroughputMetrics(
            messagesPerSecond = messagesPerSecond,
            averageLatencyMs = 100L, // This would be calculated from actual message timing
            peakThroughput = messagesPerSecond, // Simplified
            totalMessages = totalMessages
        )
    }
    
    private suspend fun checkPerformanceThresholds(metrics: PerformanceMetrics) {
        // Check memory usage
        if (metrics.memoryUsage.usagePercentage > HIGH_MEMORY_THRESHOLD) {
            emitAlert(
                AlertType.HIGH_MEMORY_USAGE,
                AlertSeverity.HIGH,
                "Memory usage is ${(metrics.memoryUsage.usagePercentage * 100).toInt()}%",
                mapOf("memoryUsage" to metrics.memoryUsage)
            )
        }
        
        // Check CPU usage
        if (metrics.cpuUsage.cpuUsagePercentage > HIGH_CPU_THRESHOLD) {
            emitAlert(
                AlertType.HIGH_CPU_USAGE,
                AlertSeverity.MEDIUM,
                "CPU usage is ${(metrics.cpuUsage.cpuUsagePercentage * 100).toInt()}%",
                mapOf("cpuUsage" to metrics.cpuUsage)
            )
        }
        
        // Check message throughput
        if (metrics.messageThroughput.messagesPerSecond < SLOW_THROUGHPUT_THRESHOLD) {
            emitAlert(
                AlertType.SLOW_MESSAGE_THROUGHPUT,
                AlertSeverity.LOW,
                "Message throughput is low: ${metrics.messageThroughput.messagesPerSecond} msg/s",
                mapOf("throughput" to metrics.messageThroughput)
            )
        }
        
        // Check for potential memory leaks
        if (metrics.memoryUsage.gcCount > 10) { // More than 10 GC cycles in monitoring interval
            emitAlert(
                AlertType.MEMORY_LEAK_DETECTED,
                AlertSeverity.MEDIUM,
                "Frequent garbage collection detected: ${metrics.memoryUsage.gcCount} cycles",
                mapOf("gcCount" to metrics.memoryUsage.gcCount, "gcTime" to metrics.memoryUsage.gcTimeMs)
            )
        }
    }
    
    private suspend fun emitAlert(
        type: AlertType,
        severity: AlertSeverity,
        message: String,
        metrics: Map<String, Any>
    ) {
        val alert = PerformanceAlert(
            id = "${type.name}_${System.currentTimeMillis()}",
            type = type,
            severity = severity,
            message = message,
            timestamp = System.currentTimeMillis(),
            metrics = metrics
        )
        
        _performanceAlerts.emit(alert)
        performanceStorage.storeAlert(alert)
        Logger.w("Performance alert: $message")
    }
}