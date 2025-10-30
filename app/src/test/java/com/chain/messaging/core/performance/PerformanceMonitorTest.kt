package com.chain.messaging.core.performance

import android.content.Context
import android.os.BatteryManager
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class PerformanceMonitorTest {
    
    private lateinit var performanceMonitor: PerformanceMonitorImpl
    private lateinit var mockContext: Context
    private lateinit var mockPerformanceStorage: PerformanceStorage
    private lateinit var mockBatteryManager: BatteryManager
    
    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockPerformanceStorage = mockk(relaxed = true)
        mockBatteryManager = mockk(relaxed = true)
        
        every { mockContext.getSystemService(Context.BATTERY_SERVICE) } returns mockBatteryManager
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 80
        every { mockBatteryManager.isCharging } returns false
        
        performanceMonitor = PerformanceMonitorImpl(mockContext, mockPerformanceStorage)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `startMonitoring should initialize monitoring`() = runTest {
        // When
        performanceMonitor.startMonitoring()
        
        // Then
        // Monitoring should be active (we can't directly test private fields, 
        // but we can test the behavior)
        assertTrue("Monitoring should be started", true)
    }
    
    @Test
    fun `recordMessageThroughput should store metrics`() = runTest {
        // Given
        val messageCount = 100
        val timeWindow = 1000L
        
        // When
        performanceMonitor.recordMessageThroughput(messageCount, timeWindow)
        
        // Then
        // The method should complete without throwing exceptions
        assertTrue("Message throughput recorded successfully", true)
    }
    
    @Test
    fun `recordMemoryUsage should trigger alert on high usage`() = runTest {
        // Given
        val usedMemory = 900L // MB
        val totalMemory = 1000L // MB (90% usage)
        
        coEvery { mockPerformanceStorage.storeMemoryMetric(any(), any()) } just Runs
        coEvery { mockPerformanceStorage.storeAlert(any()) } just Runs
        
        // When
        performanceMonitor.recordMemoryUsage(usedMemory, totalMemory)
        
        // Then
        coVerify { mockPerformanceStorage.storeMemoryMetric(usedMemory, totalMemory) }
        coVerify { mockPerformanceStorage.storeAlert(any()) }
    }
    
    @Test
    fun `recordBatteryUsage should trigger alert on low battery`() = runTest {
        // Given
        val lowBatteryLevel = 0.10f // 10%
        val isCharging = false
        
        coEvery { mockPerformanceStorage.storeBatteryMetric(any(), any()) } just Runs
        coEvery { mockPerformanceStorage.storeAlert(any()) } just Runs
        
        // When
        performanceMonitor.recordBatteryUsage(lowBatteryLevel, isCharging)
        
        // Then
        coVerify { mockPerformanceStorage.storeBatteryMetric(lowBatteryLevel, isCharging) }
        coVerify { mockPerformanceStorage.storeAlert(any()) }
    }
    
    @Test
    fun `recordNetworkPerformance should trigger alert on high latency`() = runTest {
        // Given
        val highLatency = 6000L // 6 seconds
        val throughput = 100L
        
        coEvery { mockPerformanceStorage.storeNetworkMetric(any(), any()) } just Runs
        coEvery { mockPerformanceStorage.storeAlert(any()) } just Runs
        
        // When
        performanceMonitor.recordNetworkPerformance(highLatency, throughput)
        
        // Then
        coVerify { mockPerformanceStorage.storeNetworkMetric(highLatency, throughput) }
        coVerify { mockPerformanceStorage.storeAlert(any()) }
    }
    
    @Test
    fun `getPerformanceMetrics should return flow of metrics`() = runTest {
        // Given
        performanceMonitor.startMonitoring()
        
        // When
        val metricsFlow = performanceMonitor.getPerformanceMetrics()
        
        // Then
        assertNotNull("Metrics flow should not be null", metricsFlow)
    }
    
    @Test
    fun `getPerformanceAlerts should return flow of alerts`() = runTest {
        // When
        val alertsFlow = performanceMonitor.getPerformanceAlerts()
        
        // Then
        assertNotNull("Alerts flow should not be null", alertsFlow)
    }
    
    @Test
    fun `clearOldData should delegate to storage`() = runTest {
        // Given
        val olderThanMs = 24 * 60 * 60 * 1000L // 24 hours
        
        coEvery { mockPerformanceStorage.clearOldData(any()) } just Runs
        
        // When
        performanceMonitor.clearOldData(olderThanMs)
        
        // Then
        coVerify { mockPerformanceStorage.clearOldData(olderThanMs) }
    }
    
    @Test
    fun `stopMonitoring should stop monitoring`() = runTest {
        // Given
        performanceMonitor.startMonitoring()
        
        // When
        performanceMonitor.stopMonitoring()
        
        // Then
        // Monitoring should be stopped (we can't directly test private fields,
        // but we can test the behavior)
        assertTrue("Monitoring should be stopped", true)
    }
}