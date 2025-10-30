package com.chain.messaging.presentation.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.privacy.DisappearingMessageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing disappearing message settings.
 */
@HiltViewModel
class DisappearingMessageSettingsViewModel @Inject constructor(
    private val disappearingMessageManager: DisappearingMessageManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DisappearingMessageSettingsUiState())
    val uiState: StateFlow<DisappearingMessageSettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadAvailableTimers()
    }
    
    fun loadSettings(chatId: String) {
        viewModelScope.launch {
            try {
                val currentTimer = disappearingMessageManager.getDisappearingMessageTimer(chatId)
                _uiState.value = _uiState.value.copy(
                    currentTimer = currentTimer,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun setTimer(chatId: String, timerDuration: Long?) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                disappearingMessageManager.setDisappearingMessageTimer(chatId, timerDuration)
                
                _uiState.value = _uiState.value.copy(
                    currentTimer = timerDuration,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadAvailableTimers() {
        val timers = disappearingMessageManager.getAvailableTimerOptions()
        _uiState.value = _uiState.value.copy(availableTimers = timers)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for disappearing message settings.
 */
data class DisappearingMessageSettingsUiState(
    val currentTimer: Long? = null,
    val availableTimers: List<Long> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)