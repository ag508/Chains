package com.chain.messaging.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.security.*
import com.chain.messaging.domain.model.SecurityAlert
import com.chain.messaging.domain.model.SecurityRecommendation
import com.chain.messaging.domain.model.SecurityStatus
import com.chain.messaging.domain.model.SecurityMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityMonitoringViewModel @Inject constructor(
    private val securityMonitoringManager: SecurityMonitoringManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SecurityMonitoringUiState())
    val uiState: StateFlow<SecurityMonitoringUiState> = _uiState.asStateFlow()
    
    private val _alerts = MutableStateFlow<List<SecurityAlert>>(emptyList())
    val alerts: StateFlow<List<SecurityAlert>> = _alerts.asStateFlow()
    
    init {
        observeSecurityStatus()
        observeSecurityAlerts()
        loadInitialData()
    }
    
    private fun observeSecurityStatus() {
        viewModelScope.launch {
            securityMonitoringManager.getSecurityStatus()
                .collect { status ->
                    _uiState.value = _uiState.value.copy(
                        securityStatus = status,
                        isLoading = false
                    )
                }
        }
    }
    
    private fun observeSecurityAlerts() {
        viewModelScope.launch {
            val alertsList = mutableListOf<SecurityAlert>()
            securityMonitoringManager.getSecurityAlerts()
                .collect { alert ->
                    alertsList.add(0, alert) // Add to beginning
                    _alerts.value = alertsList.toList()
                }
        }
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val recommendations = securityMonitoringManager.getSecurityRecommendations()
                val metrics = securityMonitoringManager.getSecurityMetrics()
                
                _uiState.value = _uiState.value.copy(
                    recommendations = recommendations,
                    metrics = metrics,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error occurred",
                    isLoading = false
                )
            }
        }
    }
    
    fun acknowledgeAlert(alertId: String) {
        viewModelScope.launch {
            try {
                securityMonitoringManager.acknowledgeAlert(alertId)
                
                // Update local alerts list
                val updatedAlerts = _alerts.value.map { alert ->
                    when (alert) {
                        is SecurityAlert.KeyMismatch -> if (alert.id == alertId) alert.copy(isAcknowledged = true) else alert
                        is SecurityAlert.IdentityKeyChanged -> if (alert.id == alertId) alert.copy(isAcknowledged = true) else alert
                        is SecurityAlert.SuspiciousActivity -> if (alert.id == alertId) alert.copy(isAcknowledged = true) else alert
                        is SecurityAlert.PolicyViolation -> if (alert.id == alertId) alert.copy(isAcknowledged = true) else alert
                    }
                }
                _alerts.value = updatedAlerts
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to acknowledge alert"
                )
            }
        }
    }
    
    fun refreshData() {
        loadInitialData()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun startSecurityMonitoring() {
        viewModelScope.launch {
            try {
                securityMonitoringManager.startMonitoring()
                _uiState.value = _uiState.value.copy(isMonitoringActive = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to start monitoring"
                )
            }
        }
    }
    
    fun stopSecurityMonitoring() {
        viewModelScope.launch {
            try {
                securityMonitoringManager.stopMonitoring()
                _uiState.value = _uiState.value.copy(isMonitoringActive = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to stop monitoring"
                )
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            securityMonitoringManager.stopMonitoring()
        }
    }
}

data class SecurityMonitoringUiState(
    val securityStatus: SecurityStatus? = null,
    val recommendations: List<SecurityRecommendation> = emptyList(),
    val metrics: SecurityMetrics? = null,
    val isLoading: Boolean = false,
    val isMonitoringActive: Boolean = false,
    val error: String? = null
)