package com.chain.messaging.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.webrtc.CallNotification
import com.chain.messaging.core.webrtc.CallNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for call history screen
 * Manages call history state and operations
 */
@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    private val callNotificationService: CallNotificationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CallHistoryUiState())
    val uiState: StateFlow<CallHistoryUiState> = _uiState.asStateFlow()
    
    init {
        // Observe call notifications for real-time updates
        viewModelScope.launch {
            callNotificationService.callNotifications.collect { notifications ->
                val sortedHistory = notifications.values
                    .sortedByDescending { it.timestamp }
                
                _uiState.value = _uiState.value.copy(
                    callHistory = sortedHistory,
                    isLoading = false
                )
            }
        }
    }
    
    fun loadCallHistory() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val allNotifications = callNotificationService.getAllCallNotifications()
                val sortedHistory = allNotifications.values
                    .sortedByDescending { it.timestamp }
                
                _uiState.value = _uiState.value.copy(
                    callHistory = sortedHistory,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load call history"
                )
            }
        }
    }
    
    fun deleteCallFromHistory(callId: String) {
        viewModelScope.launch {
            try {
                callNotificationService.clearCallNotification(callId)
                
                // Update local state
                val currentHistory = _uiState.value.callHistory
                val updatedHistory = currentHistory.filter { it.callId != callId }
                
                _uiState.value = _uiState.value.copy(callHistory = updatedHistory)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete call from history"
                )
            }
        }
    }
    
    fun clearCallHistory() {
        viewModelScope.launch {
            try {
                callNotificationService.clearAllCallNotifications()
                
                _uiState.value = _uiState.value.copy(
                    callHistory = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to clear call history"
                )
            }
        }
    }
    
    fun refreshCallHistory() {
        loadCallHistory()
    }
}

/**
 * UI state for call history screen
 */
data class CallHistoryUiState(
    val isLoading: Boolean = false,
    val callHistory: List<CallNotification> = emptyList(),
    val error: String? = null
)