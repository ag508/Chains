package com.chain.messaging.core.webrtc

import com.chain.messaging.domain.model.CallEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Call state machine for managing call state transitions
 * Implements requirement 6.2 for call status management
 */
@Singleton
class CallStateMachine @Inject constructor() {
    
    private val _stateTransitions = MutableSharedFlow<CallStateTransition>()
    val stateTransitions: Flow<CallStateTransition> = _stateTransitions.asSharedFlow()
    
    /**
     * Process call state transition
     */
    suspend fun processTransition(
        callId: String,
        currentState: CallState,
        event: CallEvent,
        context: CallContext
    ): CallState {
        val newState = when (currentState) {
            CallState.IDLE -> handleIdleState(event, context)
            CallState.INITIATING -> handleInitiatingState(event, context)
            CallState.RINGING -> handleRingingState(event, context)
            CallState.CONNECTING -> handleConnectingState(event, context)
            CallState.CONNECTED -> handleConnectedState(event, context)
            CallState.ENDING -> handleEndingState(event, context)
            CallState.ENDED -> handleEndedState(event, context)
            CallState.FAILED -> handleFailedState(event, context)
        }
        
        if (newState != currentState) {
            val transition = CallStateTransition(
                callId = callId,
                fromState = currentState,
                toState = newState,
                event = event,
                timestamp = System.currentTimeMillis()
            )
            
            _stateTransitions.emit(transition)
        }
        
        return newState
    }
    
    private fun handleIdleState(event: CallEvent, context: CallContext): CallState {
        return when (event) {
            is CallEvent.InitiateCall -> CallState.INITIATING
            is CallEvent.IncomingCall -> CallState.RINGING
            else -> CallState.IDLE
        }
    }
    
    private fun handleInitiatingState(event: CallEvent, context: CallContext): CallState {
        return when (event) {
            is CallEvent.CallAccepted -> CallState.CONNECTING
            is CallEvent.CallRejected -> CallState.ENDED
            is CallEvent.CallTimeout -> CallState.FAILED
            is CallEvent.EndCall -> CallState.ENDING
            else -> CallState.INITIATING
        }
    }
    
    private fun handleRingingState(event: CallEvent, context: CallContext): CallState {
        return when (event) {
            is CallEvent.AcceptCall -> CallState.CONNECTING
            is CallEvent.RejectCall -> CallState.ENDED
            is CallEvent.CallTimeout -> CallState.ENDED
            is CallEvent.EndCall -> CallState.ENDING
            else -> CallState.RINGING
        }
    }
    
    private fun handleConnectingState(event: CallEvent, context: CallContext): CallState {
        return when (event) {
            is CallEvent.CallConnected -> CallState.CONNECTED
            is CallEvent.CallFailed -> CallState.FAILED
            is CallEvent.EndCall -> CallState.ENDING
            is CallEvent.CallTimeout -> CallState.FAILED
            else -> CallState.CONNECTING
        }
    }
    
    private fun handleConnectedState(event: CallEvent, context: CallContext): CallState {
        return when (event) {
            is CallEvent.EndCall -> CallState.ENDING
            is CallEvent.CallFailed -> CallState.FAILED
            is CallEvent.ConnectionLost -> CallState.CONNECTING // Try to reconnect
            else -> CallState.CONNECTED
        }
    }
    
    private fun handleEndingState(event: CallEvent, context: CallContext): CallState {
        return when (event) {
            is CallEvent.CallEnded -> CallState.ENDED
            is CallEvent.CallFailed -> CallState.FAILED
            else -> CallState.ENDING
        }
    }
    
    private fun handleEndedState(event: CallEvent, context: CallContext): CallState {
        return when (event) {
            is CallEvent.InitiateCall -> CallState.INITIATING
            is CallEvent.IncomingCall -> CallState.RINGING
            else -> CallState.ENDED
        }
    }
    
    private fun handleFailedState(event: CallEvent, context: CallContext): CallState {
        return when (event) {
            is CallEvent.InitiateCall -> CallState.INITIATING
            is CallEvent.IncomingCall -> CallState.RINGING
            else -> CallState.FAILED
        }
    }
    
    /**
     * Check if transition is valid
     */
    fun isValidTransition(from: CallState, to: CallState, event: CallEvent): Boolean {
        return when (from) {
            CallState.IDLE -> to in listOf(CallState.INITIATING, CallState.RINGING)
            CallState.INITIATING -> to in listOf(CallState.CONNECTING, CallState.ENDED, CallState.FAILED, CallState.ENDING)
            CallState.RINGING -> to in listOf(CallState.CONNECTING, CallState.ENDED, CallState.ENDING)
            CallState.CONNECTING -> to in listOf(CallState.CONNECTED, CallState.FAILED, CallState.ENDING)
            CallState.CONNECTED -> to in listOf(CallState.ENDING, CallState.FAILED, CallState.CONNECTING)
            CallState.ENDING -> to in listOf(CallState.ENDED, CallState.FAILED)
            CallState.ENDED -> to in listOf(CallState.INITIATING, CallState.RINGING)
            CallState.FAILED -> to in listOf(CallState.INITIATING, CallState.RINGING)
        }
    }
}

/**
 * Call states
 */
enum class CallState {
    IDLE,
    INITIATING,
    RINGING,
    CONNECTING,
    CONNECTED,
    ENDING,
    ENDED,
    FAILED
}



/**
 * Call context for state transitions
 */
data class CallContext(
    val callId: String,
    val peerId: String,
    val isVideo: Boolean,
    val isOutgoing: Boolean,
    val startTime: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Call state transition
 */
data class CallStateTransition(
    val callId: String,
    val fromState: CallState,
    val toState: CallState,
    val event: CallEvent,
    val timestamp: Long
)