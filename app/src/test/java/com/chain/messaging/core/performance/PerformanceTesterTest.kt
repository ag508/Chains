package com.chain.messaging.core.performance

import android.content.Context
import android.view.WindowManager
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class PerformanceTesterTest {
    
    private lateinit var performanceTester: PerformanceTesterImpl
    private lateinit var mockContext: Context
    private lateinit var mockPerformanceMonitor: PerformanceMonitor
    private lateinit var mockMemoryManager: MemoryManager
    private lateinit var mockWindowManager: WindowManager
    
    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockPerformanceMonitor = mockk(relaxed = true)
        mockMemoryManager = mockk(relaxed = true)
        mockWindowManager = mockk(relaxed = true)
        
        every { mockContext.getSystemService(Context.WINDOW_SERVICE) } returns mockWindowManager
        
        performanceTester = PerformanceTesterImpl(mockContext, mockPerformanceMonitor, mockMemoryManager)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `benchmarkMessageThroughput should return successful result`() = runTest {
        // Given
        val messageCount = 100
        val concurrentUsers = 5
        
        // When
        val result = performanceTester.benchmarkMessageThroughput(messageCount, concurrentUsers)
        
        // Then
        assertEquals("Test name should match", "Message Throughput", result.testName)
        assertTrue("Should be successful", result.success)
        assertTrue("Should have metrics", result.metrics.isNotEmpty())
        assertTrue("Should have messages per second metric", 
            result.metrics.containsKey("messages_per_second"))
        assertTrue("Should have average latency metric", 
            result.metrics.containsKey("average_latency_ms"))
    }
    
    @Test
    fun `benchmarkMemoryUsage should return result with memory metrics`() = runTest {
        // Given
        val duration = 5000L // 5 seconds
        
        // When
        val result = performanceTester.benchmarkMemoryUsage(duration)
        
        // Then
        assertEquals("Test name should match", "Memory Usage", result.testName)
        assertTrue("Duration should be correct", result.duration >= duration)
    }
    
    @Test
    fun `benchmarkBatteryUsage should return battery metrics`() = runTest {
        // Given
        val duration = 3000L // 3 seconds
        
        // When
        val result = performanceTester.benchmarkBatteryUsage(duration)
        
        // Then
        assertEquals("Test name should match", "Battery Usage", result.testName)
        assertTrue("Should be successful", result.success)
        assertTrue("Should have battery metrics", result.metrics.isNotEmpty())
        assertTrue("Should have drain rate metric", 
            result.metrics.containsKey("estimated_drain_percent_per_hour"))
    }
    
    @Test
    fun `benchmarkNetworkPerformance should return network metrics`() = runTest {
        // Given
        val requestCount = 10
        
        // When
        val result = performanceTester.benchmarkNetworkPerformance(requestCount)
        
        // Then
        assertEquals("Test name should match", "Network Performance", result.testName)
        assertTrue("Should be successful", result.success)
        assertTrue("Should have network metrics", result.metrics.isNotEmpty())
        assertTrue("Should have average latency metric", 
            result.metrics.containsKey("average_latency_ms"))
        assertTrue("Should have requests per second metric", 
            result.metrics.containsKey("requests_per_second"))
    }
    
    @Test
    fun `runComprehensiveTest should run all benchmarks`() = runTest {
        // When
        val result = performanceTester.runComprehensiveTest()
        
        // Then
        assertTrue("Should have test results", result.testResults.isNotEmpty())
        assertEquals("Should have 4 test results", 4, result.testResults.size)
        assertTrue("Should have overall score", result.overallScore >= 0.0)
        assertNotNull("Should have summary", result.summary)
        assertNotNull("Should have recommendations", result.recommendations)
    }
    
    @Test
    fun `getBenchmarkProgress should return progress flow`() = runTest {
        // When
        val progressFlow = performanceTester.getBenchmarkProgress()
        
        // Then
        assertNotNull("Progress flow should not be null", progressFlow)
    }
    
    @Test
    fun `generatePerformanceReport should create comprehensive report`() = runTest {
        // When
        val report = performanceTester.generatePerformanceReport()
        
        // Then
        assertNotNull("Report should not be null", report)
        assertTrue("Should have timestamp", report.timestamp > 0)
        assertNotNull("Should have device info", report.deviceInfo)
        assertTrue("Should have test results", report.testResults.isNotEmpty())
        assertTrue("Should have overall score", report.overallScore >= 0.0)
        assertNotNull("Should have performance grade", report.performanceGrade)
        assertNotNull("Should have recommendations", report.recommendations)
    }
    
    @Test
    fun `comprehensive test should calculate correct overall score`() = runTest {
        // When
        val result = performanceTester.runComprehensiveTest()
        
        // Then
        val successfulTests = result.testResults.count { it.success }
        val expectedScore = (successfulTests.toDouble() / result.testResults.size) * 100.0
        assertEquals("Overall score should be calculated correctly", expectedScore, result.overallScore, 0.1)
    }
    
    @Test
    fun `performance report should have correct grade based on score`() = runTest {
        // When
        val report = performanceTester.generatePerformanceReport()
        
        // Then
        val expectedGrade = when {
            report.overallScore >= 90 -> PerformanceGrade.EXCELLENT
            report.overallScore >= 75 -> PerformanceGrade.GOOD
            report.overallScore >= 50 -> PerformanceGrade.FAIR
            report.overallScore >= 25 -> PerformanceGrade.POOR
            else -> PerformanceGrade.CRITICAL
        }
        assertEquals("Performance grade should match score", expectedGrade, report.performanceGrade)
    }
}