package com.chain.messaging.presentation.error

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.error.*
import com.chain.messaging.core.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing error display and recovery actions
 */
@HiltViewModel
class ErrorViewModel @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ErrorUiState())
    val uiState: StateFlow<ErrorUiState> = _uiState.asStateFlow()
    
    private val dismissedErrors = mutableSetOf<Long>()
    
    init {
        // Collect error events
        errorHandler.errorEvents
            .onEach { errorEvent ->
                if (errorEvent.timestamp !in dismissedErrors) {
                    handleErrorEvent(errorEvent)
                }
            }
            .launchIn(viewModelScope)
        
        // Collect retry signals
        errorRecoveryManager.retrySignals
            .onEach { retrySignal ->
                Logger.d("Retry signal received for ${retrySignal.context.operation}")
                // Components should listen to this signal and retry their operations
            }
            .launchIn(viewModelScope)
    }
    
    private fun handleErrorEvent(errorEvent: ErrorEvent) {
        val currentState = _uiState.value
        
        when (getErrorSeverity(errorEvent.error)) {
            ErrorSeverity.CRITICAL -> {
                // Show blocking dialog
                _uiState.value = currentState.copy(
                    criticalError = errorEvent,
                    showCriticalDialog = true
                )
            }
            
            ErrorSeverity.HIGH -> {
                // Show non-blocking dialog
                _uiState.value = currentState.copy(
                    highPriorityError = errorEvent,
                    showHighPriorityDialog = true
                )
            }
            
            ErrorSeverity.MEDIUM -> {
                // Show inline error
                _uiState.value = currentState.copy(
                    inlineErrors = currentState.inlineErrors + errorEvent
                )
            }
            
            ErrorSeverity.LOW -> {
                // Show snackbar
                _uiState.value = currentState.copy(
                    snackbarError = errorEvent
                )
            }
        }
    }
    
    fun dismissCriticalError() {
        val currentState = _uiState.value
        currentState.criticalError?.let { error ->
            dismissedErrors.add(error.timestamp)
        }
        _uiState.value = currentState.copy(
            criticalError = null,
            showCriticalDialog = false
        )
    }
    
    fun dismissHighPriorityError() {
        val currentState = _uiState.value
        currentState.highPriorityError?.let { error ->
            dismissedErrors.add(error.timestamp)
        }
        _uiState.value = currentState.copy(
            highPriorityError = null,
            showHighPriorityDialog = false
        )
    }
    
    fun dismissInlineError(errorEvent: ErrorEvent) {
        dismissedErrors.add(errorEvent.timestamp)
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            inlineErrors = currentState.inlineErrors - errorEvent
        )
    }
    
    fun dismissSnackbarError() {
        val currentState = _uiState.value
        currentState.snackbarError?.let { error ->
            dismissedErrors.add(error.timestamp)
        }
        _uiState.value = currentState.copy(
            snackbarError = null
        )
    }
    
    fun executeRecoveryAction(action: RecoveryAction, errorEvent: ErrorEvent) {
        viewModelScope.launch {
            try {
                when (action) {
                    is RecoveryAction.Retry -> {
                        Logger.d("Executing retry action for ${errorEvent.context.operation}")
                        errorRecoveryManager.triggerRecovery(errorEvent.context)
                    }
                    
                    is RecoveryAction.CheckNetworkSettings -> {
                        Logger.d("Opening network settings")
                        // Emit event to open network settings
                        _uiState.value = _uiState.value.copy(
                            navigationEvent = NavigationEvent.OpenNetworkSettings
                        )
                    }
                    
                    is RecoveryAction.UseOfflineMode -> {
                        Logger.d("Switching to offline mode")
                        // Enable offline mode
                        _uiState.value = _uiState.value.copy(
                            navigationEvent = NavigationEvent.EnableOfflineMode
                        )
                    }
                    
                    is RecoveryAction.ReAuthenticate -> {
                        Logger.d("Triggering re-authentication")
                        _uiState.value = _uiState.value.copy(
                            navigationEvent = NavigationEvent.OpenLogin
                        )
                    }
                    
                    is RecoveryAction.CheckPermissions -> {
                        Logger.d("Opening permission settings")
                        _uiState.value = _uiState.value.copy(
                            navigationEvent = NavigationEvent.OpenPermissionSettings
                        )
                    }
                    
                    is RecoveryAction.CleanupStorage -> {
                        Logger.d("Opening storage cleanup")
                        _uiState.value = _uiState.value.copy(
                            navigationEvent = NavigationEvent.OpenStorageCleanup
                        )
                    }
                    
                    is RecoveryAction.Custom -> {
                        Logger.d("Executing custom recovery action: ${action.label}")
                        action.action()
                    }
                    
                    else -> {
                        Logger.d("Executing recovery action: ${action.label}")
                        errorRecoveryManager.triggerRecovery(errorEvent.context)
                    }
                }
                
                // Dismiss the error after action
                when (getErrorSeverity(errorEvent.error)) {
                    ErrorSeverity.CRITICAL -> dismissCriticalError()
                    ErrorSeverity.HIGH -> dismissHighPriorityError()
                    ErrorSeverity.MEDIUM -> dismissInlineError(errorEvent)
                    ErrorSeverity.LOW -> dismissSnackbarError()
                }
                
            } catch (e: Exception) {
                Logger.e("Failed to execute recovery action", e)
            }
        }
    }
    
    fun clearNavigationEvent() {
        _uiState.value = _uiState.value.copy(navigationEvent = null)
    }
    
    private fun getErrorSeverity(error: ChainError): ErrorSeverity {
        return when (error) {
            // Critical errors that block functionality
            is ChainError.AuthError.InvalidCredentials,
            is ChainError.AuthError.TokenExpired,
            is ChainError.SystemError.OutOfMemory,
            is ChainError.StorageError.CorruptedData -> ErrorSeverity.CRITICAL
            
            // High priority errors that significantly impact functionality
            is ChainError.NetworkError.NoInternet,
            is ChainError.BlockchainError.NodeUnavailable,
            is ChainError.EncryptionError.KeyExchangeFailed,
            is ChainError.CallError.ConnectionFailed -> ErrorSeverity.HIGH
            
            // Medium priority errors that partially impact functionality
            is ChainError.StorageError.CloudStorageError,
            is ChainError.P2PError.PeerDiscoveryFailed,
            is ChainError.CallError.MediaDeviceError -> ErrorSeverity.MEDIUM
            
            // Low priority errors that don't significantly impact functionality
            else -> ErrorSeverity.LOW
        }
    }
}

/**
 * UI state for error handling
 */
data class ErrorUiState(
    val criticalError: ErrorEvent? = null,
    val showCriticalDialog: Boolean = false,
    val highPriorityError: ErrorEvent? = null,
    val showHighPriorityDialog: Boolean = false,
    val inlineErrors: List<ErrorEvent> = emptyList(),
    val snackbarError: ErrorEvent? = null,
    val navigationEvent: NavigationEvent? = null
)

/**
 * Error severity levels
 */
enum class ErrorSeverity {
    CRITICAL,  // Blocks app functionality
    HIGH,      // Significantly impacts functionality
    MEDIUM,    // Partially impacts functionality
    LOW        // Minor impact
}

/**
 * Navigation events triggered by error recovery
 */
sealed class NavigationEvent {
    object OpenNetworkSettings : NavigationEvent()
    object EnableOfflineMode : NavigationEvent()
    object OpenLogin : NavigationEvent()
    object OpenPermissionSettings : NavigationEvent()
    object OpenStorageCleanup : NavigationEvent()
}