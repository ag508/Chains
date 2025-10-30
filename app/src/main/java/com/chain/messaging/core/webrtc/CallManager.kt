package com.chain.messaging.core.webrtc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.webrtc.SessionDescription
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Call manager that coordinates WebRTC connections with blockchain signaling
 * Implements requirements 6.1, 6.2 for call management and signaling
 */
@Singleton
class CallManager @Inject constructor(
    private val webRTCManager: WebRTCManager,
    private val callSignalingService: CallSignalingService,
    private val iceServerProvider: IceServerProvider
) {
    
    private val activeCalls = ConcurrentHashMap<String, CallSession>()
    private val pendingCalls = ConcurrentHashMap<String, PendingCall>()
    private val mutex = Mutex()
    
    private val _callStateEvents = MutableSharedFlow<CallStateEvent>()
    val callStateEvents: Flow<CallStateEvent> = _callStateEvents.asSharedFlow()
    
    /**
     * Initiate an outgoing call
     */
    suspend fun initiateCall(peerId: String, isVideo: Boolean): Result<CallSession> {
        return try {
            mutex.withLock {
                val callId = generateCallId()
                
                // Create WebRTC call session
                val callSession = webRTCManager.initiateCall(peerId, isVideo)
                activeCalls[callId] = callSession.copy(id = callId)
                
                // Send call invitation through blockchain
                callSignalingService.sendCallInvitation(callId, peerId, isVideo)
                
                // Store pending call info
                pendingCalls[callId] = PendingCall(
                    callId = callId,
                    peerId = peerId,
                    isVideo = isVideo,
                    isOutgoing = true,
                    status = PendingCallStatus.INVITATION_SENT
                )
                
                _callStateEvents.emit(CallStateEvent.CallInitiated(callSession.copy(id = callId)))
                
                Result.success(callSession.copy(id = callId))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Accept an incoming call
     */
    suspend fun acceptCall(callId: String): Result<CallSession> {
        return try {
            mutex.withLock {
                val pendingCall = pendingCalls[callId]
                    ?: return Result.failure(IllegalArgumentException("Call not found: $callId"))
                
                // Accept the WebRTC call
                val callSession = webRTCManager.acceptCall(callId)
                activeCalls[callId] = callSession
                
                // Send acceptance through blockchain
                callSignalingService.sendCallAcceptance(callId, pendingCall.peerId)
                
                // Update pending call status
                pendingCalls[callId] = pendingCall.copy(status = PendingCallStatus.ACCEPTED)
                
                _callStateEvents.emit(CallStateEvent.CallAccepted(callSession))
                
                Result.success(callSession)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reject an incoming call
     */
    suspend fun rejectCall(callId: String, reason: String? = null): Result<Unit> {
        return try {
            mutex.withLock {
                val pendingCall = pendingCalls[callId]
                    ?: return Result.failure(IllegalArgumentException("Call not found: $callId"))
                
                // Send rejection through blockchain
                callSignalingService.sendCallRejection(callId, pendingCall.peerId, reason)
                
                // Clean up
                pendingCalls.remove(callId)
                activeCalls.remove(callId)
                
                _callStateEvents.emit(CallStateEvent.CallRejected(callId, reason))
                
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * End an active call
     */
    suspend fun endCall(callId: String, reason: String? = null): Result<Unit> {
        return try {
            mutex.withLock {
                val callSession = activeCalls[callId]
                val pendingCall = pendingCalls[callId]
                
                if (callSession == null && pendingCall == null) {
                    return Result.failure(IllegalArgumentException("Call not found: $callId"))
                }
                
                // End WebRTC call
                webRTCManager.endCall(callId)
                
                // Send termination through blockchain
                val peerId = callSession?.peerId ?: pendingCall?.peerId
                peerId?.let {
                    callSignalingService.sendCallTermination(callId, it, reason)
                }
                
                // Clean up
                activeCalls.remove(callId)
                pendingCalls.remove(callId)
                
                _callStateEvents.emit(CallStateEvent.CallEnded(callId, reason))
                
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get active call by ID
     */
    fun getActiveCall(callId: String): CallSession? {
        return activeCalls[callId]
    }
    
    /**
     * Get all active calls
     */
    fun getActiveCalls(): List<CallSession> {
        return activeCalls.values.toList()
    }
    
    /**
     * Get pending call by ID
     */
    fun getPendingCall(callId: String): PendingCall? {
        return pendingCalls[callId]
    }
    
    /**
     * Get all pending calls
     */
    fun getPendingCalls(): List<PendingCall> {
        return pendingCalls.values.toList()
    }
    
    /**
     * Start listening for signaling events
     */
    suspend fun startSignalingListener(userId: String) {
        // Listen for signaling events
        callSignalingService.subscribeToSignalingMessages(userId).collect { event ->
            handleSignalingEvent(event)
        }
    }
    
    /**
     * Observe call events from WebRTC manager
     */
    fun observeWebRTCEvents(): Flow<CallEvent> {
        return webRTCManager.observeCallEvents()
    }
    
    /**
     * Observe combined call state events
     */
    fun observeCallStateEvents(): Flow<CallStateEvent> {
        return combine(
            callStateEvents,
            webRTCManager.observeCallEvents().map { CallStateEvent.WebRTCEvent(it) }
        ) { stateEvent, webrtcEvent ->
            listOf(stateEvent, webrtcEvent)
        }.map { events -> events }
            .filter { it.isNotEmpty() }
            .map { it.first() }
    }
    
    private suspend fun handleSignalingEvent(event: SignalingEvent) {
        when (val message = event.message) {
            is SignalingMessage.CallInvitation -> {
                handleIncomingCallInvitation(event.fromPeerId, message)
            }
            is SignalingMessage.CallAcceptance -> {
                handleCallAcceptance(event.fromPeerId, message)
            }
            is SignalingMessage.CallRejection -> {
                handleCallRejection(event.fromPeerId, message)
            }
            is SignalingMessage.CallOffer -> {
                handleCallOffer(event.fromPeerId, message)
            }
            is SignalingMessage.CallAnswer -> {
                handleCallAnswer(event.fromPeerId, message)
            }
            is SignalingMessage.IceCandidate -> {
                handleIceCandidate(event.fromPeerId, message)
            }
            is SignalingMessage.CallTermination -> {
                handleCallTermination(event.fromPeerId, message)
            }
            is SignalingMessage.Error -> {
                _callStateEvents.emit(CallStateEvent.SignalingError(message.message))
            }
        }
    }
    
    private suspend fun handleIncomingCallInvitation(fromPeerId: String, message: SignalingMessage.CallInvitation) {
        mutex.withLock {
            // Check if we already have a pending call from this peer
            val existingCall = pendingCalls.values.find { 
                it.peerId == fromPeerId && it.status == PendingCallStatus.INVITATION_RECEIVED 
            }
            
            if (existingCall != null) {
                // Reject duplicate invitation
                callSignalingService.sendCallRejection(message.callId, fromPeerId, "Duplicate call invitation")
                return@withLock
            }
            
            // Store pending call
            pendingCalls[message.callId] = PendingCall(
                callId = message.callId,
                peerId = fromPeerId,
                isVideo = message.isVideo,
                isOutgoing = false,
                status = PendingCallStatus.INVITATION_RECEIVED
            )
            
            _callStateEvents.emit(CallStateEvent.IncomingCall(message.callId, fromPeerId, message.isVideo))
            
            // Set timeout for incoming call (30 seconds)
            scheduleCallTimeout(message.callId, 30_000L)
        }
    }
    
    private suspend fun handleCallAcceptance(fromPeerId: String, message: SignalingMessage.CallAcceptance) {
        mutex.withLock {
            val pendingCall = pendingCalls[message.callId]
            if (pendingCall != null && pendingCall.isOutgoing) {
                // Start WebRTC offer/answer exchange
                val callSession = activeCalls[message.callId]
                if (callSession != null) {
                    // Create and send offer
                    val peerConnection = webRTCManager.createPeerConnection(
                        fromPeerId,
                        pendingCall.isVideo,
                        iceServerProvider.getDefaultStunServers()
                    )
                    
                    // This would typically involve creating an offer and sending it
                    // For now, we'll emit an event that the call was accepted
                    _callStateEvents.emit(CallStateEvent.CallAccepted(callSession))
                }
            }
        }
    }
    
    private suspend fun handleCallRejection(fromPeerId: String, message: SignalingMessage.CallRejection) {
        mutex.withLock {
            pendingCalls.remove(message.callId)
            activeCalls.remove(message.callId)
            
            _callStateEvents.emit(CallStateEvent.CallRejected(message.callId, message.reason))
        }
    }
    
    private suspend fun handleCallOffer(fromPeerId: String, message: SignalingMessage.CallOffer) {
        // Handle WebRTC offer
        val callSession = activeCalls[message.callId]
        if (callSession != null) {
            // Set remote description and create answer
            // This would involve WebRTC SDP handling
            _callStateEvents.emit(CallStateEvent.OfferReceived(message.callId, message.offer))
        }
    }
    
    private suspend fun handleCallAnswer(fromPeerId: String, message: SignalingMessage.CallAnswer) {
        // Handle WebRTC answer
        val callSession = activeCalls[message.callId]
        if (callSession != null) {
            // Set remote description
            _callStateEvents.emit(CallStateEvent.AnswerReceived(message.callId, message.answer))
        }
    }
    
    private suspend fun handleIceCandidate(fromPeerId: String, message: SignalingMessage.IceCandidate) {
        // Handle ICE candidate
        webRTCManager.addIceCandidate(message.callId, message.candidate)
        _callStateEvents.emit(CallStateEvent.IceCandidateReceived(message.callId, message.candidate))
    }
    
    private suspend fun handleCallTermination(fromPeerId: String, message: SignalingMessage.CallTermination) {
        mutex.withLock {
            // End the call
            webRTCManager.endCall(message.callId)
            
            // Clean up
            activeCalls.remove(message.callId)
            pendingCalls.remove(message.callId)
            
            _callStateEvents.emit(CallStateEvent.CallEnded(message.callId, message.reason))
        }
    }
    
    private fun generateCallId(): String {
        return "call_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Schedule call timeout for automatic cleanup
     */
    private suspend fun scheduleCallTimeout(callId: String, timeoutMs: Long) {
        // In a real implementation, you would use a timer or coroutine delay
        // For now, we'll just emit a timeout event after the specified time
        kotlinx.coroutines.delay(timeoutMs)
        
        mutex.withLock {
            val pendingCall = pendingCalls[callId]
            if (pendingCall != null && pendingCall.status == PendingCallStatus.INVITATION_RECEIVED) {
                // Auto-reject expired call
                if (!pendingCall.isOutgoing) {
                    callSignalingService.sendCallRejection(callId, pendingCall.peerId, "Call timeout")
                }
                
                pendingCalls.remove(callId)
                activeCalls.remove(callId)
                
                _callStateEvents.emit(CallStateEvent.CallTimeout(callId))
            }
        }
    }
    
    /**
     * Get call statistics for monitoring
     */
    fun getCallStatistics(): CallStatistics {
        return CallStatistics(
            activeCalls = activeCalls.size,
            pendingCalls = pendingCalls.size,
            totalCallsInitiated = 0, // In real implementation, track these metrics
            totalCallsReceived = 0,
            totalCallsCompleted = 0,
            averageCallDuration = 0L
        )
    }
}

/**
 * Pending call information
 */
data class PendingCall(
    val callId: String,
    val peerId: String,
    val isVideo: Boolean,
    val isOutgoing: Boolean,
    val status: PendingCallStatus,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Pending call status
 */
enum class PendingCallStatus {
    INVITATION_SENT,
    INVITATION_RECEIVED,
    ACCEPTED,
    REJECTED,
    EXPIRED
}

/**
 * Call state events
 */
sealed class CallStateEvent {
    data class CallInitiated(val callSession: CallSession) : CallStateEvent()
    data class IncomingCall(val callId: String, val fromPeerId: String, val isVideo: Boolean) : CallStateEvent()
    data class CallAccepted(val callSession: CallSession) : CallStateEvent()
    data class CallRejected(val callId: String, val reason: String?) : CallStateEvent()
    data class CallEnded(val callId: String, val reason: String?) : CallStateEvent()
    data class CallTimeout(val callId: String) : CallStateEvent()
    data class OfferReceived(val callId: String, val offer: String) : CallStateEvent()
    data class AnswerReceived(val callId: String, val answer: String) : CallStateEvent()
    data class IceCandidateReceived(val callId: String, val candidate: IceCandidate) : CallStateEvent()
    data class SignalingError(val message: String) : CallStateEvent()
    data class WebRTCEvent(val event: CallEvent) : CallStateEvent()
}

/**
 * Call statistics for monitoring
 */
data class CallStatistics(
    val activeCalls: Int,
    val pendingCalls: Int,
    val totalCallsInitiated: Long,
    val totalCallsReceived: Long,
    val totalCallsCompleted: Long,
    val averageCallDuration: Long
)