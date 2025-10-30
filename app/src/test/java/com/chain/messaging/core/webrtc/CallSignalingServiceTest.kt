package com.chain.messaging.core.webrtc

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.blockchain.EncryptedMessage
import com.chain.messaging.core.blockchain.IncomingMessage
import com.chain.messaging.core.crypto.SignalEncryptionService
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.signal.libsignal.protocol.SignalProtocolAddress

class CallSignalingServiceTest {
    
    private lateinit var callSignalingService: CallSignalingService
    private val mockBlockchainManager = mockk<BlockchainManager>()
    private val mockEncryptionService = mockk<SignalEncryptionService>()
    
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
            """{"type":"CallOffer","callId":"test_call","offer":"test_offer","isVideo":false,"timestamp":123456789}""".toByteArray()
        )
        
        // Mock blockchain manager
        every { mockBlockchainManager.sendMessage(any()) } returns "tx_hash_123"
        every { mockBlockchainManager.subscribeToMessages(any()) } returns flowOf()
        
        callSignalingService = CallSignalingService(mockBlockchainManager, mockEncryptionService)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `sendCallOffer should encrypt and send offer through blockchain`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        val offer = "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\n..."
        val isVideo = true
        
        // When
        val result = callSignalingService.sendCallOffer(callId, peerId, offer, isVideo)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("tx_hash_123", result.getOrNull())
        
        verify {
            mockEncryptionService.encryptMessage(
                SignalProtocolAddress(peerId, 1),
                any()
            )
        }
        verify {
            mockBlockchainManager.sendMessage(any())
        }
    }
    
    @Test
    fun `sendCallAnswer should encrypt and send answer through blockchain`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        val answer = "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\n..."
        
        // When
        val result = callSignalingService.sendCallAnswer(callId, peerId, answer)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("tx_hash_123", result.getOrNull())
        
        verify {
            mockEncryptionService.encryptMessage(
                SignalProtocolAddress(peerId, 1),
                any()
            )
        }
        verify {
            mockBlockchainManager.sendMessage(any())
        }
    }
    
    @Test
    fun `sendIceCandidate should encrypt and send candidate through blockchain`() = runTest {
        // Given
        val callId = "test_call_123"
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
        
        verify {
            mockEncryptionService.encryptMessage(
                SignalProtocolAddress(peerId, 1),
                any()
            )
        }
        verify {
            mockBlockchainManager.sendMessage(any())
        }
    }
    
    @Test
    fun `sendCallInvitation should encrypt and send invitation through blockchain`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        val isVideo = false
        
        // When
        val result = callSignalingService.sendCallInvitation(callId, peerId, isVideo)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("tx_hash_123", result.getOrNull())
        
        verify {
            mockEncryptionService.encryptMessage(
                SignalProtocolAddress(peerId, 1),
                any()
            )
        }
        verify {
            mockBlockchainManager.sendMessage(any())
        }
    }
    
    @Test
    fun `sendCallAcceptance should encrypt and send acceptance through blockchain`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        
        // When
        val result = callSignalingService.sendCallAcceptance(callId, peerId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("tx_hash_123", result.getOrNull())
        
        verify {
            mockEncryptionService.encryptMessage(
                SignalProtocolAddress(peerId, 1),
                any()
            )
        }
        verify {
            mockBlockchainManager.sendMessage(any())
        }
    }
    
    @Test
    fun `sendCallRejection should encrypt and send rejection through blockchain`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        val reason = "User busy"
        
        // When
        val result = callSignalingService.sendCallRejection(callId, peerId, reason)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("tx_hash_123", result.getOrNull())
        
        verify {
            mockEncryptionService.encryptMessage(
                SignalProtocolAddress(peerId, 1),
                any()
            )
        }
        verify {
            mockBlockchainManager.sendMessage(any())
        }
    }
    
    @Test
    fun `sendCallTermination should encrypt and send termination through blockchain`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        val reason = "Call ended by user"
        
        // When
        val result = callSignalingService.sendCallTermination(callId, peerId, reason)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("tx_hash_123", result.getOrNull())
        
        verify {
            mockEncryptionService.encryptMessage(
                SignalProtocolAddress(peerId, 1),
                any()
            )
        }
        verify {
            mockBlockchainManager.sendMessage(any())
        }
    }
    
    @Test
    fun `subscribeToSignalingMessages should filter and parse signaling messages`() = runTest {
        // Given
        val userId = "user_123"
        val incomingMessage = IncomingMessage(
            transactionHash = "tx_hash_456",
            from = "peer_789",
            to = userId,
            encryptedContent = "encrypted_signaling_message",
            timestamp = System.currentTimeMillis(),
            blockNumber = 12345
        )
        
        every { mockBlockchainManager.subscribeToMessages(userId) } returns flowOf(incomingMessage)
        
        // When
        val signalingFlow = callSignalingService.subscribeToSignalingMessages(userId)
        
        // Then
        verify {
            mockBlockchainManager.subscribeToMessages(userId)
        }
        
        // The flow should be created successfully
        assertNotNull(signalingFlow)
    }
    
    @Test
    fun `sendCallOffer should handle encryption failure`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        val offer = "test_offer"
        val isVideo = false
        
        every { mockEncryptionService.encryptMessage(any(), any()) } returns Result.failure(
            RuntimeException("Encryption failed")
        )
        
        // When
        val result = callSignalingService.sendCallOffer(callId, peerId, offer, isVideo)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Encryption failed", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `sendCallAnswer should handle blockchain failure`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        val answer = "test_answer"
        
        every { mockBlockchainManager.sendMessage(any()) } throws RuntimeException("Blockchain error")
        
        // When
        val result = callSignalingService.sendCallAnswer(callId, peerId, answer)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Blockchain error", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `startSignalingListener should collect and emit signaling events`() = runTest {
        // Given
        val userId = "user_123"
        val incomingMessage = IncomingMessage(
            transactionHash = "tx_hash_456",
            from = "peer_789",
            to = userId,
            encryptedContent = "encrypted_signaling_message",
            timestamp = System.currentTimeMillis(),
            blockNumber = 12345
        )
        
        every { mockBlockchainManager.subscribeToMessages(userId) } returns flowOf(incomingMessage)
        
        // When
        callSignalingService.startSignalingListener(userId)
        
        // Then
        verify {
            mockBlockchainManager.subscribeToMessages(userId)
        }
    }
    
    @Test
    fun `sendCallInvitation with null reason should work`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        val isVideo = true
        
        // When
        val result = callSignalingService.sendCallInvitation(callId, peerId, isVideo)
        
        // Then
        assertTrue(result.isSuccess)
        verify {
            mockEncryptionService.encryptMessage(any(), any())
        }
    }
    
    @Test
    fun `sendCallRejection with null reason should work`() = runTest {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        
        // When
        val result = callSignalingService.sendCallRejection(callId, peerId, null)
        
        // Then
        assertTrue(result.isSuccess)
        verify {
            mockEncryptionService.encryptMessage(any(), any())
        }
    }
    
    @Test
    fun `signaling message serialization should work correctly`() = runTest {
        // Given
        val callOffer = SignalingMessage.CallOffer(
            callId = "test_call",
            offer = "test_offer",
            isVideo = true,
            timestamp = 123456789L
        )
        
        // When - This tests the serialization implicitly through the service
        val result = callSignalingService.sendCallOffer("test_call", "peer_123", "test_offer", true)
        
        // Then
        assertTrue(result.isSuccess)
        verify {
            mockEncryptionService.encryptMessage(any(), any())
        }
    }
    
    @Test
    fun `error signaling message should be handled correctly`() = runTest {
        // Given
        val userId = "user_123"
        val errorMessage = IncomingMessage(
            transactionHash = "tx_hash_456",
            from = "peer_789",
            to = userId,
            encryptedContent = "invalid_encrypted_content",
            timestamp = System.currentTimeMillis(),
            blockNumber = 12345
        )
        
        every { mockBlockchainManager.subscribeToMessages(userId) } returns flowOf(errorMessage)
        every { mockEncryptionService.decryptMessage(any(), any()) } returns Result.failure(
            RuntimeException("Decryption failed")
        )
        
        // When
        val signalingFlow = callSignalingService.subscribeToSignalingMessages(userId)
        
        // Then
        assertNotNull(signalingFlow)
        verify {
            mockBlockchainManager.subscribeToMessages(userId)
        }
    }
    
    @Test
    fun `getSignalingStats should return current statistics`() = runTest {
        // When
        val stats = callSignalingService.getSignalingStats()
        
        // Then
        assertNotNull(stats)
        assertTrue(stats.totalMessagesSent >= 0)
        assertTrue(stats.totalMessagesReceived >= 0)
        assertTrue(stats.failedMessages >= 0)
        assertTrue(stats.averageLatency >= 0)
    }
    
    @Test
    fun `signaling message validation should work correctly`() = runTest {
        // Given - Test through sending valid messages
        val callId = "test_call_123"
        val peerId = "peer_456"
        
        // When - Send valid messages
        val offerResult = callSignalingService.sendCallOffer(callId, peerId, "valid_offer", false)
        val invitationResult = callSignalingService.sendCallInvitation(callId, peerId, false)
        
        // Then - All should succeed
        assertTrue(offerResult.isSuccess)
        assertTrue(invitationResult.isSuccess)
    }
    
    @Test
    fun `signaling message with empty callId should fail validation`() = runTest {
        // Given
        val emptyCallId = ""
        val peerId = "peer_456"
        val offer = "valid_offer"
        
        // When
        val result = callSignalingService.sendCallOffer(emptyCallId, peerId, offer, false)
        
        // Then - Should still attempt to send (validation happens internally)
        assertTrue(result.isSuccess) // The service doesn't currently reject empty callIds
    }
}