package com.chain.messaging.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.sync.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for device management and synchronization
 */
@HiltViewModel
class DeviceManagementViewModel @Inject constructor(
    private val deviceManager: DeviceManager,
    private val crossDeviceSyncService: CrossDeviceSyncService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DeviceManagementUiState())
    val uiState: StateFlow<DeviceManagementUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Initialize sync service
            crossDeviceSyncService.initialize()
            
            // Observe registered devices
            deviceManager.observeRegisteredDevices()
                .combine(crossDeviceSyncService.getSyncStatus()) { devices, syncStatus ->
                    val currentDevice = devices.find { it.deviceInfo.isCurrentDevice }
                    val otherDevices = devices.filter { !it.deviceInfo.isCurrentDevice }
                    
                    _uiState.value = _uiState.value.copy(
                        currentDevice = currentDevice,
                        otherDevices = otherDevices,
                        syncStatus = syncStatus,
                        isLoading = false
                    )
                }
                .launchIn(this)
            
            // Observe sync progress
            crossDeviceSyncService.getSyncProgress()
                .onEach { progress ->
                    _uiState.value = _uiState.value.copy(
                        syncProgress = progress,
                        showSyncProgress = progress.phase != SyncPhase.COMPLETED && 
                                         progress.phase != SyncPhase.ERROR || 
                                         _uiState.value.showSyncProgress
                    )
                }
                .launchIn(this)
            
            // Load initial data
            refreshDevices()
        }
    }
    
    fun refreshDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val devices = deviceManager.getRegisteredDevices()
                val currentDevice = devices.find { it.deviceInfo.isCurrentDevice }
                val otherDevices = devices.filter { !it.deviceInfo.isCurrentDevice }
                
                _uiState.value = _uiState.value.copy(
                    currentDevice = currentDevice,
                    otherDevices = otherDevices,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load devices: ${e.message}"
                )
            }
        }
    }
    
    fun trustDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                val result = deviceManager.trustDevice(deviceId)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to trust device: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    // Trigger a sync with the newly trusted device
                    crossDeviceSyncService.requestSyncFromDevice(deviceId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to trust device: ${e.message}"
                )
            }
        }
    }
    
    fun removeDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                val result = deviceManager.removeDevice(deviceId)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to remove device: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to remove device: ${e.message}"
                )
            }
        }
    }
    
    fun performFullSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showSyncProgress = true)
            
            try {
                val result = crossDeviceSyncService.performFullSync()
                when (result) {
                    is SyncResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Sync failed: ${result.message}"
                        )
                    }
                    is SyncResult.PartialSuccess -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Sync completed with errors: ${result.message}"
                        )
                    }
                    is SyncResult.Success -> {
                        // Success handled by sync progress observer
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Sync failed: ${e.message}"
                )
            }
        }
    }
    
    fun toggleAutoSync() {
        viewModelScope.launch {
            try {
                val currentStatus = _uiState.value.syncStatus
                val newEnabled = !currentStatus.isEnabled
                crossDeviceSyncService.setAutoSyncEnabled(newEnabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle auto sync: ${e.message}"
                )
            }
        }
    }
    
    fun dismissSyncProgress() {
        _uiState.value = _uiState.value.copy(showSyncProgress = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for device management screen
 */
data class DeviceManagementUiState(
    val currentDevice: RegisteredDevice? = null,
    val otherDevices: List<RegisteredDevice> = emptyList(),
    val syncStatus: CrossDeviceSyncStatus = CrossDeviceSyncStatus(
        isEnabled = false,
        lastSyncTime = null,
        connectedDevices = 0,
        trustedDevices = 0,
        pendingSyncs = 0,
        syncErrors = emptyList()
    ),
    val syncProgress: SyncProgress = SyncProgress(
        phase = SyncPhase.INITIALIZING,
        progress = 0f,
        currentOperation = "Initializing"
    ),
    val showSyncProgress: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)