package com.chain.messaging.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.webrtc.CallManager
import com.chain.messaging.core.webrtc.CallNotificationService
import com.chain.messaging.core.webrtc.PendingCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for incoming call screen
 * Manages incoming call state and actions
 */
@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    private val callManager: CallManager,
    private val callNotificationService: CallNotificationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(IncomingCallUiState())
    val uiState: StateFlow<IncomingCallUiState> = _uiState.asStateFlow()
    
    private var currentCallId: String? = null
    
    fun loadIncomingCall(callId: String) {
        currentCallId = callId
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val pendingCall = callManager.getPendingCall(callId)
                if (pendingCall != null) {
                    _uiState.value = _uiState.value.copy(
                        pendingCall = pendingCall,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Incoming call not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load incoming call"
                )
            }
        }
    }
    
    fun acceptCall() {
        currentCallId?.let { callId ->
            viewModelScope.launch {
                try {
                    val result = callManager.acceptCall(callId)
                    if (result.isFailure) {
                        _uiState.value = _uiState.value.copy(
                            error = result.exceptionOrNull()?.message ?: "Failed to accept call"
                        )
                    } else {
                        // Clear the incoming call notification
                        callNotificationService.clearCallNotification(callId)
                        
                        // Show ongoing call notification
                        val pendingCall = _uiState.value.pendingCall
                        if (pendingCall != null) {
                            callNotificationService.showOngoingCallNotification(
                                callId = callId,
                                callerName = pendingCall.peerId,
                                isVideo = pendingCall.isVideo,
                                duration = "00:00"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to accept call"
                    )
                }
            }
        }
    }
    
    fun declineCall() {
        currentCallId?.let { callId ->
            viewModelScope.launch {
                try {
                    val result = callManager.rejectCall(callId, "Call declined by user")
                    if (result.isFailure) {
                        _uiState.value = _uiState.value.copy(
                            error = result.exceptionOrNull()?.message ?: "Failed to decline call"
                        )
                    } else {
                        // Clear the incoming call notification
                        callNotificationService.clearCallNotification(callId)
                        
                        // Show missed call notification
                        val pendingCall = _uiState.value.pendingCall
                        if (pendingCall != null) {
                            callNotificationService.showMissedCallNotification(
                                callId = callId,
                                callerName = pendingCall.peerId,
                                isVideo = pendingCall.isVideo,
                                timestamp = pendingCall.timestamp
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to decline call"
                    )
                }
            }
        }
    }
}

/**
 * UI state for incoming call screen
 */
data class IncomingCallUiState(
    val isLoading: Boolean = false,
    val pendingCall: PendingCall? = null,
    val error: String? = null
)