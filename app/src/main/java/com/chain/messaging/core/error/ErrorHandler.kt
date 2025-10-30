package com.chain.messaging.core.error

import com.chain.messaging.core.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central error handler for the Chain messaging app
 */
@Singleton
class ErrorHandler @Inject constructor() {
    
    private val _errorEvents = MutableSharedFlow<ErrorEvent>()
    val errorEvents: SharedFlow<ErrorEvent> = _errorEvents.asSharedFlow()
    
    /**
     * Handle an error with optional recovery strategy
     */
    suspend fun handleError(
        error: ChainError,
        context: ErrorContext,
        recoveryStrategy: RecoveryStrategy? = null
    ): ErrorResult {
        Logger.e("Error occurred in ${context.component}", error)
        
        // Emit error event for UI handling
        val errorEvent = ErrorEvent(
            error = error,
            context = context,
            userMessage = getUserFriendlyMessage(error),
            recoveryActions = getRecoveryActions(error, context),
            timestamp = System.currentTimeMillis()
        )
        
        _errorEvents.emit(errorEvent)
        
        // Apply recovery strategy if provided
        val recoveryResult = recoveryStrategy?.let { strategy ->
            try {
                strategy.recover(error, context)
            } catch (e: Exception) {
                Logger.e("Recovery strategy failed", e)
                RecoveryResult.Failed(e.toChainError())
            }
        } ?: RecoveryResult.NoRecovery
        
        return ErrorResult(
            error = error,
            context = context,
            recoveryResult = recoveryResult,
            handled = true
        )
    }
    
    /**
     * Handle an error without recovery
     */
    suspend fun handleError(error: ChainError, context: ErrorContext): ErrorResult {
        return handleError(error, context, null)
    }
    
    /**
     * Handle a throwable by converting it to ChainError
     */
    suspend fun handleThrowable(
        throwable: Throwable,
        context: ErrorContext,
        recoveryStrategy: RecoveryStrategy? = null
    ): ErrorResult {
        val chainError = throwable.toChainError()
        return handleError(chainError, context, recoveryStrategy)
    }
    
    /**
     * Get user-friendly error message
     */
    private fun getUserFriendlyMessage(error: ChainError): String {
        return when (error) {
            // Network errors
            is ChainError.NetworkError.NoInternet -> "No internet connection. Please check your network settings."
            is ChainError.NetworkError.ConnectionTimeout -> "Connection timed out. Please try again."
            is ChainError.NetworkError.ServerUnreachable -> "Unable to connect to server. Please try again later."
            is ChainError.NetworkError.RateLimited -> "Too many requests. Please wait a moment and try again."
            is ChainError.NetworkError.NetworkUnavailable -> "Network is temporarily unavailable. Please try again."
            
            // Blockchain errors
            is ChainError.BlockchainError.NodeUnavailable -> "Blockchain network is unavailable. Your messages will be sent when connection is restored."
            is ChainError.BlockchainError.TransactionFailed -> "Failed to send message. Please try again."
            is ChainError.BlockchainError.SyncFailure -> "Unable to sync with blockchain. Some messages may be delayed."
            is ChainError.BlockchainError.ConsensusFailure -> "Network consensus issue. Please wait and try again."
            
            // Encryption errors
            is ChainError.EncryptionError.KeyExchangeFailed -> "Unable to establish secure connection with this contact. Please try again."
            is ChainError.EncryptionError.DecryptionFailed -> "Unable to decrypt message. The message may be corrupted."
            is ChainError.EncryptionError.SessionNotFound -> "Secure session not found. Please restart the conversation."
            is ChainError.EncryptionError.InvalidSignature -> "Message signature is invalid. This message may not be authentic."
            
            // Storage errors
            is ChainError.StorageError.DatabaseError -> "Database error occurred. Please restart the app."
            is ChainError.StorageError.CloudStorageError -> "Cloud storage error. Please check your account settings."
            is ChainError.StorageError.StorageQuotaExceeded -> "Storage quota exceeded. Please free up some space."
            is ChainError.StorageError.CorruptedData -> "Data corruption detected. Some information may be lost."
            
            // Auth errors
            is ChainError.AuthError.InvalidCredentials -> "Invalid credentials. Please check your login information."
            is ChainError.AuthError.AuthenticationFailed -> "Authentication failed. Please try logging in again."
            is ChainError.AuthError.TokenExpired -> "Your session has expired. Please log in again."
            is ChainError.AuthError.BiometricUnavailable -> "Biometric authentication is not available on this device."
            
            // Call errors
            is ChainError.CallError.ConnectionFailed -> "Call connection failed. Please check your network and try again."
            is ChainError.CallError.MediaDeviceError -> "Media device error. Please check your camera/microphone permissions."
            is ChainError.CallError.CallRejected -> "Call was rejected."
            is ChainError.CallError.CallTimeout -> "Call connection timed out. Please try again."
            
            // P2P errors
            is ChainError.P2PError.PeerDiscoveryFailed -> "Unable to find other users. Please check your network connection."
            is ChainError.P2PError.MessageRoutingFailed -> "Message routing failed. Your message will be sent when network improves."
            
            // UI errors
            is ChainError.UIError.InvalidInput -> "Invalid input: ${error.message}"
            is ChainError.UIError.MediaAccessError -> "Cannot access media. Please check your permissions."
            
            // System errors
            is ChainError.SystemError.OutOfMemory -> "Low memory. Please close some apps and try again."
            is ChainError.SystemError.ServiceUnavailable -> "Service temporarily unavailable. Please try again later."
            is ChainError.SystemError.FeatureNotSupported -> "This feature is not supported on your device."
            
            else -> "An unexpected error occurred. Please try again."
        }
    }
    
    /**
     * Get available recovery actions for an error
     */
    private fun getRecoveryActions(error: ChainError, context: ErrorContext): List<RecoveryAction> {
        return when (error) {
            is ChainError.NetworkError -> listOf(
                RecoveryAction.Retry("Retry"),
                RecoveryAction.CheckNetworkSettings("Check Network"),
                RecoveryAction.UseOfflineMode("Work Offline")
            )
            
            is ChainError.BlockchainError -> listOf(
                RecoveryAction.Retry("Retry"),
                RecoveryAction.QueueForLater("Queue Message"),
                RecoveryAction.SwitchNode("Try Different Node")
            )
            
            is ChainError.EncryptionError.KeyExchangeFailed -> listOf(
                RecoveryAction.Retry("Retry"),
                RecoveryAction.VerifyContact("Verify Contact"),
                RecoveryAction.ResetSession("Reset Session")
            )
            
            is ChainError.StorageError.StorageQuotaExceeded -> listOf(
                RecoveryAction.CleanupStorage("Free Space"),
                RecoveryAction.ChangeStorageProvider("Change Provider")
            )
            
            is ChainError.AuthError.TokenExpired -> listOf(
                RecoveryAction.ReAuthenticate("Log In Again")
            )
            
            is ChainError.CallError -> listOf(
                RecoveryAction.Retry("Try Again"),
                RecoveryAction.CheckPermissions("Check Permissions"),
                RecoveryAction.SwitchToAudio("Audio Only")
            )
            
            else -> listOf(RecoveryAction.Retry("Try Again"))
        }
    }
}

/**
 * Context information about where the error occurred
 */
data class ErrorContext(
    val component: String,
    val operation: String,
    val userId: String? = null,
    val chatId: String? = null,
    val messageId: String? = null,
    val additionalInfo: Map<String, Any> = emptyMap()
)

/**
 * Error event emitted by the error handler
 */
data class ErrorEvent(
    val error: ChainError,
    val context: ErrorContext,
    val userMessage: String,
    val recoveryActions: List<RecoveryAction>,
    val timestamp: Long
)

/**
 * Result of error handling
 */
data class ErrorResult(
    val error: ChainError,
    val context: ErrorContext,
    val recoveryResult: RecoveryResult,
    val handled: Boolean
)

/**
 * Available recovery actions
 */
sealed class RecoveryAction(val label: String) {
    object Retry : RecoveryAction("Retry")
    object CheckNetworkSettings : RecoveryAction("Check Network")
    object UseOfflineMode : RecoveryAction("Work Offline")
    object QueueForLater : RecoveryAction("Queue for Later")
    object SwitchNode : RecoveryAction("Switch Node")
    object VerifyContact : RecoveryAction("Verify Contact")
    object ResetSession : RecoveryAction("Reset Session")
    object CleanupStorage : RecoveryAction("Free Up Space")
    object ChangeStorageProvider : RecoveryAction("Change Provider")
    object ReAuthenticate : RecoveryAction("Log In Again")
    object CheckPermissions : RecoveryAction("Check Permissions")
    object SwitchToAudio : RecoveryAction("Audio Only")
    data class Custom(val customLabel: String, val action: suspend () -> Unit) : RecoveryAction(customLabel)
}