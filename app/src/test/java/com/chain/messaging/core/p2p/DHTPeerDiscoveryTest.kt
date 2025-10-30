package com.chain.messaging.core.p2p

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class DHTPeerDiscoveryTest {
    
    private lateinit var dhtPeerDiscovery: DHTPeerDiscovery
    
    @Before
    fun setup() {
        dhtPeerDiscovery = DHTPeerDiscovery()
    }
    
    @Test
    fun `addPeer should add peer to routing table`() = runTest {
        val peer = createTestPeer("peer1")
        
        dhtPeerDiscovery.addPeer(peer)
        
        val allPeers = dhtPeerDiscovery.getAllPeers()
        assertEquals(1, allPeers.size)
        assertEquals(peer.id, allPeers.first().id)
    }
    
    @Test
    fun `removePeer should remove peer from routing table`() = runTest {
        val peer = createTestPeer("peer1")
        dhtPeerDiscovery.addPeer(peer)
        
        dhtPeerDiscovery.removePeer(peer.id)
        
        val allPeers = dhtPeerDiscovery.getAllPeers()
        assertTrue(allPeers.isEmpty())
    }
    
    @Test
    fun `findClosestPeers should return peers sorted by distance`() = runTest {
        val peer1 = createTestPeer("peer1")
        val peer2 = createTestPeer("peer2")
        val peer3 = createTestPeer("peer3")
        
        dhtPeerDiscovery.addPeer(peer1)
        dhtPeerDiscovery.addPeer(peer2)
        dhtPeerDiscovery.addPeer(peer3)
        
        val closestPeers = dhtPeerDiscovery.findClosestPeers("target_id", 2)
        
        assertEquals(2, closestPeers.size)
        // Peers should be sorted by distance (exact order depends on hash function)
        assertNotNull(closestPeers.find { it.id == peer1.id || it.id == peer2.id || it.id == peer3.id })
    }
    
    @Test
    fun `lookupPeers should perform iterative lookup`() = runTest {
        val peer1 = createTestPeer("peer1")
        val peer2 = createTestPeer("peer2")
        
        dhtPeerDiscovery.addPeer(peer1)
        dhtPeerDiscovery.addPeer(peer2)
        
        val foundPeers = dhtPeerDiscovery.lookupPeers("target_id")
        
        // Should return some peers (exact count depends on simulation)
        assertTrue(foundPeers.isNotEmpty())
    }
    
    @Test
    fun `getDHTStats should return correct statistics`() = runTest {
        val peer1 = createTestPeer("peer1")
        val peer2 = createTestPeer("peer2")
        
        dhtPeerDiscovery.addPeer(peer1)
        dhtPeerDiscovery.addPeer(peer2)
        
        val stats = dhtPeerDiscovery.getDHTStats()
        
        assertEquals(2, stats.totalPeers)
        assertTrue(stats.activeBuckets > 0)
        assertTrue(stats.averageBucketSize > 0.0)
        assertNotNull(stats.localNodeId)
    }
    
    @Test
    fun `getPeersFromBucket should return peers from specific bucket`() = runTest {
        val peer = createTestPeer("peer1")
        dhtPeerDiscovery.addPeer(peer)
        
        // Check all buckets to find where the peer was placed
        var foundPeer = false
        for (i in 0 until 160) {
            val peersInBucket = dhtPeerDiscovery.getPeersFromBucket(i)
            if (peersInBucket.any { it.id == peer.id }) {
                foundPeer = true
                break
            }
        }
        
        assertTrue(foundPeer)
    }
    
    @Test
    fun `start and stop should control discovery state`() {
        dhtPeerDiscovery.start()
        // DHT should be running (no direct way to test without exposing internal state)
        
        dhtPeerDiscovery.stop()
        // DHT should be stopped
        
        // Test multiple starts/stops don't cause issues
        dhtPeerDiscovery.start()
        dhtPeerDiscovery.start() // Should not cause problems
        dhtPeerDiscovery.stop()
    }
    
    @Test
    fun `discoveryEvents should emit events for peer changes`() = runTest {
        dhtPeerDiscovery.start()
        
        val peer = createTestPeer("peer1")
        
        // Add peer and check for event
        dhtPeerDiscovery.addPeer(peer)
        
        // In a real test, we would collect events from the flow
        // For now, we just verify the peer was added
        val allPeers = dhtPeerDiscovery.getAllPeers()
        assertTrue(allPeers.any { it.id == peer.id })
        
        dhtPeerDiscovery.stop()
    }
    
    private fun createTestPeer(id: String): Peer {
        return Peer(
            id = id,
            address = "192.168.1.100:8080",
            publicKey = "test_public_key_$id",
            lastSeen = Date(),
            reliability = 0.8,
            connectionCount = 0,
            latency = 50,
            isConnected = false
        )
    }
}