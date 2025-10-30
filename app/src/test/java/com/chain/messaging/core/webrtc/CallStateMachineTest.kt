package com.chain.messaging.core.webrtc

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CallStateMachineTest {
    
    private lateinit var callStateMachine: CallStateMachine
    
    @Before
    fun setup() {
        callStateMachine = CallStateMachine()
    }
    
    @Test
    fun `idle state should transition to initiating on initiate call event`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.IDLE,
            event = CallEvent.InitiateCall,
            context = context
        )
        
        // Then
        assertEquals(CallState.INITIATING, newState)
    }
    
    @Test
    fun `idle state should transition to ringing on incoming call event`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = false
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.IDLE,
            event = CallEvent.IncomingCall,
            context = context
        )
        
        // Then
        assertEquals(CallState.RINGING, newState)
    }
    
    @Test
    fun `initiating state should transition to connecting on call accepted`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.INITIATING,
            event = CallEvent.CallAccepted,
            context = context
        )
        
        // Then
        assertEquals(CallState.CONNECTING, newState)
    }
    
    @Test
    fun `initiating state should transition to ended on call rejected`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.INITIATING,
            event = CallEvent.CallRejected,
            context = context
        )
        
        // Then
        assertEquals(CallState.ENDED, newState)
    }
    
    @Test
    fun `ringing state should transition to connecting on accept call`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = false
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.RINGING,
            event = CallEvent.AcceptCall,
            context = context
        )
        
        // Then
        assertEquals(CallState.CONNECTING, newState)
    }
    
    @Test
    fun `ringing state should transition to ended on reject call`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = false
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.RINGING,
            event = CallEvent.RejectCall,
            context = context
        )
        
        // Then
        assertEquals(CallState.ENDED, newState)
    }
    
    @Test
    fun `connecting state should transition to connected on call connected`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.CONNECTING,
            event = CallEvent.CallConnected,
            context = context
        )
        
        // Then
        assertEquals(CallState.CONNECTED, newState)
    }
    
    @Test
    fun `connecting state should transition to failed on call failed`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.CONNECTING,
            event = CallEvent.CallFailed,
            context = context
        )
        
        // Then
        assertEquals(CallState.FAILED, newState)
    }
    
    @Test
    fun `connected state should transition to ending on end call`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.CONNECTED,
            event = CallEvent.EndCall,
            context = context
        )
        
        // Then
        assertEquals(CallState.ENDING, newState)
    }
    
    @Test
    fun `connected state should transition to connecting on connection lost`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.CONNECTED,
            event = CallEvent.ConnectionLost,
            context = context
        )
        
        // Then
        assertEquals(CallState.CONNECTING, newState)
    }
    
    @Test
    fun `ending state should transition to ended on call ended`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.ENDING,
            event = CallEvent.CallEnded,
            context = context
        )
        
        // Then
        assertEquals(CallState.ENDED, newState)
    }
    
    @Test
    fun `state transitions should emit transition events`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        val transitions = mutableListOf<CallStateTransition>()
        
        // Collect transitions
        val job = kotlinx.coroutines.launch {
            callStateMachine.stateTransitions.collect { transition ->
                transitions.add(transition)
            }
        }
        
        // When
        callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.IDLE,
            event = CallEvent.InitiateCall,
            context = context
        )
        
        // Give some time for the flow to emit
        kotlinx.coroutines.delay(100)
        job.cancel()
        
        // Then
        assertEquals(1, transitions.size)
        val transition = transitions.first()
        assertEquals(callId, transition.callId)
        assertEquals(CallState.IDLE, transition.fromState)
        assertEquals(CallState.INITIATING, transition.toState)
        assertEquals(CallEvent.InitiateCall, transition.event)
    }
    
    @Test
    fun `isValidTransition should return correct validation results`() {
        // Test valid transitions
        assertTrue(callStateMachine.isValidTransition(CallState.IDLE, CallState.INITIATING, CallEvent.InitiateCall))
        assertTrue(callStateMachine.isValidTransition(CallState.IDLE, CallState.RINGING, CallEvent.IncomingCall))
        assertTrue(callStateMachine.isValidTransition(CallState.INITIATING, CallState.CONNECTING, CallEvent.CallAccepted))
        assertTrue(callStateMachine.isValidTransition(CallState.RINGING, CallState.CONNECTING, CallEvent.AcceptCall))
        assertTrue(callStateMachine.isValidTransition(CallState.CONNECTING, CallState.CONNECTED, CallEvent.CallConnected))
        assertTrue(callStateMachine.isValidTransition(CallState.CONNECTED, CallState.ENDING, CallEvent.EndCall))
        assertTrue(callStateMachine.isValidTransition(CallState.ENDING, CallState.ENDED, CallEvent.CallEnded))
        
        // Test invalid transitions
        assertFalse(callStateMachine.isValidTransition(CallState.IDLE, CallState.CONNECTED, CallEvent.InitiateCall))
        assertFalse(callStateMachine.isValidTransition(CallState.ENDED, CallState.CONNECTING, CallEvent.CallConnected))
        assertFalse(callStateMachine.isValidTransition(CallState.FAILED, CallState.CONNECTED, CallEvent.CallConnected))
    }
    
    @Test
    fun `timeout events should be handled correctly`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When - Timeout in initiating state
        val newState1 = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.INITIATING,
            event = CallEvent.CallTimeout,
            context = context
        )
        
        // Then
        assertEquals(CallState.FAILED, newState1)
        
        // When - Timeout in ringing state
        val newState2 = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.RINGING,
            event = CallEvent.CallTimeout,
            context = context
        )
        
        // Then
        assertEquals(CallState.ENDED, newState2)
    }
    
    @Test
    fun `error events should be handled correctly`() = runTest {
        // Given
        val callId = "test_call_123"
        val context = CallContext(
            callId = callId,
            peerId = "peer_456",
            isVideo = false,
            isOutgoing = true
        )
        
        // When - Error in any state should not change state (unless specifically handled)
        val newState = callStateMachine.processTransition(
            callId = callId,
            currentState = CallState.CONNECTED,
            event = CallEvent.Error("Network error"),
            context = context
        )
        
        // Then - State should remain the same
        assertEquals(CallState.CONNECTED, newState)
    }
    
    @Test
    fun `call context should contain correct information`() {
        // Given
        val callId = "test_call_123"
        val peerId = "peer_456"
        val isVideo = true
        val isOutgoing = false
        val metadata = mapOf("quality" to "HD", "codec" to "VP8")
        
        // When
        val context = CallContext(
            callId = callId,
            peerId = peerId,
            isVideo = isVideo,
            isOutgoing = isOutgoing,
            metadata = metadata
        )
        
        // Then
        assertEquals(callId, context.callId)
        assertEquals(peerId, context.peerId)
        assertEquals(isVideo, context.isVideo)
        assertEquals(isOutgoing, context.isOutgoing)
        assertEquals(metadata, context.metadata)
        assertTrue(context.startTime > 0)
    }
}