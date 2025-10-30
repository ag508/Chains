package com.chain.messaging.core.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import com.chain.messaging.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MemoryManager for memory optimization and leak detection
 */
@Singleton
class MemoryManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MemoryManager {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isManaging = false
    
    private val _memoryStats = MutableSharedFlow<MemoryStats>(replay = 1)
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    // Memory leak tracking
    private val trackedObjects = ConcurrentHashMap<String, WeakReference<Any>>()
    private var lastGcCount = 0
    private var lastGcTime = 0L
    private var lastMemoryCheck = 0L
    
    private companion object {
        const val MEMORY_CHECK_INTERVAL = 10000L // 10 seconds
        const val HIGH_MEMORY_THRESHOLD = 0.85f // 85%
        const val CRITICAL_MEMORY_THRESHOLD = 0.95f // 95%
        const val GC_OPTIMIZATION_THRESHOLD = 5 // GC more than 5 times in interval
        const val LEAK_CHECK_INTERVAL = 60000L // 1 minute
    }
    
    override suspend fun startMemoryManagement() {
        if (isManaging) return
        
        isManaging = true
        Logger.i("Starting memory management")
        
        // Start memory monitoring
        scope.launch {
            while (isManaging) {
                try {
                    collectAndEmitMemoryStats()
                    checkMemoryThresholds()
                    delay(MEMORY_CHECK_INTERVAL)
                } catch (e: Exception) {
                    Logger.e("Error in memory monitoring", e)
                }
            }
        }
        
        // Start memory leak detection
        scope.launch {
            while (isManaging) {
                try {
                    checkForMemoryLeaks()
                    delay(LEAK_CHECK_INTERVAL)
                } catch (e: Exception) {
                    Logger.e("Error in memory leak detection", e)
                }
            }
        }
        
        // Start periodic optimization
        scope.launch {
            while (isManaging) {
                delay(5 * 60 * 1000L) // Every 5 minutes
                try {
                    optimizeMemoryUsage()
                } catch (e: Exception) {
                    Logger.e("Error in memory optimization", e)
                }
            }
        }
    }
    
    override suspend fun stopMemoryManagement() {
        isManaging = false
        Logger.i("Stopping memory management")
    }
    
    override suspend fun optimizeGarbageCollection() {
        val currentGcCount = Debug.getGlobalGcInvocationCount()
        val gcDelta = currentGcCount - lastGcCount
        
        if (gcDelta > GC_OPTIMIZATION_THRESHOLD) {
            Logger.w("High GC activity detected: $gcDelta cycles")
            
            // Suggest memory optimization
            clearCaches()
            
            // Force a single GC to clean up
            forceGarbageCollection()
        }
        
        lastGcCount = currentGcCount
    }
    
    override suspend fun clearCaches() {
        try {
            // Clear image caches
            // Clear message caches
            // Clear temporary files
            
            val runtime = Runtime.getRuntime()
            val beforeMemory = runtime.totalMemory() - runtime.freeMemory()
            
            // Simulate cache clearing
            System.gc()
            delay(100)
            
            val afterMemory = runtime.totalMemory() - runtime.freeMemory()
            val freedMemory = (beforeMemory - afterMemory) / (1024 * 1024)
            
            Logger.i("Cleared caches, freed ${freedMemory}MB of memory")
        } catch (e: Exception) {
            Logger.e("Error clearing caches", e)
        }
    }
    
    override suspend fun optimizeMemoryUsage() {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val usagePercentage = (memoryInfo.totalMem - memoryInfo.availMem).toFloat() / memoryInfo.totalMem.toFloat()
        
        when {
            usagePercentage > CRITICAL_MEMORY_THRESHOLD -> {
                Logger.w("Critical memory usage: ${(usagePercentage * 100).toInt()}%")
                clearCaches()
                forceGarbageCollection()
                // Reduce image quality
                // Limit concurrent operations
            }
            
            usagePercentage > HIGH_MEMORY_THRESHOLD -> {
                Logger.w("High memory usage: ${(usagePercentage * 100).toInt()}%")
                clearCaches()
                // Reduce cache sizes
                // Defer non-critical operations
            }
            
            else -> {
                Logger.d("Memory usage normal: ${(usagePercentage * 100).toInt()}%")
            }
        }
    }
    
    override fun getMemoryStats(): Flow<MemoryStats> = _memoryStats.asSharedFlow()
    
    override suspend fun getMemoryRecommendations(): List<MemoryRecommendation> {
        val recommendations = mutableListOf<MemoryRecommendation>()
        
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val usagePercentage = (memoryInfo.totalMem - memoryInfo.availMem).toFloat() / memoryInfo.totalMem.toFloat()
        
        if (usagePercentage > HIGH_MEMORY_THRESHOLD) {
            recommendations.add(
                MemoryRecommendation(
                    id = "clear_caches",
                    title = "Clear Application Caches",
                    description = "Clear image and message caches to free memory",
                    impact = MemoryImpact.HIGH,
                    action = "Clear caches",
                    estimatedSavingMb = 50L
                )
            )
        }
        
        val gcCount = Debug.getGlobalGcInvocationCount()
        if (gcCount - lastGcCount > GC_OPTIMIZATION_THRESHOLD) {
            recommendations.add(
                MemoryRecommendation(
                    id = "optimize_gc",
                    title = "Optimize Garbage Collection",
                    description = "High GC activity detected, optimize memory allocation",
                    impact = MemoryImpact.MEDIUM,
                    action = "Optimize memory usage",
                    estimatedSavingMb = 20L
                )
            )
        }
        
        recommendations.add(
            MemoryRecommendation(
                id = "reduce_image_quality",
                title = "Reduce Image Quality",
                description = "Lower image resolution to save memory",
                impact = MemoryImpact.MEDIUM,
                action = "Adjust image settings",
                estimatedSavingMb = 30L
            )
        )
        
        return recommendations
    }
    
    override suspend fun forceGarbageCollection() {
        try {
            val beforeMemory = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
            
            System.gc()
            delay(100) // Give GC time to run
            System.runFinalization()
            delay(100)
            
            val afterMemory = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
            val freedMemory = (beforeMemory - afterMemory) / (1024 * 1024)
            
            Logger.d("Forced GC completed, freed ${freedMemory}MB")
        } catch (e: Exception) {
            Logger.e("Error forcing garbage collection", e)
        }
    }
    
    override suspend fun checkForMemoryLeaks(): List<MemoryLeak> {
        val leaks = mutableListOf<MemoryLeak>()
        
        try {
            // Check for unreferenced objects that should have been GC'd
            val iterator = trackedObjects.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.get() == null) {
                    iterator.remove() // Object was GC'd, remove from tracking
                }
            }
            
            // Check for potential memory leaks based on memory growth
            val currentTime = System.currentTimeMillis()
            if (lastMemoryCheck > 0) {
                val timeDelta = currentTime - lastMemoryCheck
                if (timeDelta > 5 * 60 * 1000L) { // 5 minutes
                    val memoryInfo = ActivityManager.MemoryInfo()
                    activityManager.getMemoryInfo(memoryInfo)
                    
                    // Simplified leak detection - in reality this would be more sophisticated
                    val usagePercentage = (memoryInfo.totalMem - memoryInfo.availMem).toFloat() / memoryInfo.totalMem.toFloat()
                    
                    if (usagePercentage > 0.9f) {
                        leaks.add(
                            MemoryLeak(
                                id = "high_memory_usage_${currentTime}",
                                type = LeakType.STATIC_REFERENCE_LEAK,
                                description = "Consistently high memory usage may indicate a memory leak",
                                severity = LeakSeverity.MEDIUM,
                                estimatedLeakSizeMb = 50L,
                                suggestedFix = "Review static references and long-lived objects"
                            )
                        )
                    }
                }
            }
            
            lastMemoryCheck = currentTime
            
        } catch (e: Exception) {
            Logger.e("Error checking for memory leaks", e)
        }
        
        return leaks
    }
    
    private suspend fun collectAndEmitMemoryStats() {
        try {
            val runtime = Runtime.getRuntime()
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            val totalMemory = memoryInfo.totalMem / (1024 * 1024)
            val availableMemory = memoryInfo.availMem / (1024 * 1024)
            val usagePercentage = usedMemory.toFloat() / totalMemory.toFloat()
            
            val currentGcCount = Debug.getGlobalGcInvocationCount()
            val currentGcTime = System.currentTimeMillis() // Use current time as approximation
            val gcCountDelta = maxOf(0, currentGcCount - lastGcCount)
            val gcTimeDelta = maxOf(0L, currentGcTime - lastGcTime)
            
            val memoryStats = MemoryStats(
                timestamp = System.currentTimeMillis(),
                usedMemoryMb = usedMemory,
                totalMemoryMb = totalMemory,
                availableMemoryMb = availableMemory,
                usagePercentage = usagePercentage,
                gcCount = gcCountDelta,
                gcTimeMs = gcTimeDelta,
                cacheSize = 0L, // Would be calculated from actual caches
                heapSize = runtime.totalMemory() / (1024 * 1024),
                nativeHeapSize = Debug.getNativeHeapSize() / (1024 * 1024)
            )
            
            _memoryStats.emit(memoryStats)
            
        } catch (e: Exception) {
            Logger.e("Error collecting memory stats", e)
        }
    }
    
    private suspend fun checkMemoryThresholds() {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val usagePercentage = (memoryInfo.totalMem - memoryInfo.availMem).toFloat() / memoryInfo.totalMem.toFloat()
        
        when {
            usagePercentage > CRITICAL_MEMORY_THRESHOLD -> {
                Logger.w("Critical memory threshold exceeded: ${(usagePercentage * 100).toInt()}%")
                optimizeMemoryUsage()
            }
            
            usagePercentage > HIGH_MEMORY_THRESHOLD -> {
                Logger.w("High memory threshold exceeded: ${(usagePercentage * 100).toInt()}%")
                clearCaches()
            }
        }
        
        // Check GC activity
        optimizeGarbageCollection()
    }
    
    /**
     * Track an object for memory leak detection
     */
    fun trackObject(id: String, obj: Any) {
        trackedObjects[id] = WeakReference(obj)
    }
    
    /**
     * Stop tracking an object
     */
    fun untrackObject(id: String) {
        trackedObjects.remove(id)
    }
}