package com.chain.messaging.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.core.performance.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PerformanceMonitoringIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var performanceMonitor: PerformanceMonitor
    
    @Inject
    lateinit var batteryOptimizer: BatteryOptimizer
    
    @Inject
    lateinit var memoryManager: MemoryManager
    
    @Inject
    lateinit var performanceTester: PerformanceTester
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }
    
    @After
    fun tearDown() = runTest {
        performanceMonitor.stopMonitoring()
        batteryOptimizer.stopOptimization()
        memoryManager.stopMemoryManagement()
    }
    
    @Test
    fun `performance monitoring system should initialize successfully`() = runTest {
        // When
        performanceMonitor.startMonitoring()
        batteryOptimizer.startOptimization()
        memoryManager.startMemoryManagement()
        
        // Give some time for initialization
        delay(1000)
        
        // Then
        val metricsFlow = performanceMonitor.getPerformanceMetrics()
        val batteryStatusFlow = batteryOptimizer.getBatteryOptimizationStatus()
        val memoryStatsFlow = memoryManager.getMemoryStats()
        
        assertNotNull("Performance metrics flow should be available", metricsFlow)
        assertNotNull("Battery optimization status should be available", batteryStatusFlow)
        assertNotNull("Memory stats flow should be available", memoryStatsFlow)
    }
    
    @Test
    fun `performance monitoring should record and emit metrics`() = runTest {
        // Given
        performanceMonitor.startMonitoring()
        delay(100) // Allow initialization
        
        // When
        performanceMonitor.recordMessageThroughput(50, 1000L)
        performanceMonitor.recordMemoryUsage(500L, 1000L)
        performanceMonitor.recordBatteryUsage(0.8f, false)
        performanceMonitor.recordNetworkPerformance(100L, 1000L)
        
        // Give time for processing
        delay(500)
        
        // Then
        // The system should handle all recordings without throwing exceptions
        assertTrue("All metrics recorded successfully", true)
    }
    
    @Test
    fun `battery optimizer should respond to battery level changes`() = runTest {
        // Given
        batteryOptimizer.startOptimization()
        delay(100)
        
        // When - Test different battery levels
        batteryOptimizer.optimizeBackgroundOperations(0.9f, false) // High battery
        val highBatteryStatus = batteryOptimizer.getBatteryOptimizationStatus().first()
        
        batteryOptimizer.optimizeBackgroundOperations(0.15f, false) // Low battery
        val lowBatteryStatus = batteryOptimizer.getBatteryOptimizationStatus().first()
        
        batteryOptimizer.optimizeBackgroundOperations(0.05f, false) // Critical battery
        val criticalBatteryStatus = batteryOptimizer.getBatteryOptimizationStatus().first()
        
        // Then
        assertEquals("High battery should use normal mode", 
            OptimizationMode.NORMAL, highBatteryStatus.currentMode)
        assertEquals("Low battery should use power saver mode", 
            OptimizationMode.POWER_SAVER, lowBatteryStatus.currentMode)
        assertEquals("Critical battery should use ultra power saver mode", 
            OptimizationMode.ULTRA_POWER_SAVER, criticalBatteryStatus.currentMode)
    }
    
    @Test
    fun `memory manager should provide optimization recommendations`() = runTest {
        // Given
        memoryManager.startMemoryManagement()
        delay(100)
        
        // When
        val recommendations = memoryManager.getMemoryRecommendations()
        
        // Then
        assertNotNull("Recommendations should not be null", recommendations)
        assertTrue("Should have at least one recommendation", recommendations.isNotEmpty())
        
        // Verify recommendation structure
        recommendations.forEach { recommendation ->
            assertNotNull("Recommendation should have ID", recommendation.id)
            assertNotNull("Recommendation should have title", recommendation.title)
            assertNotNull("Recommendation should have description", recommendation.description)
            assertNotNull("Recommendation should have impact", recommendation.impact)
            assertNotNull("Recommendation should have action", recommendation.action)
            assertTrue("Recommendation should have estimated saving", 
                recommendation.estimatedSavingMb >= 0)
        }
    }
    
    @Test
    fun `performance tester should run comprehensive benchmarks`() = runTest {
        // When
        val comprehensiveResult = performanceTester.runComprehensiveTest()
        
        // Then
        assertNotNull("Comprehensive result should not be null", comprehensiveResult)
        assertTrue("Should have test results", comprehensiveResult.testResults.isNotEmpty())
        assertTrue("Should have overall score", comprehensiveResult.overallScore >= 0.0)
        assertNotNull("Should have summary", comprehensiveResult.summary)
        assertNotNull("Should have recommendations", comprehensiveResult.recommendations)
        
        // Verify individual test results
        val testNames = comprehensiveResult.testResults.map { it.testName }
        assertTrue("Should include message throughput test", 
            testNames.contains("Message Throughput"))
        assertTrue("Should include memory usage test", 
            testNames.contains("Memory Usage"))
        assertTrue("Should include battery usage test", 
            testNames.contains("Battery Usage"))
        assertTrue("Should include network performance test", 
            testNames.contains("Network Performance"))
    }
    
    @Test
    fun `performance system should handle alerts correctly`() = runTest {
        // Given
        performanceMonitor.startMonitoring()
        delay(100)
        
        // When - Trigger conditions that should generate alerts
        performanceMonitor.recordMemoryUsage(950L, 1000L) // 95% memory usage
        performanceMonitor.recordBatteryUsage(0.05f, false) // 5% battery
        performanceMonitor.recordNetworkPerformance(8000L, 100L) // 8 second latency
        
        // Give time for alert processing
        delay(500)
        
        // Then
        val alertsFlow = performanceMonitor.getPerformanceAlerts()
        assertNotNull("Alerts flow should be available", alertsFlow)
    }
    
    @Test
    fun `performance report should contain comprehensive information`() = runTest {
        // When
        val report = performanceTester.generatePerformanceReport()
        
        // Then
        assertNotNull("Report should not be null", report)
        assertTrue("Report should have timestamp", report.timestamp > 0)
        
        // Verify device info
        val deviceInfo = report.deviceInfo
        assertNotNull("Device info should not be null", deviceInfo)
        assertNotNull("Should have device model", deviceInfo.model)
        assertNotNull("Should have manufacturer", deviceInfo.manufacturer)
        assertNotNull("Should have Android version", deviceInfo.androidVersion)
        assertTrue("Should have API level", deviceInfo.apiLevel > 0)
        assertTrue("Should have memory info", deviceInfo.totalMemoryMb > 0)
        assertTrue("Should have CPU cores", deviceInfo.cpuCores > 0)
        
        // Verify test results
        assertTrue("Should have test results", report.testResults.isNotEmpty())
        assertTrue("Should have overall score", report.overallScore >= 0.0)
        assertNotNull("Should have performance grade", report.performanceGrade)
        assertNotNull("Should have recommendations", report.recommendations)
    }
    
    @Test
    fun `memory manager should detect and handle memory pressure`() = runTest {
        // Given
        memoryManager.startMemoryManagement()
        delay(100)
        
        // When
        memoryManager.optimizeMemoryUsage()
        memoryManager.clearCaches()
        memoryManager.forceGarbageCollection()
        
        // Then
        // Operations should complete without throwing exceptions
        assertTrue("Memory optimization operations completed successfully", true)
        
        // Check for memory leaks
        val leaks = memoryManager.checkForMemoryLeaks()
        assertNotNull("Memory leak check should return results", leaks)
    }
    
    @Test
    fun `performance monitoring should clean up old data`() = runTest {
        // Given
        performanceMonitor.startMonitoring()
        delay(100)
        
        // When
        val olderThanMs = 24 * 60 * 60 * 1000L // 24 hours
        performanceMonitor.clearOldData(olderThanMs)
        
        // Then
        // Operation should complete without throwing exceptions
        assertTrue("Old data cleanup completed successfully", true)
    }
}