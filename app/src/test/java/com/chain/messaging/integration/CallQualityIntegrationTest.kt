package com.chain.messaging.integration

import com.chain.messaging.core.webrtc.*
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.webrtc.PeerConnection

/**
 * Integration tests for call quality optimization system
 * Tests the complete flow of quality monitoring, bandwidth tracking, codec selection, and recording detection
 */
class CallQualityIntegrationTest {
    
    private lateinit var callQualityManager: CallQualityManager
    private lateinit var bandwidthMonitor: BandwidthMonitorImpl
    private lateinit var codecManager: CodecManagerImpl
    private lateinit var callRecordingManager: CallRecordingManagerImpl
    private lateinit var screenshotDetector: ScreenshotDetector
    
    private val mockPeerConnection = mockk<PeerConnection>()
    private val mockContext = mockk<android.content.Context>()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Initialize real implementations
        bandwidthMonitor = BandwidthMonitorImpl()
        codecManager = CodecManagerImpl()
        callRecordingManager = CallRecordingManagerImpl(mockContext)
        screenshotDetector = ScreenshotDetector(mockContext)
        
        // Mock context dependencies
        every { mockContext.filesDir } returns mockk {
            every { absolutePath } returns "/mock/files"
        }
        every { mockContext.contentResolver } returns mockk()
        every { mockContext.getSystemService(any()) } returns mockk()
        
        callQualityManager = CallQualityManager(
            bandwidthMonitor,
            codecManager,
            callRecordingManager,
            screenshotDetector
        )
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `complete call quality monitoring flow should work end-to-end`() = runTest {
        // Given
        val callId = "integration_test_call"
        val isVideo = true
        
        // When - Start monitoring
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, isVideo)
        
        // Allow some time for monitoring to initialize
        delay(100)
        
        // Then - Verify monitoring is active
        val qualityMetrics = callQualityManager.getCurrentQualityMetrics(callId)
        assertNotNull("Quality metrics should be available", qualityMetrics)
        assertEquals("Call ID should match", callId, qualityMetrics!!.callId)
        
        // Verify bandwidth monitoring is active
        val bandwidthInfo = bandwidthMonitor.getCurrentBandwidth(callId)
        assertEquals("Bandwidth call ID should match", callId, bandwidthInfo.callId)
        assertTrue("Available bandwidth should be positive", bandwidthInfo.availableBandwidth > 0)
        
        // Clean up
        callQualityManager.stopQualityMonitoring(callId)
    }
    
    @Test
    fun `quality adjustment should trigger codec changes`() = runTest {
        // Given
        val callId = "quality_adjustment_test"
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, true)
        
        // When - Adjust quality to low
        callQualityManager.adjustCallQuality(callId, VideoQuality.LOW)
        
        // Then - Verify codec settings were applied
        val codecInfo = codecManager.getCurrentCodec(callId)
        assertEquals("Video width should be low quality", 320, codecInfo.settings.videoWidth)
        assertEquals("Video height should be low quality", 240, codecInfo.settings.videoHeight)
        assertEquals("Video bitrate should be low quality", 500, codecInfo.settings.videoBitrate)
        
        // Clean up
        callQualityManager.stopQualityMonitoring(callId)
    }
    
    @Test
    fun `bandwidth changes should trigger quality events`() = runTest {
        // Given
        val callId = "bandwidth_change_test"
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, true)
        
        // When - Start bandwidth monitoring (this happens automatically)
        delay(100) // Allow monitoring to start
        
        // Then - Should receive bandwidth updates
        val bandwidthUpdates = bandwidthMonitor.observeBandwidthChanges().take(1).toList()
        assertTrue("Should receive bandwidth updates", bandwidthUpdates.isNotEmpty())
        assertEquals("Update should be for correct call", callId, bandwidthUpdates.first().callId)
        
        // Clean up
        callQualityManager.stopQualityMonitoring(callId)
    }
    
    @Test
    fun `codec selection should adapt to network conditions`() = runTest {
        // Given
        val excellentConditions = NetworkConditions(3000, 0.005, 50, 10.0)
        val poorConditions = NetworkConditions(400, 0.08, 500, 80.0)
        
        // When & Then - Test excellent conditions
        val excellentCodec = codecManager.selectOptimalCodec(excellentConditions)
        assertEquals("Should select H264 for excellent conditions", SupportedCodec.H264, excellentCodec)
        
        // When & Then - Test poor conditions
        val poorCodec = codecManager.selectOptimalCodec(poorConditions)
        assertEquals("Should select OPUS (audio only) for very poor conditions", SupportedCodec.OPUS, poorCodec)
    }
    
    @Test
    fun `adaptive codec settings should respond to network degradation`() = runTest {
        // Given
        val callId = "adaptive_codec_test"
        val baseQuality = VideoQuality.HD
        val degradedConditions = NetworkConditions(800, 0.06, 400, 60.0)
        
        // When
        val baseSettings = codecManager.getCodecSettingsForQuality(baseQuality)
        val adaptiveSettings = codecManager.getAdaptiveCodecSettings(baseQuality, degradedConditions)
        
        // Then - Settings should be degraded due to poor conditions
        assertTrue("Video bitrate should be reduced", adaptiveSettings.videoBitrate < baseSettings.videoBitrate)
        assertTrue("Frame rate should be reduced", adaptiveSettings.videoFrameRate < baseSettings.videoFrameRate)
        assertTrue("Audio bitrate should be reduced", adaptiveSettings.audioBitrate < baseSettings.audioBitrate)
    }
    
    @Test
    fun `screenshot detection should work for video calls`() = runTest {
        // Given
        val callId = "screenshot_detection_test"
        
        // When - Start monitoring for video call
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, true)
        
        // Then - Screenshot detection should be active
        assertTrue("Screenshot detection should be active", screenshotDetector.isDetectionActive())
        
        // Clean up
        callQualityManager.stopQualityMonitoring(callId)
        assertFalse("Screenshot detection should be stopped", screenshotDetector.isDetectionActive())
    }
    
    @Test
    fun `audio-only calls should not enable screenshot detection`() = runTest {
        // Given
        val callId = "audio_only_test"
        
        // When - Start monitoring for audio-only call
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, false)
        
        // Then - Screenshot detection should not be active
        assertFalse("Screenshot detection should not be active for audio calls", screenshotDetector.isDetectionActive())
        
        // Clean up
        callQualityManager.stopQualityMonitoring(callId)
    }
    
    @Test
    fun `quality monitoring should emit appropriate events`() = runTest {
        // Given
        val callId = "event_emission_test"
        
        // When - Start monitoring
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, true)
        
        // Then - Should emit monitoring started event
        val events = callQualityManager.callQualityEvents.take(1).toList()
        assertTrue("Should emit events", events.isNotEmpty())
        assertTrue("First event should be monitoring started", events.first() is CallQualityEvent.MonitoringStarted)
        
        // When - Stop monitoring
        callQualityManager.stopQualityMonitoring(callId)
        
        // Then - Should emit monitoring stopped event
        val stopEvents = callQualityManager.callQualityEvents.take(1).toList()
        assertTrue("Should emit stop events", stopEvents.isNotEmpty())
        assertTrue("Should emit monitoring stopped", stopEvents.first() is CallQualityEvent.MonitoringStopped)
    }
    
    @Test
    fun `multiple concurrent calls should be handled correctly`() = runTest {
        // Given
        val call1Id = "concurrent_call_1"
        val call2Id = "concurrent_call_2"
        
        // When - Start monitoring for multiple calls
        callQualityManager.startQualityMonitoring(mockPeerConnection, call1Id, true)
        callQualityManager.startQualityMonitoring(mockPeerConnection, call2Id, false)
        
        // Then - Both calls should have metrics
        val metrics1 = callQualityManager.getCurrentQualityMetrics(call1Id)
        val metrics2 = callQualityManager.getCurrentQualityMetrics(call2Id)
        
        assertNotNull("Call 1 should have metrics", metrics1)
        assertNotNull("Call 2 should have metrics", metrics2)
        assertEquals("Call 1 ID should match", call1Id, metrics1!!.callId)
        assertEquals("Call 2 ID should match", call2Id, metrics2!!.callId)
        
        // Clean up
        callQualityManager.stopQualityMonitoring(call1Id)
        callQualityManager.stopQualityMonitoring(call2Id)
    }
    
    @Test
    fun `bandwidth estimation should return realistic values`() = runTest {
        // When
        val estimatedBandwidth = bandwidthMonitor.estimateAvailableBandwidth()
        
        // Then
        assertTrue("Bandwidth should be positive", estimatedBandwidth > 0)
        assertTrue("Bandwidth should be realistic (< 10 Mbps)", estimatedBandwidth < 10000)
        assertTrue("Bandwidth should be reasonable (> 50 kbps)", estimatedBandwidth > 50)
    }
    
    @Test
    fun `call recording permissions should be checked`() = runTest {
        // When
        val hasPermissions = callRecordingManager.hasRecordingPermissions()
        
        // Then - In test environment, this should return true (mocked)
        assertTrue("Should have recording permissions in test", hasPermissions)
    }
}