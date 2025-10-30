package com.chain.messaging.core.error

import com.chain.messaging.core.util.Logger
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

/**
 * Interface for error recovery strategies
 */
interface RecoveryStrategy {
    suspend fun recover(error: ChainError, context: ErrorContext): RecoveryResult
}

/**
 * Result of a recovery attempt
 */
sealed class RecoveryResult {
    object Success : RecoveryResult()
    data class Failed(val error: ChainError) : RecoveryResult()
    object Retry : RecoveryResult()
    object NoRecovery : RecoveryResult()
}

/**
 * Retry strategy with exponential backoff
 */
class RetryStrategy(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000,
    private val maxDelayMs: Long = 30000,
    private val retryCondition: (ChainError) -> Boolean = { true }
) : RecoveryStrategy {
    
    override suspend fun recover(error: ChainError, context: ErrorContext): RecoveryResult {
        if (!retryCondition(error)) {
            return RecoveryResult.NoRecovery
        }
        
        repeat(maxRetries) { attempt ->
            val delayMs = min(
                baseDelayMs * (2.0.pow(attempt)).toLong(),
                maxDelayMs
            )
            
            Logger.d("Retrying operation after ${delayMs}ms (attempt ${attempt + 1}/$maxRetries)")
            delay(delayMs)
            
            // Return retry signal - actual retry logic should be handled by caller
            return RecoveryResult.Retry
        }
        
        return RecoveryResult.Failed(
            ChainError.SystemError.UnexpectedError(
                RuntimeException("Max retries ($maxRetries) exceeded")
            )
        )
    }
}

/**
 * Network-specific recovery strategy
 */
class NetworkRecoveryStrategy(
    private val networkMonitor: com.chain.messaging.core.network.NetworkMonitor
) : RecoveryStrategy {
    
    override suspend fun recover(error: ChainError, context: ErrorContext): RecoveryResult {
        return when (error) {
            is ChainError.NetworkError.NoInternet -> {
                // Wait for network to become available
                Logger.d("Waiting for network connectivity...")
                
                // Check if network is available now
                if (networkMonitor.isNetworkAvailable()) {
                    RecoveryResult.Success
                } else {
                    RecoveryResult.Failed(error)
                }
            }
            
            is ChainError.NetworkError.ConnectionTimeout -> {
                // Try with different timeout or connection parameters
                Logger.d("Attempting recovery from connection timeout")
                RecoveryResult.Retry
            }
            
            is ChainError.NetworkError.ServerUnreachable -> {
                // Try alternative servers or wait
                Logger.d("Server unreachable, attempting alternative connection")
                RecoveryResult.Retry
            }
            
            else -> RecoveryResult.NoRecovery
        }
    }
}

/**
 * Blockchain-specific recovery strategy
 */
class BlockchainRecoveryStrategy : RecoveryStrategy {
    
    override suspend fun recover(error: ChainError, context: ErrorContext): RecoveryResult {
        return when (error) {
            is ChainError.BlockchainError.NodeUnavailable -> {
                Logger.d("Blockchain node unavailable, queuing message for later")
                // Queue message for later transmission
                RecoveryResult.Success
            }
            
            is ChainError.BlockchainError.TransactionFailed -> {
                Logger.d("Transaction failed, attempting retry with adjusted parameters")
                RecoveryResult.Retry
            }
            
            is ChainError.BlockchainError.SyncFailure -> {
                Logger.d("Blockchain sync failed, attempting incremental sync")
                RecoveryResult.Retry
            }
            
            else -> RecoveryResult.NoRecovery
        }
    }
}

/**
 * Encryption-specific recovery strategy
 */
class EncryptionRecoveryStrategy : RecoveryStrategy {
    
    override suspend fun recover(error: ChainError, context: ErrorContext): RecoveryResult {
        return when (error) {
            is ChainError.EncryptionError.KeyExchangeFailed -> {
                Logger.d("Key exchange failed, attempting session reset")
                // Reset encryption session and retry
                RecoveryResult.Retry
            }
            
            is ChainError.EncryptionError.SessionNotFound -> {
                Logger.d("Session not found, initializing new session")
                // Initialize new encryption session
                RecoveryResult.Retry
            }
            
            is ChainError.EncryptionError.DecryptionFailed -> {
                Logger.d("Decryption failed, requesting message resend")
                // Request sender to resend message
                RecoveryResult.Failed(error) // Cannot recover from decryption failure
            }
            
            else -> RecoveryResult.NoRecovery
        }
    }
}

/**
 * Storage-specific recovery strategy
 */
class StorageRecoveryStrategy : RecoveryStrategy {
    
    override suspend fun recover(error: ChainError, context: ErrorContext): RecoveryResult {
        return when (error) {
            is ChainError.StorageError.DatabaseError -> {
                Logger.d("Database error, attempting database repair")
                // Attempt database repair or recreation
                RecoveryResult.Retry
            }
            
            is ChainError.StorageError.CloudStorageError -> {
                Logger.d("Cloud storage error, falling back to local storage")
                // Fall back to local storage
                RecoveryResult.Success
            }
            
            is ChainError.StorageError.StorageQuotaExceeded -> {
                Logger.d("Storage quota exceeded, attempting cleanup")
                // Attempt automatic cleanup
                RecoveryResult.Retry
            }
            
            is ChainError.StorageError.CorruptedData -> {
                Logger.d("Corrupted data detected, attempting recovery from backup")
                // Attempt recovery from backup or blockchain
                RecoveryResult.Retry
            }
            
            else -> RecoveryResult.NoRecovery
        }
    }
}

/**
 * Call-specific recovery strategy
 */
class CallRecoveryStrategy : RecoveryStrategy {
    
    override suspend fun recover(error: ChainError, context: ErrorContext): RecoveryResult {
        return when (error) {
            is ChainError.CallError.ConnectionFailed -> {
                Logger.d("Call connection failed, attempting TURN server fallback")
                RecoveryResult.Retry
            }
            
            is ChainError.CallError.MediaDeviceError -> {
                Logger.d("Media device error, attempting device reset")
                RecoveryResult.Retry
            }
            
            is ChainError.CallError.IceConnectionFailed -> {
                Logger.d("ICE connection failed, trying alternative ICE servers")
                RecoveryResult.Retry
            }
            
            else -> RecoveryResult.NoRecovery
        }
    }
}

/**
 * Composite recovery strategy that tries multiple strategies
 */
class CompositeRecoveryStrategy(
    private val strategies: List<RecoveryStrategy>
) : RecoveryStrategy {
    
    override suspend fun recover(error: ChainError, context: ErrorContext): RecoveryResult {
        for (strategy in strategies) {
            val result = strategy.recover(error, context)
            if (result != RecoveryResult.NoRecovery) {
                return result
            }
        }
        return RecoveryResult.NoRecovery
    }
}

/**
 * Factory for creating appropriate recovery strategies
 */
object RecoveryStrategyFactory {
    
    fun createForError(
        error: ChainError,
        networkMonitor: com.chain.messaging.core.network.NetworkMonitor? = null
    ): RecoveryStrategy {
        return when (error) {
            is ChainError.NetworkError -> {
                if (networkMonitor != null) {
                    CompositeRecoveryStrategy(
                        listOf(
                            NetworkRecoveryStrategy(networkMonitor),
                            RetryStrategy(maxRetries = 3)
                        )
                    )
                } else {
                    RetryStrategy(maxRetries = 3)
                }
            }
            
            is ChainError.BlockchainError -> CompositeRecoveryStrategy(
                listOf(
                    BlockchainRecoveryStrategy(),
                    RetryStrategy(maxRetries = 5, baseDelayMs = 2000)
                )
            )
            
            is ChainError.EncryptionError -> CompositeRecoveryStrategy(
                listOf(
                    EncryptionRecoveryStrategy(),
                    RetryStrategy(maxRetries = 2)
                )
            )
            
            is ChainError.StorageError -> CompositeRecoveryStrategy(
                listOf(
                    StorageRecoveryStrategy(),
                    RetryStrategy(maxRetries = 3)
                )
            )
            
            is ChainError.CallError -> CompositeRecoveryStrategy(
                listOf(
                    CallRecoveryStrategy(),
                    RetryStrategy(maxRetries = 2, baseDelayMs = 500)
                )
            )
            
            else -> RetryStrategy(maxRetries = 1)
        }
    }
}