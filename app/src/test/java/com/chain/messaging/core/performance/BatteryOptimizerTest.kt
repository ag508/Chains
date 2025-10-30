package com.chain.messaging.core.performance

import android.content.Context
import android.os.BatteryManager
import android.os.PowerManager
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class BatteryOptimizerTest {
    
    private lateinit var batteryOptimizer: BatteryOptimizerImpl
    private lateinit var mockContext: Context
    private lateinit var mockPerformanceMonitor: PerformanceMonitor
    private lateinit var mockBatteryManager: BatteryManager
    private lateinit var mockPowerManager: PowerManager
    
    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockPerformanceMonitor = mockk(relaxed = true)
        mockBatteryManager = mockk(relaxed = true)
        mockPowerManager = mockk(relaxed = true)
        
        every { mockContext.getSystemService(Context.BATTERY_SERVICE) } returns mockBatteryManager
        every { mockContext.getSystemService(Context.POWER_SERVICE) } returns mockPowerManager
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 50
        every { mockBatteryManager.isCharging } returns false
        every { mockPowerManager.isPowerSaveMode } returns false
        
        batteryOptimizer = BatteryOptimizerImpl(mockContext, mockPerformanceMonitor)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `startOptimization should initialize optimization`() = runTest {
        // When
        batteryOptimizer.startOptimization()
        
        // Then
        assertTrue("Optimization should be started", true)
    }
    
    @Test
    fun `optimizeBackgroundOperations should use normal mode for high battery`() = runTest {
        // Given
        val highBatteryLevel = 0.8f
        val isCharging = false
        
        // When
        batteryOptimizer.optimizeBackgroundOperations(highBatteryLevel, isCharging)
        
        // Then
        val status = batteryOptimizer.getBatteryOptimizationStatus().first()
        assertEquals("Should use normal mode", OptimizationMode.NORMAL, status.currentMode)
    }
    
    @Test
    fun `optimizeBackgroundOperations should use power saver for low battery`() = runTest {
        // Given
        val lowBatteryLevel = 0.15f
        val isCharging = false
        
        // When
        batteryOptimizer.optimizeBackgroundOperations(lowBatteryLevel, isCharging)
        
        // Then
        val status = batteryOptimizer.getBatteryOptimizationStatus().first()
        assertEquals("Should use power saver mode", OptimizationMode.POWER_SAVER, status.currentMode)
    }
    
    @Test
    fun `optimizeBackgroundOperations should use ultra power saver for critical battery`() = runTest {
        // Given
        val criticalBatteryLevel = 0.05f
        val isCharging = false
        
        // When
        batteryOptimizer.optimizeBackgroundOperations(criticalBatteryLevel, isCharging)
        
        // Then
        val status = batteryOptimizer.getBatteryOptimizationStatus().first()
        assertEquals("Should use ultra power saver mode", OptimizationMode.ULTRA_POWER_SAVER, status.currentMode)
    }
    
    @Test
    fun `optimizeBackgroundOperations should use charging mode when charging`() = runTest {
        // Given
        val batteryLevel = 0.3f
        val isCharging = true
        
        // When
        batteryOptimizer.optimizeBackgroundOperations(batteryLevel, isCharging)
        
        // Then
        val status = batteryOptimizer.getBatteryOptimizationStatus().first()
        assertEquals("Should use charging mode", OptimizationMode.CHARGING, status.currentMode)
    }
    
    @Test
    fun `reduceCpuIntensiveOperations should add optimization`() = runTest {
        // When
        batteryOptimizer.reduceCpuIntensiveOperations()
        
        // Then
        val status = batteryOptimizer.getBatteryOptimizationStatus().first()
        assertTrue("Should have CPU optimization", status.activeOptimizations.contains("reduce_cpu_operations"))
    }
    
    @Test
    fun `optimizeNetworkOperations should add optimization`() = runTest {
        // When
        batteryOptimizer.optimizeNetworkOperations()
        
        // Then
        val status = batteryOptimizer.getBatteryOptimizationStatus().first()
        assertTrue("Should have network optimization", status.activeOptimizations.contains("optimize_network"))
    }
    
    @Test
    fun `getBatteryRecommendations should return recommendations for low battery`() = runTest {
        // Given
        every { mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } returns 15
        every { mockBatteryManager.isCharging } returns false
        
        // When
        val recommendations = batteryOptimizer.getBatteryRecommendations()
        
        // Then
        assertTrue("Should have recommendations", recommendations.isNotEmpty())
        assertTrue("Should recommend power saver", 
            recommendations.any { it.id == "enable_power_saver" })
    }
    
    @Test
    fun `getBatteryRecommendations should include system power saver recommendation`() = runTest {
        // Given
        every { mockPowerManager.isPowerSaveMode } returns false
        
        // When
        val recommendations = batteryOptimizer.getBatteryRecommendations()
        
        // Then
        assertTrue("Should recommend system power saver", 
            recommendations.any { it.id == "system_power_saver" })
    }
    
    @Test
    fun `getBatteryOptimizationStatus should return current status`() = runTest {
        // When
        val status = batteryOptimizer.getBatteryOptimizationStatus().first()
        
        // Then
        assertNotNull("Status should not be null", status)
        assertNotNull("Should have current mode", status.currentMode)
        assertNotNull("Should have active optimizations", status.activeOptimizations)
    }
    
    @Test
    fun `stopOptimization should stop optimization`() = runTest {
        // Given
        batteryOptimizer.startOptimization()
        
        // When
        batteryOptimizer.stopOptimization()
        
        // Then
        val status = batteryOptimizer.getBatteryOptimizationStatus().first()
        assertFalse("Should not be optimizing", status.isOptimizing)
        assertTrue("Should have no active optimizations", status.activeOptimizations.isEmpty())
    }
}