package com.chain.messaging.core.error

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.core.p2p.P2PManager
import com.chain.messaging.core.util.Logger
import com.chain.messaging.core.webrtc.WebRTCManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Error handling extensions for blockchain operations
 */
@Singleton
class BlockchainErrorHandler @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager
) {
    
    suspend fun <T> handleBlockchainOperation(
        operation: String,
        block: suspend () -> T
    ): Result<T> {
        val context = ErrorContext(
            component = "BlockchainManager",
            operation = operation
        )
        
        return errorRecoveryManager.withErrorHandling(context) {
            try {
                block()
            } catch (e: Exception) {
                val chainError = when {
                    e.message?.contains("timeout") == true -> 
                        ChainError.BlockchainError.NodeUnavailable(e)
                    e.message?.contains("transaction failed") == true -> 
                        ChainError.BlockchainError.TransactionFailed(null, e)
                    e.message?.contains("insufficient funds") == true -> 
                        ChainError.BlockchainError.InsufficientFunds(e)
                    e.message?.contains("consensus") == true -> 
                        ChainError.BlockchainError.ConsensusFailure(e)
                    else -> ChainError.BlockchainError.NodeUnavailable(e)
                }
                throw chainError
            }
        }
    }
    
    fun createRetryableBlockchainFlow<T>(
        operation: String,
        block: suspend () -> T
    ) = flow {
        val result = handleBlockchainOperation(operation, block)
        result.fold(
            onSuccess = { emit(it) },
            onFailure = { throw it }
        )
    }.retry(3) { cause ->
        Logger.w("Retrying blockchain operation: $operation", cause)
        cause is ChainError.BlockchainError.NodeUnavailable ||
        cause is ChainError.BlockchainError.SyncFailure
    }.catch { cause ->
        Logger.e("Blockchain operation failed after retries: $operation", cause)
        emit(null as T) // Emit null or default value
    }
}

/**
 * Error handling extensions for encryption operations
 */
@Singleton
class EncryptionErrorHandler @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager
) {
    
    suspend fun <T> handleEncryptionOperation(
        operation: String,
        userId: String? = null,
        block: suspend () -> T
    ): Result<T> {
        val context = ErrorContext(
            component = "EncryptionService",
            operation = operation,
            userId = userId
        )
        
        return errorRecoveryManager.withErrorHandling(context) {
            try {
                block()
            } catch (e: Exception) {
                val chainError = when {
                    e.message?.contains("key exchange") == true -> 
                        ChainError.EncryptionError.KeyExchangeFailed(userId ?: "unknown", e)
                    e.message?.contains("decrypt") == true -> 
                        ChainError.EncryptionError.DecryptionFailed(e)
                    e.message?.contains("encrypt") == true -> 
                        ChainError.EncryptionError.EncryptionFailed(e)
                    e.message?.contains("signature") == true -> 
                        ChainError.EncryptionError.InvalidSignature(e)
                    e.message?.contains("session") == true -> 
                        ChainError.EncryptionError.SessionNotFound(userId ?: "unknown")
                    else -> ChainError.EncryptionError.EncryptionFailed(e)
                }
                throw chainError
            }
        }
    }
}

/**
 * Error handling extensions for network operations
 */
@Singleton
class NetworkErrorHandler @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager,
    private val networkMonitor: NetworkMonitor
) {
    
    suspend fun <T> handleNetworkOperation(
        operation: String,
        block: suspend () -> T
    ): Result<T> {
        val context = ErrorContext(
            component = "NetworkManager",
            operation = operation
        )
        
        // Check network availability first
        if (!networkMonitor.isNetworkAvailable()) {
            val error = ChainError.NetworkError.NoInternet()
            errorHandler.handleError(error, context)
            return Result.failure(error)
        }
        
        return errorRecoveryManager.retryOperation(context, maxRetries = 3) {
            try {
                block()
            } catch (e: Exception) {
                val chainError = when {
                    e.message?.contains("timeout") == true -> 
                        ChainError.NetworkError.ConnectionTimeout(e)
                    e.message?.contains("unreachable") == true -> 
                        ChainError.NetworkError.ServerUnreachable("server", e)
                    e.message?.contains("rate limit") == true -> 
                        ChainError.NetworkError.RateLimited()
                    else -> ChainError.NetworkError.NetworkUnavailable(e)
                }
                throw chainError
            }
        }
    }
}

/**
 * Error handling extensions for P2P operations
 */
@Singleton
class P2PErrorHandler @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager
) {
    
    suspend fun <T> handleP2POperation(
        operation: String,
        peerId: String? = null,
        block: suspend () -> T
    ): Result<T> {
        val context = ErrorContext(
            component = "P2PManager",
            operation = operation,
            additionalInfo = peerId?.let { mapOf("peerId" to it) } ?: emptyMap()
        )
        
        return errorRecoveryManager.withErrorHandling(context) {
            try {
                block()
            } catch (e: Exception) {
                val chainError = when {
                    e.message?.contains("peer discovery") == true -> 
                        ChainError.P2PError.PeerDiscoveryFailed(e)
                    e.message?.contains("connection") == true && peerId != null -> 
                        ChainError.P2PError.PeerConnectionFailed(peerId, e)
                    e.message?.contains("routing") == true -> 
                        ChainError.P2PError.MessageRoutingFailed(e)
                    e.message?.contains("DHT") == true -> 
                        ChainError.P2PError.DHTError(operation, e)
                    e.message?.contains("NAT") == true -> 
                        ChainError.P2PError.NATTraversalFailed(e)
                    else -> ChainError.P2PError.PeerDiscoveryFailed(e)
                }
                throw chainError
            }
        }
    }
}

/**
 * Error handling extensions for WebRTC operations
 */
@Singleton
class WebRTCErrorHandler @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager
) {
    
    suspend fun <T> handleWebRTCOperation(
        operation: String,
        callId: String? = null,
        block: suspend () -> T
    ): Result<T> {
        val context = ErrorContext(
            component = "WebRTCManager",
            operation = operation,
            additionalInfo = callId?.let { mapOf("callId" to it) } ?: emptyMap()
        )
        
        return errorRecoveryManager.withErrorHandling(context) {
            try {
                block()
            } catch (e: Exception) {
                val chainError = when {
                    e.message?.contains("connection failed") == true -> 
                        ChainError.CallError.ConnectionFailed(e)
                    e.message?.contains("media device") == true -> 
                        ChainError.CallError.MediaDeviceError("unknown", e)
                    e.message?.contains("signaling") == true -> 
                        ChainError.CallError.SignalingError(e)
                    e.message?.contains("ICE") == true -> 
                        ChainError.CallError.IceConnectionFailed(e)
                    e.message?.contains("timeout") == true -> 
                        ChainError.CallError.CallTimeout(e)
                    else -> ChainError.CallError.ConnectionFailed(e)
                }
                throw chainError
            }
        }
    }
}

/**
 * Error handling extensions for storage operations
 */
@Singleton
class StorageErrorHandler @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager
) {
    
    suspend fun <T> handleStorageOperation(
        operation: String,
        storageType: String = "local",
        block: suspend () -> T
    ): Result<T> {
        val context = ErrorContext(
            component = "StorageManager",
            operation = operation,
            additionalInfo = mapOf("storageType" to storageType)
        )
        
        return errorRecoveryManager.withErrorHandling(context) {
            try {
                block()
            } catch (e: Exception) {
                val chainError = when {
                    e.message?.contains("database") == true -> 
                        ChainError.StorageError.DatabaseError(operation, e)
                    e.message?.contains("file system") == true -> 
                        ChainError.StorageError.FileSystemError(operation, e)
                    e.message?.contains("cloud") == true -> 
                        ChainError.StorageError.CloudStorageError(storageType, operation, e)
                    e.message?.contains("quota") == true -> 
                        ChainError.StorageError.StorageQuotaExceeded(storageType)
                    e.message?.contains("corrupted") == true -> 
                        ChainError.StorageError.CorruptedData("unknown", e)
                    e.message?.contains("access denied") == true -> 
                        ChainError.StorageError.AccessDenied("storage", e)
                    else -> ChainError.StorageError.DatabaseError(operation, e)
                }
                throw chainError
            }
        }
    }
}

/**
 * Error handling extensions for messaging operations
 */
@Singleton
class MessagingErrorHandler @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager
) {
    
    suspend fun <T> handleMessagingOperation(
        operation: String,
        chatId: String? = null,
        messageId: String? = null,
        block: suspend () -> T
    ): Result<T> {
        val context = ErrorContext(
            component = "MessagingService",
            operation = operation,
            chatId = chatId,
            messageId = messageId
        )
        
        return errorRecoveryManager.withErrorHandling(context) {
            block()
        }
    }
}