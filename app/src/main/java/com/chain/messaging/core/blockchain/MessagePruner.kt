package com.chain.messaging.core.blockchain

import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles automatic pruning of messages from the blockchain after 48-hour delivery window
 */
class MessagePruner {
    
    private val TAG = "MessagePruner"
    
    private val deliveredMessages = ConcurrentHashMap<String, DeliveredMessage>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var isRunning = false
    private var isInitialized = false
    
    /**
     * Initialize the message pruner
     */
    suspend fun initialize() {
        if (!isInitialized) {
            isInitialized = true
            Log.i(TAG, "Message pruner initialized")
        }
    }
    
    /**
     * Start the automatic pruning service
     */
    fun start() {
        if (isRunning) return
        
        isRunning = true
        coroutineScope.launch {
            startPruningLoop()
        }
        Log.i(TAG, "Message pruner started")
    }
    
    /**
     * Stop the automatic pruning service
     */
    fun stop() {
        isRunning = false
        Log.i(TAG, "Message pruner stopped")
    }
    
    /**
     * Mark a message as delivered
     */
    fun markMessageDelivered(transactionHash: String, deliveredAt: Long = System.currentTimeMillis()) {
        val deliveredMessage = DeliveredMessage(
            transactionHash = transactionHash,
            deliveredAt = deliveredAt,
            pruneAt = deliveredAt + DELIVERY_WINDOW_MS
        )
        
        deliveredMessages[transactionHash] = deliveredMessage
        Log.d(TAG, "Marked message as delivered: $transactionHash")
    }
    
    /**
     * Check if a message should be pruned
     */
    fun shouldPruneMessage(transactionHash: String): Boolean {
        val deliveredMessage = deliveredMessages[transactionHash] ?: return false
        return System.currentTimeMillis() >= deliveredMessage.pruneAt
    }
    
    /**
     * Get all messages that should be pruned
     */
    fun getMessagesToPrune(): List<String> {
        val now = System.currentTimeMillis()
        return deliveredMessages.values
            .filter { now >= it.pruneAt }
            .map { it.transactionHash }
    }
    
    /**
     * Remove a message from tracking (after successful pruning)
     */
    fun removeFromTracking(transactionHash: String) {
        deliveredMessages.remove(transactionHash)
        Log.d(TAG, "Removed message from tracking: $transactionHash")
    }
    
    /**
     * Get pruning statistics
     */
    fun getPruningStats(): PruningStats {
        val now = System.currentTimeMillis()
        val totalTracked = deliveredMessages.size
        val readyToPrune = deliveredMessages.values.count { now >= it.pruneAt }
        val pendingPrune = totalTracked - readyToPrune
        
        return PruningStats(
            totalTrackedMessages = totalTracked,
            readyToPrune = readyToPrune,
            pendingPrune = pendingPrune
        )
    }
    
    /**
     * Force prune messages older than the specified date
     */
    suspend fun forceProneOlderThan(olderThan: Date): List<String> {
        val cutoffTime = olderThan.time
        val messagesToPrune = deliveredMessages.values
            .filter { it.deliveredAt < cutoffTime }
            .map { it.transactionHash }
        
        messagesToPrune.forEach { transactionHash ->
            deliveredMessages.remove(transactionHash)
        }
        
        Log.i(TAG, "Force pruned ${messagesToPrune.size} messages older than $olderThan")
        return messagesToPrune
    }
    
    /**
     * Get time remaining until a message should be pruned
     */
    fun getTimeUntilPrune(transactionHash: String): Long? {
        val deliveredMessage = deliveredMessages[transactionHash] ?: return null
        val timeRemaining = deliveredMessage.pruneAt - System.currentTimeMillis()
        return if (timeRemaining > 0) timeRemaining else 0
    }
    
    private suspend fun startPruningLoop() {
        while (isRunning) {
            try {
                performPruningCycle()
                delay(PRUNING_CHECK_INTERVAL_MS)
            } catch (e: Exception) {
                Log.e(TAG, "Error during pruning cycle", e)
                delay(PRUNING_CHECK_INTERVAL_MS) // Continue despite errors
            }
        }
    }
    
    private suspend fun performPruningCycle() {
        val messagesToPrune = getMessagesToPrune()
        
        if (messagesToPrune.isNotEmpty()) {
            Log.d(TAG, "Found ${messagesToPrune.size} messages ready for pruning")
            
            // In a real implementation, this would send pruning requests to the blockchain
            messagesToPrune.forEach { transactionHash ->
                try {
                    // Simulate blockchain pruning request
                    pruneMessageFromBlockchain(transactionHash)
                    removeFromTracking(transactionHash)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to prune message: $transactionHash", e)
                }
            }
        }
        
        // Clean up old tracking data
        cleanupOldTrackingData()
    }
    
    private suspend fun pruneMessageFromBlockchain(transactionHash: String) {
        // This would be implemented to send a pruning request to the blockchain network
        // For now, we simulate the operation
        delay(100) // Simulate network delay
        Log.d(TAG, "Pruned message from blockchain: $transactionHash")
    }
    
    private fun cleanupOldTrackingData() {
        val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) // 7 days
        val toRemove = deliveredMessages.entries
            .filter { (_, message) -> message.deliveredAt < cutoffTime }
            .map { it.key }
        
        toRemove.forEach { transactionHash ->
            deliveredMessages.remove(transactionHash)
        }
        
        if (toRemove.isNotEmpty()) {
            Log.d(TAG, "Cleaned up ${toRemove.size} old tracking entries")
        }
    }
    
    companion object {
        private const val DELIVERY_WINDOW_MS = 48 * 60 * 60 * 1000L // 48 hours
        private const val PRUNING_CHECK_INTERVAL_MS = 60 * 60 * 1000L // 1 hour
    }
}

/**
 * Represents a delivered message awaiting pruning
 */
data class DeliveredMessage(
    val transactionHash: String,
    val deliveredAt: Long,
    val pruneAt: Long
)

/**
 * Pruning statistics
 */
data class PruningStats(
    val totalTrackedMessages: Int,
    val readyToPrune: Int,
    val pendingPrune: Int
)