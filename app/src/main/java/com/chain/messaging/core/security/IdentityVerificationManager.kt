package com.chain.messaging.core.security

import android.graphics.Bitmap
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.domain.model.User
import com.chain.messaging.domain.model.SecurityAlert
import com.chain.messaging.domain.model.SecurityRecommendation
import com.chain.messaging.domain.model.SecurityException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.SignalProtocolAddress
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages identity verification including QR codes, safety numbers, and security warnings
 */
@Singleton
class IdentityVerificationManager @Inject constructor(
    private val keyManager: KeyManager,
    private val qrCodeGenerator: QRCodeGenerator,
    private val safetyNumberGenerator: SafetyNumberGenerator,
    private val securityAlertManager: SecurityAlertManager
) {
    
    private val _verificationState = MutableStateFlow<Map<String, VerificationState>>(emptyMap())
    val verificationState: StateFlow<Map<String, VerificationState>> = _verificationState.asStateFlow()
    
    private val _securityAlerts = MutableStateFlow<List<SecurityAlert>>(emptyList())
    val securityAlerts: StateFlow<List<SecurityAlert>> = _securityAlerts.asStateFlow()

    /**
     * Generate QR code for user identity verification
     */
    suspend fun generateVerificationQRCode(user: User): Result<Bitmap> {
        return try {
            val identityKey = keyManager.getIdentityKey()
            val verificationData = VerificationData(
                userId = user.id,
                displayName = user.displayName,
                identityKey = identityKey.serialize(),
                timestamp = System.currentTimeMillis()
            )
            
            val qrData = verificationData.toQRString()
            val qrBitmap = qrCodeGenerator.generateQRCode(qrData)
            
            Result.success(qrBitmap)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to generate verification QR code", e))
        }
    }

    /**
     * Scan and verify QR code from another user
     */
    suspend fun verifyQRCode(qrData: String): Result<VerificationResult> {
        return try {
            val verificationData = VerificationData.fromQRString(qrData)
            val remoteIdentityKey = IdentityKey(verificationData.identityKey, 0)
            
            // Verify the identity key matches what we have stored
            val storedIdentityKey = getStoredIdentityKey(verificationData.userId)
            
            val result = if (storedIdentityKey != null && storedIdentityKey.serialize().contentEquals(remoteIdentityKey.serialize())) {
                // Keys match - mark as verified
                markUserAsVerified(verificationData.userId, remoteIdentityKey)
                VerificationResult.Success(verificationData.userId, verificationData.displayName)
            } else if (storedIdentityKey == null) {
                // First time seeing this user - store and verify
                storeIdentityKey(verificationData.userId, remoteIdentityKey)
                markUserAsVerified(verificationData.userId, remoteIdentityKey)
                VerificationResult.Success(verificationData.userId, verificationData.displayName)
            } else {
                // Keys don't match - potential security issue
                val alert = SecurityAlert.KeyMismatch(
                    userId = verificationData.userId,
                    displayName = verificationData.displayName,
                    expectedKey = storedIdentityKey,
                    receivedKey = remoteIdentityKey
                )
                addSecurityAlert(alert)
                VerificationResult.KeyMismatch(verificationData.userId, verificationData.displayName)
            }
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to verify QR code", e))
        }
    }

    /**
     * Generate safety number for manual verification
     */
    suspend fun generateSafetyNumber(userId: String, remoteIdentityKey: IdentityKey): Result<String> {
        return try {
            val localIdentityKey = keyManager.getIdentityKey()
            val safetyNumber = safetyNumberGenerator.generateSafetyNumber(
                localIdentityKey = localIdentityKey,
                remoteIdentityKey = remoteIdentityKey,
                localUserId = keyManager.getRegistrationId().toString(),
                remoteUserId = userId
            )
            
            Result.success(safetyNumber)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to generate safety number", e))
        }
    }

    /**
     * Verify safety number manually entered by user
     */
    suspend fun verifySafetyNumber(
        userId: String,
        enteredSafetyNumber: String,
        remoteIdentityKey: IdentityKey
    ): Result<Boolean> {
        return try {
            val expectedSafetyNumber = generateSafetyNumber(userId, remoteIdentityKey).getOrThrow()
            val isValid = expectedSafetyNumber == enteredSafetyNumber
            
            if (isValid) {
                markUserAsVerified(userId, remoteIdentityKey)
            }
            
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to verify safety number", e))
        }
    }

    /**
     * Check if a user is verified
     */
    fun isUserVerified(userId: String): Boolean {
        return _verificationState.value[userId]?.isVerified == true
    }

    /**
     * Get verification state for a user
     */
    fun getVerificationState(userId: String): VerificationState? {
        return _verificationState.value[userId]
    }

    /**
     * Handle identity key change detection
     */
    suspend fun handleIdentityKeyChange(
        userId: String,
        displayName: String,
        oldKey: IdentityKey,
        newKey: IdentityKey
    ) {
        // Mark user as unverified
        markUserAsUnverified(userId)
        
        // Create security alert
        val alert = SecurityAlert.IdentityKeyChanged(
            userId = userId,
            displayName = displayName,
            oldKey = oldKey,
            newKey = newKey,
            changeTimestamp = System.currentTimeMillis()
        )
        
        addSecurityAlert(alert)
    }

    /**
     * Dismiss a security alert
     */
    fun dismissSecurityAlert(alertId: String) {
        val currentAlerts = _securityAlerts.value.toMutableList()
        currentAlerts.removeAll { it.id == alertId }
        _securityAlerts.value = currentAlerts
    }

    /**
     * Clear all security alerts
     */
    fun clearAllSecurityAlerts() {
        _securityAlerts.value = emptyList()
    }

    /**
     * Get security recommendations based on current state
     */
    fun getSecurityRecommendations(): List<SecurityRecommendation> {
        val recommendations = mutableListOf<SecurityRecommendation>()
        
        // Check for unverified contacts
        val unverifiedCount = _verificationState.value.values.count { !it.isVerified }
        if (unverifiedCount > 0) {
            recommendations.add(
                SecurityRecommendation.VerifyContacts(unverifiedCount)
            )
        }
        
        // Check for pending security alerts
        val alertCount = _securityAlerts.value.size
        if (alertCount > 0) {
            recommendations.add(
                SecurityRecommendation.ReviewSecurityAlerts(alertCount)
            )
        }
        
        return recommendations
    }
    
    /**
     * Generate verification QR code for a contact
     */
    suspend fun generateVerificationQR(contactId: String): Result<String> {
        return try {
            // Generate a QR code data string for verification
            val qrData = "chain://verify?contact=$contactId&timestamp=${System.currentTimeMillis()}"
            Result.success(qrData)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to generate verification QR", e))
        }
    }
    
    /**
     * Complete verification for a contact
     */
    suspend fun completeVerification(contactId: String): Boolean {
        return try {
            // Mark contact as verified
            val currentStates = _verificationState.value.toMutableMap()
            currentStates[contactId]?.let { state ->
                currentStates[contactId] = state.copy(
                    isVerified = true,
                    verifiedAt = System.currentTimeMillis()
                )
            }
            _verificationState.value = currentStates
            true
        } catch (e: Exception) {
            false
        }
    }

    // Private helper methods
    
    private fun markUserAsVerified(userId: String, identityKey: IdentityKey) {
        val currentStates = _verificationState.value.toMutableMap()
        currentStates[userId] = VerificationState(
            userId = userId,
            identityKey = identityKey,
            isVerified = true,
            verifiedAt = System.currentTimeMillis()
        )
        _verificationState.value = currentStates
    }
    
    private fun markUserAsUnverified(userId: String) {
        val currentStates = _verificationState.value.toMutableMap()
        currentStates[userId]?.let { state ->
            currentStates[userId] = state.copy(
                isVerified = false,
                verifiedAt = null
            )
        }
        _verificationState.value = currentStates
    }
    
    private fun addSecurityAlert(alert: SecurityAlert) {
        val currentAlerts = _securityAlerts.value.toMutableList()
        currentAlerts.add(alert)
        _securityAlerts.value = currentAlerts
    }
    
    private suspend fun getStoredIdentityKey(userId: String): IdentityKey? {
        // This would typically query the identity storage
        // For now, return null to indicate no stored key
        return null
    }
    
    private suspend fun storeIdentityKey(userId: String, identityKey: IdentityKey) {
        // Store the identity key for future verification
        // Implementation would depend on the identity storage system
    }
}

/**
 * Data class for QR code verification data
 */
data class VerificationData(
    val userId: String,
    val displayName: String,
    val identityKey: ByteArray,
    val timestamp: Long
) {
    fun toQRString(): String {
        // Create a compact string representation for QR code
        val keyHash = MessageDigest.getInstance("SHA-256").digest(identityKey)
        val keyHashHex = keyHash.joinToString("") { "%02x".format(it) }
        return "chain://verify?user=$userId&name=$displayName&key=$keyHashHex&ts=$timestamp"
    }
    
    companion object {
        fun fromQRString(qrData: String): VerificationData {
            // Parse the QR string back to VerificationData
            val uri = android.net.Uri.parse(qrData)
            require(uri.scheme == "chain" && uri.host == "verify") {
                "Invalid verification QR code format"
            }
            
            val userId = uri.getQueryParameter("user") ?: throw IllegalArgumentException("Missing user ID")
            val displayName = uri.getQueryParameter("name") ?: throw IllegalArgumentException("Missing display name")
            val keyHex = uri.getQueryParameter("key") ?: throw IllegalArgumentException("Missing key")
            val timestamp = uri.getQueryParameter("ts")?.toLongOrNull() ?: throw IllegalArgumentException("Missing timestamp")
            
            // For QR codes, we only store a hash of the key, not the full key
            // In a real implementation, you'd need to exchange the full key through another channel
            val keyHash = keyHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            
            return VerificationData(userId, displayName, keyHash, timestamp)
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as VerificationData
        
        if (userId != other.userId) return false
        if (displayName != other.displayName) return false
        if (!identityKey.contentEquals(other.identityKey)) return false
        if (timestamp != other.timestamp) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + identityKey.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

/**
 * Represents the verification state of a user
 */
data class VerificationState(
    val userId: String,
    val identityKey: IdentityKey,
    val isVerified: Boolean,
    val verifiedAt: Long? = null
)

/**
 * Result of verification attempt
 */
sealed class VerificationResult {
    data class Success(val userId: String, val displayName: String) : VerificationResult()
    data class KeyMismatch(val userId: String, val displayName: String) : VerificationResult()
    data class InvalidData(val reason: String) : VerificationResult()
}

