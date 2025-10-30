package com.chain.messaging.core.webrtc

import android.content.Context
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.webrtc.*

class WebRTCManagerTest {
    
    private lateinit var webRTCManager: WebRTCManagerImpl
    private val mockContext = mockk<Context>()
    private val mockPeerConnectionFactory = mockk<PeerConnectionFactory>()
    private val mockPeerConnection = mockk<PeerConnection>()
    private val mockMediaStream = mockk<MediaStream>()
    private val mockAudioTrack = mockk<AudioTrack>()
    private val mockVideoTrack = mockk<VideoTrack>()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock PeerConnectionFactory behavior
        every { mockPeerConnectionFactory.createPeerConnection(any(), any()) } returns mockPeerConnection
        every { mockPeerConnectionFactory.createLocalMediaStream(any()) } returns mockMediaStream
        every { mockPeerConnectionFactory.createAudioSource(any()) } returns mockk()
        every { mockPeerConnectionFactory.createAudioTrack(any(), any()) } returns mockAudioTrack
        every { mockPeerConnectionFactory.createVideoSource(any()) } returns mockk()
        every { mockPeerConnectionFactory.createVideoTrack(any(), any()) } returns mockVideoTrack
        
        // Mock MediaStream behavior
        every { mockMediaStream.addTrack(any<AudioTrack>()) } returns true
        every { mockMediaStream.addTrack(any<VideoTrack>()) } returns true
        every { mockMediaStream.audioTracks } returns listOf(mockAudioTrack)
        every { mockMediaStream.videoTracks } returns listOf(mockVideoTrack)
        
        // Mock PeerConnection behavior
        every { mockPeerConnection.addTrack(any(), any()) } returns mockk()
        every { mockPeerConnection.setLocalDescription(any(), any()) } just Runs
        every { mockPeerConnection.createOffer(any(), any()) } just Runs
        every { mockPeerConnection.createAnswer(any(), any()) } just Runs
        every { mockPeerConnection.addIceCandidate(any()) } returns true
        every { mockPeerConnection.close() } just Runs
        
        // Mock track behavior
        every { mockAudioTrack.dispose() } just Runs
        every { mockVideoTrack.dispose() } just Runs
        
        webRTCManager = WebRTCManagerImpl(mockContext, mockPeerConnectionFactory)
    }
    
    @After
    fun tearDown() {
        runTest {
            webRTCManager.cleanup()
        }
    }
    
    @Test
    fun `initialize should setup WebRTC components`() = runTest {
        // Given
        mockkStatic(PeerConnectionFactory::class)
        every { PeerConnectionFactory.initialize(any()) } just Runs
        
        // When
        webRTCManager.initialize()
        
        // Then
        verify { PeerConnectionFactory.initialize(any()) }
    }
    
    @Test
    fun `createPeerConnection should create and configure peer connection`() = runTest {
        // Given
        val peerId = "test_peer"
        val isVideo = true
        val iceServers = listOf(IceServer("stun:stun.l.google.com:19302"))
        
        // When
        val result = webRTCManager.createPeerConnection(peerId, isVideo, iceServers)
        
        // Then
        assertNotNull(result)
        verify { mockPeerConnectionFactory.createPeerConnection(any(), any()) }
        verify { mockPeerConnection.addTrack(any(), any()) }
    }
    
    @Test
    fun `initiateCall should create call session and send offer`() = runTest {
        // Given
        val peerId = "test_peer"
        val isVideo = false
        
        // When
        val callSession = webRTCManager.initiateCall(peerId, isVideo)
        
        // Then
        assertNotNull(callSession)
        assertEquals(peerId, callSession.peerId)
        assertEquals(isVideo, callSession.isVideo)
        assertEquals(CallStatus.INITIATING, callSession.status)
        
        verify { mockPeerConnection.createOffer(any(), any()) }
        verify { mockPeerConnection.setLocalDescription(any(), any()) }
    }
    
    @Test
    fun `acceptCall should update call status and create answer`() = runTest {
        // Given
        val peerId = "test_peer"
        val callSession = webRTCManager.initiateCall(peerId, false)
        
        // When
        val acceptedSession = webRTCManager.acceptCall(callSession.id)
        
        // Then
        assertEquals(CallStatus.CONNECTING, acceptedSession.status)
        verify { mockPeerConnection.createAnswer(any(), any()) }
        verify { mockPeerConnection.setLocalDescription(any(), any()) }
    }
    
    @Test
    fun `endCall should close peer connection and update status`() = runTest {
        // Given
        val peerId = "test_peer"
        val callSession = webRTCManager.initiateCall(peerId, false)
        
        // When
        webRTCManager.endCall(callSession.id)
        
        // Then
        verify { mockPeerConnection.close() }
    }
    
    @Test
    fun `getLocalMediaStream should create audio stream`() = runTest {
        // Given
        val isVideo = false
        
        // When
        val mediaStream = webRTCManager.getLocalMediaStream(isVideo)
        
        // Then
        assertNotNull(mediaStream)
        verify { mockPeerConnectionFactory.createAudioSource(any()) }
        verify { mockPeerConnectionFactory.createAudioTrack(any(), any()) }
        verify { mockMediaStream.addTrack(mockAudioTrack) }
    }
    
    @Test
    fun `getLocalMediaStream should create audio and video stream when video enabled`() = runTest {
        // Given
        val isVideo = true
        
        // Mock video capturer creation
        mockkStatic("com.chain.messaging.core.webrtc.WebRTCManagerImplKt")
        val mockVideoCapturer = mockk<CameraVideoCapturer>()
        every { mockVideoCapturer.initialize(any(), any(), any()) } just Runs
        
        // When
        val mediaStream = webRTCManager.getLocalMediaStream(isVideo)
        
        // Then
        assertNotNull(mediaStream)
        verify { mockPeerConnectionFactory.createAudioSource(any()) }
        verify { mockPeerConnectionFactory.createVideoSource(any()) }
        verify { mockPeerConnectionFactory.createAudioTrack(any(), any()) }
        verify { mockPeerConnectionFactory.createVideoTrack(any(), any()) }
    }
    
    @Test
    fun `addIceCandidate should add candidate to peer connection`() = runTest {
        // Given
        val peerId = "test_peer"
        val callSession = webRTCManager.initiateCall(peerId, false)
        val candidate = IceCandidate("candidate", 0, "audio")
        
        // When
        webRTCManager.addIceCandidate(callSession.id, candidate)
        
        // Then
        verify { mockPeerConnection.addIceCandidate(any()) }
    }
    
    @Test
    fun `observeCallEvents should emit call events`() = runTest {
        // Given
        val peerId = "test_peer"
        
        // When
        val callSession = webRTCManager.initiateCall(peerId, false)
        val event = webRTCManager.observeCallEvents().first()
        
        // Then
        assertTrue(event is CallEvent.IncomingCall)
        assertEquals(callSession, (event as CallEvent.IncomingCall).callSession)
    }
    
    @Test
    fun `cleanup should dispose all resources`() = runTest {
        // Given
        val peerId = "test_peer"
        webRTCManager.initiateCall(peerId, true)
        
        // When
        webRTCManager.cleanup()
        
        // Then
        verify { mockAudioTrack.dispose() }
        verify { mockVideoTrack.dispose() }
        verify { mockPeerConnection.close() }
        verify { mockPeerConnectionFactory.dispose() }
    }
    
    @Test
    fun `createPeerConnection with empty ice servers should use defaults`() = runTest {
        // Given
        val peerId = "test_peer"
        val isVideo = false
        val emptyIceServers = emptyList<IceServer>()
        
        // When
        val result = webRTCManager.createPeerConnection(peerId, isVideo, emptyIceServers)
        
        // Then
        assertNotNull(result)
        verify { mockPeerConnectionFactory.createPeerConnection(any(), any()) }
    }
    
    @Test
    fun `acceptCall with invalid call ID should throw exception`() = runTest {
        // Given
        val invalidCallId = "invalid_call_id"
        
        // When & Then
        try {
            webRTCManager.acceptCall(invalidCallId)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Call not found: $invalidCallId", e.message)
        }
    }
}