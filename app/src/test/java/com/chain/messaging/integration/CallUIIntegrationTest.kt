package com.chain.messaging.integration

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chain.messaging.core.webrtc.*
import com.chain.messaging.presentation.call.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import javax.inject.Inject

/**
 * Integration tests for call UI components
 * Tests the complete flow of call interface, controls, and notifications
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CallUIIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var callManager: CallManager
    
    @Inject
    lateinit var callNotificationService: CallNotificationService
    
    @Inject
    lateinit var webRTCManager: WebRTCManager
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        hiltRule.inject()
    }
    
    @Test
    fun testCallViewModelInitialization() = runTest {
        // Given
        val callViewModel = CallViewModel(callManager, webRTCManager)
        
        // When
        val initialState = callViewModel.uiState.value
        
        // Then
        assertFalse(initialState.isLoading)
        assertNull(initialState.callSession)
        assertNull(initialState.error)
        assertFalse(initialState.isMuted)
        assertFalse(initialState.isVideoEnabled)
        assertFalse(initialState.isSpeakerOn)
        assertEquals("00:00", initialState.callDuration)
    }
    
    @Test
    fun testIncomingCallViewModelInitialization() = runTest {
        // Given
        val incomingCallViewModel = IncomingCallViewModel(callManager, callNotificationService)
        
        // When
        val initialState = incomingCallViewModel.uiState.value
        
        // Then
        assertFalse(initialState.isLoading)
        assertNull(initialState.pendingCall)
        assertNull(initialState.error)
    }
    
    @Test
    fun testCallHistoryViewModelInitialization() = runTest {
        // Given
        val callHistoryViewModel = CallHistoryViewModel(callNotificationService)
        
        // When
        val initialState = callHistoryViewModel.uiState.value
        
        // Then
        assertFalse(initialState.isLoading)
        assertTrue(initialState.callHistory.isEmpty())
        assertNull(initialState.error)
    }
    
    @Test
    fun testCallNotificationFlow() = runTest {
        // Given
        val callId = "ui_test_call"
        val callerName = "UI Test Caller"
        val isVideo = false
        
        // When - Show incoming call notification
        callNotificationService.showIncomingCallNotification(callId, callerName, isVideo)
        
        // Then
        val notification = callNotificationService.getCallNotification(callId)
        assertNotNull(notification)
        assertEquals(CallNotificationType.INCOMING, notification?.type)
        assertEquals(callerName, notification?.callerName)
        assertEquals(isVideo, notification?.isVideo)
        
        // When - Update to ongoing call
        callNotificationService.showOngoingCallNotification(callId, callerName, isVideo, "01:30")
        
        // Then
        val ongoingNotification = callNotificationService.getCallNotification(callId)
        assertEquals(CallNotificationType.ONGOING, ongoingNotification?.type)
        assertTrue(ongoingNotification?.message?.contains("01:30") == true)
        
        // When - Clear notification
        callNotificationService.clearCallNotification(callId)
        
        // Then
        assertNull(callNotificationService.getCallNotification(callId))
    }
    
    @Test
    fun testCallViewModelWithRealCall() = runTest {
        // Given
        val peerId = "ui_integration_peer"
        val isVideo = false
        val callViewModel = CallViewModel(callManager, webRTCManager)
        
        // When - Initiate a call
        val callResult = callManager.initiateCall(peerId, isVideo)
        assertTrue(callResult.isSuccess)
        
        val callSession = callResult.getOrNull()
        assertNotNull(callSession)
        
        // Load call in view model
        callViewModel.loadCall(callSession?.id ?: "")
        
        // Then
        val state = callViewModel.uiState.value
        assertNotNull(state.callSession)
        assertEquals(peerId, state.callSession?.peerId)
        assertEquals(isVideo, state.callSession?.isVideo)
    }
    
    @Test
    fun testIncomingCallViewModelWithRealCall() = runTest {
        // Given
        val peerId = "incoming_ui_peer"
        val isVideo = true
        val incomingCallViewModel = IncomingCallViewModel(callManager, callNotificationService)
        
        // When - Initiate a call (simulating incoming)
        val callResult = callManager.initiateCall(peerId, isVideo)
        assertTrue(callResult.isSuccess)
        
        val callSession = callResult.getOrNull()
        assertNotNull(callSession)
        
        // Load incoming call in view model
        incomingCallViewModel.loadIncomingCall(callSession?.id ?: "")
        
        // Then
        val state = incomingCallViewModel.uiState.value
        assertNotNull(state.pendingCall)
        assertEquals(peerId, state.pendingCall?.peerId)
        assertEquals(isVideo, state.pendingCall?.isVideo)
    }
    
    @Test
    fun testCallHistoryWithRealNotifications() = runTest {
        // Given
        val callHistoryViewModel = CallHistoryViewModel(callNotificationService)
        
        // Create some test notifications
        callNotificationService.showIncomingCallNotification("call_1", "Alice", false)
        callNotificationService.showMissedCallNotification("call_2", "Bob", true, System.currentTimeMillis())
        callNotificationService.showOngoingCallNotification("call_3", "Charlie", false, "02:30")
        
        // When
        callHistoryViewModel.loadCallHistory()
        
        // Then
        val state = callHistoryViewModel.uiState.value
        assertEquals(3, state.callHistory.size)
        
        // Verify different call types are present
        val callTypes = state.callHistory.map { it.type }.toSet()
        assertTrue(callTypes.contains(CallNotificationType.INCOMING))
        assertTrue(callTypes.contains(CallNotificationType.MISSED))
        assertTrue(callTypes.contains(CallNotificationType.ONGOING))
    }
    
    @Test
    fun testCallControlsToggling() = runTest {
        // Given
        val callViewModel = CallViewModel(callManager, webRTCManager)
        
        // When - Toggle mute
        assertFalse(callViewModel.uiState.value.isMuted)
        callViewModel.toggleMute()
        assertTrue(callViewModel.uiState.value.isMuted)
        callViewModel.toggleMute()
        assertFalse(callViewModel.uiState.value.isMuted)
        
        // When - Toggle speaker
        assertFalse(callViewModel.uiState.value.isSpeakerOn)
        callViewModel.toggleSpeaker()
        assertTrue(callViewModel.uiState.value.isSpeakerOn)
        callViewModel.toggleSpeaker()
        assertFalse(callViewModel.uiState.value.isSpeakerOn)
        
        // When - Toggle video
        assertFalse(callViewModel.uiState.value.isVideoEnabled)
        callViewModel.toggleVideo()
        assertTrue(callViewModel.uiState.value.isVideoEnabled)
        callViewModel.toggleVideo()
        assertFalse(callViewModel.uiState.value.isVideoEnabled)
    }
    
    @Test
    fun testCallLifecycleWithNotifications() = runTest {
        // Given
        val peerId = "lifecycle_ui_peer"
        val callerName = "Lifecycle Test"
        val isVideo = false
        
        // When - Initiate call
        val callResult = callManager.initiateCall(peerId, isVideo)
        assertTrue(callResult.isSuccess)
        
        val callSession = callResult.getOrNull()
        assertNotNull(callSession)
        
        // Show incoming notification
        callNotificationService.showIncomingCallNotification(callSession?.id ?: "", callerName, isVideo)
        
        // Accept call
        val acceptResult = callManager.acceptCall(callSession?.id ?: "")
        assertTrue(acceptResult.isSuccess)
        
        // Show ongoing notification
        callNotificationService.showOngoingCallNotification(callSession?.id ?: "", callerName, isVideo, "01:00")
        
        // End call
        val endResult = callManager.endCall(callSession?.id ?: "", "Test completed")
        assertTrue(endResult.isSuccess)
        
        // Clear notification
        callNotificationService.clearCallNotification(callSession?.id ?: "")
        
        // Then - Verify cleanup
        assertNull(callManager.getActiveCall(callSession?.id ?: ""))
        assertNull(callManager.getPendingCall(callSession?.id ?: ""))
        assertNull(callNotificationService.getCallNotification(callSession?.id ?: ""))
    }
    
    @Test
    fun testMultipleCallsHandling() = runTest {
        // Given
        val peer1 = "multi_peer_1"
        val peer2 = "multi_peer_2"
        val peer3 = "multi_peer_3"
        
        // When - Initiate multiple calls
        val call1 = callManager.initiateCall(peer1, false).getOrNull()
        val call2 = callManager.initiateCall(peer2, true).getOrNull()
        val call3 = callManager.initiateCall(peer3, false).getOrNull()
        
        // Show notifications for all calls
        call1?.let { callNotificationService.showIncomingCallNotification(it.id, peer1, false) }
        call2?.let { callNotificationService.showIncomingCallNotification(it.id, peer2, true) }
        call3?.let { callNotificationService.showIncomingCallNotification(it.id, peer3, false) }
        
        // Then
        assertEquals(3, callManager.getActiveCalls().size)
        assertEquals(3, callManager.getPendingCalls().size)
        assertEquals(3, callNotificationService.getAllCallNotifications().size)
        
        // When - End one call
        call2?.let { callManager.endCall(it.id) }
        call2?.let { callNotificationService.clearCallNotification(it.id) }
        
        // Then
        assertEquals(2, callManager.getActiveCalls().size)
        assertEquals(2, callManager.getPendingCalls().size)
        assertEquals(2, callNotificationService.getAllCallNotifications().size)
    }
    
    @Test
    fun testCallHistoryDeletion() = runTest {
        // Given
        val callHistoryViewModel = CallHistoryViewModel(callNotificationService)
        
        // Create test notifications
        callNotificationService.showMissedCallNotification("delete_call_1", "Delete Test 1", false, System.currentTimeMillis())
        callNotificationService.showMissedCallNotification("delete_call_2", "Delete Test 2", true, System.currentTimeMillis())
        
        callHistoryViewModel.loadCallHistory()
        assertEquals(2, callHistoryViewModel.uiState.value.callHistory.size)
        
        // When - Delete one call
        callHistoryViewModel.deleteCallFromHistory("delete_call_1")
        
        // Then
        assertEquals(1, callHistoryViewModel.uiState.value.callHistory.size)
        assertEquals("delete_call_2", callHistoryViewModel.uiState.value.callHistory[0].callId)
        
        // When - Clear all history
        callHistoryViewModel.clearCallHistory()
        
        // Then
        assertTrue(callHistoryViewModel.uiState.value.callHistory.isEmpty())
    }
    
    @Test
    fun testNetworkQualityIndicator() = runTest {
        // Given
        val callViewModel = CallViewModel(callManager, webRTCManager)
        
        // When - Network quality should be updated based on connection state
        // (This would typically be tested with mock WebRTC events)
        
        // Then - Initial state should have no network quality
        assertNull(callViewModel.uiState.value.networkQuality)
        
        // The network quality would be updated through WebRTC connection state changes
        // which are observed in the ViewModel's init block
    }
}