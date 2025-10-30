package com.chain.messaging.core.group

import com.chain.messaging.core.p2p.P2PManager
import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Implementation of MessageDeliveryOptimizer that provides adaptive optimization
 * strategies for large-scale message delivery.
 */
@Singleton
class MessageDeliveryOptimizerImpl @Inject constructor(
    private val p2pManager: P2PManager
) : MessageDeliveryOptimizer {
    
    private val encryptedMessageCache = ConcurrentHashMap<String, ByteArray>()
    private val deliveryMetricsFlow = MutableSharedFlow<DeliveryMetrics>()
    private val recipientConnectivity = ConcurrentHashMap<String, Float>()
    private val deliveryHistory = ConcurrentHashMap<String, List<Long>>()
    
    companion object {
        private const val CACHE_TTL_MS = 300_000L // 5 minutes
        private const val MIN_BATCH_SIZE = 10
        private const val MAX_BATCH_SIZE = 500
        private const val BASE_RETRY_DELAY = 1000L // 1 second
        private const val MAX_RETRY_DELAY = 60_000L // 1 minute
    }
    
    override suspend fun optimizeDeliveryOrder(
        recipients: List<String>,
        message: Message
    ): List<String> {
        // Sort recipients by connectivity score and delivery priority
        return recipients.sortedWith { a, b ->
            val connectivityA = recipientConnectivity[a] ?: 0.5f
            val connectivityB = recipientConnectivity[b] ?: 0.5f
            
            // Prioritize recipients with better connectivity
            connectivityB.compareTo(connectivityA)
        }
    }
    
    override suspend fun calculateOptimalBatchSize(
        totalRecipients: Int,
        networkConditions: NetworkConditions
    ): Int {
        val baseSize = when {
            totalRecipients < 100 -> MIN_BATCH_SIZE
            totalRecipients < 1000 -> 50
            totalRecipients < 10000 -> 100
            else -> 200
        }
        
        // Adjust based on network conditions
        val networkFactor = calculateNetworkFactor(networkConditions)
        val adjustedSize = (baseSize * networkFactor).toInt()
        
        return adjustedSize.coerceIn(MIN_BATCH_SIZE, MAX_BATCH_SIZE)
    }
    
    override suspend fun applyRateLimiting(
        deliveryRate: Float,
        networkLoad: Float
    ): DeliveryThrottling {
        val maxConcurrent = when {
            networkLoad < 0.3f -> 50
            networkLoad < 0.6f -> 30
            networkLoad < 0.8f -> 20
            else -> 10
        }
        
        val delayBetweenBatches = when {
            networkLoad < 0.3f -> 100L
            networkLoad < 0.6f -> 250L
            networkLoad < 0.8f -> 500L
            else -> 1000L
        }
        
        return DeliveryThrottling(
            maxConcurrentDeliveries = maxConcurrent,
            delayBetweenBatches = delayBetweenBatches,
            backoffMultiplier = 1.5f + networkLoad,
            maxRetryAttempts = if (networkLoad > 0.8f) 5 else 3
        )
    }
    
    override suspend fun cacheEncryptedMessage(
        messageId: String,
        encryptedContent: ByteArray,
        recipients: List<String>
    ) {
        // Cache with TTL
        recipients.forEach { recipientId ->
            val cacheKey = "${messageId}_$recipientId"
            encryptedMessageCache[cacheKey] = encryptedContent
        }
        
        // Schedule cache cleanup (in a real implementation, use a proper cache with TTL)
        // For now, we'll rely on manual cleanup
    }
    
    override suspend fun getCachedEncryptedMessage(
        messageId: String,
        recipientId: String
    ): ByteArray? {
        val cacheKey = "${messageId}_$recipientId"
        return encryptedMessageCache[cacheKey]
    }
    
    override fun observeDeliveryMetrics(): Flow<DeliveryMetrics> {
        return deliveryMetricsFlow
    }
    
    override suspend fun predictDeliveryTime(
        remainingRecipients: Int,
        currentDeliveryRate: Float
    ): Long {
        if (currentDeliveryRate <= 0) {
            return Long.MAX_VALUE
        }
        
        // Estimate based on current rate with some buffer for network variations
        val baseTime = (remainingRecipients / currentDeliveryRate * 1000).toLong()
        val bufferMultiplier = 1.2f // 20% buffer
        
        return (baseTime * bufferMultiplier).toLong()
    }
    
    override suspend fun handleDeliveryFailure(
        recipientId: String,
        attemptCount: Int,
        lastError: Throwable
    ): RetryStrategy {
        val shouldRetry = attemptCount < 5
        val delay = calculateRetryDelay(attemptCount)
        val useAlternativeRoute = attemptCount > 2
        
        val priority = when {
            attemptCount == 1 -> DeliveryPriority.NORMAL
            attemptCount <= 3 -> DeliveryPriority.HIGH
            else -> DeliveryPriority.LOW
        }
        
        // Update recipient connectivity score
        updateRecipientConnectivity(recipientId, false)
        
        return RetryStrategy(
            shouldRetry = shouldRetry,
            delayBeforeRetry = delay,
            useAlternativeRoute = useAlternativeRoute,
            priority = priority
        )
    }
    
    private fun calculateNetworkFactor(networkConditions: NetworkConditions): Float {
        val bandwidthFactor = min(networkConditions.bandwidth / 1_000_000f, 1f) // Normalize to 1 Mbps
        val latencyFactor = max(0.1f, 1f - (networkConditions.latency / 1000f)) // Penalize high latency
        val losssFactor = max(0.1f, 1f - networkConditions.packetLoss)
        val stabilityFactor = networkConditions.connectionStability
        
        return (bandwidthFactor * latencyFactor * losssFactor * stabilityFactor).coerceIn(0.1f, 2.0f)
    }
    
    private fun calculateRetryDelay(attemptCount: Int): Long {
        // Exponential backoff with jitter
        val baseDelay = BASE_RETRY_DELAY * (2.0.pow(attemptCount - 1)).toLong()
        val jitter = (Math.random() * 0.1 * baseDelay).toLong()
        
        return (baseDelay + jitter).coerceAtMost(MAX_RETRY_DELAY)
    }
    
    private fun updateRecipientConnectivity(recipientId: String, success: Boolean) {
        val currentScore = recipientConnectivity[recipientId] ?: 0.5f
        val adjustment = if (success) 0.1f else -0.1f
        val newScore = (currentScore + adjustment).coerceIn(0f, 1f)
        
        recipientConnectivity[recipientId] = newScore
    }
    
    /**
     * Updates delivery metrics based on recent performance
     */
    suspend fun updateDeliveryMetrics(
        deliveryTime: Long,
        success: Boolean,
        throughput: Float,
        networkUtilization: Float
    ) {
        val errorRate = if (success) 0f else 1f
        
        val metrics = DeliveryMetrics(
            averageDeliveryTime = deliveryTime,
            successRate = if (success) 1f else 0f,
            throughput = throughput,
            networkUtilization = networkUtilization,
            errorRate = errorRate,
            timestamp = System.currentTimeMillis()
        )
        
        deliveryMetricsFlow.emit(metrics)
    }
    
    /**
     * Cleans up expired cache entries
     */
    fun cleanupCache() {
        // In a real implementation, this would check TTL and remove expired entries
        // For now, we'll implement a simple size-based cleanup
        if (encryptedMessageCache.size > 10000) {
            val keysToRemove = encryptedMessageCache.keys.take(5000)
            keysToRemove.forEach { encryptedMessageCache.remove(it) }
        }
    }
}