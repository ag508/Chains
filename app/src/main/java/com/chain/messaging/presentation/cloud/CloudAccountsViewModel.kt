package com.chain.messaging.presentation.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.cloud.AuthResult
import com.chain.messaging.core.cloud.CloudAccount
import com.chain.messaging.core.cloud.CloudAuthManager
import com.chain.messaging.core.cloud.CloudService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudAccountsViewModel @Inject constructor(
    private val cloudAuthManager: CloudAuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CloudAccountsUiState())
    val uiState: StateFlow<CloudAccountsUiState> = _uiState.asStateFlow()
    
    init {
        loadAccounts()
        observeAuthState()
    }
    
    fun authenticateService(service: CloudService) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                authenticatingService = service,
                error = null
            )
            
            when (val result = cloudAuthManager.authenticate(service)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        authenticatingService = null,
                        message = "Successfully connected to ${service.displayName}"
                    )
                    loadAccounts()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        authenticatingService = null,
                        error = "Failed to connect to ${service.displayName}: ${result.message}"
                    )
                }
                is AuthResult.Cancelled -> {
                    _uiState.value = _uiState.value.copy(
                        authenticatingService = null,
                        message = "Authentication cancelled"
                    )
                }
            }
        }
    }
    
    fun signOutFromService(service: CloudService) {
        viewModelScope.launch {
            val success = cloudAuthManager.signOut(service)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    message = "Signed out from ${service.displayName}"
                )
                loadAccounts()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to sign out from ${service.displayName}"
                )
            }
        }
    }
    
    fun refreshToken(service: CloudService) {
        viewModelScope.launch {
            when (val result = cloudAuthManager.refreshToken(service)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        message = "Token refreshed for ${service.displayName}"
                    )
                    loadAccounts()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to refresh token: ${result.message}"
                    )
                }
                is AuthResult.Cancelled -> {
                    // Should not happen for token refresh
                }
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            val accounts = cloudAuthManager.getAuthenticatedAccounts()
            val authStates = CloudService.values().associateWith { service ->
                accounts.any { it.service == service }
            }
            
            _uiState.value = _uiState.value.copy(
                accounts = accounts,
                authenticationStates = authStates
            )
        }
    }
    
    private fun observeAuthState() {
        viewModelScope.launch {
            cloudAuthManager.observeAuthState().collect { authStates ->
                _uiState.value = _uiState.value.copy(
                    authenticationStates = authStates
                )
            }
        }
    }
}

data class CloudAccountsUiState(
    val accounts: List<CloudAccount> = emptyList(),
    val authenticationStates: Map<CloudService, Boolean> = emptyMap(),
    val authenticatingService: CloudService? = null,
    val message: String? = null,
    val error: String? = null
)