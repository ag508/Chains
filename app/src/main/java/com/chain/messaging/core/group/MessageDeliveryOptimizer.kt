package com.chain.messaging.core.group

import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Interface for optimizing message delivery performance in large groups.
 * Provides adaptive algorithms and caching strategies for efficient delivery.
 */
interface MessageDeliveryOptimizer {
    
    /**
     * Optimizes delivery order based on recipient connectivity and priority.
     */
    suspend fun optimizeDeliveryOrder(
        recipients: List<String>,
        message: Message
    ): List<String>
    
    /**
     * Determines optimal batch size based on current network conditions.
     */
    suspend fun calculateOptimalBatchSize(
        totalRecipients: Int,
        networkConditions: NetworkConditions
    ): Int
    
    /**
     * Manages delivery rate limiting to prevent network congestion.
     */
    suspend fun applyRateLimiting(
        deliveryRate: Float,
        networkLoad: Float
    ): DeliveryThrottling
    
    /**
     * Caches encrypted messages to avoid re-encryption for multiple recipients.
     */
    suspend fun cacheEncryptedMessage(
        messageId: String,
        encryptedContent: ByteArray,
        recipients: List<String>
    )
    
    /**
     * Retrieves cached encrypted message if available.
     */
    suspend fun getCachedEncryptedMessage(
        messageId: String,
        recipientId: String
    ): ByteArray?
    
    /**
     * Monitors delivery performance and adjusts strategies.
     */
    fun observeDeliveryMetrics(): Flow<DeliveryMetrics>
    
    /**
     * Predicts delivery completion time based on current performance.
     */
    suspend fun predictDeliveryTime(
        remainingRecipients: Int,
        currentDeliveryRate: Float
    ): Long
    
    /**
     * Handles delivery failures with exponential backoff.
     */
    suspend fun handleDeliveryFailure(
        recipientId: String,
        attemptCount: Int,
        lastError: Throwable
    ): RetryStrategy
}

/**
 * Network conditions affecting delivery performance
 */
data class NetworkConditions(
    val bandwidth: Long,           // bytes per second
    val latency: Long,            // milliseconds
    val packetLoss: Float,        // percentage
    val connectionStability: Float, // 0.0 to 1.0
    val peerCount: Int
)

/**
 * Delivery throttling configuration
 */
data class DeliveryThrottling(
    val maxConcurrentDeliveries: Int,
    val delayBetweenBatches: Long,    // milliseconds
    val backoffMultiplier: Float,
    val maxRetryAttempts: Int
)

/**
 * Delivery performance metrics
 */
data class DeliveryMetrics(
    val averageDeliveryTime: Long,    // milliseconds
    val successRate: Float,           // percentage
    val throughput: Float,            // messages per second
    val networkUtilization: Float,    // percentage
    val errorRate: Float,             // percentage
    val timestamp: Long
)

/**
 * Retry strategy for failed deliveries
 */
data class RetryStrategy(
    val shouldRetry: Boolean,
    val delayBeforeRetry: Long,       // milliseconds
    val useAlternativeRoute: Boolean,
    val priority: DeliveryPriority
)

/**
 * Delivery priority levels
 */
enum class DeliveryPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}