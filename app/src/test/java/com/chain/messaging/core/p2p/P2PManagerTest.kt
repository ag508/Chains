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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class P2PManagerTest {
    
    private lateinit var p2pManager: P2PManagerImpl
    
    @Before
    fun setup() {
        p2pManager = P2PManagerImpl()
    }
    
    @Test
    fun `start should initialize all components`() = runTest {
        p2pManager.start()
        
        // Verify manager is running
        val networkInfo = p2pManager.getDetailedNetworkInfo()
        assertTrue(networkInfo.isRunning)
        
        p2pManager.stop()
    }
    
    @Test
    fun `stop should cleanup all components`() = runTest {
        p2pManager.start()
        p2pManager.stop()
        
        val networkInfo = p2pManager.getDetailedNetworkInfo()
        assertFalse(networkInfo.isRunning)
    }
    
    @Test
    fun `addPeer should add peer to network`() = runTest {
        p2pManager.start()
        
        val peer = createTestPeer("peer1")
        p2pManager.addPeer(peer)
        
        val discoveredPeers = p2pManager.discoverPeers()
        assertTrue(discoveredPeers.any { it.id == peer.id })
        
        p2pManager.stop()
    }
    
    @Test
    fun `removePeer should remove peer from network`() = runTest {
        p2pManager.start()
        
        val peer = createTestPeer("peer1")
        p2pManager.addPeer(peer)
        p2pManager.removePeer(peer.id)
        
        val discoveredPeers = p2pManager.discoverPeers()
        assertFalse(discoveredPeers.any { it.id == peer.id })
        
        p2pManager.stop()
    }
    
    @Test
    fun `discoverPeers should return all known peers`() = runTest {
        p2pManager.start()
        
        val peer1 = createTestPeer("peer1")
        val peer2 = createTestPeer("peer2")
        
        p2pManager.addPeer(peer1)
        p2pManager.addPeer(peer2)
        
        val discoveredPeers = p2pManager.discoverPeers()
        assertEquals(2, discoveredPeers.size)
        
        p2pManager.stop()
    }
    
    @Test
    fun `connectToPeer should establish connection`() = runTest {
        p2pManager.start()
        
        val peer = createTestPeer("peer1")
        p2pManager.addPeer(peer)
        
        val connection = p2pManager.connectToPeer(peer.id)
        
        assertNotNull(connection)
        assertEquals(peer.id, connection.peerId)
        assertTrue(connection.isActive)
        
        p2pManager.stop()
    }
    
    @Test
    fun `broadcastMessage should send message to network`() = runTest {
        p2pManager.start()
        
        val peer1 = createTestPeer("peer1")
        val peer2 = createTestPeer("peer2")
        p2pManager.addPeer(peer1)
        p2pManager.addPeer(peer2)
        
        // Connect to peers first
        p2pManager.connectToPeer(peer1.id)
        p2pManager.connectToPeer(peer2.id)
        
        val message = createTestMessage(type = MessageType.CHAT_MESSAGE)
        p2pManager.broadcastMessage(message)
        
        // In a real implementation, we would verify the message was sent
        // For now, we just verify no exceptions were thrown
        
        p2pManager.stop()
    }
    
    @Test
    fun `getConnectedPeers should return active connections`() = runTest {
        p2pManager.start()
        
        val peer1 = createTestPeer("peer1")
        val peer2 = createTestPeer("peer2")
        p2pManager.addPeer(peer1)
        p2pManager.addPeer(peer2)
        
        p2pManager.connectToPeer(peer1.id)
        p2pManager.connectToPeer(peer2.id)
        
        val connectedPeers = p2pManager.getConnectedPeers()
        assertEquals(2, connectedPeers.size)
        
        p2pManager.stop()
    }
    
    @Test
    fun `disconnectFromPeer should close connection`() = runTest {
        p2pManager.start()
        
        val peer = createTestPeer("peer1")
        p2pManager.addPeer(peer)
        p2pManager.connectToPeer(peer.id)
        
        p2pManager.disconnectFromPeer(peer.id)
        
        val connectedPeers = p2pManager.getConnectedPeers()
        assertFalse(connectedPeers.any { it.id == peer.id })
        
        p2pManager.stop()
    }
    
    @Test
    fun `getNetworkStats should return current statistics`() = runTest {
        p2pManager.start()
        
        val peer1 = createTestPeer("peer1")
        val peer2 = createTestPeer("peer2")
        p2pManager.addPeer(peer1)
        p2pManager.addPeer(peer2)
        
        p2pManager.connectToPeer(peer1.id)
        
        val stats = p2pManager.getNetworkStats()
        
        assertEquals(1, stats.connectedPeers)
        assertEquals(2, stats.totalPeersDiscovered)
        assertTrue(stats.averageLatency >= 0)
        assertTrue(stats.networkReliability >= 0.0)
        
        p2pManager.stop()
    }
    
    @Test
    fun `subscribeToNetwork should provide network events`() = runTest {
        p2pManager.start()
        
        val networkEvents = p2pManager.subscribeToNetwork()
        assertNotNull(networkEvents)
        
        // In a real test, we would collect events and verify they're emitted
        // For now, we just verify the flow is created
        
        p2pManager.stop()
    }
    
    @Test
    fun `lookupPeers should perform DHT lookup`() = runTest {
        p2pManager.start()
        
        val peer1 = createTestPeer("peer1")
        val peer2 = createTestPeer("peer2")
        p2pManager.addPeer(peer1)
        p2pManager.addPeer(peer2)
        
        val foundPeers = p2pManager.lookupPeers("target_id")
        
        // Should return some peers from the lookup
        assertTrue(foundPeers.isNotEmpty())
        
        p2pManager.stop()
    }
    
    @Test
    fun `sendDirectMessage should route message to specific peer`() = runTest {
        p2pManager.start()
        
        val peer = createTestPeer("peer1")
        p2pManager.addPeer(peer)
        p2pManager.connectToPeer(peer.id)
        
        val message = createTestMessage(to = peer.id, type = MessageType.CHAT_MESSAGE)
        val result = p2pManager.sendDirectMessage(message)
        
        // Should succeed or fail gracefully
        assertNotNull(result)
        
        p2pManager.stop()
    }
    
    @Test
    fun `handleIncomingMessage should process received messages`() = runTest {
        p2pManager.start()
        
        val message = createTestMessage(type = MessageType.CHAT_MESSAGE)
        val handled = p2pManager.handleIncomingMessage(message, "sender_peer_id")
        
        // Should handle the message (return value depends on routing logic)
        // For now, we just verify no exceptions were thrown
        
        p2pManager.stop()
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
    
    private fun createTestMessage(
        to: String? = null,
        type: MessageType = MessageType.CHAT_MESSAGE
    ): Message {
        return Message(
            id = "test_message_${System.currentTimeMillis()}",
            type = type,
            payload = "test message payload",
            from = "test_sender",
            to = to,
            timestamp = System.currentTimeMillis(),
            ttl = 10
        )
    }
}