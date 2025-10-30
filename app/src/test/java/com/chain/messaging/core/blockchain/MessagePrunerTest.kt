package com.chain.messaging.core.blockchain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MessagePrunerTest {
    
    private lateinit var messagePruner: MessagePruner
    
    @Before
    fun setup() {
        messagePruner = MessagePruner()
    }
    
    @Test
    fun `markMessageDelivered should track message for pruning`() {
        val transactionHash = "test_tx_hash"
        val deliveredAt = System.currentTimeMillis()
        
        messagePruner.markMessageDelivered(transactionHash, deliveredAt)
        
        val stats = messagePruner.getPruningStats()
        assertEquals(1, stats.totalTrackedMessages)
        assertEquals(0, stats.readyToPrune) // Should not be ready immediately
        assertEquals(1, stats.pendingPrune)
    }
    
    @Test
    fun `shouldPruneMessage should return false for recent messages`() {
        val transactionHash = "test_tx_hash"
        val recentTime = System.currentTimeMillis()
        
        messagePruner.markMessageDelivered(transactionHash, recentTime)
        
        assertFalse(messagePruner.shouldPruneMessage(transactionHash))
    }
    
    @Test
    fun `shouldPruneMessage should return true for old messages`() {
        val transactionHash = "test_tx_hash"
        val oldTime = System.currentTimeMillis() - (49 * 60 * 60 * 1000) // 49 hours ago
        
        messagePruner.markMessageDelivered(transactionHash, oldTime)
        
        assertTrue(messagePruner.shouldPruneMessage(transactionHash))
    }
    
    @Test
    fun `getMessagesToPrune should return old messages`() {
        val recentHash = "recent_tx"
        val oldHash = "old_tx"
        val now = System.currentTimeMillis()
        val oldTime = now - (49 * 60 * 60 * 1000) // 49 hours ago
        
        messagePruner.markMessageDelivered(recentHash, now)
        messagePruner.markMessageDelivered(oldHash, oldTime)
        
        val messagesToPrune = messagePruner.getMessagesToPrune()
        
        assertEquals(1, messagesToPrune.size)
        assertTrue(messagesToPrune.contains(oldHash))
        assertFalse(messagesToPrune.contains(recentHash))
    }
    
    @Test
    fun `removeFromTracking should remove message`() {
        val transactionHash = "test_tx_hash"
        
        messagePruner.markMessageDelivered(transactionHash)
        assertEquals(1, messagePruner.getPruningStats().totalTrackedMessages)
        
        messagePruner.removeFromTracking(transactionHash)
        assertEquals(0, messagePruner.getPruningStats().totalTrackedMessages)
    }
    
    @Test
    fun `forceProneOlderThan should prune messages before cutoff`() = runTest {
        val now = System.currentTimeMillis()
        val cutoffTime = now - (24 * 60 * 60 * 1000) // 24 hours ago
        val oldTime = now - (48 * 60 * 60 * 1000) // 48 hours ago
        val recentTime = now - (12 * 60 * 60 * 1000) // 12 hours ago
        
        val oldHash = "old_tx"
        val recentHash = "recent_tx"
        
        messagePruner.markMessageDelivered(oldHash, oldTime)
        messagePruner.markMessageDelivered(recentHash, recentTime)
        
        val prunedMessages = messagePruner.forceProneOlderThan(Date(cutoffTime))
        
        assertEquals(1, prunedMessages.size)
        assertTrue(prunedMessages.contains(oldHash))
        
        val stats = messagePruner.getPruningStats()
        assertEquals(1, stats.totalTrackedMessages) // Only recent message should remain
    }
    
    @Test
    fun `getTimeUntilPrune should return correct time remaining`() {
        val transactionHash = "test_tx_hash"
        val deliveredAt = System.currentTimeMillis()
        
        messagePruner.markMessageDelivered(transactionHash, deliveredAt)
        
        val timeRemaining = messagePruner.getTimeUntilPrune(transactionHash)
        
        assertNotNull(timeRemaining)
        assertTrue(timeRemaining!! > 0)
        // Should be approximately 48 hours (allowing for small timing differences)
        assertTrue(timeRemaining > (47 * 60 * 60 * 1000))
        assertTrue(timeRemaining <= (48 * 60 * 60 * 1000))
    }
    
    @Test
    fun `getTimeUntilPrune should return null for unknown message`() {
        val timeRemaining = messagePruner.getTimeUntilPrune("unknown_hash")
        assertEquals(null, timeRemaining)
    }
    
    @Test
    fun `getPruningStats should return correct statistics`() {
        val now = System.currentTimeMillis()
        val oldTime = now - (49 * 60 * 60 * 1000) // 49 hours ago
        
        messagePruner.markMessageDelivered("recent_tx", now)
        messagePruner.markMessageDelivered("old_tx", oldTime)
        
        val stats = messagePruner.getPruningStats()
        
        assertEquals(2, stats.totalTrackedMessages)
        assertEquals(1, stats.readyToPrune)
        assertEquals(1, stats.pendingPrune)
    }
    
    @Test
    fun `start and stop should control pruner state`() {
        messagePruner.start()
        // Pruner should be running (no direct way to test this without exposing internal state)
        
        messagePruner.stop()
        // Pruner should be stopped
        
        // Test that multiple starts/stops don't cause issues
        messagePruner.start()
        messagePruner.start() // Should not cause problems
        messagePruner.stop()
    }
}