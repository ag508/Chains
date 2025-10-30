package com.chain.messaging.core.offline

import com.chain.messaging.domain.model.Message
import java.time.LocalDateTime

/**
 * Represents a message queued for offline sending
 */
data class QueuedMessage(
    val id: String,
    val message: Message,
    val queuedAt: LocalDateTime,
    val retryCount: Int = 0,
    val lastRetryAt: LocalDateTime? = null,
    val priority: MessagePriority = MessagePriority.NORMAL,
    val maxRetries: Int = 5
) {
    /**
     * Check if this message has exceeded maximum retry attempts
     */
    fun hasExceededMaxRetries(): Boolean = retryCount >= maxRetries
    
    /**
     * Create a new instance with incremented retry count
     */
    fun withIncrementedRetry(): QueuedMessage = copy(
        retryCount = retryCount + 1,
        lastRetryAt = LocalDateTime.now()
    )
    
    /**
     * Check if enough time has passed for next retry attempt
     */
    fun canRetry(backoffStrategy: BackoffStrategy): Boolean {
        if (lastRetryAt == null) return true
        val nextRetryTime = backoffStrategy.getNextRetryTime(retryCount, lastRetryAt)
        return LocalDateTime.now().isAfter(nextRetryTime)
    }
}

/**
 * Priority levels for queued messages
 */
enum class MessagePriority {
    HIGH,    // System messages, calls
    NORMAL,  // Regular text messages
    LOW      // Media messages, large files
}