package com.chain.messaging.core.audio

import android.content.Context
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit tests for VoiceMessageProcessor
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VoiceMessageProcessorTest {
    
    private lateinit var context: Context
    private lateinit var voiceMessageProcessor: VoiceMessageProcessor
    private lateinit var mockFile: File
    private lateinit var recordingResult: VoiceRecordingResult
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockFile = mockk(relaxed = true)
        
        every { mockFile.exists() } returns true
        every { mockFile.absolutePath } returns "/test/path/voice.m4a"
        every { mockFile.name } returns "voice.m4a"
        every { mockFile.length() } returns 1024L
        every { mockFile.parent } returns "/test/path"
        
        recordingResult = VoiceRecordingResult(
            file = mockFile,
            duration = 5000L,
            fileSize = 1024L
        )
        
        voiceMessageProcessor = VoiceMessageProcessor(context)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `processVoiceMessage with no compression returns success`() = runTest {
        // When
        val result = voiceMessageProcessor.processVoiceMessage(
            recordingResult,
            CompressionLevel.NONE
        )
        
        // Then
        assertTrue(result.isSuccess)
        val mediaMessage = result.getOrNull()
        assertNotNull(mediaMessage)
        assertEquals("audio/mp4", mediaMessage?.mimeType)
        assertEquals(mockFile.name, mediaMessage?.fileName)
        assertTrue(mediaMessage?.isLocal == true)
    }
    
    @Test
    fun `processVoiceMessage with medium compression returns success`() = runTest {
        // Given
        val compressedFile = mockk<File>(relaxed = true)
        every { compressedFile.exists() } returns true
        every { compressedFile.absolutePath } returns "/test/path/compressed_voice.m4a"
        every { compressedFile.name } returns "compressed_voice.m4a"
        every { compressedFile.length() } returns 512L
        every { mockFile.copyTo(any<File>(), any<Boolean>()) } returns compressedFile
        
        // When
        val result = voiceMessageProcessor.processVoiceMessage(
            recordingResult,
            CompressionLevel.MEDIUM
        )
        
        // Then
        assertTrue(result.isSuccess)
        val mediaMessage = result.getOrNull()
        assertNotNull(mediaMessage)
        assertEquals("audio/mp4", mediaMessage?.mimeType)
    }
    
    @Test
    fun `loadWaveformData returns empty list when file not found`() = runTest {
        // Given
        val nonExistentPath = "/non/existent/path.m4a"
        
        // When
        val waveformData = voiceMessageProcessor.loadWaveformData(nonExistentPath)
        
        // Then
        assertTrue(waveformData.isEmpty())
    }
    
    @Test
    fun `optimizeForSending returns original when file size acceptable`() = runTest {
        // Given
        val mediaMessage = com.chain.messaging.domain.model.MediaMessage(
            uri = mockFile.absolutePath,
            fileName = mockFile.name,
            mimeType = "audio/mp4",
            fileSize = 1024L, // Small file
            duration = 5000L,
            isLocal = true
        )
        
        // When
        val result = voiceMessageProcessor.optimizeForSending(mediaMessage)
        
        // Then
        assertTrue(result.isSuccess)
        val optimizedMessage = result.getOrNull()
        assertEquals(mediaMessage, optimizedMessage)
    }
    
    @Test
    fun `optimizeForSending compresses when file too large`() = runTest {
        // Given
        val largeFile = mockk<File>(relaxed = true)
        every { largeFile.exists() } returns true
        every { largeFile.absolutePath } returns "/test/path/large_voice.m4a"
        every { largeFile.name } returns "large_voice.m4a"
        every { largeFile.length() } returns 15 * 1024 * 1024L // 15MB
        
        val mediaMessage = com.chain.messaging.domain.model.MediaMessage(
            uri = largeFile.absolutePath,
            fileName = largeFile.name,
            mimeType = "audio/mp4",
            fileSize = 15 * 1024 * 1024L,
            duration = 60000L,
            isLocal = true
        )
        
        // When
        val result = voiceMessageProcessor.optimizeForSending(mediaMessage)
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `optimizeForSending fails when file does not exist`() = runTest {
        // Given
        every { mockFile.exists() } returns false
        
        val mediaMessage = com.chain.messaging.domain.model.MediaMessage(
            uri = mockFile.absolutePath,
            fileName = mockFile.name,
            mimeType = "audio/mp4",
            fileSize = 1024L,
            duration = 5000L,
            isLocal = true
        )
        
        // When
        val result = voiceMessageProcessor.optimizeForSending(mediaMessage)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `getQualityInfo returns correct quality for high bitrate`() = runTest {
        // Given
        val mediaMessage = com.chain.messaging.domain.model.MediaMessage(
            uri = mockFile.absolutePath,
            fileName = mockFile.name,
            mimeType = "audio/mp4",
            fileSize = 1024L,
            duration = 5000L,
            isLocal = true
        )
        
        // When
        val qualityInfo = voiceMessageProcessor.getQualityInfo(mediaMessage)
        
        // Then
        assertNotNull(qualityInfo)
        assertEquals(1024L, qualityInfo.fileSize)
        assertEquals(5000L, qualityInfo.duration)
    }
}