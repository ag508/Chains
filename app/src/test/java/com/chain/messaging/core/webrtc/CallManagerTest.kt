package com.chain.messaging.core.webrtc

import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CallManagerTest {
    
    private lateinit var callManager: CallManagerImpl
    private val mockWebRTCManager = mockk<WebRTCManager>()
    private val mockSignalingService = mockk<CallSignalingService>()
    private val mockIceServerProvider = mockk<IceServerProvider>()
    private val mockPeerConnection = mockk<org.webrtc.PeerConnection>()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock WebRTC manager
        coEvery { mockWebRTCManager.initiateCall(any(), any()) } returns createMockCallSession()
        coEvery { mockWebRTCManager.acceptCall(any()) } returns createMockCallSession()
        coEvery { mockWebRTCManager.endCall(any()) } just Runs
        coEvery { mockWebRTCManager.createPeerConnection(any(), any(), any()) } returns mockPeerConnection
        
        // Mock signaling service
        coEvery { mockSignalingService.sendCallInvitation(any(), any(), any(), any()) } just Runs
        coEvery { mockSignalingService.sendCallAnswer(any(), any(), any()) } just Runs
        coEvery { mockSignalingService.sendCallRejection(any(), any(), any()) } just Runs
        coEvery { mockSignalingService.sendCallEnd(any(), any(), any()) } just Runs
        every { mockSignalingService.observeIncomingCallSignals() } returns flowOf()
        
        // Mock ICE server provider
        every { mockIceServerProvider.getDefaultStunServers() } returns listOf(
            IceServer("stun:stun.l.google.com:19302")
        )
        
        callManager = CallManagerImpl(mockWebRTCManager, mockSignalingService, mockIceServerProvider)
    }
    
    @Test
    fun `initiateCall should create call session and send invitation`() = runTest {
        // Given
        val recipientId = "recipient_123"
        val isVideo = true
        
        // When
        val callSession = callManager.initiateCall(recipientId, isVideo)
        
        // Then
        assertNotNull(callSession)
        assertEquals(recipientId, callSession.peerId)
        assertEquals(isVideo, callSession.isVideo)
        
        coVerify { mockWebRTCManager.initiateCall(recipientId, isVideo) }
        coVerify { mockSignalingService.sendCallInvitation(any(), any(), isVideo, any()) }
    }
    
    @Test
    fun `acceptCall should accept call and send answer`() = runTest {
        // Given
        val callId = "call_123"
        val recipientId = "recipient_456"
        
        // First initiate a call to have it in active calls
        val initiatedCall = callManager.initiateCall(recipientId, false)
        
        // When
        val acceptedCall = callManager.acceptCall(initiatedCall.id)
        
        // Then
        assertNotNull(acceptedCall)
        coVerify { mockWebRTCManager.acceptCall(initiatedCall.id) }
        coVerify { mockSignalingService.sendCallAnswer(any(), any(), any()) }
    }
    
    @Test
    fun `rejectCall should send rejection and remove call`() = runTest {
        // Given
        val recipientId = "recipient_123"
        val reason = "User busy"
        val callSession = callManager.initiateCall(recipientId, false)
        
        // When
        callManager.rejectCall(callSession.id, reason)
        
        // Then
        coVerify { mockSignalingService.sendCallRejection(recipientId, callSession.id, reason) }
        assertNull(callManager.getActiveCall(callSession.id))
    }
    
    @Test
    fun `endCall should end call and send end signal`() = runTest {
        // Given
        val recipientId = "recipient_123"
        val reason = "Call completed"
        val callSession = callManager.initiateCall(recipientId, false)
        
        // When
        callManager.endCall(callSession.id, reason)
        
        // Then
        coVerify { mockWebRTCManager.endCall(callSession.id) }
        coVerify { mockSignalingService.sendCallEnd(recipientId, callSession.id, reason) }
        assertNull(callManager.getActiveCall(callSession.id))
    }
    
    @Test
    fun `getActiveCall should return call session if exists`() = runTest {
        // Given
        val recipientId = "recipient_123"
        val callSession = callManager.initiateCall(recipientId, false)
        
        // When
        val activeCall = callManager.getActiveCall(callSession.id)
        
        // Then
        assertNotNull(activeCall)
        assertEquals(callSession.id, activeCall?.id)
    }
    
    @Test
    fun `getActiveCall should return null if call does not exist`() {
        // Given
        val nonExistentCallId = "non_existent_call"
        
        // When
        val activeCall = callManager.getActiveCall(nonExistentCallId)
        
        // Then
        assertNull(activeCall)
    }
    
    @Test
    fun `getActiveCalls should return list of active calls`() = runTest {
        // Given
        val recipientId1 = "recipient_1"
        val recipientId2 = "recipient_2"
        val call1 = callManager.initiateCall(recipientId1, false)
        val call2 = callManager.initiateCall(recipientId2, true)
        
        // When
        val activeCalls = callManager.getActiveCalls()
        
        // Then
        assertEquals(2, activeCalls.size)
        assertTrue(activeCalls.any { it.id == call1.id })
        assertTrue(activeCalls.any { it.id == call2.id })
    }
    
    @Test
    fun `observeCallStateChanges should emit call state changes`() = runTest {
        // Given
        val recipientId = "recipient_123"
        
        // When
        val callSession = callManager.initiateCall(recipientId, false)
        val stateChange = callManager.observeCallStateChanges().first()
        
        // Then
        assertTrue(stateChange is CallStateChange.CallInitiated)
        assertEquals(callSession, (stateChange as CallStateChange.CallInitiated).callSession)
    }
    
    @Test
    fun `handleIncomingSignal should handle invitation signal`() = runTest {
        // Given
        val invitation = CallSignal.Invitation(
            callId = "call_123",
            senderId = "sender_456",
            recipientId = "recipient_789",
            isVideo = true,
            sdp = "offer_sdp",
            sdpType = "offer",
            timestamp = System.currentTimeMillis()
        )
        
        // When
        callManager.handleIncomingSignal(invitation)
        
        // Then
        val activeCall = callManager.getActiveCall(invitation.callId)
        assertNotNull(activeCall)
        assertEquals(invitation.senderId, activeCall?.peerId)
        assertEquals(invitation.isVideo, activeCall?.isVideo)
        assertEquals(CallStatus.RINGING, activeCall?.status)
    }
    
    @Test
    fun `acceptCall with invalid call ID should throw exception`() = runTest {
        // Given
        val invalidCallId = "invalid_call_id"
        
        // When & Then
        try {
            callManager.acceptCall(invalidCallId)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Call not found: $invalidCallId", e.message)
        }
    }
    
    private fun createMockCallSession(): CallSession {
        return CallSession(
            id = "call_123",
            peerId = "peer_456",
            isVideo = false,
            status = CallStatus.INITIATING,
            localStream = null,
            remoteStream = null
        )
    }
}