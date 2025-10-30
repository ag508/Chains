package com.chain.messaging.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.webrtc.*
import com.chain.messaging.domain.model.CallEvent
import com.chain.messaging.domain.model.CallSession
import com.chain.messaging.domain.model.CallStatus
import com.chain.messaging.domain.model.NetworkQuality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import javax.inject.Inject

/**
 * ViewModel for call screen
 * Manages call state, controls, and WebRTC interactions
 */
@HiltViewModel
class CallViewModel @Inject constructor(
    private val callManager: CallManager,
    private val webRTCManager: WebRTCManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()
    
    private var currentCallId: String? = null
    private var callStartTime: Long = 0
    private var eglBase: EglBase? = null
    
    init {
        // Initialize EGL context for video rendering
        eglBase = EglBase.create()
        _uiState.value = _uiState.value.copy(eglBaseContext = eglBase?.eglBaseContext)
        
        // Observe call state events
        viewModelScope.launch {
            callManager.observeCallStateEvents().collect { event ->
                handleCallStateEvent(event)
            }
        }
        
        // Observe WebRTC events
        viewModelScope.launch {
            webRTCManager.observeCallEvents().collect { event ->
                handleWebRTCEvent(event)
            }
        }
        
        // Observe connection state
        viewModelScope.launch {
            webRTCManager.observeConnectionState().collect { state ->
                updateNetworkQuality(state)
            }
        }
        
        // Update call duration timer
        viewModelScope.launch {
            while (true) {
                if (_uiState.value.callSession?.status == CallStatus.CONNECTED && callStartTime > 0) {
                    val duration = System.currentTimeMillis() - callStartTime
                    _uiState.value = _uiState.value.copy(
                        callDuration = formatDuration(duration)
                    )
                }
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }
    
    fun loadCall(callId: String) {
        currentCallId = callId
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val callSession = callManager.getActiveCall(callId)
                if (callSession != null) {
                    _uiState.value = _uiState.value.copy(
                        callSession = callSession,
                        isLoading = false
                    )
                    
                    if (callSession.status == CallStatus.CONNECTED) {
                        callStartTime = System.currentTimeMillis()
                    }
                    
                    // Get local media stream
                    val localStream = webRTCManager.getLocalMediaStream(callSession.isVideo)
                    val localVideoTrack = if (callSession.isVideo) {
                        localStream.videoTracks.firstOrNull()
                    } else null
                    
                    _uiState.value = _uiState.value.copy(
                        localVideoTrack = localVideoTrack,
                        isVideoEnabled = callSession.isVideo
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Call not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load call"
                )
            }
        }
    }
    
    fun toggleMute() {
        val currentState = _uiState.value
        val newMuteState = !currentState.isMuted
        
        // Toggle audio track enabled state
        currentState.callSession?.localStream?.audioTracks?.forEach { audioTrack ->
            audioTrack.setEnabled(!newMuteState)
        }
        
        _uiState.value = currentState.copy(isMuted = newMuteState)
    }
    
    fun toggleVideo() {
        val currentState = _uiState.value
        val newVideoState = !currentState.isVideoEnabled
        
        viewModelScope.launch {
            try {
                if (newVideoState) {
                    // Enable video
                    val localStream = webRTCManager.getLocalMediaStream(true)
                    val videoTrack = localStream.videoTracks.firstOrNull()
                    
                    _uiState.value = currentState.copy(
                        isVideoEnabled = true,
                        localVideoTrack = videoTrack
                    )
                } else {
                    // Disable video
                    currentState.localVideoTrack?.setEnabled(false)
                    _uiState.value = currentState.copy(
                        isVideoEnabled = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    error = "Failed to toggle video: ${e.message}"
                )
            }
        }
    }
    
    fun toggleSpeaker() {
        val currentState = _uiState.value
        val newSpeakerState = !currentState.isSpeakerOn
        
        // Note: Speaker toggle would typically involve AudioManager
        // For now, we'll just update the UI state
        _uiState.value = currentState.copy(isSpeakerOn = newSpeakerState)
    }
    
    fun switchCamera() {
        // Camera switching would typically involve the video capturer
        // For now, we'll just trigger a UI update
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            // Could add camera facing state here
        )
    }
    
    fun endCall() {
        currentCallId?.let { callId ->
            viewModelScope.launch {
                try {
                    callManager.endCall(callId, "Call ended by user")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to end call: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun handleCallStateEvent(event: CallStateEvent) {
        when (event) {
            is CallStateEvent.CallInitiated -> {
                val localEvent = event
                if (localEvent.callSession.id == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        callSession = localEvent.callSession,
                        isLoading = false
                    )
                }
            }
            is CallStateEvent.IncomingCall -> {
                val localEvent = event
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        callSession = _uiState.value.callSession?.copy(status = CallStatus.RINGING)
                    )
                }
            }
            is CallStateEvent.CallAccepted -> {
                val localEvent = event
                if (localEvent.callSession.id == currentCallId) {
                    _uiState.value = _uiState.value.copy(callSession = localEvent.callSession)
                    callStartTime = System.currentTimeMillis()
                }
            }
            is CallStateEvent.CallRejected -> {
                val localEvent = event
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        callSession = _uiState.value.callSession?.copy(status = CallStatus.ENDED),
                        error = "Call was rejected: ${localEvent.reason ?: "No reason provided"}"
                    )
                }
            }
            is CallStateEvent.CallEnded -> {
                val localEvent = event
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        callSession = _uiState.value.callSession?.copy(status = CallStatus.ENDED)
                    )
                }
            }
            is CallStateEvent.CallTimeout -> {
                val localEvent = event
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        callSession = _uiState.value.callSession?.copy(status = CallStatus.FAILED),
                        error = "Call timed out"
                    )
                }
            }
            is CallStateEvent.OfferReceived -> {
                val localEvent = event
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        callSession = _uiState.value.callSession?.copy(status = CallStatus.CONNECTING)
                    )
                }
            }
            is CallStateEvent.AnswerReceived -> {
                val localEvent = event
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        callSession = _uiState.value.callSession?.copy(status = CallStatus.CONNECTING)
                    )
                }
            }
            is CallStateEvent.IceCandidateReceived -> {
                // ICE candidates are typically handled by the WebRTC manager
                // No UI state changes needed for this event
            }
            is CallStateEvent.SignalingError -> {
                val localEvent = event
                _uiState.value = _uiState.value.copy(error = localEvent.message)
            }
            is CallStateEvent.WebRTCEvent -> {
                val localEvent = event
                handleWebRTCEvent(localEvent.event)
            }
        }
    }
    
    private fun handleWebRTCEvent(event: CallEvent) {
        when (event) {
            is CallEvent.RemoteStreamAdded -> {
                val localEvent = event // Local variable for smart cast
                if (localEvent.callId == currentCallId) {
                    val remoteVideoTrack = localEvent.stream.videoTracks.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        remoteVideoTrack = remoteVideoTrack,
                        callSession = _uiState.value.callSession?.copy(
                            remoteStream = localEvent.stream,
                            status = CallStatus.CONNECTED
                        )
                    )
                    
                    if (callStartTime == 0L) {
                        callStartTime = System.currentTimeMillis()
                    }
                }
            }
            is CallEvent.RemoteStreamRemoved -> {
                val localEvent = event // Local variable for smart cast
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(remoteVideoTrack = null)
                }
            }
            is CallEvent.CallFailed -> {
                val localEvent = event // Local variable for smart cast
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        error = localEvent.error,
                        callSession = _uiState.value.callSession?.copy(status = CallStatus.FAILED)
                    )
                }
            }
            is CallEvent.IncomingCallEvent -> {
                // Handle incoming call event if needed for this view
                val localEvent = event
                if (localEvent.callSession.id == currentCallId) {
                    _uiState.value = _uiState.value.copy(callSession = localEvent.callSession)
                }
            }
            is CallEvent.CallAccepted -> {
                // Handle call accepted event
                val localEvent = event
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        callSession = _uiState.value.callSession?.copy(status = CallStatus.CONNECTED)
                    )
                    if (callStartTime == 0L) {
                        callStartTime = System.currentTimeMillis()
                    }
                }
            }
            is CallEvent.CallEnded -> {
                // Handle call ended event
                val localEvent = event
                if (localEvent.callId == currentCallId) {
                    _uiState.value = _uiState.value.copy(
                        callSession = _uiState.value.callSession?.copy(status = CallStatus.ENDED)
                    )
                }
            }
            is CallEvent.IceCandidateReceived -> {
                // Handle ICE candidate - typically handled by WebRTC manager
                // No UI state changes needed for this event
            }
            else -> {
                // Handle any other event types that don't require UI updates
                // This ensures the when expression is exhaustive
            }
        }
    }
    
    private fun updateNetworkQuality(connectionState: ConnectionState) {
        val quality = when (connectionState) {
            ConnectionState.CONNECTED -> NetworkQuality.EXCELLENT
            ConnectionState.CONNECTING -> NetworkQuality.GOOD
            ConnectionState.DISCONNECTED -> NetworkQuality.POOR
            ConnectionState.FAILED -> NetworkQuality.BAD
            else -> NetworkQuality.GOOD
        }
        
        _uiState.value = _uiState.value.copy(networkQuality = quality)
    }
    
    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        eglBase?.release()
    }
}

/**
 * UI state for call screen
 */
data class CallUiState(
    val isLoading: Boolean = false,
    val callSession: CallSession? = null,
    val error: String? = null,
    val isMuted: Boolean = false,
    val isVideoEnabled: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val callDuration: String = "00:00",
    val networkQuality: NetworkQuality? = null,
    val localVideoTrack: VideoTrack? = null,
    val remoteVideoTrack: VideoTrack? = null,
    val eglBaseContext: EglBase.Context? = null
)