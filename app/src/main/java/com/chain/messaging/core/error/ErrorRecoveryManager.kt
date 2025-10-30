package com.chain.messaging.core.error

import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.core.util.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages automatic error recovery across the application
 */
@Singleton
class ErrorRecoveryManager @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val networkMonitor: NetworkMonitor
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeRecoveries = mutableMapOf<String, Job>()
    
    init {
        // Monitor error events and attempt automatic recovery
        errorHandler.errorEvents
            .onEach { errorEvent ->
                attemptAutomaticRecovery(errorEvent)
            }
            .launchIn(scope)
    }
    
    /**
     * Attempt automatic recovery for an error event
     */
    private suspend fun attemptAutomaticRecovery(errorEvent: ErrorEvent) {
        val recoveryKey = "${errorEvent.context.component}-${errorEvent.context.operation}"
        
        // Cancel any existing recovery for this operation
        activeRecoveries[recoveryKey]?.cancel()
        
        // Start new recovery job
        val recoveryJob = scope.launch {
            try {
                val strategy = RecoveryStrategyFactory.createForError(
                    errorEvent.error,
                    networkMonitor
                )
                
                val result = strategy.recover(errorEvent.error, errorEvent.context)
                
                when (result) {
                    is RecoveryResult.Success -> {
                        Logger.i("Automatic recovery successful for ${errorEvent.context.operation}")
                    }
                    
                    is RecoveryResult.Retry -> {
                        Logger.i("Recovery suggests retry for ${errorEvent.context.operation}")
                        // Emit retry signal that components can listen to
                        _retrySignals.emit(
                            RetrySignal(
                                context = errorEvent.context,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    
                    is RecoveryResult.Failed -> {
                        Logger.w("Automatic recovery failed for ${errorEvent.context.operation}", result.error)
                    }
                    
                    RecoveryResult.NoRecovery -> {
                        Logger.d("No automatic recovery available for ${errorEvent.context.operation}")
                    }
                }
            } catch (e: Exception) {
                Logger.e("Error during automatic recovery", e)
            } finally {
                activeRecoveries.remove(recoveryKey)
            }
        }
        
        activeRecoveries[recoveryKey] = recoveryJob
    }
    
    private val _retrySignals = MutableSharedFlow<RetrySignal>()
    val retrySignals: SharedFlow<RetrySignal> = _retrySignals.asSharedFlow()
    
    /**
     * Monitor network connectivity and trigger recovery for network-related errors
     */
    fun startNetworkRecoveryMonitoring() {
        networkMonitor.isConnected
            .distinctUntilChanged()
            .filter { it } // Only when network becomes available
            .onEach {
                Logger.i("Network connectivity restored, triggering network error recovery")
                triggerNetworkErrorRecovery()
            }
            .launchIn(scope)
    }
    
    /**
     * Trigger recovery for all network-related errors when connectivity is restored
     */
    private suspend fun triggerNetworkErrorRecovery() {
        _retrySignals.emit(
            RetrySignal(
                context = ErrorContext(
                    component = "NetworkRecovery",
                    operation = "ConnectivityRestored"
                ),
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Manually trigger recovery for a specific operation
     */
    suspend fun triggerRecovery(context: ErrorContext) {
        _retrySignals.emit(
            RetrySignal(
                context = context,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Cancel all active recovery operations
     */
    fun cancelAllRecoveries() {
        activeRecoveries.values.forEach { it.cancel() }
        activeRecoveries.clear()
    }
    
    /**
     * Get status of active recoveries
     */
    fun getActiveRecoveries(): Map<String, Boolean> {
        return activeRecoveries.mapValues { it.value.isActive }
    }
    
    fun cleanup() {
        scope.cancel()
        activeRecoveries.clear()
    }
}

/**
 * Signal emitted when a retry should be attempted
 */
data class RetrySignal(
    val context: ErrorContext,
    val timestamp: Long
)

/**
 * Extension functions for easier error handling in components
 */
suspend inline fun <T> ErrorRecoveryManager.withErrorHandling(
    context: ErrorContext,
    crossinline operation: suspend () -> T
): Result<T> {
    return try {
        Result.success(operation())
    } catch (e: Exception) {
        val chainError = e.toChainError()
        Logger.e("Operation failed in ${context.component}.${context.operation}", chainError)
        
        // Trigger automatic recovery
        triggerRecovery(context)
        
        Result.failure(chainError)
    }
}

/**
 * Retry an operation with automatic error handling
 */
suspend inline fun <T> ErrorRecoveryManager.retryOperation(
    context: ErrorContext,
    maxRetries: Int = 3,
    crossinline operation: suspend () -> T
): Result<T> {
    var lastError: ChainError? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return Result.success(operation())
        } catch (e: Exception) {
            lastError = e.toChainError()
            Logger.w("Operation attempt ${attempt + 1} failed in ${context.component}.${context.operation}", lastError)
            
            if (attempt < maxRetries - 1) {
                // Wait before retry
                delay(1000L * (attempt + 1))
            }
        }
    }
    
    // All retries failed
    lastError?.let { error ->
        triggerRecovery(context)
        return Result.failure(error)
    }
    
    return Result.failure(ChainError.SystemError.UnexpectedError())
}