package com.chain.messaging.core.webrtc

import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class CallRecordingDetectorTest {
    
    private lateinit var callRecordingDetector: CallRecordingDetector
    private lateinit var context: Context
    private val mockAudioManager = mockk<AudioManager>()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        context = ApplicationProvider.getApplicationContext()
        
        // Mock system service
        mockkStatic(Context::class)
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns mockAudioManager
        
        // Mock AudioManager default behavior
        every { mockAudioManager.isWiredHeadsetOn } returns false
        every { mockAudioManager.isBluetoothA2dpOn } returns false
        every { mockAudioManager.isSpeakerphoneOn } returns false
        every { mockAudioManager.mode } returns AudioManager.MODE_NORMAL
        
        callRecordingDetector = CallRecordingDetector(context)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
        unmockkStatic(Context::class)
    }
    
    @Test
    fun `startRecordingDetection should initialize monitoring`() = runTest {
        // Given
        val callId = "recording_test_call"
        
        // When
        callRecordingDetector.startRecordingDetection(callId)
        
        // Then - should not throw exception
        // Monitoring state is internal, so we can't directly verify it
        // But we can verify it doesn't crash
    }
    
    @Test
    fun `stopRecordingDetection should stop monitoring`() = runTest {
        // Given
        val callId = "recording_test_call"
        callRecordingDetector.startRecordingDetection(callId)
        
        // When
        callRecordingDetector.stopRecordingDetection()
        
        // Then - should not throw exception
    }
    
    @Test
    fun `isRecordingDetected should return false by default`() = runTest {
        // When
        val isRecording = callRecordingDetector.isRecordingDetected()
        
        // Then
        assertFalse(isRecording)
    }
    
    @Test
    fun `isRecordingDetected should detect wired headset as potential recording`() = runTest {
        // Given
        every { mockAudioManager.isWiredHeadsetOn } returns true
        every { mockAudioManager.isSpeakerphoneOn } returns true
        
        // When
        val isRecording = callRecordingDetector.isRecordingDetected()
        
        // Then
        assertTrue(isRecording) // Unusual combination suggests recording
    }
    
    @Test
    fun `isRecordingDetected should not detect normal headset usage`() = runTest {
        // Given
        every { mockAudioManager.isWiredHeadsetOn } returns true
        every { mockAudioManager.isSpeakerphoneOn } returns false
        
        // When
        val isRecording = callRecordingDetector.isRecordingDetected()
        
        // Then
        assertFalse(isRecording) // Normal headset usage
    }
    
    @Test
    fun `isRecordingDetected should not detect normal bluetooth usage`() = runTest {
        // Given
        every { mockAudioManager.isBluetoothA2dpOn } returns true
        every { mockAudioManager.isSpeakerphoneOn } returns false
        
        // When
        val isRecording = callRecordingDetector.isRecordingDetected()
        
        // Then
        assertFalse(isRecording) // Normal Bluetooth usage
    }
    
    @Test
    fun `recording events should be emitted when detection starts`() = runTest {
        // Given
        val callId = "event_test_call"
        every { mockAudioManager.isWiredHeadsetOn } returns true
        every { mockAudioManager.isBluetoothA2dpOn } returns false
        
        // When
        callRecordingDetector.startRecordingDetection(callId)
        
        // Then - Events flow should be available
        val eventsFlow = callRecordingDetector.recordingEvents
        assertNotNull(eventsFlow)
    }
    
    @Test
    fun `multiple start calls should not cause issues`() = runTest {
        // Given
        val callId = "multiple_start_call"
        
        // When
        callRecordingDetector.startRecordingDetection(callId)
        callRecordingDetector.startRecordingDetection(callId) // Second call
        
        // Then - should not throw exception
        callRecordingDetector.stopRecordingDetection()
    }
    
    @Test
    fun `stop without start should not cause issues`() = runTest {
        // When & Then - should not throw exception
        callRecordingDetector.stopRecordingDetection()
    }
    
    @Test
    fun `audio routing changes should be detected`() = runTest {
        // Given
        val callId = "audio_routing_call"
        
        // Initially normal
        every { mockAudioManager.isWiredHeadsetOn } returns false
        every { mockAudioManager.isSpeakerphoneOn } returns false
        
        callRecordingDetector.startRecordingDetection(callId)
        assertFalse(callRecordingDetector.isRecordingDetected())
        
        // When - Audio routing changes to suspicious pattern
        every { mockAudioManager.isWiredHeadsetOn } returns true
        every { mockAudioManager.isSpeakerphoneOn } returns true
        
        // Then
        assertTrue(callRecordingDetector.isRecordingDetected())
    }
    
    @Test
    fun `communication mode should not trigger false positive`() = runTest {
        // Given
        every { mockAudioManager.mode } returns AudioManager.MODE_IN_COMMUNICATION
        every { mockAudioManager.isWiredHeadsetOn } returns false
        every { mockAudioManager.isSpeakerphoneOn } returns false
        
        // When
        val isRecording = callRecordingDetector.isRecordingDetected()
        
        // Then
        assertFalse(isRecording) // MODE_IN_COMMUNICATION is normal for calls
    }
}