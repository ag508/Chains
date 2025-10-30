package com.chain.messaging.core.webrtc

import android.content.Context
import com.chain.messaging.core.webrtc.ConnectionState
import com.chain.messaging.core.webrtc.IceServer
import com.chain.messaging.domain.model.CallEvent
import com.chain.messaging.domain.model.CallSession
import com.chain.messaging.domain.model.CallStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.webrtc.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * WebRTC Manager implementation
 * Handles WebRTC connection management, ICE candidate gathering, and STUN/TURN server integration
 */
@Singleton
class WebRTCManagerImpl @Inject constructor(
    private val context: Context,
    private val peerConnectionFactory: PeerConnectionFactory
) : WebRTCManager {
    
    private val activeCalls = ConcurrentHashMap<String, CallSession>()
    private val peerConnections = ConcurrentHashMap<String, PeerConnection>()
    private val mutex = Mutex()
    
    private val _callEvents = MutableSharedFlow<CallEvent>()
    private val _connectionState = MutableSharedFlow<ConnectionState>()
    
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    
    // Default STUN/TURN servers for NAT traversal
    private val defaultIceServers = listOf(
        IceServer("stun:stun.l.google.com:19302"),
        IceServer("stun:stun1.l.google.com:19302"),
        IceServer("stun:stun2.l.google.com:19302")
    )
    
    override suspend fun initialize() {
        // Initialize WebRTC components
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )
    }
    
    override suspend fun createPeerConnection(
        peerId: String,
        isVideo: Boolean,
        iceServers: List<IceServer>
    ): PeerConnection {
        return mutex.withLock {
            val rtcConfig = PeerConnection.RTCConfiguration(
                (iceServers.ifEmpty { defaultIceServers }).map { server ->
                    PeerConnection.IceServer.builder(server.url)
                        .apply {
                            server.username?.let { setUsername(it) }
                            server.credential?.let { setPassword(it) }
                        }
                        .createIceServer()
                }
            ).apply {
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
                tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            }
            
            val observer = createPeerConnectionObserver(peerId)
            val peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, observer)
                ?: throw RuntimeException("Failed to create PeerConnection")
            
            // Add local media stream
            val localStream = getLocalMediaStream(isVideo)
            localStream.audioTracks.forEach { peerConnection.addTrack(it, listOf("local_stream")) }
            if (isVideo) {
                localStream.videoTracks.forEach { peerConnection.addTrack(it, listOf("local_stream")) }
            }
            
            peerConnections[peerId] = peerConnection
            peerConnection
        }
    }
    
    override suspend fun initiateCall(peerId: String, isVideo: Boolean): CallSession {
        val callId = generateCallId()
        val peerConnection = createPeerConnection(peerId, isVideo, defaultIceServers)
        
        val callSession = CallSession(
            id = callId,
            peerId = peerId,
            isVideo = isVideo,
            status = CallStatus.INITIATING,
            localStream = getLocalMediaStream(isVideo),
            remoteStream = null
        )
        
        activeCalls[callId] = callSession
        
        // Create offer
        val offer = createOffer(peerConnection)
        peerConnection.setLocalDescription(createSdpObserver(), offer)
        
        _callEvents.emit(CallEvent.IncomingCall(callSession))
        
        return callSession
    }
    
    override suspend fun acceptCall(callId: String): CallSession {
        val callSession = activeCalls[callId] 
            ?: throw IllegalArgumentException("Call not found: $callId")
        
        val updatedSession = callSession.copy(status = CallStatus.CONNECTING)
        activeCalls[callId] = updatedSession
        
        val peerConnection = peerConnections[callSession.peerId]
            ?: throw IllegalStateException("PeerConnection not found for call: $callId")
        
        // Create answer
        val answer = createAnswer(peerConnection)
        peerConnection.setLocalDescription(createSdpObserver(), answer)
        
        _callEvents.emit(CallEvent.CallAccepted(callId))
        
        return updatedSession
    }
    
    override suspend fun endCall(callId: String) {
        val callSession = activeCalls[callId] ?: return
        
        // Close peer connection
        peerConnections[callSession.peerId]?.close()
        peerConnections.remove(callSession.peerId)
        
        // Update call status
        val endedSession = callSession.copy(status = CallStatus.ENDED)
        activeCalls[callId] = endedSession
        
        _callEvents.emit(CallEvent.CallEnded(callId, null))
        
        // Clean up after a delay
        activeCalls.remove(callId)
    }
    
    override suspend fun getLocalMediaStream(isVideo: Boolean): MediaStream {
        val mediaStream = peerConnectionFactory.createLocalMediaStream("local_stream")
        
        // Add audio track
        if (localAudioTrack == null) {
            val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
            localAudioTrack = peerConnectionFactory.createAudioTrack("audio_track", audioSource)
        }
        localAudioTrack?.let { mediaStream.addTrack(it) }
        
        // Add video track if needed
        if (isVideo && localVideoTrack == null) {
            val videoSource = peerConnectionFactory.createVideoSource(false)
            videoCapturer = createVideoCapturer()
            videoCapturer?.initialize(
                SurfaceTextureHelper.create("CaptureThread", EglBase.create().eglBaseContext),
                context,
                videoSource.capturerObserver
            )
            localVideoTrack = peerConnectionFactory.createVideoTrack("video_track", videoSource)
        }
        
        if (isVideo) {
            localVideoTrack?.let { mediaStream.addTrack(it) }
        }
        
        return mediaStream
    }
    
    override suspend fun addIceCandidate(callId: String, candidate: IceCandidate) {
        val callSession = activeCalls[callId] ?: return
        val peerConnection = peerConnections[callSession.peerId] ?: return
        
        val iceCandidate = org.webrtc.IceCandidate(
            candidate.sdpMid,
            candidate.sdpMLineIndex,
            candidate.sdp
        )
        
        peerConnection.addIceCandidate(iceCandidate)
    }
    
    override fun observeCallEvents(): Flow<CallEvent> = _callEvents.asSharedFlow()
    
    override fun observeConnectionState(): Flow<ConnectionState> = _connectionState.asSharedFlow()
    
    override suspend fun cleanup() {
        // Stop video capturer
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        videoCapturer = null
        
        // Dispose tracks
        localAudioTrack?.dispose()
        localVideoTrack?.dispose()
        localAudioTrack = null
        localVideoTrack = null
        
        // Close all peer connections
        peerConnections.values.forEach { it.close() }
        peerConnections.clear()
        
        // Clear active calls
        activeCalls.clear()
        
        // Dispose factory
        peerConnectionFactory.dispose()
    }
    
    override suspend fun shutdown() {
        // End all active calls
        activeCalls.keys.forEach { callId ->
            endCall(callId)
        }
        
        // Perform cleanup
        cleanup()
        
        // Clear event flows
        _callEvents.resetReplayCache()
        _connectionState.resetReplayCache()
    }
    
    private fun createPeerConnectionObserver(peerId: String) = object : PeerConnection.Observer {
        override fun onSignalingChange(state: PeerConnection.SignalingState?) {
            // Handle signaling state changes
        }
        
        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
            state?.let {
                val connectionState = when (it) {
                    PeerConnection.IceConnectionState.NEW -> ConnectionState.NEW
                    PeerConnection.IceConnectionState.CHECKING -> ConnectionState.CONNECTING
                    PeerConnection.IceConnectionState.CONNECTED -> ConnectionState.CONNECTED
                    PeerConnection.IceConnectionState.COMPLETED -> ConnectionState.CONNECTED
                    PeerConnection.IceConnectionState.DISCONNECTED -> ConnectionState.DISCONNECTED
                    PeerConnection.IceConnectionState.FAILED -> ConnectionState.FAILED
                    PeerConnection.IceConnectionState.CLOSED -> ConnectionState.CLOSED
                }
                _connectionState.tryEmit(connectionState)
            }
        }
        
        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            // Handle ICE connection receiving changes
        }
        
        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
            // Handle ICE gathering state changes
        }
        
        override fun onIceCandidate(candidate: org.webrtc.IceCandidate?) {
            candidate?.let {
                val iceCandidate = IceCandidate(
                    sdp = it.sdp,
                    sdpMLineIndex = it.sdpMLineIndex,
                    sdpMid = it.sdpMid
                )
                
                // Find call ID for this peer
                val callId = activeCalls.values.find { call -> call.peerId == peerId }?.id
                callId?.let { id ->
                    _callEvents.tryEmit(CallEvent.IceCandidateReceived(id, iceCandidate))
                }
            }
        }
        
        override fun onIceCandidatesRemoved(candidates: Array<out org.webrtc.IceCandidate>?) {
            // Handle removed ICE candidates
        }
        
        override fun onAddStream(stream: MediaStream?) {
            stream?.let {
                val callId = activeCalls.values.find { call -> call.peerId == peerId }?.id
                callId?.let { id ->
                    // Update call session with remote stream
                    activeCalls[id]?.let { session ->
                        activeCalls[id] = session.copy(
                            remoteStream = stream,
                            status = CallStatus.CONNECTED
                        )
                    }
                    _callEvents.tryEmit(CallEvent.RemoteStreamAdded(id, stream))
                }
            }
        }
        
        override fun onRemoveStream(stream: MediaStream?) {
            val callId = activeCalls.values.find { call -> call.peerId == peerId }?.id
            callId?.let { id ->
                _callEvents.tryEmit(CallEvent.RemoteStreamRemoved(id))
            }
        }
        
        override fun onDataChannel(dataChannel: DataChannel?) {
            // Handle data channel creation
        }
        
        override fun onRenegotiationNeeded() {
            // Handle renegotiation needed
        }
        
        override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
            // Handle track addition
        }
    }
    
    private fun createSdpObserver() = object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription?) {
            // Handle SDP creation success
        }
        
        override fun onSetSuccess() {
            // Handle SDP set success
        }
        
        override fun onCreateFailure(error: String?) {
            // Handle SDP creation failure
        }
        
        override fun onSetFailure(error: String?) {
            // Handle SDP set failure
        }
    }
    
    private suspend fun createOffer(peerConnection: PeerConnection): SessionDescription {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        
        return suspendCoroutine { continuation ->
            peerConnection.createOffer(object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                    sessionDescription?.let { continuation.resume(it) }
                        ?: continuation.resumeWithException(RuntimeException("Failed to create offer"))
                }
                
                override fun onSetSuccess() {}
                override fun onCreateFailure(error: String?) {
                    continuation.resumeWithException(RuntimeException("Create offer failed: $error"))
                }
                override fun onSetFailure(error: String?) {}
            }, constraints)
        }
    }
    
    private suspend fun createAnswer(peerConnection: PeerConnection): SessionDescription {
        val constraints = MediaConstraints()
        
        return suspendCoroutine { continuation ->
            peerConnection.createAnswer(object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                    sessionDescription?.let { continuation.resume(it) }
                        ?: continuation.resumeWithException(RuntimeException("Failed to create answer"))
                }
                
                override fun onSetSuccess() {}
                override fun onCreateFailure(error: String?) {
                    continuation.resumeWithException(RuntimeException("Create answer failed: $error"))
                }
                override fun onSetFailure(error: String?) {}
            }, constraints)
        }
    }
    
    private fun createVideoCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        
        // Try to find front camera first
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        
        // Fall back to any camera
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        
        return null
    }
    
    private fun generateCallId(): String {
        return "call_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}