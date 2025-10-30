package com.chain.messaging.core.webrtc

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.blockchain.EncryptedMessage
import com.chain.messaging.core.blockchain.MessageType
import com.chain.messaging.core.crypto.SignalEncryptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.signal.libsignal.protocol.SignalProtocolAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling WebRTC call signaling through blockchain
 * Implements requirement 6.2 for call offer/answer exchange via blockchain messages
 */
@Singleton
class CallSignalingService @Inject constructor(
    private val blockchainManager: BlockchainManager,
    private val encryptionService: SignalEncryptionService
) {
    
    private val _signalingEvents = MutableSharedFlow<SignalingEvent>()
    val signalingEvents: Flow<SignalingEvent> = _signalingEvents.asSharedFlow()
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Send call offer through blockchain
     */
    suspend fun sendCallOffer(
        callId: String,
        peerId: String,
        offer: String,
        isVideo: Boolean
    ): Result<String> {
        return try {
            val signalingMessage = SignalingMessage.CallOffer(
                callId = callId,
                offer = offer,
                isVideo = isVideo,
                timestamp = System.currentTimeMillis()
            )
            
            val txHash = sendSignalingMessage(peerId, signalingMessage)
            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send call answer through blockchain
     */
    suspend fun sendCallAnswer(
        callId: String,
        peerId: String,
        answer: String
    ): Result<String> {
        return try {
            val signalingMessage = SignalingMessage.CallAnswer(
                callId = callId,
                answer = answer,
                timestamp = System.currentTimeMillis()
            )
            
            val txHash = sendSignalingMessage(peerId, signalingMessage)
            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send ICE candidate through blockchain
     */
    suspend fun sendIceCandidate(
        callId: String,
        peerId: String,
        candidate: IceCandidate
    ): Result<String> {
        return try {
            val signalingMessage = SignalingMessage.IceCandidate(
                callId = callId,
                candidate = candidate,
                timestamp = System.currentTimeMillis()
            )
            
            val txHash = sendSignalingMessage(peerId, signalingMessage)
            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send call invitation through blockchain
     */
    suspend fun sendCallInvitation(
        callId: String,
        peerId: String,
        isVideo: Boolean
    ): Result<String> {
        return try {
            val signalingMessage = SignalingMessage.CallInvitation(
                callId = callId,
                isVideo = isVideo,
                timestamp = System.currentTimeMillis()
            )
            
            val txHash = sendSignalingMessage(peerId, signalingMessage)
            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send call acceptance through blockchain
     */
    suspend fun sendCallAcceptance(
        callId: String,
        peerId: String
    ): Result<String> {
        return try {
            val signalingMessage = SignalingMessage.CallAcceptance(
                callId = callId,
                timestamp = System.currentTimeMillis()
            )
            
            val txHash = sendSignalingMessage(peerId, signalingMessage)
            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send call rejection through blockchain
     */
    suspend fun sendCallRejection(
        callId: String,
        peerId: String,
        reason: String? = null
    ): Result<String> {
        return try {
            val signalingMessage = SignalingMessage.CallRejection(
                callId = callId,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
            
            val txHash = sendSignalingMessage(peerId, signalingMessage)
            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send call termination through blockchain
     */
    suspend fun sendCallTermination(
        callId: String,
        peerId: String,
        reason: String? = null
    ): Result<String> {
        return try {
            val signalingMessage = SignalingMessage.CallTermination(
                callId = callId,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
            
            val txHash = sendSignalingMessage(peerId, signalingMessage)
            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Subscribe to incoming signaling messages for a user
     */
    fun subscribeToSignalingMessages(userId: String): Flow<SignalingEvent> {
        return blockchainManager.subscribeToMessages(userId)
            .filter { message -> isSignalingMessage(message.encryptedContent) }
            .map { message ->
                try {
                    val decryptedContent = decryptSignalingMessage(message.from, message.encryptedContent)
                    val signalingMessage = json.decodeFromString<SignalingMessage>(decryptedContent)
                    
                    SignalingEvent(
                        fromPeerId = message.from,
                        message = signalingMessage,
                        transactionHash = message.transactionHash,
                        timestamp = message.timestamp
                    )
                } catch (e: Exception) {
                    // Return error event if decryption/parsing fails
                    SignalingEvent(
                        fromPeerId = message.from,
                        message = SignalingMessage.Error("Failed to parse signaling message: ${e.message}"),
                        transactionHash = message.transactionHash,
                        timestamp = message.timestamp
                    )
                }
            }
    }
    
    /**
     * Start listening for signaling messages
     */
    suspend fun startSignalingListener(userId: String) {
        subscribeToSignalingMessages(userId).collect { event ->
            _signalingEvents.emit(event)
        }
    }
    
    private suspend fun sendSignalingMessage(
        peerId: String,
        signalingMessage: SignalingMessage
    ): String {
        val messageJson = json.encodeToString(signalingMessage)
        
        // Encrypt the signaling message
        val recipientAddress = SignalProtocolAddress(peerId, 1)
        val encryptedContent = encryptionService.encryptMessage(
            recipientAddress,
            messageJson.toByteArray()
        ).getOrThrow()
        
        // Send through blockchain with special signaling message type
        val blockchainMessage = EncryptedMessage(
            content = encryptedContent.ciphertext.toString(Charsets.UTF_8),
            type = MessageType.SYSTEM, // Use SYSTEM type for signaling messages
            keyId = "webrtc_signaling",
            timestamp = System.currentTimeMillis()
        )
        
        return blockchainManager.sendMessage(blockchainMessage)
    }
    
    private fun isSignalingMessage(encryptedContent: String): Boolean {
        // Simple heuristic to identify signaling messages
        // In practice, you might use a more sophisticated method
        return try {
            val decrypted = decryptSignalingMessage("", encryptedContent)
            decrypted.contains("\"type\"") && 
            (decrypted.contains("CallOffer") || 
             decrypted.contains("CallAnswer") || 
             decrypted.contains("IceCandidate") ||
             decrypted.contains("CallInvitation") ||
             decrypted.contains("CallAcceptance") ||
             decrypted.contains("CallRejection") ||
             decrypted.contains("CallTermination"))
        } catch (e: Exception) {
            false
        }
    }
    
    private fun decryptSignalingMessage(fromPeerId: String, encryptedContent: String): String {
        // Decrypt the signaling message
        val senderAddress = SignalProtocolAddress(fromPeerId, 1)
        val decryptedBytes = encryptionService.decryptMessage(
            senderAddress,
            encryptedContent.toByteArray()
        ).getOrThrow()
        
        return String(decryptedBytes)
    }
    
    /**
     * Get signaling message statistics for monitoring
     */
    fun getSignalingStats(): SignalingStats {
        return SignalingStats(
            totalMessagesSent = 0, // In real implementation, track these metrics
            totalMessagesReceived = 0,
            failedMessages = 0,
            averageLatency = 0L
        )
    }
    
    /**
     * Validate signaling message format
     */
    private fun validateSignalingMessage(message: SignalingMessage): Boolean {
        return when (message) {
            is SignalingMessage.CallOffer -> message.callId.isNotBlank() && message.offer.isNotBlank()
            is SignalingMessage.CallAnswer -> message.callId.isNotBlank() && message.answer.isNotBlank()
            is SignalingMessage.IceCandidate -> message.callId.isNotBlank() && message.candidate.sdp.isNotBlank()
            is SignalingMessage.CallInvitation -> message.callId.isNotBlank()
            is SignalingMessage.CallAcceptance -> message.callId.isNotBlank()
            is SignalingMessage.CallRejection -> message.callId.isNotBlank()
            is SignalingMessage.CallTermination -> message.callId.isNotBlank()
            is SignalingMessage.Error -> true
        }
    }
}

/**
 * Signaling message sealed class for different WebRTC signaling types
 */
@Serializable
sealed class SignalingMessage {
    @Serializable
    data class CallOffer(
        val callId: String,
        val offer: String,
        val isVideo: Boolean,
        val timestamp: Long
    ) : SignalingMessage()
    
    @Serializable
    data class CallAnswer(
        val callId: String,
        val answer: String,
        val timestamp: Long
    ) : SignalingMessage()
    
    @Serializable
    data class IceCandidate(
        val callId: String,
        val candidate: com.chain.messaging.core.webrtc.IceCandidate,
        val timestamp: Long
    ) : SignalingMessage()
    
    @Serializable
    data class CallInvitation(
        val callId: String,
        val isVideo: Boolean,
        val timestamp: Long
    ) : SignalingMessage()
    
    @Serializable
    data class CallAcceptance(
        val callId: String,
        val timestamp: Long
    ) : SignalingMessage()
    
    @Serializable
    data class CallRejection(
        val callId: String,
        val reason: String? = null,
        val timestamp: Long
    ) : SignalingMessage()
    
    @Serializable
    data class CallTermination(
        val callId: String,
        val reason: String? = null,
        val timestamp: Long
    ) : SignalingMessage()
    
    @Serializable
    data class Error(
        val message: String
    ) : SignalingMessage()
}

/**
 * Signaling event data class
 */
data class SignalingEvent(
    val fromPeerId: String,
    val message: SignalingMessage,
    val transactionHash: String,
    val timestamp: Long
)

/**
 * Signaling statistics for monitoring
 */
data class SignalingStats(
    val totalMessagesSent: Long,
    val totalMessagesReceived: Long,
    val failedMessages: Long,
    val averageLatency: Long
)