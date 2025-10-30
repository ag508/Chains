package com.chain.messaging.presentation.call

import com.chain.messaging.core.webrtc.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class IncomingCallViewModelTest {
    
    private lateinit var incomingCallViewModel: IncomingCallViewModel
    private val mockCallManager = mockk<CallManager>()
    private val mockCallNotificationService = mockk<CallNotificationService>()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock CallManager
        every { mockCallManager.getPendingCall(any()) } returns null
        coEvery { mockCallManager.acceptCall(any()) } returns Result.success(mockk())
        coEvery { mockCallManager.rejectCall(any(), any()) } returns Result.success(Unit)
        
        // Mock CallNotificationService
        every { mockCallNotificationService.clearCallNotification(any()) } just Runs
        every { mockCallNotificationService.showOngoingCallNotification(any(), any(), any(), any()) } just Runs
        every { mockCallNotificationService.showMissedCallNotification(any(), any(), any(), any()) } just Runs
        
        incomingCallViewModel = IncomingCallViewModel(mockCallManager, mockCallNotificationService)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `initial state should be correct`() = runTest {
        // Given
        val initialState = incomingCallViewModel.uiState.value
        
        // Then
        assertFalse(initialState.isLoading)
        assertNull(initialState.pendingCall)
        assertNull(initialState.error)
    }
    
    @Test
    fun `loadIncomingCall should load existing pending call`() = runTest {
        // Given
        val callId = "incoming_call_123"
        val pendingCall = PendingCall(
            callId = callId,
            peerId = "caller_456",
            isVideo = true,
            isOutgoing = false,
            status = PendingCallStatus.INVITATION_RECEIVED
        )
        
        every { mockCallManager.getPendingCall(callId) } returns pendingCall
        
        // When
        incomingCallViewModel.loadIncomingCall(callId)
        
        // Then
        val state = incomingCallViewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(pendingCall, state.pendingCall)
        assertNull(state.error)
        
        verify { mockCallManager.getPendingCall(callId) }
    }
    
    @Test
    fun `loadIncomingCall should handle non-existent call`() = runTest {
        // Given
        val callId = "non_existent_call"
        
        every { mockCallManager.getPendingCall(callId) } returns null
        
        // When
        incomingCallViewModel.loadIncomingCall(callId)
        
        // Then
        val state = incomingCallViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.pendingCall)
        assertEquals("Incoming call not found", state.error)
    }
    
    @Test
    fun `loadIncomingCall should handle exceptions`() = runTest {
        // Given
        val callId = "error_call"
        val errorMessage = "Database error"
        
        every { mockCallManager.getPendingCall(callId) } throws RuntimeException(errorMessage)
        
        // When
        incomingCallViewModel.loadIncomingCall(callId)
        
        // Then
        val state = incomingCallViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.pendingCall)
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `acceptCall should accept call and update notifications`() = runTest {
        // Given
        val callId = "accept_call_123"
        val pendingCall = PendingCall(
            callId = callId,
            peerId = "caller_456",
            isVideo = false,
            isOutgoing = false,
            status = PendingCallStatus.INVITATION_RECEIVED
        )
        val acceptedSession = CallSession(
            id = callId,
            peerId = "caller_456",
            isVideo = false,
            status = CallStatus.CONNECTING,
            localStream = null,
            remoteStream = null
        )
        
        every { mockCallManager.getPendingCall(callId) } returns pendingCall
        coEvery { mockCallManager.acceptCall(callId) } returns Result.success(acceptedSession)
        
        incomingCallViewModel.loadIncomingCall(callId)
        
        // When
        incomingCallViewModel.acceptCall()
        
        // Then
        coVerify { mockCallManager.acceptCall(callId) }
        verify { mockCallNotificationService.clearCallNotification(callId) }
        verify { 
            mockCallNotificationService.showOngoingCallNotification(
                callId = callId,
                callerName = pendingCall.peerId,
                isVideo = pendingCall.isVideo,
                duration = "00:00"
            )
        }
    }
    
    @Test
    fun `acceptCall should handle call manager failure`() = runTest {
        // Given
        val callId = "accept_fail_call"
        val errorMessage = "Failed to accept call"
        val pendingCall = PendingCall(
            callId = callId,
            peerId = "caller_456",
            isVideo = false,
            isOutgoing = false,
            status = PendingCallStatus.INVITATION_RECEIVED
        )
        
        every { mockCallManager.getPendingCall(callId) } returns pendingCall
        coEvery { mockCallManager.acceptCall(callId) } returns Result.failure(RuntimeException(errorMessage))
        
        incomingCallViewModel.loadIncomingCall(callId)
        
        // When
        incomingCallViewModel.acceptCall()
        
        // Then
        val state = incomingCallViewModel.uiState.value
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `acceptCall should handle exceptions`() = runTest {
        // Given
        val callId = "accept_exception_call"
        val errorMessage = "Network error"
        val pendingCall = PendingCall(
            callId = callId,
            peerId = "caller_456",
            isVideo = false,
            isOutgoing = false,
            status = PendingCallStatus.INVITATION_RECEIVED
        )
        
        every { mockCallManager.getPendingCall(callId) } returns pendingCall
        coEvery { mockCallManager.acceptCall(callId) } throws RuntimeException(errorMessage)
        
        incomingCallViewModel.loadIncomingCall(callId)
        
        // When
        incomingCallViewModel.acceptCall()
        
        // Then
        val state = incomingCallViewModel.uiState.value
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `declineCall should reject call and show missed call notification`() = runTest {
        // Given
        val callId = "decline_call_123"
        val pendingCall = PendingCall(
            callId = callId,
            peerId = "caller_456",
            isVideo = true,
            isOutgoing = false,
            status = PendingCallStatus.INVITATION_RECEIVED,
            timestamp = System.currentTimeMillis()
        )
        
        every { mockCallManager.getPendingCall(callId) } returns pendingCall
        coEvery { mockCallManager.rejectCall(callId, "Call declined by user") } returns Result.success(Unit)
        
        incomingCallViewModel.loadIncomingCall(callId)
        
        // When
        incomingCallViewModel.declineCall()
        
        // Then
        coVerify { mockCallManager.rejectCall(callId, "Call declined by user") }
        verify { mockCallNotificationService.clearCallNotification(callId) }
        verify { 
            mockCallNotificationService.showMissedCallNotification(
                callId = callId,
                callerName = pendingCall.peerId,
                isVideo = pendingCall.isVideo,
                timestamp = pendingCall.timestamp
            )
        }
    }
    
    @Test
    fun `declineCall should handle call manager failure`() = runTest {
        // Given
        val callId = "decline_fail_call"
        val errorMessage = "Failed to decline call"
        val pendingCall = PendingCall(
            callId = callId,
            peerId = "caller_456",
            isVideo = false,
            isOutgoing = false,
            status = PendingCallStatus.INVITATION_RECEIVED
        )
        
        every { mockCallManager.getPendingCall(callId) } returns pendingCall
        coEvery { mockCallManager.rejectCall(callId, any()) } returns Result.failure(RuntimeException(errorMessage))
        
        incomingCallViewModel.loadIncomingCall(callId)
        
        // When
        incomingCallViewModel.declineCall()
        
        // Then
        val state = incomingCallViewModel.uiState.value
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `declineCall should handle exceptions`() = runTest {
        // Given
        val callId = "decline_exception_call"
        val errorMessage = "Network error"
        val pendingCall = PendingCall(
            callId = callId,
            peerId = "caller_456",
            isVideo = false,
            isOutgoing = false,
            status = PendingCallStatus.INVITATION_RECEIVED
        )
        
        every { mockCallManager.getPendingCall(callId) } returns pendingCall
        coEvery { mockCallManager.rejectCall(callId, any()) } throws RuntimeException(errorMessage)
        
        incomingCallViewModel.loadIncomingCall(callId)
        
        // When
        incomingCallViewModel.declineCall()
        
        // Then
        val state = incomingCallViewModel.uiState.value
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `acceptCall should not fail when no call is loaded`() = runTest {
        // Given - No call loaded
        
        // When
        incomingCallViewModel.acceptCall()
        
        // Then - Should not crash or call manager
        verify(exactly = 0) { mockCallManager.acceptCall(any()) }
    }
    
    @Test
    fun `declineCall should not fail when no call is loaded`() = runTest {
        // Given - No call loaded
        
        // When
        incomingCallViewModel.declineCall()
        
        // Then - Should not crash or call manager
        coVerify(exactly = 0) { mockCallManager.rejectCall(any(), any()) }
    }
}