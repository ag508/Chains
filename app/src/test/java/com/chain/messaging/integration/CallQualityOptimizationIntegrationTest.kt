package com.chain.messaging.integration

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chain.messaging.core.webrtc.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import javax.inject.Inject

/**
 * Integration tests for call quality optimization
 * Tests automatic quality adjustment, bandwidth monitoring, codec selection,
 * and call recording/screenshot detection
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CallQualityOptimizationIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var callQualityManager: CallQualityManager
    
    @Inject
    lateinit var bandwidthMonitor: BandwidthMonitor
    
    @Inject
    lateinit var codecSelector: CodecSelector
    
    @Inject
    lateinit var callRecordingDetector: CallRecordingDetector
    
    @Inject
    lateinit var screenshotDetector: ScreenshotDetector
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        hiltRule.inject()
    }
    
    @Test
    fun testCallQualityManagerInitialization() = runTest {
        // When
        val initialMetrics = callQualityManager.qualityMetrics.first()
        val initialCondition = callQualityManager.networkCondition.first()
        
        // Then
        assertNotNull(initialMetrics)
        assertEquals(0L, initialMetrics.bandwidth)
        assertEquals(0.0, initialMetrics.packetLoss, 0.001)
        assertEquals(NetworkCondition.GOOD, initialCondition)
    }
    
    @Test
    fun testBandwidthMonitorInitialization() = runTest {
        // When
        val initialStats = bandwidthMonitor.bandwidthStats.first()
        
        // Then
        assertNotNull(initialStats)
        assertEquals(0L, initialStats.availableBandwidth)
        assertEquals(0L, initialStats.usedBandwidth)
        assertTrue(initialStats.timestamp > 0)
    }
    
    @Test
    fun testCodecSelectorRecommendations() {
        // Test video codec selection for different network conditions
        NetworkCondition.values().forEach { condition ->
            val videoCodec = codecSelector.selectVideoCodec(condition)
            val audioCodec = codecSelector.selectAudioCodec(condition)
            
            assertNotNull(videoCodec)
            assertNotNull(audioCodec)
            assertTrue(videoCodec.isNotEmpty())
            assertTrue(audioCodec.isNotEmpty())
        }
    }
    
    @Test
    fun testVideoEncodingParametersForAllQualities() {
        // Test that all video quality levels have valid parameters
        VideoQuality.values().forEach { quality ->
            val params = codecSelector.getVideoEncodingParams(quality)
            
            assertNotNull(params)
            
            if (quality != VideoQuality.AUDIO_ONLY) {
                assertTrue("Width should be positive for $quality", params.width > 0)
                assertTrue("Height should be positive for $quality", params.height > 0)
                assertTrue("Frame rate should be positive for $quality", params.frameRate > 0)
                assertTrue("Bitrate should be positive for $quality", params.bitrate > 0)
            } else {
                assertEquals("Audio only should have zero video params", 0, params.width)
                assertEquals("Audio only should have zero video params", 0, params.height)
                assertEquals("Audio only should have zero video params", 0, params.frameRate)
                assertEquals("Audio only should have zero video params", 0, params.bitrate)
            }
        }
    }
    
    @Test
    fun testCallRecordingDetectorLifecycle() = runTest {
        // Given
        val callId = "recording_integration_test"
        
        // When - Start detection
        callRecordingDetector.startRecordingDetection(callId)
        
        // Then - Should not crash
        val isRecording = callRecordingDetector.isRecordingDetected()
        assertNotNull(isRecording)
        
        // When - Stop detection
        callRecordingDetector.stopRecordingDetection()
        
        // Then - Should not crash
    }
    
    @Test
    fun testScreenshotDetectorLifecycle() = runTest {
        // Given
        val callId = "screenshot_integration_test"
        
        // When - Start detection
        screenshotDetector.startScreenshotDetection(callId)
        
        // Then
        assertTrue(screenshotDetector.isDetectionActive())
        
        // When - Stop detection
        screenshotDetector.stopScreenshotDetection()
        
        // Then
        assertFalse(screenshotDetector.isDetectionActive())
    }
    
    @Test
    fun testQualityRecommendationsConsistency() = runTest {
        // Test that quality recommendations are consistent across components
        val qualityManager = callQualityManager
        val codecSel = codecSelector
        
        // Get recommendations for different network conditions
        NetworkCondition.values().forEach { condition ->
            // This would require a way to set network condition in quality manager
            // For now, test that the codec selector provides consistent recommendations
            val videoCodec = codecSel.selectVideoCodec(condition)
            val audioCodec = codecSel.selectAudioCodec(condition)
            
            // Verify recommendations make sense for the network condition
            when (condition) {
                NetworkCondition.EXCELLENT, NetworkCondition.GOOD -> {
                    assertEquals("opus", audioCodec)
                    assertTrue(videoCodec in listOf("H264", "VP8"))
                }
                NetworkCondition.POOR -> {
                    assertEquals("G722", audioCodec)
                    assertEquals("VP8", videoCodec)
                }
                NetworkCondition.BAD -> {
                    assertEquals("PCMU", audioCodec)
                    assertEquals("VP8", videoCodec)
                }
            }
        }
    }
    
    @Test
    fun testBandwidthMonitoringFlow() = runTest {
        // Given
        val callId = "bandwidth_flow_test"
        val testStats = BandwidthStats(
            availableBandwidth = 1000000L,
            usedBandwidth = 500000L,
            uploadSpeed = 100000L,
            downloadSpeed = 200000L
        )
        
        // When - Start monitoring
        bandwidthMonitor.startMonitoring(callId)
        
        // Update stats
        bandwidthMonitor.updateBandwidthStats(testStats)
        
        // Then
        val currentStats = bandwidthMonitor.bandwidthStats.first()
        assertEquals(testStats.availableBandwidth, currentStats.availableBandwidth)
        assertEquals(testStats.usedBandwidth, currentStats.usedBandwidth)
        
        val availableBandwidth = bandwidthMonitor.getAvailableBandwidth()
        assertEquals(testStats.availableBandwidth, availableBandwidth)
        
        // Cleanup
        bandwidthMonitor.stopMonitoring(callId)
    }
    
    @Test
    fun testCallQualityMonitoringFlow() = runTest {
        // Given
        val callId = "quality_monitoring_test"
        
        // When - Start quality monitoring (with mock peer connection)
        // Note: This would require a real or mock PeerConnection in practice
        callQualityManager.startQualityMonitoring(mockk(), callId)
        
        // Then - Should not crash
        val metrics = callQualityManager.qualityMetrics.first()
        assertNotNull(metrics)
        
        // Cleanup
        callQualityManager.stopQualityMonitoring(callId)
    }
    
    @Test
    fun testRecordingDetectionEvents() = runTest {
        // Given
        val callId = "recording_events_test"
        
        // When
        callRecordingDetector.startRecordingDetection(callId)
        val eventsFlow = callRecordingDetector.recordingEvents
        
        // Then
        assertNotNull(eventsFlow)
        
        // Cleanup
        callRecordingDetector.stopRecordingDetection()
    }
    
    @Test
    fun testScreenshotDetectionEvents() = runTest {
        // Given
        val callId = "screenshot_events_test"
        
        // When
        screenshotDetector.startScreenshotDetection(callId)
        val eventsFlow = screenshotDetector.screenshotEvents
        
        // Then
        assertNotNull(eventsFlow)
        
        // Cleanup
        screenshotDetector.stopScreenshotDetection()
    }
    
    @Test
    fun testMultipleCallQualityMonitoring() = runTest {
        // Given
        val callId1 = "quality_call_1"
        val callId2 = "quality_call_2"
        
        // When - Start monitoring multiple calls
        callQualityManager.startQualityMonitoring(mockk(), callId1)
        callQualityManager.startQualityMonitoring(mockk(), callId2)
        
        // Then - Should handle multiple calls
        val metrics = callQualityManager.qualityMetrics.first()
        assertNotNull(metrics)
        
        // Cleanup
        callQualityManager.stopQualityMonitoring(callId1)
        callQualityManager.stopQualityMonitoring(callId2)
    }
    
    @Test
    fun testQualityOptimizationIntegration() = runTest {
        // Test integration between different quality optimization components
        
        // Given
        val callId = "integration_test_call"
        
        // When - Start all monitoring
        callQualityManager.startQualityMonitoring(mockk(), callId)
        bandwidthMonitor.startMonitoring(callId)
        callRecordingDetector.startRecordingDetection(callId)
        screenshotDetector.startScreenshotDetection(callId)
        
        // Update bandwidth stats to simulate network changes
        val highBandwidthStats = BandwidthStats(
            availableBandwidth = 5000000L, // 5 Mbps
            usedBandwidth = 1000000L,      // 1 Mbps
            uploadSpeed = 500000L,
            downloadSpeed = 2000000L
        )
        bandwidthMonitor.updateBandwidthStats(highBandwidthStats)
        
        // Then - Get quality recommendations
        val videoQuality = callQualityManager.getRecommendedVideoQuality()
        val audioCodec = callQualityManager.getRecommendedAudioCodec()
        
        assertNotNull(videoQuality)
        assertNotNull(audioCodec)
        
        // Verify high bandwidth allows for good quality
        assertTrue("High bandwidth should allow good quality", 
            videoQuality in listOf(VideoQuality.HD, VideoQuality.STANDARD))
        assertEquals("High bandwidth should use OPUS codec", AudioCodec.OPUS, audioCodec)
        
        // Cleanup
        callQualityManager.stopQualityMonitoring(callId)
        bandwidthMonitor.stopMonitoring(callId)
        callRecordingDetector.stopRecordingDetection()
        screenshotDetector.stopScreenshotDetection()
    }
    
    @Test
    fun testLowBandwidthOptimization() = runTest {
        // Test quality optimization under low bandwidth conditions
        
        // Given
        val callId = "low_bandwidth_test"
        
        // When - Simulate low bandwidth
        val lowBandwidthStats = BandwidthStats(
            availableBandwidth = 100000L, // 100 Kbps
            usedBandwidth = 80000L,       // 80 Kbps
            uploadSpeed = 50000L,
            downloadSpeed = 50000L
        )
        
        bandwidthMonitor.startMonitoring(callId)
        bandwidthMonitor.updateBandwidthStats(lowBandwidthStats)
        
        // Then - Quality should be optimized for low bandwidth
        val videoCodec = codecSelector.selectVideoCodec(NetworkCondition.BAD)
        val audioCodec = codecSelector.selectAudioCodec(NetworkCondition.BAD)
        val videoParams = codecSelector.getVideoEncodingParams(VideoQuality.LOW)
        
        assertEquals("VP8", videoCodec) // Lower bandwidth codec
        assertEquals("PCMU", audioCodec) // Lower bandwidth codec
        assertTrue("Low quality should have reduced bitrate", videoParams.bitrate <= 500)
        
        // Cleanup
        bandwidthMonitor.stopMonitoring(callId)
    }
}