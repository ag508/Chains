package com.chain.messaging.presentation.security

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.security.IdentityVerificationManager
import com.chain.messaging.core.security.QRCodeScanner
import com.chain.messaging.domain.model.ScanResult
import com.chain.messaging.domain.model.VerificationResult
import com.chain.messaging.domain.model.VerificationState
import com.chain.messaging.domain.model.SecurityAlert
import com.chain.messaging.domain.model.SecurityRecommendation
import com.chain.messaging.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.signal.libsignal.protocol.IdentityKey
import javax.inject.Inject

@HiltViewModel
class IdentityVerificationViewModel @Inject constructor(
    private val identityVerificationManager: IdentityVerificationManager,
    private val qrCodeScanner: QRCodeScanner
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(IdentityVerificationUiState())
    val uiState: StateFlow<IdentityVerificationUiState> = _uiState.asStateFlow()
    
    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap.asStateFlow()
    
    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()
    
    private val _safetyNumber = MutableStateFlow<String?>(null)
    val safetyNumber: StateFlow<String?> = _safetyNumber.asStateFlow()
    
    init {
        observeVerificationState()
        observeSecurityAlerts()
    }
    
    /**
     * Generate QR code for current user
     */
    fun generateQRCode(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            identityVerificationManager.generateVerificationQRCode(user)
                .onSuccess { bitmap ->
                    _qrCodeBitmap.value = bitmap
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to generate QR code"
                    )
                }
        }
    }
    
    /**
     * Scan QR code from bitmap
     */
    fun scanQRCode(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            qrCodeScanner.scanQRCode(bitmap)
                .onSuccess { qrData ->
                    verifyScannedData(qrData)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to scan QR code"
                    )
                }
        }
    }
    
    /**
     * Verify scanned QR data
     */
    private fun verifyScannedData(qrData: String) {
        viewModelScope.launch {
            identityVerificationManager.verifyQRCode(qrData)
                .onSuccess { result ->
                    _scanResult.value = when (result) {
                        is VerificationResult.Success -> ScanResult.Success(result.userId, result.displayName, result.publicKey, result.timestamp)
                        is VerificationResult.KeyMismatch -> ScanResult.KeyMismatch(result.userId, result.displayName, result.expectedKey, result.receivedKey)
                        is VerificationResult.InvalidData -> ScanResult.Error(result.reason)
                        else -> ScanResult.Error("Unknown verification result")
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _scanResult.value = ScanResult.Error(error.message ?: "Verification failed")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }
    
    /**
     * Generate safety number for manual verification
     */
    fun generateSafetyNumber(userId: String, remoteIdentityKey: IdentityKey) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            identityVerificationManager.generateSafetyNumber(userId, remoteIdentityKey)
                .onSuccess { safetyNumber ->
                    _safetyNumber.value = safetyNumber
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to generate safety number"
                    )
                }
        }
    }
    
    /**
     * Verify manually entered safety number
     */
    fun verifySafetyNumber(userId: String, enteredSafetyNumber: String, remoteIdentityKey: IdentityKey) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            identityVerificationManager.verifySafetyNumber(userId, enteredSafetyNumber, remoteIdentityKey)
                .onSuccess { isValid ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verificationSuccess = isValid
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to verify safety number"
                    )
                }
        }
    }
    
    /**
     * Dismiss security alert
     */
    fun dismissSecurityAlert(alertId: String) {
        identityVerificationManager.dismissSecurityAlert(alertId)
    }
    
    /**
     * Clear all security alerts
     */
    fun clearAllSecurityAlerts() {
        identityVerificationManager.clearAllSecurityAlerts()
    }
    
    /**
     * Clear scan result
     */
    fun clearScanResult() {
        _scanResult.value = null
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear verification success state
     */
    fun clearVerificationSuccess() {
        _uiState.value = _uiState.value.copy(verificationSuccess = null)
    }
    
    private fun observeVerificationState() {
        viewModelScope.launch {
            identityVerificationManager.verificationState.collect { verificationStates ->
                _uiState.value = _uiState.value.copy(verificationStates = verificationStates)
            }
        }
    }
    
    private fun observeSecurityAlerts() {
        viewModelScope.launch {
            combine(
                identityVerificationManager.securityAlerts,
                identityVerificationManager.verificationState
            ) { alerts, verificationStates ->
                val recommendations = identityVerificationManager.getSecurityRecommendations()
                _uiState.value = _uiState.value.copy(
                    securityAlerts = alerts,
                    securityRecommendations = recommendations
                )
            }.collect { }
        }
    }
}

/**
 * UI state for identity verification
 */
data class IdentityVerificationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val verificationSuccess: Boolean? = null,
    val verificationStates: Map<String, VerificationState> = emptyMap(),
    val securityAlerts: List<SecurityAlert> = emptyList(),
    val securityRecommendations: List<SecurityRecommendation> = emptyList()
)

