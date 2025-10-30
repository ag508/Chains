package com.chain.messaging.core.audio

import android.content.Context
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit tests for VoicePlayer
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VoicePlayerTest {
    
    private lateinit var context: Context
    private lateinit var voicePlayer: VoicePlayer
    private lateinit var mockFile: File
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockFile = mockk(relaxed = true)
        
        every { mockFile.exists() } returns true
        every { mockFile.absolutePath } returns "/test/path/audio.m4a"
        
        voicePlayer = VoicePlayer(context)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `playback state is initially idle`() = runTest {
        // When
        val initialState = voicePlayer.playbackState.first()
        
        // Then
        assertEquals(PlaybackState.IDLE, initialState)
    }
    
    @Test
    fun `current position is initially zero`() = runTest {
        // When
        val initialPosition = voicePlayer.currentPosition.first()
        
        // Then
        assertEquals(0L, initialPosition)
    }
    
    @Test
    fun `duration is initially zero`() = runTest {
        // When
        val initialDuration = voicePlayer.duration.first()
        
        // Then
        assertEquals(0L, initialDuration)
    }
    
    @Test
    fun `current playing file is initially null`() = runTest {
        // When
        val initialFile = voicePlayer.currentPlayingFile.first()
        
        // Then
        assertNull(initialFile)
    }
    
    @Test
    fun `play fails when file does not exist`() = runTest {
        // Given
        every { mockFile.exists() } returns false
        
        // When
        val result = voicePlayer.play(mockFile)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `pause fails when not playing`() = runTest {
        // Given - player is in idle state
        
        // When
        val result = voicePlayer.pause()
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
    
    @Test
    fun `resume fails when not paused`() = runTest {
        // Given - player is in idle state
        
        // When
        val result = voicePlayer.resume()
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
    
    @Test
    fun `seekTo fails when no media player initialized`() = runTest {
        // Given - no media player
        
        // When
        val result = voicePlayer.seekTo(5000L)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
    
    @Test
    fun `getPlaybackSpeed returns normal speed`() {
        // When
        val speed = voicePlayer.getPlaybackSpeed()
        
        // Then
        assertEquals(1.0f, speed)
    }
    
    @Test
    fun `setPlaybackSpeed returns success`() = runTest {
        // When
        val result = voicePlayer.setPlaybackSpeed(1.5f)
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `isPlaying returns false for different file`() {
        // Given
        val differentFile = mockk<File>()
        every { differentFile.absolutePath } returns "/different/path/audio.m4a"
        
        // When
        val isPlaying = voicePlayer.isPlaying(differentFile)
        
        // Then
        assertFalse(isPlaying)
    }
    
    @Test
    fun `stop returns success`() = runTest {
        // When
        val result = voicePlayer.stop()
        
        // Then
        assertTrue(result.isSuccess)
    }
}