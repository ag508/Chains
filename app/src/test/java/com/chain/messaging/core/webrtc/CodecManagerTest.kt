package com.chain.messaging.core.webrtc

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CodecManagerTest {
    
    private lateinit var codecManager: CodecManagerImpl
    
    @Before
    fun setup() {
        codecManager = CodecManagerImpl()
    }
    
    @Test
    fun `getCurrentCodec should return default codec for new call`() = runTest {
        // Given
        val callId = "call_123"
        
        // When
        val codecInfo = codecManager.getCurrentCodec(callId)
        
        // Then
        assertEquals(callId, codecInfo.callId)
        assertEquals(SupportedCodec.VP8, codecInfo.videoCodec)
        assertEquals(SupportedCodec.OPUS, codecInfo.audioCodec)
    }
    
    @Test
    fun `getCodecSettingsForQuality should return HD settings`() {
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
    fun `getCodecSettingsForQuality should return STANDARD settings`() {
        // When
        val settings = codecManager.getCodecSettingsForQuality(VideoQuality.STANDARD)
        
        // Then
        assertEquals(640, settings.videoWidth)
        assertEquals(480, settings.videoHeight)
        assertEquals(30, settings.videoFrameRate)
        assertEquals(1000, settings.videoBitrate)
        assertEquals(64, settings.audioBitrate)
    }
    
    @Test
    fun `getCodecSettingsForQuality should return LOW settings`() {
        // When
        val settings = codecManager.getCodecSettingsForQuality(VideoQuality.LOW)
        
        // Then
        assertEquals(320, settings.videoWidth)
        assertEquals(240, settings.videoHeight)
        assertEquals(15, settings.videoFrameRate)
        assertEquals(500, settings.videoBitrate)
        assertEquals(32, settings.audioBitrate)
    }
    
    @Test
    fun `getCodecSettingsForQuality should return AUDIO_ONLY settings`() {
        // When
        val settings = codecManager.getCodecSettingsForQuality(VideoQuality.AUDIO_ONLY)
        
        // Then
        assertEquals(0, settings.videoWidth)
        assertEquals(0, settings.videoHeight)
        assertEquals(0, settings.videoFrameRate)
        assertEquals(0, settings.videoBitrate)
        assertEquals(64, settings.audioBitrate)
    }
    
    @Test
    fun `applyCodecSettings should update codec info`() = runTest {
        // Given
        val callId = "call_123"
        val newSettings = CodecSettings(
            videoWidth = 1920,
            videoHeight = 1080,
            videoFrameRate = 60,
            videoBitrate = 4000,
            audioBitrate = 256
        )
        
        // When
        codecManager.applyCodecSettings(callId, newSettings)
        
        // Then
        val codecInfo = codecManager.getCurrentCodec(callId)
        assertEquals(newSettings, codecInfo.settings)
    }
    
    @Test
    fun `getAvailableCodecs should return supported codecs`() {
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
    fun `selectOptimalCodec should return H264 for good conditions`() {
        // Given
        val goodConditions = NetworkConditions(
            bandwidth = 3000,
            packetLoss = 0.005,
            jitter = 5.0,
            rtt = 20L
        )
        
        // When
        val codec = codecManager.selectOptimalCodec(goodConditions)
        
        // Then
        assertEquals(SupportedCodec.H264, codec)
    }
    
    @Test
    fun `selectOptimalCodec should return VP8 for moderate conditions`() {
        // Given
        val moderateConditions = NetworkConditions(
            bandwidth = 1500,
            packetLoss = 0.02,
            jitter = 15.0,
            rtt = 50L
        )
        
        // When
        val codec = codecManager.selectOptimalCodec(moderateConditions)
        
        // Then
        assertEquals(SupportedCodec.VP8, codec)
    }
    
    @Test
    fun `selectOptimalCodec should return VP8 for poor conditions`() {
        // Given
        val poorConditions = NetworkConditions(
            bandwidth = 500,
            packetLoss = 0.1,
            jitter = 50.0,
            rtt = 200L
        )
        
        // When
        val codec = codecManager.selectOptimalCodec(poorConditions)
        
        // Then
        assertEquals(SupportedCodec.VP8, codec)
    }
}