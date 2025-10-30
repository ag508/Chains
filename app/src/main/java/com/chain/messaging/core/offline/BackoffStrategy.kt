package com.chain.messaging.core.offline

import java.time.LocalDateTime
import kotlin.math.min
import kotlin.math.pow

/**
 * Interface for retry backoff strategies
 */
interface BackoffStrategy {
    /**
     * Calculate the next retry time based on retry count and last attempt
     */
    fun getNextRetryTime(retryCount: Int, lastRetryAt: LocalDateTime): LocalDateTime
    
    /**
     * Get the delay in seconds for the next retry
     */
    fun getDelaySeconds(retryCount: Int): Long
}

/**
 * Exponential backoff strategy with jitter
 */
class ExponentialBackoffStrategy(
    private val baseDelaySeconds: Long = 2,
    private val maxDelaySeconds: Long = 300, // 5 minutes max
    private val jitterFactor: Double = 0.1
) : BackoffStrategy {
    
    override fun getNextRetryTime(retryCount: Int, lastRetryAt: LocalDateTime): LocalDateTime {
        val delaySeconds = getDelaySeconds(retryCount)
        return lastRetryAt.plusSeconds(delaySeconds)
    }
    
    override fun getDelaySeconds(retryCount: Int): Long {
        // Calculate exponential delay: baseDelay * 2^retryCount
        val exponentialDelay = baseDelaySeconds * (2.0.pow(retryCount)).toLong()
        
        // Cap at maximum delay
        val cappedDelay = min(exponentialDelay, maxDelaySeconds)
        
        // Add jitter to prevent thundering herd
        val jitter = (cappedDelay * jitterFactor * Math.random()).toLong()
        
        return cappedDelay + jitter
    }
}

/**
 * Linear backoff strategy
 */
class LinearBackoffStrategy(
    private val baseDelaySeconds: Long = 5,
    private val incrementSeconds: Long = 5,
    private val maxDelaySeconds: Long = 60
) : BackoffStrategy {
    
    override fun getNextRetryTime(retryCount: Int, lastRetryAt: LocalDateTime): LocalDateTime {
        val delaySeconds = getDelaySeconds(retryCount)
        return lastRetryAt.plusSeconds(delaySeconds)
    }
    
    override fun getDelaySeconds(retryCount: Int): Long {
        val linearDelay = baseDelaySeconds + (retryCount * incrementSeconds)
        return min(linearDelay, maxDelaySeconds)
    }
}