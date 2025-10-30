package com.chain.messaging.core.offline

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

class BackoffStrategyTest {
    
    @Test
    fun `ExponentialBackoffStrategy should increase delay exponentially`() {
        // Given
        val strategy = ExponentialBackoffStrategy(baseDelaySeconds = 2, maxDelaySeconds = 60)
        
        // When & Then
        assertEquals(2, strategy.getDelaySeconds(0)) // 2 * 2^0 = 2
        assertTrue(strategy.getDelaySeconds(1) >= 4) // 2 * 2^1 = 4 (plus jitter)
        assertTrue(strategy.getDelaySeconds(2) >= 8) // 2 * 2^2 = 8 (plus jitter)
        assertTrue(strategy.getDelaySeconds(3) >= 16) // 2 * 2^3 = 16 (plus jitter)
    }
    
    @Test
    fun `ExponentialBackoffStrategy should cap at maximum delay`() {
        // Given
        val strategy = ExponentialBackoffStrategy(baseDelaySeconds = 2, maxDelaySeconds = 30)
        
        // When
        val delay = strategy.getDelaySeconds(10) // Would be 2 * 2^10 = 2048 without cap
        
        // Then
        assertTrue(delay <= 33) // Max delay + jitter (30 + 10% jitter)
    }
    
    @Test
    fun `ExponentialBackoffStrategy should add jitter`() {
        // Given
        val strategy = ExponentialBackoffStrategy(baseDelaySeconds = 10, jitterFactor = 0.2)
        
        // When
        val delays = (1..10).map { strategy.getDelaySeconds(1) }
        
        // Then
        // All delays should be different due to jitter (very high probability)
        val uniqueDelays = delays.toSet()
        assertTrue("Expected multiple unique delays due to jitter", uniqueDelays.size > 1)
    }
    
    @Test
    fun `ExponentialBackoffStrategy getNextRetryTime should add delay to last retry time`() {
        // Given
        val strategy = ExponentialBackoffStrategy(baseDelaySeconds = 5)
        val lastRetryAt = LocalDateTime.now()
        
        // When
        val nextRetryTime = strategy.getNextRetryTime(0, lastRetryAt)
        
        // Then
        assertTrue(nextRetryTime.isAfter(lastRetryAt))
        assertTrue(nextRetryTime.isBefore(lastRetryAt.plusSeconds(10))) // Should be around 5 seconds + jitter
    }
    
    @Test
    fun `LinearBackoffStrategy should increase delay linearly`() {
        // Given
        val strategy = LinearBackoffStrategy(baseDelaySeconds = 5, incrementSeconds = 3)
        
        // When & Then
        assertEquals(5, strategy.getDelaySeconds(0)) // 5 + 0*3 = 5
        assertEquals(8, strategy.getDelaySeconds(1)) // 5 + 1*3 = 8
        assertEquals(11, strategy.getDelaySeconds(2)) // 5 + 2*3 = 11
        assertEquals(14, strategy.getDelaySeconds(3)) // 5 + 3*3 = 14
    }
    
    @Test
    fun `LinearBackoffStrategy should cap at maximum delay`() {
        // Given
        val strategy = LinearBackoffStrategy(baseDelaySeconds = 5, incrementSeconds = 10, maxDelaySeconds = 20)
        
        // When
        val delay = strategy.getDelaySeconds(5) // Would be 5 + 5*10 = 55 without cap
        
        // Then
        assertEquals(20, delay)
    }
    
    @Test
    fun `LinearBackoffStrategy getNextRetryTime should add delay to last retry time`() {
        // Given
        val strategy = LinearBackoffStrategy(baseDelaySeconds = 10)
        val lastRetryAt = LocalDateTime.now()
        
        // When
        val nextRetryTime = strategy.getNextRetryTime(1, lastRetryAt)
        
        // Then
        assertTrue(nextRetryTime.isAfter(lastRetryAt))
        assertEquals(lastRetryAt.plusSeconds(15), nextRetryTime) // 10 + 1*5 = 15
    }
}