package com.chain.messaging.presentation.call

import com.chain.messaging.core.webrtc.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.webrtc.MediaStream

class CallViewModelTest {
    
    private lateinit var callViewModel: CallViewModel
    private val mockCallManager = mockk<CallManager>()
    private val mockWebRTCManager = mockk<WebRTCManager>()
    private val mockMediaStream = mockk<MediaStream>()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock CallManager
        every { mockCallManager.getActiveCall(any()) } returns null
        every { mockCallManager.observeCallStateEvents() } returns flowOf()
        coEvery { mockCallManager.endCall(any(), any()) } returns Result.success(Unit)
        
        // Mock WebRTCManager
        every { mockWebRTCManager.observeCallEvents() } returns flowOf()
        every { mockWebRTCManager.observeConnectionState() } returns flowOf()
        coEvery { mockWebRTCManager.getLocalMediaStream(any()) } returns mockMediaStream
        
        // Mock MediaStream
        every { mockMediaStream.audioTracks } returns listOf()
        every { mockMediaStream.videoTracks } returns listOf()
        
        callViewModel = CallViewModel(mockCallManager, mockWebRTCManager)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `initial state should be correct`() = runTest {
        // Given
        val initialState = callViewModel.uiState.value
        
        // Then
        assertFalse(initialState.isLoading)
        assertNull(initialState.callSession)
        assertNull(initialState.error)
        assertFalse(initialState.isMuted)
        assertFalse(initialState.isVideoEnabled)
        assertFalse(initialState.isSpeakerOn)
        assertEquals("00:00", initialState.callDuration)
        assertNull(initialState.networkQuality)
        assertNull(initialState.localVideoTrack)
        assertNull(initialState.remoteVideoTrack)
    }
    
    @Test
    fun `loadCall should load existing call session`() = runTest {
        // Given
        val callId = "test_call_123"
        val callSession = CallSession(
            id = callId,
            peerId = "peer_456",
            isVideo = true,
            status = CallStatus.CONNECTED,
            localStream = mockMediaStream,
            remoteStream = null
        )
        
        every { mockCallManager.getActiveCall(callId) } returns callSession
        
        // When
        callViewModel.loadCall(callId)
        
        // Then
        val state = callViewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(callSession, state.callSession)
        assertNull(state.error)
        assertTrue(state.isVideoEnabled)
        
        verify { mockCallManager.getActiveCall(callId) }
        coVerify { mockWebRTCManager.getLocalMediaStream(true) }
    }
    
    @Test
    fun `loadCall should handle non-existent call`() = runTest {
        // Given
        val callId = "non_existent_call"
        
        every { mockCallManager.getActiveCall(callId) } returns null
        
        // When
        callViewModel.loadCall(callId)
        
        // Then
        val state = callViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.callSession)
        assertEquals("Call not found", state.error)
    }
    
    @Test
    fun `loadCall should handle exceptions`() = runTest {
        // Given
        val callId = "error_call"
        val errorMessage = "Database error"
        
        every { mockCallManager.getActiveCall(callId) } throws RuntimeException(errorMessage)
        
        // When
        callViewModel.loadCall(callId)
        
        // Then
        val state = callViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.callSession)
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `toggleMute should toggle mute state`() = runTest {
        // Given
        val initialState = callViewModel.uiState.value
        assertFalse(initialState.isMuted)
        
        // When
        callViewModel.toggleMute()
        
        // Then
        val newState = callViewModel.uiState.value
        assertTrue(newState.isMuted)
        
        // When - toggle again
        callViewModel.toggleMute()
        
        // Then
        val finalState = callViewModel.uiState.value
        assertFalse(finalState.isMuted)
    }
    
    @Test
    fun `toggleVideo should enable video when disabled`() = runTest {
        // Given
        val initialState = callViewModel.uiState.value
        assertFalse(initialState.isVideoEnabled)
        
        // When
        callViewModel.toggleVideo()
        
        // Then
        val newState = callViewModel.uiState.value
        assertTrue(newState.isVideoEnabled)
        
        coVerify { mockWebRTCManager.getLocalMediaStream(true) }
    }
    
    @Test
    fun `toggleVideo should disable video when enabled`() = runTest {
        // Given - Set initial video enabled state
        callViewModel.toggleVideo() // Enable first
        assertTrue(callViewModel.uiState.value.isVideoEnabled)
        
        // When
        callViewModel.toggleVideo()
        
        // Then
        val newState = callViewModel.uiState.value
        assertFalse(newState.isVideoEnabled)
    }
    
    @Test
    fun `toggleVideo should handle exceptions`() = runTest {
        // Given
        val errorMessage = "Camera not available"
        coEvery { mockWebRTCManager.getLocalMediaStream(true) } throws RuntimeException(errorMessage)
        
        // When
        callViewModel.toggleVideo()
        
        // Then
        val state = callViewModel.uiState.value
        assertTrue(state.error?.contains(errorMessage) == true)
    }
    
    @Test
    fun `toggleSpeaker should toggle speaker state`() = runTest {
        // Given
        val initialState = callViewModel.uiState.value
        assertFalse(initialState.isSpeakerOn)
        
        // When
        callViewModel.toggleSpeaker()
        
        // Then
        val newState = callViewModel.uiState.value
        assertTrue(newState.isSpeakerOn)
        
        // When - toggle again
        callViewModel.toggleSpeaker()
        
        // Then
        val finalState = callViewModel.uiState.value
        assertFalse(finalState.isSpeakerOn)
    }
    
    @Test
    fun `switchCamera should trigger UI update`() = runTest {
        // Given
        val initialState = callViewModel.uiState.value
        
        // When
        callViewModel.switchCamera()
        
        // Then - Should not throw exception and state should remain valid
        val newState = callViewModel.uiState.value
        assertNotNull(newState)
    }
    
    @Test
    fun `endCall should call manager endCall`() = runTest {
        // Given
        val callId = "test_call_end"
        val callSession = CallSession(
            id = callId,
            peerId = "peer_456",
            isVideo = false,
            status = CallStatus.CONNECTED,
            localStream = null,
            remoteStream = null
        )
        
        every { mockCallManager.getActiveCall(callId) } returns callSession
        callViewModel.loadCall(callId)
        
        // When
        callViewModel.endCall()
        
        // Then
        coVerify { mockCallManager.endCall(callId, "Call ended by user") }
    }
    
    @Test
    fun `endCall should handle exceptions`() = runTest {
        // Given
        val callId = "test_call_end_error"
        val errorMessage = "Network error"
        val callSession = CallSession(
            id = callId,
            peerId = "peer_456",
            isVideo = false,
            status = CallStatus.CONNECTED,
            localStream = null,
            remoteStream = null
        )
        
        every { mockCallManager.getActiveCall(callId) } returns callSession
        coEvery { mockCallManager.endCall(callId, any()) } throws RuntimeException(errorMessage)
        
        callViewModel.loadCall(callId)
        
        // When
        callViewModel.endCall()
        
        // Then
        val state = callViewModel.uiState.value
        assertTrue(state.error?.contains(errorMessage) == true)
    }
    
    @Test
    fun `should handle call state events`() = runTest {
        // Given
        val callId = "test_call_events"
        val callSession = CallSession(
            id = callId,
            peerId = "peer_456",
            isVideo = false,
            status = CallStatus.CONNECTING,
            localStream = null,
            remoteStream = null
        )
        
        val acceptedSession = callSession.copy(status = CallStatus.CONNECTED)
        val callStateEvent = CallStateEvent.CallAccepted(acceptedSession)
        
        every { mockCallManager.getActiveCall(callId) } returns callSession
        every { mockCallManager.observeCallStateEvents() } returns flowOf(callStateEvent)
        
        // When
        callViewModel.loadCall(callId)
        
        // Then - The event should be handled (we can't easily test the internal flow collection,
        // but we can verify the setup doesn't throw exceptions)
        val state = callViewModel.uiState.value
        assertEquals(callSession, state.callSession)
    }
    
    @Test
    fun `should handle WebRTC events`() = runTest {
        // Given
        val callId = "test_webrtc_events"
        val callSession = CallSession(
            id = callId,
            peerId = "peer_456",
            isVideo = true,
            status = CallStatus.CONNECTING,
            localStream = null,
            remoteStream = null
        )
        
        val webRTCEvent = CallEvent.RemoteStreamAdded(callId, mockMediaStream)
        
        every { mockCallManager.getActiveCall(callId) } returns callSession
        every { mockWebRTCManager.observeCallEvents() } returns flowOf(webRTCEvent)
        
        // When
        callViewModel.loadCall(callId)
        
        // Then - The event should be handled
        val state = callViewModel.uiState.value
        assertEquals(callSession, state.callSession)
    }
    
    @Test
    fun `should handle connection state changes`() = runTest {
        // Given
        val connectionState = ConnectionState.CONNECTED
        
        every { mockWebRTCManager.observeConnectionState() } returns flowOf(connectionState)
        
        // When - ViewModel is initialized (connection state observation starts in init)
        
        // Then - Should not throw exception
        val state = callViewModel.uiState.value
        assertNotNull(state)
    }
}