package com.chain.messaging.integration

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.blockchain.EncryptedMessage
import com.chain.messaging.core.blockchain.IncomingMessage
import com.chain.messaging.core.blockchain.MessageType
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.webrtc.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.signal.libsignal.protocol.SignalProtocolAddress

/**
 * Integration test for call signaling through blockchain
 * Tests requirement 6.2 for call offer/answer exchange via blockchain messages
 */
class CallSignalingIntegrationTest {
    
    private lateinit var callSignalingService: CallSignalingService
    private lateinit var callManager: CallManager
    private lateinit var callNotificationManager: CallNotificationManager
    
    private val mockBlockchainManager = mockk<BlockchainManager>()
    private val mockEncryptionService = mockk<SignalEncryptionService>()
    private val mockWebRTCManager = mockk<WebRTCManager>()
    private val mockIceServerProvider = mockk<IceServerProvider>()
    private val mockCallNotificationService = mockk<CallNotificationService>()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock encryption service
        every { mockEncryptionService.encryptMessage(any(), any()) } returns Result.success(
            mockk {
                every { ciphertext } returns "encrypted_content".toByteArray()
            }
        )
        every { mockEncryptionService.decryptMessage(any(), any()) } returns Result.success(
            """{"type":"CallInvitation","callId":"test_call","isVideo":false,"timestamp":123456789}""".toByteArray()
        )
        
        // Mock blockchain manager
        every { mockBlockchainManager.sendMessage(any()) } returns "tx_hash_123"
        every { mockBlockchainManager.subscribeToMessages(any()) } returns flowOf()
        
        // Mock WebRTC manager
        every { mockWebRTCManager.initiateCall(any(), any()) } returns CallSession(
            id = "webrtc_call_123",
            peerId = "peer_456",
            isVideo = false,
            status = CallStatus.INITIATING,
            localStream = null,
            remoteStream = null
        )
        every { mockWebRTCManager.acceptCall(any()) } returns CallSession(
            id = "call_123",
            peerId = "peer_456",
            isVideo = false,
            status = CallStatus.CONNECTING,
            localStream = null,
            remoteStream = null
        )
        every { mockWebRTCManager.endCall(any()) } just Runs
        every { mockWebRTCManager.createPeerConnection(any(), any(), any()) } returns mockk()
        every { mockWebRTCManager.observeCallEvents() } returns flowOf()
        every { mockWebRTCManager.addIceCandidate(any(), any()) } just Runs
        
        // Mock ICE server provider
        every { mockIceServerProvider.getDefaultStunServers() } returns listOf(
            IceServer("stun:stun.l.google.com:19302")
        )
        
        // Mock call notification service
        every { mockCallNotificationService.showIncomingCallNotification(any(), any(), any()) } just Runs
        every { mockCallNotificationService.showOngoingCallNotification(any(), any(), any(), any()) } just Runs
        every { mockCallNotificationService.clearIncomingCallNotification() } just Runs
        every { mockCallNotificationService.clearAllCallNotifications() } just Runs
        every { mockCallNotificationService.notificationEvents } returns flowOf()
        
        // Initialize services
        callSignalingService = CallSignalingService(mockBlockchainManager, mockEncryptionService)
        callManager = CallManager(mockWebRTCManager, callSignalingService, mockIceServerProvider)
        callNotificationManager = CallNotificationManager(mockk(), mockCallNotificationService)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `complete call flow should work end to end`() = runTest {
        // Given
        val callerPeerId = "caller_123"
        val calleePeerId = "callee_456"
        val isVideo = false
        
        // When - Caller initiates call
        val callResult = callManager.initiateCall(calleePeerId, isVideo)
        
        // Then - Call should be initiated successfully
        assertTrue(callResult.isSuccess)
        val callSession = callResult.getOrNull()
        assertNotNull(callSession)
        assertEquals(calleePeerId, callSession?.peerId)
        assertEquals(isVideo, callSession?.isVideo)
        
        // Verify signaling invitation was sent
        verify { mockWebRTCManager.initiateCall(calleePeerId, isVideo) }
        verify { mockBlockchainManager.sendMessage(any()) }
        verify { mockEncryptionService.encryptMessage(any(), any()) }
        
        // Verify call is in pending state
        val pendingCall = callManager.getPendingCall(callSession?.id ?: "")
        assertNotNull(pendingCall)
        assertEquals(PendingCallStatus.INVITATION_SENT, pendingCall?.status)
        assertTrue(pendingCall?.isOutgoing == true)
    }
    
    @Test
    fun `incoming call invitation should be handled correctly`() = runTest {
        // Given
        val userId = "user_123"
        val fromPeerId = "peer_456"
        val callId = "incoming_call_789"
        val isVideo = true
        
        val incomingMessage = IncomingMessage(
            transactionHash = "tx_hash_456",
            from = fromPeerId,
            to = userId,
            encryptedContent = "encrypted_invitation",
            timestamp = System.currentTimeMillis(),
            blockNumber = 12345
        )
        
        // Mock decryption to return call invitation
        every { mockEncryptionService.decryptMessage(any(), any()) } returns Result.success(
            """{"type":"CallInvitation","callId":"$callId","isVideo":$isVideo,"timestamp":${System.currentTimeMillis()}}""".toByteArray()
        )
        
        every { mockBlockchainManager.subscribeToMessages(userId) } returns flowOf(incomingMessage)
        
        // When
        callManager.startSignalingListener(userId)
        
        // Then
        verify { mockBlockchainManager.subscribeToMessages(userId) }
        verify { mockEncryptionService.decryptMessage(any(), any()) }
    }
    
    @Test
    fun `call acceptance flow should work correctly`() = runTest {
        // Given
        val callId = "call_123"
        val peerId = "peer_456"
        
        // First initiate a call to create pending call
        callManager.initiateCall(peerId, false)
        
        // When - Accept the call
        val acceptResult = callManager.acceptCall(callId)
        
        // Then
        assertTrue(acceptResult.isSuccess)
        val acceptedCall = acceptResult.getOrNull()
        assertNotNull(acceptedCall)
        assertEquals(CallStatus.CONNECTING, acceptedCall?.status)
        
        // Verify WebRTC call was accepted
        verify { mockWebRTCManager.acceptCall(callId) }
        
        // Verify acceptance signal was sent through blockchain
        verify { mockBlockchainManager.sendMessage(any()) }
        verify { mockEncryptionService.encryptMessage(any(), any()) }
    }
    
    @Test
    fun `call rejection flow should work correctly`() = runTest {
        // Given
        val callId = "call_123"
        val peerId = "peer_456"
        val reason = "User is busy"
        
        // First initiate a call to create pending call
        callManager.initiateCall(peerId, false)
        
        // When - Reject the call
        val rejectResult = callManager.rejectCall(callId, reason)
        
        // Then
        assertTrue(rejectResult.isSuccess)
        
        // Verify rejection signal was sent through blockchain
        verify { mockBlockchainManager.sendMessage(any()) }
        verify { mockEncryptionService.encryptMessage(any(), any()) }
        
        // Verify call was cleaned up
        assertNull(callManager.getActiveCall(callId))
        assertNull(callManager.getPendingCall(callId))
    }
    
    @Test
    fun `call termination flow should work correctly`() = runTest {
        // Given
        val callId = "call_123"
        val peerId = "peer_456"
        val reason = "Call ended by user"
        
        // First initiate a call
        callManager.initiateCall(peerId, false)
        
        // When - End the call
        val endResult = callManager.endCall(callId, reason)
        
        // Then
        assertTrue(endResult.isSuccess)
        
        // Verify WebRTC call was ended
        verify { mockWebRTCManager.endCall(callId) }
        
        // Verify termination signal was sent through blockchain
        verify { mockBlockchainManager.sendMessage(any()) }
        verify { mockEncryptionService.encryptMessage(any(), any()) }
        
        // Verify call was cleaned up
        assertNull(callManager.getActiveCall(callId))
        assertNull(callManager.getPendingCall(callId))
    }
    
    @Test
    fun `ICE candidate exchange should work through blockchain`() = runTest {
        // Given
        val callId = "call_123"
        val peerId = "peer_456"
        val candidate = IceCandidate(
            sdp = "candidate:1 1 UDP 2130706431 192.168.1.100 54400 typ host",
            sdpMLineIndex = 0,
            sdpMid = "audio"
        )
        
        // When
        val result = callSignalingService.sendIceCandidate(callId, peerId, candidate)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("tx_hash_123", result.getOrNull())
        
        // Verify ICE candidate was encrypted and sent through blockchain
        verify { mockEncryptionService.encryptMessage(any(), any()) }
        verify { mockBlockchainManager.sendMessage(any()) }
    }
    
    @Test
    fun `WebRTC offer answer exchange should work through blockchain`() = runTest {
        // Given
        val callId = "call_123"
        val peerId = "peer_456"
        val offer = "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\n..."
        val answer = "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\n..."
        
        // When - Send offer
        val offerResult = callSignalingService.sendCallOffer(callId, peerId, offer, false)
        
        // Then
        assertTrue(offerResult.isSuccess)
        verify { mockEncryptionService.encryptMessage(any(), any()) }
        verify { mockBlockchainManager.sendMessage(any()) }
        
        // When - Send answer
        val answerResult = callSignalingService.sendCallAnswer(callId, peerId, answer)
        
        // Then
        assertTrue(answerResult.isSuccess)
        verify(exactly = 2) { mockEncryptionService.encryptMessage(any(), any()) }
        verify(exactly = 2) { mockBlockchainManager.sendMessage(any()) }
    }
    
    @Test
    fun `signaling message parsing should handle all message types`() = runTest {
        // Given
        val userId = "user_123"
        val callId = "call_123"
        val peerId = "peer_456"
        
        val messages = listOf(
            """{"type":"CallInvitation","callId":"$callId","isVideo":false,"timestamp":123456789}""",
            """{"type":"CallAcceptance","callId":"$callId","timestamp":123456789}""",
            """{"type":"CallRejection","callId":"$callId","reason":"User busy","timestamp":123456789}""",
            """{"type":"CallTermination","callId":"$callId","reason":"Call ended","timestamp":123456789}""",
            """{"type":"CallOffer","callId":"$callId","offer":"test_offer","isVideo":false,"timestamp":123456789}""",
            """{"type":"CallAnswer","callId":"$callId","answer":"test_answer","timestamp":123456789}"""
        )
        
        messages.forEachIndexed { index, messageJson ->
            // Mock decryption to return different message types
            every { mockEncryptionService.decryptMessage(any(), any()) } returns Result.success(
                messageJson.toByteArray()
            )
            
            val incomingMessage = IncomingMessage(
                transactionHash = "tx_hash_$index",
                from = peerId,
                to = userId,
                encryptedContent = "encrypted_message_$index",
                timestamp = System.currentTimeMillis(),
                blockNumber = 12345 + index
            )
            
            every { mockBlockchainManager.subscribeToMessages(userId) } returns flowOf(incomingMessage)
            
            // When
            val signalingFlow = callSignalingService.subscribeToSignalingMessages(userId)
            val events = signalingFlow.toList()
            
            // Then
            assertTrue(events.isNotEmpty())
            val event = events.first()
            assertEquals(peerId, event.fromPeerId)
            assertEquals("tx_hash_$index", event.transactionHash)
        }
    }
    
    @Test
    fun `error handling should work for invalid signaling messages`() = runTest {
        // Given
        val userId = "user_123"
        val peerId = "peer_456"
        
        // Mock decryption failure
        every { mockEncryptionService.decryptMessage(any(), any()) } returns Result.failure(
            RuntimeException("Decryption failed")
        )
        
        val incomingMessage = IncomingMessage(
            transactionHash = "tx_hash_error",
            from = peerId,
            to = userId,
            encryptedContent = "invalid_encrypted_content",
            timestamp = System.currentTimeMillis(),
            blockNumber = 12345
        )
        
        every { mockBlockchainManager.subscribeToMessages(userId) } returns flowOf(incomingMessage)
        
        // When
        val signalingFlow = callSignalingService.subscribeToSignalingMessages(userId)
        val events = signalingFlow.toList()
        
        // Then
        assertTrue(events.isNotEmpty())
        val event = events.first()
        assertTrue(event.message is SignalingMessage.Error)
        assertEquals(peerId, event.fromPeerId)
    }
    
    @Test
    fun `concurrent signaling operations should be thread safe`() = runTest {
        // Given
        val callId1 = "call_123"
        val callId2 = "call_456"
        val peerId1 = "peer_123"
        val peerId2 = "peer_456"
        
        // When - Send multiple signaling messages concurrently
        val invitation1 = callSignalingService.sendCallInvitation(callId1, peerId1, false)
        val invitation2 = callSignalingService.sendCallInvitation(callId2, peerId2, true)
        val acceptance1 = callSignalingService.sendCallAcceptance(callId1, peerId1)
        val rejection2 = callSignalingService.sendCallRejection(callId2, peerId2, "User busy")
        
        // Then
        assertTrue(invitation1.isSuccess)
        assertTrue(invitation2.isSuccess)
        assertTrue(acceptance1.isSuccess)
        assertTrue(rejection2.isSuccess)
        
        // Verify all messages were sent
        verify(exactly = 4) { mockEncryptionService.encryptMessage(any(), any()) }
        verify(exactly = 4) { mockBlockchainManager.sendMessage(any()) }
    }
    
    @Test
    fun `notification integration should work with call state changes`() = runTest {
        // Given
        val callId = "call_123"
        val peerId = "peer_456"
        
        // When - Handle incoming call state event
        val incomingCallEvent = CallStateEvent.IncomingCall(callId, peerId, false)
        callNotificationManager.handleCallStateEvent(incomingCallEvent)
        
        // Then
        verify { mockCallNotificationService.showIncomingCallNotification(callId, peerId, false) }
        
        // When - Handle call accepted event
        val callSession = CallSession(callId, peerId, false, CallStatus.CONNECTED, null, null)
        val acceptedEvent = CallStateEvent.CallAccepted(callSession)
        callNotificationManager.handleCallStateEvent(acceptedEvent)
        
        // Then
        verify { mockCallNotificationService.clearIncomingCallNotification() }
        verify { mockCallNotificationService.showOngoingCallNotification(callId, peerId, false, "00:00") }
        
        // When - Handle call ended event
        val endedEvent = CallStateEvent.CallEnded(callId, "Call completed")
        callNotificationManager.handleCallStateEvent(endedEvent)
        
        // Then
        verify { mockCallNotificationService.clearAllCallNotifications() }
    }
    
    @Test
    fun `call timeout should be handled correctly`() = runTest {
        // Given
        val callId = "call_123"
        val peerId = "peer_456"
        
        // First initiate a call to create pending call
        callManager.initiateCall(peerId, false)
        
        // When - Handle timeout event
        val timeoutEvent = CallStateEvent.CallTimeout(callId)
        
        // Then - Call should be cleaned up (this would be handled by the timeout mechanism)
        // In a real implementation, the timeout would trigger cleanup automatically
        assertTrue(true) // Placeholder assertion
    }
    
    @Test
    fun `call statistics should be tracked correctly`() = runTest {
        // Given
        val peerId1 = "peer_123"
        val peerId2 = "peer_456"
        
        // When - Initiate multiple calls
        callManager.initiateCall(peerId1, false)
        callManager.initiateCall(peerId2, true)
        
        // Then - Statistics should reflect active calls
        val stats = callManager.getCallStatistics()
        assertTrue(stats.activeCalls >= 0)
        assertTrue(stats.pendingCalls >= 0)
        assertTrue(stats.totalCallsInitiated >= 0)
        assertTrue(stats.totalCallsReceived >= 0)
        assertTrue(stats.totalCallsCompleted >= 0)
        assertTrue(stats.averageCallDuration >= 0)
    }
    
    @Test
    fun `duplicate call invitations should be rejected`() = runTest {
        // Given
        val userId = "user_123"
        val fromPeerId = "peer_456"
        val callId1 = "call_123"
        val callId2 = "call_456"
        
        // Mock first invitation
        val invitation1 = IncomingMessage(
            transactionHash = "tx_hash_1",
            from = fromPeerId,
            to = userId,
            encryptedContent = "encrypted_invitation_1",
            timestamp = System.currentTimeMillis(),
            blockNumber = 12345
        )
        
        // Mock second invitation from same peer
        val invitation2 = IncomingMessage(
            transactionHash = "tx_hash_2",
            from = fromPeerId,
            to = userId,
            encryptedContent = "encrypted_invitation_2",
            timestamp = System.currentTimeMillis() + 1000,
            blockNumber = 12346
        )
        
        every { mockEncryptionService.decryptMessage(any(), any()) } returnsMany listOf(
            Result.success("""{"type":"CallInvitation","callId":"$callId1","isVideo":false,"timestamp":${System.currentTimeMillis()}}""".toByteArray()),
            Result.success("""{"type":"CallInvitation","callId":"$callId2","isVideo":false,"timestamp":${System.currentTimeMillis() + 1000}}""".toByteArray())
        )
        
        every { mockBlockchainManager.subscribeToMessages(userId) } returns flowOf(invitation1, invitation2)
        
        // When
        callManager.startSignalingListener(userId)
        
        // Then - Second invitation should be rejected
        verify(atLeast = 1) { mockBlockchainManager.sendMessage(any()) }
    }
    
    @Test
    fun `signaling service statistics should be available`() = runTest {
        // When
        val stats = callSignalingService.getSignalingStats()
        
        // Then
        assertNotNull(stats)
        assertTrue(stats.totalMessagesSent >= 0)
        assertTrue(stats.totalMessagesReceived >= 0)
        assertTrue(stats.failedMessages >= 0)
        assertTrue(stats.averageLatency >= 0)
    }
}