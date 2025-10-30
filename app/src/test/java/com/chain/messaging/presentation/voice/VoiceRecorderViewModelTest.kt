package com.chain.messaging.presentation.voice

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.chain.messaging.core.audio.*
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
 * Unit tests for VoiceRecorderViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VoiceRecorderViewModelTest {
    
    private lateinit var voiceRecorder: VoiceRecorder
    private lateinit var voiceMessageProcessor: VoiceMessageProcessor
    private lateinit var viewModel: VoiceRecorderViewModel
    private lateinit var context: Context
    
    @Before
    fun setup() {
        voiceRecorder = mockk(relaxed = true)
        voiceMessageProcessor = mockk(relaxed = true)
        context = mockk(relaxed = true)
        
        // Mock recorder state flows
        every { voiceRecorder.recordingState } returns MutableStateFlow(RecordingState.IDLE)
        every { voiceRecorder.amplitude } returns MutableStateFlow(0)
        every { voiceRecorder.duration } returns MutableStateFlow(0L)
        
        viewModel = VoiceRecorderViewModel(voiceRecorder, voiceMessageProcessor)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `checkPermissions sets hasPermission to true when granted`() {
        // Given
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // When
        viewModel.checkPermissions(context)
        
        // Then
        assertTrue(viewModel.hasPermission.value)
    }
    
    @Test
    fun `checkPermissions sets hasPermission to false when denied`() {
        // Given
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_DENIED
        
        // When
        viewModel.checkPermissions(context)
        
        // Then
        assertFalse(viewModel.hasPermission.value)
    }
    
    @Test
    fun `startRecording sets error when permission not granted`() = runTest {
        // Given
        // hasPermission is false by default
        
        // When
        viewModel.startRecording()
        
        // Then
        assertEquals("Recording permission not granted", viewModel.error.value)
        verify(exactly = 0) { voiceRecorder.startRecording() }
    }
    
    @Test
    fun `startRecording calls voiceRecorder when permission granted`() = runTest {
        // Given
        viewModel.checkPermissions(context)
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_GRANTED
        viewModel.checkPermissions(context)
        
        val mockFile = mockk<File>()
        coEvery { voiceRecorder.startRecording() } returns Result.success(mockFile)
        
        // When
        viewModel.startRecording()
        
        // Then
        coVerify { voiceRecorder.startRecording() }
    }
    
    @Test
    fun `startRecording sets error when recorder fails`() = runTest {
        // Given
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
        } returns PackageManager.PERMISSION_GRANTED
        viewModel.checkPermissions(context)
        
        val exception = RuntimeException("Recording failed")
        coEvery { voiceRecorder.startRecording() } returns Result.failure(exception)
        
        // When
        viewModel.startRecording()
        
        // Then
        assertEquals("Failed to start recording: Recording failed", viewModel.error.value)
    }
    
    @Test
    fun `stopRecording processes recording and calls onComplete`() = runTest {
        // Given
        val mockFile = mockk<File>()
        val recordingResult = VoiceRecordingResult(mockFile, 5000L, 1024L)
        val mediaMessage = com.chain.messaging.domain.model.MediaMessage(
            uri = "/test/path",
            fileName = "voice.m4a",
            mimeType = "audio/mp4",
            fileSize = 1024L,
            duration = 5000L,
            isLocal = true
        )
        
        coEvery { voiceRecorder.stopRecording() } returns Result.success(recordingResult)
        coEvery { 
            voiceMessageProcessor.processVoiceMessage(recordingResult, CompressionLevel.MEDIUM) 
        } returns Result.success(mediaMessage)
        
        var completedPath: String? = null
        
        // When
        viewModel.stopRecording { path ->
            completedPath = path
        }
        
        // Then
        assertEquals("/test/path", completedPath)
        coVerify { voiceRecorder.stopRecording() }
        coVerify { voiceMessageProcessor.processVoiceMessage(recordingResult, CompressionLevel.MEDIUM) }
    }
    
    @Test
    fun `stopRecording sets error when recorder fails`() = runTest {
        // Given
        val exception = RuntimeException("Stop failed")
        coEvery { voiceRecorder.stopRecording() } returns Result.failure(exception)
        
        // When
        viewModel.stopRecording { }
        
        // Then
        assertEquals("Failed to stop recording: Stop failed", viewModel.error.value)
    }
    
    @Test
    fun `stopRecording sets error when processor fails`() = runTest {
        // Given
        val mockFile = mockk<File>()
        val recordingResult = VoiceRecordingResult(mockFile, 5000L, 1024L)
        val exception = RuntimeException("Processing failed")
        
        coEvery { voiceRecorder.stopRecording() } returns Result.success(recordingResult)
        coEvery { 
            voiceMessageProcessor.processVoiceMessage(recordingResult, CompressionLevel.MEDIUM) 
        } returns Result.failure(exception)
        
        // When
        viewModel.stopRecording { }
        
        // Then
        assertEquals("Failed to process recording: Processing failed", viewModel.error.value)
    }
    
    @Test
    fun `cancelRecording calls voiceRecorder cancelRecording`() = runTest {
        // Given
        coEvery { voiceRecorder.cancelRecording() } returns Result.success(Unit)
        
        // When
        viewModel.cancelRecording()
        
        // Then
        coVerify { voiceRecorder.cancelRecording() }
    }
    
    @Test
    fun `cancelRecording sets error when fails`() = runTest {
        // Given
        val exception = RuntimeException("Cancel failed")
        coEvery { voiceRecorder.cancelRecording() } returns Result.failure(exception)
        
        // When
        viewModel.cancelRecording()
        
        // Then
        assertEquals("Failed to cancel recording: Cancel failed", viewModel.error.value)
    }
    
    @Test
    fun `clearError sets error to null`() {
        // Given
        viewModel.startRecording() // This will set an error due to no permission
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.error.value)
    }
}