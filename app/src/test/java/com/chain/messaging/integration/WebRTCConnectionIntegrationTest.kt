package com.chain.messaging.integration

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chain.messaging.core.webrtc.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import javax.inject.Inject

/**
 * Integration tests for WebRTC connection establishment
 * Tests the complete flow of WebRTC connection management, ICE candidate gathering,
 * and STUN/TURN server integration
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WebRTCConnectionIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var webRTCManager: WebRTCManager
    
    @Inject
    lateinit var iceServerProvider: IceServerProvider
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        hiltRule.inject()
    }
    
    @Test
    fun testWebRTCInitialization() = runTest {
        // When
        webRTCManager.initialize()
        
        // Then - should not throw exception
        // WebRTC initialization is successful if no exception is thrown
    }
    
    @Test
    fun testPeerConnectionCreation() = runTest {
        // Given
        webRTCManager.initialize()
        val peerId = "test_peer_123"
        val isVideo = false
        val iceServers = iceServerProvider.getDefaultStunServers()
        
        // When
        val peerConnection = webRTCManager.createPeerConnection(peerId, isVideo, iceServers)
        
        // Then
        assertNotNull(peerConnection)
    }
    
    @Test
    fun testCallInitiation() = runTest {
        // Given
        webRTCManager.initialize()
        val peerId = "test_peer_456"
        val isVideo = false
        
        // When
        val callSession = webRTCManager.initiateCall(peerId, isVideo)
        
        // Then
        assertNotNull(callSession)
        assertEquals(peerId, callSession.peerId)
        assertEquals(isVideo, callSession.isVideo)
        assertEquals(CallStatus.INITIATING, callSession.status)
        assertNotNull(callSession.localStream)
    }
    
    @Test
    fun testVideoCallInitiation() = runTest {
        // Given
        webRTCManager.initialize()
        val peerId = "test_peer_video"
        val isVideo = true
        
        // When
        val callSession = webRTCManager.initiateCall(peerId, isVideo)
        
        // Then
        assertNotNull(callSession)
        assertEquals(peerId, callSession.peerId)
        assertTrue(callSession.isVideo)
        assertEquals(CallStatus.INITIATING, callSession.status)
        assertNotNull(callSession.localStream)
    }
    
    @Test
    fun testCallAcceptance() = runTest {
        // Given
        webRTCManager.initialize()
        val peerId = "test_peer_accept"
        val initiatedCall = webRTCManager.initiateCall(peerId, false)
        
        // When
        val acceptedCall = webRTCManager.acceptCall(initiatedCall.id)
        
        // Then
        assertEquals(CallStatus.CONNECTING, acceptedCall.status)
        assertEquals(initiatedCall.id, acceptedCall.id)
        assertEquals(initiatedCall.peerId, acceptedCall.peerId)
    }
    
    @Test
    fun testCallTermination() = runTest {
        // Given
        webRTCManager.initialize()
        val peerId = "test_peer_end"
        val callSession = webRTCManager.initiateCall(peerId, false)
        
        // When
        webRTCManager.endCall(callSession.id)
        
        // Then - should not throw exception
        // Call termination is successful if no exception is thrown
    }
    
    @Test
    fun testLocalMediaStreamCreation() = runTest {
        // Given
        webRTCManager.initialize()
        
        // When - Audio only
        val audioStream = webRTCManager.getLocalMediaStream(isVideo = false)
        
        // Then
        assertNotNull(audioStream)
        assertTrue(audioStream.audioTracks.isNotEmpty())
        assertTrue(audioStream.videoTracks.isEmpty())
    }
    
    @Test
    fun testLocalVideoStreamCreation() = runTest {
        // Given
        webRTCManager.initialize()
        
        // When - Audio and Video
        val videoStream = webRTCManager.getLocalMediaStream(isVideo = true)
        
        // Then
        assertNotNull(videoStream)
        assertTrue(videoStream.audioTracks.isNotEmpty())
        // Note: Video tracks might be empty in test environment without camera
    }
    
    @Test
    fun testIceCandidateHandling() = runTest {
        // Given
        webRTCManager.initialize()
        val peerId = "test_peer_ice"
        val callSession = webRTCManager.initiateCall(peerId, false)
        val iceCandidate = IceCandidate(
            sdp = "candidate:1 1 UDP 2130706431 192.168.1.100 54400 typ host",
            sdpMLineIndex = 0,
            sdpMid = "audio"
        )
        
        // When
        webRTCManager.addIceCandidate(callSession.id, iceCandidate)
        
        // Then - should not throw exception
        // ICE candidate addition is successful if no exception is thrown
    }
    
    @Test
    fun testCallEventObservation() = runTest {
        // Given
        webRTCManager.initialize()
        val peerId = "test_peer_events"
        
        // When
        val callSession = webRTCManager.initiateCall(peerId, false)
        
        // Then
        withTimeout(5000) {
            val event = webRTCManager.observeCallEvents().first()
            assertTrue(event is CallEvent.IncomingCall)
            assertEquals(callSession, (event as CallEvent.IncomingCall).callSession)
        }
    }
    
    @Test
    fun testConnectionStateObservation() = runTest {
        // Given
        webRTCManager.initialize()
        
        // When
        val connectionStateFlow = webRTCManager.observeConnectionState()
        
        // Then
        assertNotNull(connectionStateFlow)
        // Connection state observation is successful if flow is created
    }
    
    @Test
    fun testIceServerValidation() {
        // Given
        val validStunServer = IceServer("stun:stun.l.google.com:19302")
        val validTurnServer = IceServer(
            url = "turn:turnserver.example.com:3478",
            username = "user",
            credential = "pass"
        )
        val invalidServer = IceServer("")
        
        // When & Then
        assertTrue(iceServerProvider.validateIceServer(validStunServer))
        assertTrue(iceServerProvider.validateIceServer(validTurnServer))
        assertFalse(iceServerProvider.validateIceServer(invalidServer))
    }
    
    @Test
    fun testMultipleCallSessions() = runTest {
        // Given
        webRTCManager.initialize()
        val peerId1 = "test_peer_1"
        val peerId2 = "test_peer_2"
        
        // When
        val call1 = webRTCManager.initiateCall(peerId1, false)
        val call2 = webRTCManager.initiateCall(peerId2, true)
        
        // Then
        assertNotEquals(call1.id, call2.id)
        assertEquals(peerId1, call1.peerId)
        assertEquals(peerId2, call2.peerId)
        assertFalse(call1.isVideo)
        assertTrue(call2.isVideo)
    }
    
    @Test
    fun testResourceCleanup() = runTest {
        // Given
        webRTCManager.initialize()
        val peerId = "test_peer_cleanup"
        webRTCManager.initiateCall(peerId, true)
        
        // When
        webRTCManager.cleanup()
        
        // Then - should not throw exception
        // Resource cleanup is successful if no exception is thrown
    }
}