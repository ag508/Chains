package com.chain.messaging.core.blockchain

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

/**
 * Manages pending message transactions before they are confirmed on the blockchain
 */
class TransactionPool {
    
    private val TAG = "TransactionPool"
    
    private val pendingTransactions = ConcurrentHashMap<String, PendingTransaction>()
    private val transactionQueue = ConcurrentHashMap<String, MutableList<MessageTransaction>>()
    
    private val _poolState = MutableStateFlow(TransactionPoolState())
    val poolState: StateFlow<TransactionPoolState> = _poolState.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isInitialized = false
    
    /**
     * Initialize the transaction pool
     */
    suspend fun initialize() {
        if (!isInitialized) {
            isInitialized = true
            startPoolMaintenance()
            Log.i(TAG, "Transaction pool initialized")
        }
    }
    
    /**
     * Add a transaction to the pending pool
     */
    fun addTransaction(transaction: MessageTransaction) {
        val pendingTransaction = PendingTransaction(
            transaction = transaction,
            addedAt = System.currentTimeMillis(),
            retryCount = 0,
            status = TransactionStatus.PENDING
        )
        
        pendingTransactions[transaction.id] = pendingTransaction
        
        // Add to user's transaction queue
        transactionQueue.getOrPut(transaction.from) { ArrayList() }.add(transaction)
        
        updatePoolState()
        Log.d(TAG, "Added transaction to pool: ${transaction.id}")
    }
    
    /**
     * Mark a transaction as confirmed
     */
    fun confirmTransaction(transactionId: String, blockNumber: Long) {
        pendingTransactions[transactionId]?.let { pending ->
            val confirmedTransaction = pending.copy(
                status = TransactionStatus.CONFIRMED,
                blockNumber = blockNumber,
                confirmedAt = System.currentTimeMillis()
            )
            pendingTransactions[transactionId] = confirmedTransaction
            
            // Remove from user's queue
            val userId = pending.transaction.from
            transactionQueue[userId]?.removeIf { it.id == transactionId }
            
            updatePoolState()
            Log.d(TAG, "Confirmed transaction: $transactionId in block $blockNumber")
        }
    }
    
    /**
     * Mark a transaction as failed
     */
    fun failTransaction(transactionId: String, reason: String) {
        pendingTransactions[transactionId]?.let { pending ->
            val failedTransaction = pending.copy(
                status = TransactionStatus.FAILED,
                failureReason = reason,
                failedAt = System.currentTimeMillis()
            )
            pendingTransactions[transactionId] = failedTransaction
            
            updatePoolState()
            Log.w(TAG, "Failed transaction: $transactionId - $reason")
        }
    }
    
    /**
     * Get all pending transactions for a user
     */
    fun getPendingTransactions(userId: String): List<MessageTransaction> {
        return transactionQueue[userId]?.toList() ?: emptyList()
    }
    
    /**
     * Get transaction status
     */
    fun getTransactionStatus(transactionId: String): TransactionStatus? {
        return pendingTransactions[transactionId]?.status
    }
    
    /**
     * Get all transactions that need retry
     */
    fun getTransactionsForRetry(): List<MessageTransaction> {
        val now = System.currentTimeMillis()
        val retryThreshold = 30000 // 30 seconds
        
        return pendingTransactions.values
            .filter { 
                it.status == TransactionStatus.PENDING && 
                it.retryCount < MAX_RETRY_COUNT &&
                (now - it.lastRetryAt) > retryThreshold
            }
            .map { it.transaction }
    }
    
    /**
     * Increment retry count for a transaction
     */
    fun incrementRetryCount(transactionId: String) {
        pendingTransactions[transactionId]?.let { pending ->
            val updatedTransaction = pending.copy(
                retryCount = pending.retryCount + 1,
                lastRetryAt = System.currentTimeMillis()
            )
            pendingTransactions[transactionId] = updatedTransaction
            
            if (updatedTransaction.retryCount >= MAX_RETRY_COUNT) {
                failTransaction(transactionId, "Max retry count exceeded")
            }
        }
    }
    
    /**
     * Remove old confirmed and failed transactions
     */
    fun cleanup() {
        val now = System.currentTimeMillis()
        val cleanupThreshold = 24 * 60 * 60 * 1000 // 24 hours
        
        val toRemove = pendingTransactions.entries
            .filter { (_, pending) ->
                (pending.status == TransactionStatus.CONFIRMED || pending.status == TransactionStatus.FAILED) &&
                (now - (pending.confirmedAt ?: pending.failedAt ?: 0)) > cleanupThreshold
            }
            .map { it.key }
        
        toRemove.forEach { transactionId ->
            pendingTransactions.remove(transactionId)
        }
        
        if (toRemove.isNotEmpty()) {
            updatePoolState()
            Log.d(TAG, "Cleaned up ${toRemove.size} old transactions")
        }
    }
    
    /**
     * Get pool statistics
     */
    fun getPoolStats(): TransactionPoolStats {
        val pending = pendingTransactions.values.count { it.status == TransactionStatus.PENDING }
        val confirmed = pendingTransactions.values.count { it.status == TransactionStatus.CONFIRMED }
        val failed = pendingTransactions.values.count { it.status == TransactionStatus.FAILED }
        
        return TransactionPoolStats(
            totalTransactions = pendingTransactions.size,
            pendingCount = pending,
            confirmedCount = confirmed,
            failedCount = failed
        )
    }
    
    private fun startPoolMaintenance() {
        coroutineScope.launch {
            while (true) {
                try {
                    cleanup()
                    delay(60000) // Run cleanup every minute
                } catch (e: Exception) {
                    Log.e(TAG, "Error during pool maintenance", e)
                }
            }
        }
    }
    
    private fun updatePoolState() {
        val stats = getPoolStats()
        _poolState.value = TransactionPoolState(
            totalTransactions = stats.totalTransactions,
            pendingCount = stats.pendingCount,
            confirmedCount = stats.confirmedCount,
            failedCount = stats.failedCount,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    companion object {
        private const val MAX_RETRY_COUNT = 3
    }
}

/**
 * Represents a transaction in the pool with metadata
 */
data class PendingTransaction(
    val transaction: MessageTransaction,
    val addedAt: Long,
    val retryCount: Int,
    val status: TransactionStatus,
    val blockNumber: Long = 0,
    val confirmedAt: Long? = null,
    val failedAt: Long? = null,
    val failureReason: String? = null,
    val lastRetryAt: Long = addedAt
)

/**
 * Transaction status enumeration
 */
enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED
}

/**
 * Pool state for UI updates
 */
data class TransactionPoolState(
    val totalTransactions: Int = 0,
    val pendingCount: Int = 0,
    val confirmedCount: Int = 0,
    val failedCount: Int = 0,
    val lastUpdated: Long = 0
)

/**
 * Pool statistics
 */
data class TransactionPoolStats(
    val totalTransactions: Int,
    val pendingCount: Int,
    val confirmedCount: Int,
    val failedCount: Int
)