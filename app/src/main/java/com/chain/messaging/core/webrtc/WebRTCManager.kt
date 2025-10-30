package com.chain.messaging.core.webrtc

import com.chain.messaging.domain.model.CallEvent
import com.chain.messaging.domain.model.CallSession
import kotlinx.coroutines.flow.Flow
import org.webrtc.MediaStream
import org.webrtc.PeerConnection

/**
 * WebRTC Manager interface for handling real-time voice and video communication
 * Implements requirements 6.1 and 6.5 for WebRTC connection establishment and NAT traversal
 */
interface WebRTCManager {
    /**
     * Initialize WebRTC components and configuration
     */
    suspend fun initialize()
    
    /**
     * Create a new peer connection for a call
     */
    suspend fun createPeerConnection(
        peerId: String,
        isVideo: Boolean,
        iceServers: List<IceServer>
    ): PeerConnection
    
    /**
     * Initiate a call to another peer
     */
    suspend fun initiateCall(peerId: String, isVideo: Boolean): CallSession
    
    /**
     * Accept an incoming call
     */
    suspend fun acceptCall(callId: String): CallSession
    
    /**
     * End an active call
     */
    suspend fun endCall(callId: String)
    
    /**
     * Get local media stream (audio/video)
     */
    suspend fun getLocalMediaStream(isVideo: Boolean): MediaStream
    
    /**
     * Handle ICE candidate exchange
     */
    suspend fun addIceCandidate(callId: String, candidate: IceCandidate)
    
    /**
     * Observe call events
     */
    fun observeCallEvents(): Flow<CallEvent>
    
    /**
     * Observe connection state changes
     */
    fun observeConnectionState(): Flow<ConnectionState>
    
    /**
     * Clean up resources
     */
    suspend fun cleanup()
    
    /**
     * Shutdown WebRTC manager
     */
    suspend fun shutdown()
}



/**
 * Connection state enumeration
 */
enum class ConnectionState {
    NEW,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    FAILED,
    CLOSED
}

/**
 * ICE server configuration
 */
data class IceServer(
    val url: String,
    val username: String? = null,
    val credential: String? = null
)

/**
 * ICE candidate data
 */
@kotlinx.serialization.Serializable
data class IceCandidate(
    val sdp: String,
    val sdpMLineIndex: Int,
    val sdpMid: String?
)

