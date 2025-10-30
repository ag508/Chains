package com.chain.messaging.integration

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.chain.messaging.core.audio.*
import com.chain.messaging.domain.model.MediaMessage
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Integration tests for voice message functionality
 * Tests the complete flow from recording to playback
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VoiceMessageIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var voiceRecorder: VoiceRecorder
    private lateinit var voicePlayer: VoicePlayer
    private lateinit var voiceMessageProcessor: VoiceMessageProcessor
    private lateinit var mockFile: File
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockFile = mockk(relaxed = true)
        
        // Mock file system
        every { context.filesDir } returns mockFile
        every { mockFile.exists() } returns true
        every { mockFile.mkdirs() } returns true
        every { mockFile.absolutePath } returns "/test/audio"
        every { mockFile.name } returns "voice_123456789.m4a"
        every { mockFile.length() } returns 1024L
        every { mockFile.parent } returns "/test"
        
        // Mock permissions
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_GRANTED
        
        voiceRecorder = VoiceRecorder(context)
        voicePlayer = VoicePlayer(context)
        voiceMessageProcessor = VoiceMessageProcessor(context)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `complete voice message flow - record, process, and prepare for playback`() = runTest {
        // Test recording permission check
        assertTrue(voiceRecorder.hasRecordingPermission())
        
        // Test initial states
        assertEquals(RecordingState.IDLE, voiceRecorder.recordingState.value)
        assertEquals(PlaybackState.IDLE, voicePlayer.playbackState.value)
        
        // Test voice message processing
        val recordingResult = VoiceRecordingResult(
            file = mockFile,
            duration = 5000L,
            fileSize = 1024L
        )
        
        val processResult = voiceMessageProcessor.processVoiceMessage(
            recordingResult,
            CompressionLevel.MEDIUM
        )
        
        assertTrue(processResult.isSuccess)
        val mediaMessage = processResult.getOrNull()
        assertNotNull(mediaMessage)
        assertEquals("audio/mp4", mediaMessage?.mimeType)
        assertEquals(mockFile.name, mediaMessage?.fileName)
        assertEquals(1024L, mediaMessage?.fileSize)
        assertTrue(mediaMessage?.isLocal == true)
    }
    
    @Test
    fun `voice message compression levels work correctly`() = runTest {
        val recordingResult = VoiceRecordingResult(
            file = mockFile,
            duration = 10000L,
            fileSize = 2048L
        )
        
        // Test different compression levels
        val compressionLevels = listOf(
            CompressionLevel.NONE,
            CompressionLevel.LOW,
            CompressionLevel.MEDIUM,
            CompressionLevel.HIGH
        )
        
        compressionLevels.forEach { level ->
            val result = voiceMessageProcessor.processVoiceMessage(recordingResult, level)
            assertTrue("Compression level $level should succeed", result.isSuccess)
            
            val mediaMessage = result.getOrNull()
            assertNotNull("MediaMessage should not be null for $level", mediaMessage)
            assertEquals("audio/mp4", mediaMessage?.mimeType)
        }
    }
    
    @Test
    fun `voice message optimization handles different file sizes`() = runTest {
        // Test small file (should not be compressed)
        val smallMediaMessage = MediaMessage(
            uri = mockFile.absolutePath,
            fileName = mockFile.name,
            mimeType = "audio/mp4",
            fileSize = 1024L, // 1KB
            duration = 5000L,
            isLocal = true
        )
        
        val smallResult = voiceMessageProcessor.optimizeForSending(smallMediaMessage)
        assertTrue(smallResult.isSuccess)
        assertEquals(smallMediaMessage, smallResult.getOrNull())
        
        // Test large file (should be compressed)
        val largeFile = mockk<File>(relaxed = true)
        every { largeFile.exists() } returns true
        every { largeFile.absolutePath } returns "/test/large_voice.m4a"
        every { largeFile.name } returns "large_voice.m4a"
        every { largeFile.length() } returns 15 * 1024 * 1024L // 15MB
        
        val largeMediaMessage = MediaMessage(
            uri = largeFile.absolutePath,
            fileName = largeFile.name,
            mimeType = "audio/mp4",
            fileSize = 15 * 1024 * 1024L,
            duration = 60000L,
            isLocal = true
        )
        
        val largeResult = voiceMessageProcessor.optimizeForSending(largeMediaMessage)
        assertTrue(largeResult.isSuccess)
        // The result should be different (compressed) for large files
        assertNotNull(largeResult.getOrNull())
    }
    
    @Test
    fun `voice quality assessment works correctly`() = runTest {
        val mediaMessage = MediaMessage(
            uri = mockFile.absolutePath,
            fileName = mockFile.name,
            mimeType = "audio/mp4",
            fileSize = 1024L,
            duration = 5000L,
            isLocal = true
        )
        
        val qualityInfo = voiceMessageProcessor.getQualityInfo(mediaMessage)
        
        assertNotNull(qualityInfo)
        assertEquals(1024L, qualityInfo.fileSize)
        assertEquals(5000L, qualityInfo.duration)
        assertTrue(qualityInfo.compressionRatio >= 0)
    }
    
    @Test
    fun `waveform data generation and loading works`() = runTest {
        val filePath = "/test/path/voice.m4a"
        
        // Test loading waveform data (should return empty for non-existent file)
        val waveformData = voiceMessageProcessor.loadWaveformData(filePath)
        
        // Since the file doesn't actually exist, it should return empty list
        assertTrue(waveformData.isEmpty())
    }
    
    @Test
    fun `voice player handles file validation correctly`() = runTest {
        // Test with non-existent file
        val nonExistentFile = mockk<File>()
        every { nonExistentFile.exists() } returns false
        
        val playResult = voicePlayer.play(nonExistentFile)
        assertTrue(playResult.isFailure)
        assertTrue(playResult.exceptionOrNull() is IllegalArgumentException)
        
        // Test with existing file
        every { mockFile.exists() } returns true
        // Note: In a real test, MediaPlayer would need to be mocked more extensively
        // For now, we just verify the file existence check works
    }
    
    @Test
    fun `voice recorder permission handling works correctly`() {
        // Test with permission granted
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_GRANTED
        
        assertTrue(voiceRecorder.hasRecordingPermission())
        
        // Test with permission denied
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_DENIED
        
        assertFalse(voiceRecorder.hasRecordingPermission())
    }
    
    @Test
    fun `voice message states are properly managed`() = runTest {
        // Test initial states
        assertEquals(RecordingState.IDLE, voiceRecorder.recordingState.value)
        assertEquals(0, voiceRecorder.amplitude.value)
        assertEquals(0L, voiceRecorder.duration.value)
        
        assertEquals(PlaybackState.IDLE, voicePlayer.playbackState.value)
        assertEquals(0L, voicePlayer.currentPosition.value)
        assertEquals(0L, voicePlayer.duration.value)
        assertNull(voicePlayer.currentPlayingFile.value)
        
        // Test playback speed
        assertEquals(1.0f, voicePlayer.getPlaybackSpeed())
        
        val speedResult = voicePlayer.setPlaybackSpeed(1.5f)
        assertTrue(speedResult.isSuccess)
    }
}