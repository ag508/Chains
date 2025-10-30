package com.chain.messaging.presentation.voice

import com.chain.messaging.core.audio.PlaybackState
import com.chain.messaging.core.audio.VoiceMessageProcessor
import com.chain.messaging.core.audio.VoicePlayer
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit tests for VoicePlayerViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VoicePlayerViewModelTest {
    
    private lateinit var voicePlayer: VoicePlayer
    private lateinit var voiceMessageProcessor: VoiceMessageProcessor
    private lateinit var viewModel: VoicePlayerViewModel
    private lateinit var mockFile: File
    
    @Before
    fun setup() {
        voicePlayer = mockk(relaxed = true)
        voiceMessageProcessor = mockk(relaxed = true)
        mockFile = mockk(relaxed = true)
        
        // Mock player state flows
        every { voicePlayer.playbackState } returns MutableStateFlow(PlaybackState.IDLE)
        every { voicePlayer.currentPosition } returns MutableStateFlow(0L)
        every { voicePlayer.duration } returns MutableStateFlow(0L)
        every { voicePlayer.currentPlayingFile } returns MutableStateFlow(null)
        
        every { mockFile.absolutePath } returns "/test/path/voice.m4a"
        
        viewModel = VoicePlayerViewModel(voicePlayer, voiceMessageProcessor)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `play calls voicePlayer play`() = runTest {
        // Given
        coEvery { voicePlayer.play(mockFile) } returns Result.success(Unit)
        
        // When
        viewModel.play(mockFile)
        
        // Then
        coVerify { voicePlayer.play(mockFile) }
    }
    
    @Test
    fun `play sets error when fails`() = runTest {
        // Given
        val exception = RuntimeException("Play failed")
        coEvery { voicePlayer.play(mockFile) } returns Result.failure(exception)
        
        // When
        viewModel.play(mockFile)
        
        // Then
        assertEquals("Failed to play voice message: Play failed", viewModel.error.value)
    }
    
    @Test
    fun `pause calls voicePlayer pause`() = runTest {
        // Given
        coEvery { voicePlayer.pause() } returns Result.success(Unit)
        
        // When
        viewModel.pause()
        
        // Then
        coVerify { voicePlayer.pause() }
    }
    
    @Test
    fun `pause sets error when fails`() = runTest {
        // Given
        val exception = RuntimeException("Pause failed")
        coEvery { voicePlayer.pause() } returns Result.failure(exception)
        
        // When
        viewModel.pause()
        
        // Then
        assertEquals("Failed to pause playback: Pause failed", viewModel.error.value)
    }
    
    @Test
    fun `resume calls voicePlayer resume`() = runTest {
        // Given
        coEvery { voicePlayer.resume() } returns Result.success(Unit)
        
        // When
        viewModel.resume()
        
        // Then
        coVerify { voicePlayer.resume() }
    }
    
    @Test
    fun `resume sets error when fails`() = runTest {
        // Given
        val exception = RuntimeException("Resume failed")
        coEvery { voicePlayer.resume() } returns Result.failure(exception)
        
        // When
        viewModel.resume()
        
        // Then
        assertEquals("Failed to resume playback: Resume failed", viewModel.error.value)
    }
    
    @Test
    fun `stop calls voicePlayer stop`() = runTest {
        // Given
        coEvery { voicePlayer.stop() } returns Result.success(Unit)
        
        // When
        viewModel.stop()
        
        // Then
        coVerify { voicePlayer.stop() }
    }
    
    @Test
    fun `stop sets error when fails`() = runTest {
        // Given
        val exception = RuntimeException("Stop failed")
        coEvery { voicePlayer.stop() } returns Result.failure(exception)
        
        // When
        viewModel.stop()
        
        // Then
        assertEquals("Failed to stop playback: Stop failed", viewModel.error.value)
    }
    
    @Test
    fun `seekTo calls voicePlayer seekTo`() = runTest {
        // Given
        val position = 5000L
        coEvery { voicePlayer.seekTo(position) } returns Result.success(Unit)
        
        // When
        viewModel.seekTo(position)
        
        // Then
        coVerify { voicePlayer.seekTo(position) }
    }
    
    @Test
    fun `seekTo sets error when fails`() = runTest {
        // Given
        val position = 5000L
        val exception = RuntimeException("Seek failed")
        coEvery { voicePlayer.seekTo(position) } returns Result.failure(exception)
        
        // When
        viewModel.seekTo(position)
        
        // Then
        assertEquals("Failed to seek: Seek failed", viewModel.error.value)
    }
    
    @Test
    fun `isPlayingFile calls voicePlayer isPlaying`() {
        // Given
        val filePath = "/test/path/voice.m4a"
        every { voicePlayer.isPlaying(any()) } returns true
        
        // When
        val result = viewModel.isPlayingFile(filePath)
        
        // Then
        assertTrue(result)
        verify { voicePlayer.isPlaying(any()) }
    }
    
    @Test
    fun `loadWaveformData calls processor loadWaveformData`() = runTest {
        // Given
        val filePath = "/test/path/voice.m4a"
        val waveformData = listOf(0.5f, 0.8f, 0.3f)
        coEvery { voiceMessageProcessor.loadWaveformData(filePath) } returns waveformData
        
        // When
        viewModel.loadWaveformData(filePath)
        
        // Then
        coVerify { voiceMessageProcessor.loadWaveformData(filePath) }
        assertEquals(waveformData, viewModel.waveformData.value)
    }
    
    @Test
    fun `setPlaybackSpeed calls voicePlayer setPlaybackSpeed`() = runTest {
        // Given
        val speed = 1.5f
        coEvery { voicePlayer.setPlaybackSpeed(speed) } returns Result.success(Unit)
        
        // When
        viewModel.setPlaybackSpeed(speed)
        
        // Then
        coVerify { voicePlayer.setPlaybackSpeed(speed) }
    }
    
    @Test
    fun `setPlaybackSpeed sets error when fails`() = runTest {
        // Given
        val speed = 1.5f
        val exception = RuntimeException("Speed change failed")
        coEvery { voicePlayer.setPlaybackSpeed(speed) } returns Result.failure(exception)
        
        // When
        viewModel.setPlaybackSpeed(speed)
        
        // Then
        assertEquals("Failed to set playback speed: Speed change failed", viewModel.error.value)
    }
    
    @Test
    fun `getPlaybackSpeed calls voicePlayer getPlaybackSpeed`() {
        // Given
        every { voicePlayer.getPlaybackSpeed() } returns 1.0f
        
        // When
        val speed = viewModel.getPlaybackSpeed()
        
        // Then
        assertEquals(1.0f, speed)
        verify { voicePlayer.getPlaybackSpeed() }
    }
    
    @Test
    fun `clearError sets error to null`() {
        // Given
        viewModel.play(mockFile) // This might set an error
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.error.value)
    }
}