package com.chain.messaging.core.audio

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
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
 * Unit tests for VoiceRecorder
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VoiceRecorderTest {
    
    private lateinit var context: Context
    private lateinit var voiceRecorder: VoiceRecorder
    private lateinit var mockFile: File
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockFile = mockk(relaxed = true)
        
        // Mock file system
        every { context.filesDir } returns mockFile
        every { mockFile.exists() } returns true
        every { mockFile.mkdirs() } returns true
        
        voiceRecorder = VoiceRecorder(context)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `hasRecordingPermission returns true when permission granted`() {
        // Given
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // When
        val hasPermission = voiceRecorder.hasRecordingPermission()
        
        // Then
        assertTrue(hasPermission)
    }
    
    @Test
    fun `hasRecordingPermission returns false when permission denied`() {
        // Given
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_DENIED
        
        // When
        val hasPermission = voiceRecorder.hasRecordingPermission()
        
        // Then
        assertFalse(hasPermission)
    }
    
    @Test
    fun `startRecording fails when permission not granted`() = runTest {
        // Given
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_DENIED
        
        // When
        val result = voiceRecorder.startRecording()
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
    
    @Test
    fun `recording state is initially idle`() = runTest {
        // When
        val initialState = voiceRecorder.recordingState.first()
        
        // Then
        assertEquals(RecordingState.IDLE, initialState)
    }
    
    @Test
    fun `amplitude is initially zero`() = runTest {
        // When
        val initialAmplitude = voiceRecorder.amplitude.first()
        
        // Then
        assertEquals(0, initialAmplitude)
    }
    
    @Test
    fun `duration is initially zero`() = runTest {
        // When
        val initialDuration = voiceRecorder.duration.first()
        
        // Then
        assertEquals(0L, initialDuration)
    }
    
    @Test
    fun `stopRecording fails when not recording`() = runTest {
        // Given - recorder is in idle state
        
        // When
        val result = voiceRecorder.stopRecording()
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
    
    @Test
    fun `cancelRecording fails when not recording`() = runTest {
        // Given - recorder is in idle state
        
        // When
        val result = voiceRecorder.cancelRecording()
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}