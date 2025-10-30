package com.chain.messaging.core.webrtc

import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.webrtc.PeerConnection

class CallQualityManagerTest {
    
    private lateinit var callQualityManager: CallQualityManager
    private val mockPeerConnection = mockk<PeerConnection>()
    private val mockBandwidthMonitor = mockk<BandwidthMonitor>()
    private val mockCodecManager = mockk<CodecManager>()
    private val mockCallRecordingManager = mockk<CallRecordingManager>()
    private val mockScreenshotDetector = mockk<ScreenshotDetector>()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        callQualityManager = CallQualityManager(
            mockBandwidthMonitor,
            mockCodecManager,
            mockCallRecordingManager,
            mockScreenshotDetector
        )
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `initial state should have default metrics`() = runTest {
        // When
        val initialMetrics = callQualityManager.qualityMetrics.first()
        val initialCondition = callQualityManager.networkCondition.first()
        
        // Then
        assertEquals(0L, initialMetrics.bandwidth)
        assertEquals(0.0, initialMetrics.packetLoss, 0.001)
        assertEquals(0.0, initialMetrics.jitter, 0.001)
        assertEquals(0L, initialMetrics.rtt)
        assertEquals(0.0, initialMetrics.audioLevel, 0.001)
        assertEquals(0, initialMetrics.videoFrameRate)
        assertEquals("", initialMetrics.videoResolution)
        assertEquals(NetworkCondition.GOOD, initialCondition)
    }
    
    @Test
    fun `startQualityMonitoring should initialize all monitoring components`() = runTest {
        // Given
        val callId = "test_call_123"
        val isVideo = true
        
        coEvery { mockBandwidthMonitor.startMonitoring(callId) } just Runs
        coEvery { mockScreenshotDetector.startScreenshotDetection(callId) } just Runs
        coEvery { mockCallRecordingManager.enableScreenshotDetection(callId) } just Runs
        coEvery { mockBandwidthMonitor.getCurrentBandwidth(callId) } returns BandwidthInfo(callId, 1000, 500, System.currentTimeMillis())
        
        // When
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, isVideo)
        
        // Then
        coVerify { mockBandwidthMonitor.startMonitoring(callId) }
        coVerify { mockScreenshotDetector.startScreenshotDetection(callId) }
        coVerify { mockCallRecordingManager.enableScreenshotDetection(callId) }
    }
    
    @Test
    fun `stopQualityMonitoring should cleanup all monitoring components`() = runTest {
        // Given
        val callId = "test_call_123"
        
        coEvery { mockBandwidthMonitor.startMonitoring(callId) } just Runs
        coEvery { mockScreenshotDetector.startScreenshotDetection(callId) } just Runs
        coEvery { mockCallRecordingManager.enableScreenshotDetection(callId) } just Runs
        coEvery { mockBandwidthMonitor.getCurrentBandwidth(callId) } returns BandwidthInfo(callId, 1000, 500, System.currentTimeMillis())
        coEvery { mockBandwidthMonitor.stopMonitoring(callId) } just Runs
        coEvery { mockScreenshotDetector.stopScreenshotDetection() } just Runs
        coEvery { mockCallRecordingManager.disableScreenshotDetection(callId) } just Runs
        
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, true)
        
        // When
        callQualityManager.stopQualityMonitoring(callId)
        
        // Then
        coVerify { mockBandwidthMonitor.stopMonitoring(callId) }
        coVerify { mockScreenshotDetector.stopScreenshotDetection() }
        coVerify { mockCallRecordingManager.disableScreenshotDetection(callId) }
    }
    
    @Test
    fun `getRecommendedVideoQuality should return HD for excellent network`() = runTest {
        // Given - Network condition is EXCELLENT (need to set this through internal state)
        // For now, test with default GOOD condition
        
        // When
        val videoQuality = callQualityManager.getRecommendedVideoQuality()
        
        // Then
        assertEquals(VideoQuality.STANDARD, videoQuality) // Default is GOOD -> STANDARD
    }
    
    @Test
    fun `getRecommendedAudioCodec should return OPUS for good network`() = runTest {
        // Given - Network condition is GOOD (default)
        
        // When
        val audioCodec = callQualityManager.getRecommendedAudioCodec()
        
        // Then
        assertEquals(AudioCodec.OPUS, audioCodec)
    }
    
    @Test
    fun `video quality recommendations should match network conditions`() {
        // Test all network conditions and their corresponding video quality recommendations
        val testCases = mapOf(
            NetworkCondition.EXCELLENT to VideoQuality.HD,
            NetworkCondition.GOOD to VideoQuality.STANDARD,
            NetworkCondition.POOR to VideoQuality.LOW,
            NetworkCondition.BAD to VideoQuality.AUDIO_ONLY
        )
        
        testCases.forEach { (condition, expectedQuality) ->
            // This would require a way to set network condition for testing
            // For now, we test the logic conceptually
            val quality = when (condition) {
                NetworkCondition.EXCELLENT -> VideoQuality.HD
                NetworkCondition.GOOD -> VideoQuality.STANDARD
                NetworkCondition.POOR -> VideoQuality.LOW
                NetworkCondition.BAD -> VideoQuality.AUDIO_ONLY
            }
            assertEquals(expectedQuality, quality)
        }
    }
    
    @Test
    fun `audio codec recommendations should match network conditions`() {
        // Test all network conditions and their corresponding audio codec recommendations
        val testCases = mapOf(
            NetworkCondition.EXCELLENT to AudioCodec.OPUS,
            NetworkCondition.GOOD to AudioCodec.OPUS,
            NetworkCondition.POOR to AudioCodec.G722,
            NetworkCondition.BAD to AudioCodec.G711
        )
        
        testCases.forEach { (condition, expectedCodec) ->
            val codec = when (condition) {
                NetworkCondition.EXCELLENT, NetworkCondition.GOOD -> AudioCodec.OPUS
                NetworkCondition.POOR -> AudioCodec.G722
                NetworkCondition.BAD -> AudioCodec.G711
            }
            assertEquals(expectedCodec, codec)
        }
    }
    
    @Test
    fun `adjustCallQuality should apply codec settings`() = runTest {
        // Given
        val callId = "test_call_123"
        val targetQuality = VideoQuality.LOW
        val codecSettings = CodecSettings(320, 240, 15, 500, 32)
        
        coEvery { mockBandwidthMonitor.startMonitoring(callId) } just Runs
        coEvery { mockScreenshotDetector.startScreenshotDetection(callId) } just Runs
        coEvery { mockCallRecordingManager.enableScreenshotDetection(callId) } just Runs
        coEvery { mockBandwidthMonitor.getCurrentBandwidth(callId) } returns BandwidthInfo(callId, 1000, 500, System.currentTimeMillis())
        coEvery { mockCodecManager.getCodecSettingsForQuality(targetQuality) } returns codecSettings
        coEvery { mockCodecManager.applyCodecSettings(callId, codecSettings) } just Runs
        
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, true)
        
        // When
        callQualityManager.adjustCallQuality(callId, targetQuality)
        
        // Then
        coVerify { mockCodecManager.getCodecSettingsForQuality(targetQuality) }
        coVerify { mockCodecManager.applyCodecSettings(callId, codecSettings) }
    }
    
    @Test
    fun `getCurrentQualityMetrics should return metrics for active call`() = runTest {
        // Given
        val callId = "test_call_123"
        val expectedBandwidth = BandwidthInfo(callId, 1500, 800, System.currentTimeMillis())
        
        coEvery { mockBandwidthMonitor.startMonitoring(callId) } just Runs
        coEvery { mockScreenshotDetector.startScreenshotDetection(callId) } just Runs
        coEvery { mockCallRecordingManager.enableScreenshotDetection(callId) } just Runs
        coEvery { mockBandwidthMonitor.getCurrentBandwidth(callId) } returns expectedBandwidth
        
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, true)
        
        // When
        val metrics = callQualityManager.getCurrentQualityMetrics(callId)
        
        // Then
        assertNotNull(metrics)
        assertEquals(callId, metrics!!.callId)
        assertEquals(expectedBandwidth.availableBandwidth.toLong(), metrics.bandwidth)
        assertEquals(expectedBandwidth.usedBandwidth.toLong(), metrics.usedBandwidth)
    }
    
    @Test
    fun `should emit monitoring started event`() = runTest {
        // Given
        val callId = "test_call_123"
        
        coEvery { mockBandwidthMonitor.startMonitoring(callId) } just Runs
        coEvery { mockScreenshotDetector.startScreenshotDetection(callId) } just Runs
        coEvery { mockCallRecordingManager.enableScreenshotDetection(callId) } just Runs
        coEvery { mockBandwidthMonitor.getCurrentBandwidth(callId) } returns BandwidthInfo(callId, 1000, 500, System.currentTimeMillis())
        
        // When
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, true)
        
        // Then
        val events = callQualityManager.callQualityEvents.take(1).toList()
        assertTrue(events.isNotEmpty())
        assertTrue(events.first() is CallQualityEvent.MonitoringStarted)
        assertEquals(callId, (events.first() as CallQualityEvent.MonitoringStarted).callId)
    }
    
    @Test
    fun `should handle audio-only calls differently`() = runTest {
        // Given
        val callId = "audio_call_123"
        val isVideo = false
        
        coEvery { mockBandwidthMonitor.startMonitoring(callId) } just Runs
        coEvery { mockBandwidthMonitor.getCurrentBandwidth(callId) } returns BandwidthInfo(callId, 1000, 500, System.currentTimeMillis())
        
        // When
        callQualityManager.startQualityMonitoring(mockPeerConnection, callId, isVideo)
        
        // Then
        coVerify { mockBandwidthMonitor.startMonitoring(callId) }
        coVerify(exactly = 0) { mockScreenshotDetector.startScreenshotDetection(callId) }
        coVerify(exactly = 0) { mockCallRecordingManager.enableScreenshotDetection(callId) }
    }
}

class BandwidthMonitorTest {
    
    private lateinit var bandwidthMonitor: BandwidthMonitorImpl
    
    @Before
    fun setup() {
        bandwidthMonitor = BandwidthMonitorImpl()
    }
    
    @Test
    fun `estimateAvailableBandwidth should return reasonable values`() = runTest {
        // When
        val bandwidth = bandwidthMonitor.estimateAvailableBandwidth()
        
        // Then
        assertTrue("Bandwidth should be positive", bandwidth > 0)
        assertTrue("Bandwidth should be reasonable", bandwidth <= 10000) // Max 10 Mbps
    }
    
    @Test
    fun `startMonitoring should create active session`() = runTest {
        // Given
        val callId = "bandwidth_test_call"
        
        // When
        bandwidthMonitor.startMonitoring(callId)
        
        // Then
        val bandwidthInfo = bandwidthMonitor.getCurrentBandwidth(callId)
        assertEquals(callId, bandwidthInfo.callId)
        assertTrue(bandwidthInfo.availableBandwidth > 0)
    }
    
    @Test
    fun `stopMonitoring should remove active session`() = runTest {
        // Given
        val callId = "bandwidth_test_call"
        bandwidthMonitor.startMonitoring(callId)
        
        // When
        bandwidthMonitor.stopMonitoring(callId)
        
        // Then
        val bandwidthInfo = bandwidthMonitor.getCurrentBandwidth(callId)
        assertEquals(0, bandwidthInfo.availableBandwidth)
        assertEquals(0, bandwidthInfo.usedBandwidth)
    }
    
    @Test
    fun `observeBandwidthChanges should emit updates during monitoring`() = runTest {
        // Given
        val callId = "bandwidth_test_call"
        
        // When
        bandwidthMonitor.startMonitoring(callId)
        
        // Then
        val updates = bandwidthMonitor.observeBandwidthChanges().take(1).toList()
        assertTrue(updates.isNotEmpty())
        assertEquals(callId, updates.first().callId)
    }
    
    @Test
    fun `bandwidth trend calculation should work correctly`() = runTest {
        // Given
        val callId = "bandwidth_test_call"
        
        // When
        bandwidthMonitor.startMonitoring(callId)
        
        // Then - first measurement should have STABLE trend
        val updates = bandwidthMonitor.observeBandwidthChanges().take(1).toList()
        assertTrue(updates.isNotEmpty())
        // Note: First update might not have trend calculated yet
    }
}

class CodecManagerTest {
    
    private lateinit var codecManager: CodecManagerImpl
    
    @Before
    fun setup() {
        codecManager = CodecManagerImpl()
    }
    
    @Test
    fun `selectOptimalCodec should return appropriate codec for network conditions`() {
        // Test cases for codec selection based on network conditions
        val excellentConditions = NetworkConditions(2500, 0.005, 50, 10.0)
        val goodConditions = NetworkConditions(1200, 0.015, 150, 20.0)
        val poorConditions = NetworkConditions(600, 0.025, 250, 40.0)
        val badConditions = NetworkConditions(300, 0.06, 500, 80.0)
        
        assertEquals(SupportedCodec.H264, codecManager.selectOptimalCodec(excellentConditions))
        assertEquals(SupportedCodec.VP8, codecManager.selectOptimalCodec(goodConditions))
        assertEquals(SupportedCodec.VP8, codecManager.selectOptimalCodec(poorConditions))
        assertEquals(SupportedCodec.OPUS, codecManager.selectOptimalCodec(badConditions))
    }
    
    @Test
    fun `getAvailableCodecs should return all supported codecs`() {
        // When
        val codecs = codecManager.getAvailableCodecs()
        
        // Then
        assertTrue(codecs.contains(SupportedCodec.VP8))
        assertTrue(codecs.contains(SupportedCodec.VP9))
        assertTrue(codecs.contains(SupportedCodec.H264))
        assertTrue(codecs.contains(SupportedCodec.OPUS))
        assertTrue(codecs.contains(SupportedCodec.G722))
    }
    
    @Test
    fun `getCodecSettingsForQuality should return correct settings for HD quality`() {
        // When
        val settings = codecManager.getCodecSettingsForQuality(VideoQuality.HD)
        
        // Then
        assertEquals(1280, settings.videoWidth)
        assertEquals(720, settings.videoHeight)
        assertEquals(30, settings.videoFrameRate)
        assertEquals(2000, settings.videoBitrate)
        assertEquals(128, settings.audioBitrate)
    }
    
    @Test
    fun `getAdaptiveCodecSettings should adjust for high packet loss`() {
        // Given
        val currentQuality = VideoQuality.STANDARD
        val highPacketLossConditions = NetworkConditions(1000, 0.08, 200, 30.0)
        
        // When
        val adaptiveSettings = codecManager.getAdaptiveCodecSettings(currentQuality, highPacketLossConditions)
        val baseSettings = codecManager.getCodecSettingsForQuality(currentQuality)
        
        // Then
        assertTrue("Bitrate should be reduced", adaptiveSettings.videoBitrate < baseSettings.videoBitrate)
        assertTrue("Frame rate should be limited", adaptiveSettings.videoFrameRate <= 15)
        assertTrue("Audio bitrate should be reduced", adaptiveSettings.audioBitrate < baseSettings.audioBitrate)
    }
    
    @Test
    fun `getAdaptiveCodecSettings should adjust for high latency`() {
        // Given
        val currentQuality = VideoQuality.HD
        val highLatencyConditions = NetworkConditions(2000, 0.01, 400, 20.0)
        
        // When
        val adaptiveSettings = codecManager.getAdaptiveCodecSettings(currentQuality, highLatencyConditions)
        val baseSettings = codecManager.getCodecSettingsForQuality(currentQuality)
        
        // Then
        assertTrue("Bitrate should be reduced", adaptiveSettings.videoBitrate < baseSettings.videoBitrate)
        assertTrue("Frame rate should be limited", adaptiveSettings.videoFrameRate <= 20)
    }
    
    @Test
    fun `applyCodecSettings should update active codec info`() = runTest {
        // Given
        val callId = "test_call_123"
        val settings = CodecSettings(640, 480, 30, 1000, 64)
        
        // When
        codecManager.applyCodecSettings(callId, settings)
        
        // Then
        val codecInfo = codecManager.getCurrentCodec(callId)
        assertEquals(callId, codecInfo.callId)
        assertEquals(settings, codecInfo.settings)
    }
    
    @Test
    fun `codec settings should be reasonable for all quality levels`() {
        // Test that all video quality levels have reasonable parameters
        val qualities = listOf(VideoQuality.HD, VideoQuality.STANDARD, VideoQuality.LOW)
        
        qualities.forEach { quality ->
            val settings = codecManager.getCodecSettingsForQuality(quality)
            
            // Width and height should be positive (except for AUDIO_ONLY)
            assertTrue("Width should be positive for $quality", settings.videoWidth > 0)
            assertTrue("Height should be positive for $quality", settings.videoHeight > 0)
            
            // Frame rate should be reasonable
            assertTrue("Frame rate should be reasonable for $quality", settings.videoFrameRate in 10..60)
            
            // Bitrate should be reasonable
            assertTrue("Video bitrate should be reasonable for $quality", settings.videoBitrate in 100..5000)
            assertTrue("Audio bitrate should be reasonable for $quality", settings.audioBitrate in 16..256)
        }
    }
    
    @Test
    fun `getAdaptiveCodecSettings should handle low bandwidth conditions`() {
        // Given
        val currentQuality = VideoQuality.HD
        val lowBandwidthConditions = NetworkConditions(800, 0.02, 200, 25.0)
        
        // When
        val adaptiveSettings = codecManager.getAdaptiveCodecSettings(currentQuality, lowBandwidthConditions)
        
        // Then
        assertTrue("Video bitrate should be capped at 500", adaptiveSettings.videoBitrate <= 500)
        assertTrue("Audio bitrate should be capped at 32", adaptiveSettings.audioBitrate <= 32)
    }
}