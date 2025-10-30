package com.chain.messaging.presentation.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.cloud.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageManagementViewModel @Inject constructor(
    private val storageQuotaMonitor: StorageQuotaMonitor,
    private val fileCleanupManager: FileCleanupManager,
    private val localStorageFallback: LocalStorageFallback
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StorageManagementUiState())
    val uiState: StateFlow<StorageManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadStorageInfo()
        observeQuotaAlerts()
    }
    
    fun refreshStorageInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val storageInfos = storageQuotaMonitor.checkAllQuotas()
                val localStorageInfo = localStorageFallback.getLocalStorageInfo()
                val recommendations = storageQuotaMonitor.getStorageRecommendations()
                val cleanupSuggestions = fileCleanupManager.getCleanupSuggestions()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    storageInfos = storageInfos,
                    localStorageInfo = localStorageInfo,
                    recommendations = recommendations,
                    cleanupSuggestions = cleanupSuggestions,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load storage info: ${e.message}"
                )
            }
        }
    }
    
    fun performCleanup(cleanupType: CleanupType? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPerformingCleanup = true)
            
            try {
                val result = if (cleanupType == null) {
                    fileCleanupManager.performFullCleanup()
                } else {
                    // Perform specific cleanup based on type
                    when (cleanupType) {
                        CleanupType.LOCAL_CACHE -> {
                            val localResult = fileCleanupManager.cleanupLocalFiles()
                            CleanupResult(
                                totalFilesDeleted = localResult.filesDeleted,
                                totalSpaceFreed = localResult.spaceFreed,
                                serviceResults = emptyList()
                            )
                        }
                        else -> fileCleanupManager.performFullCleanup()
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isPerformingCleanup = false,
                    lastCleanupResult = result,
                    message = "Cleanup completed: ${result.totalFilesDeleted} files deleted, ${formatFileSize(result.totalSpaceFreed)} freed"
                )
                
                // Refresh storage info after cleanup
                refreshStorageInfo()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPerformingCleanup = false,
                    error = "Cleanup failed: ${e.message}"
                )
            }
        }
    }
    
    fun optimizeStorage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOptimizing = true)
            
            try {
                val result = fileCleanupManager.optimizeStorage()
                
                _uiState.value = _uiState.value.copy(
                    isOptimizing = false,
                    message = "Storage optimized: ${result.filesOptimized} files optimized, ${formatFileSize(result.spaceSaved)} saved"
                )
                
                refreshStorageInfo()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isOptimizing = false,
                    error = "Optimization failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearAlert(service: CloudService) {
        storageQuotaMonitor.clearAlertsForService(service)
    }
    
    fun clearAllAlerts() {
        storageQuotaMonitor.clearAllAlerts()
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun loadStorageInfo() {
        refreshStorageInfo()
    }
    
    private fun observeQuotaAlerts() {
        viewModelScope.launch {
            storageQuotaMonitor.quotaAlerts.collect { alerts ->
                _uiState.value = _uiState.value.copy(quotaAlerts = alerts)
            }
        }
    }
    
    private fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
}

data class StorageManagementUiState(
    val isLoading: Boolean = false,
    val isPerformingCleanup: Boolean = false,
    val isOptimizing: Boolean = false,
    val storageInfos: List<StorageInfo> = emptyList(),
    val localStorageInfo: LocalStorageInfo? = null,
    val quotaAlerts: List<QuotaAlert> = emptyList(),
    val recommendations: List<StorageRecommendation> = emptyList(),
    val cleanupSuggestions: List<CleanupSuggestion> = emptyList(),
    val lastCleanupResult: CleanupResult? = null,
    val message: String? = null,
    val error: String? = null
)