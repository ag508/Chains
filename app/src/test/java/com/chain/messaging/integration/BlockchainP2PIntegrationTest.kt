package com.chain.messaging.integration

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.blockchain.MessageTransaction
import com.chain.messaging.core.p2p.P2PManager
import com.chain.messaging.core.p2p.Peer
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageType
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import javax.inject.Inject

/**
 * Integration tests for blockchain and P2P functionality
 * Tests the interaction between blockchain message storage and P2P network delivery
 */
@HiltAndroidTest
class BlockchainP2PIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var blockchainManager: BlockchainManager

    @Inject
    lateinit var p2pManager: P2PManager

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testBlockchainMessageStorageAndP2PDelivery() = runTest {
        // Given: A message transaction to store and deliver
        val messageTransaction = MessageTransaction(
            id = "tx_${UUID.randomUUID()}",
            from = "sender_123",
            to = "recipient_456",
            encryptedContent = "encrypted_message_content",
            messageType = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            signature = "transaction_signature",
            nonce = "random_nonce"
        )

        // When: Message is stored on blockchain
        val txHash = blockchainManager.storeMessageTransaction(messageTransaction)
        assertNotNull("Transaction hash should be generated", txHash)

        // Then: Message should be retrievable from blockchain
        val storedTransaction = blockchainManager.getTransaction(txHash)
        assertNotNull("Transaction should be stored", storedTransaction)
        assertEquals(messageTransaction.id, storedTransaction?.id)
        assertEquals(messageTransaction.encryptedContent, storedTransaction?.encryptedContent)

        // And: P2P network should be notified of new message
        val networkEvents = p2pManager.getNetworkEvents().first()
        assertTrue("Network should have message events", networkEvents.isNotEmpty())

        // Verify P2P delivery to recipient
        val deliveredMessages = p2pManager.getMessagesForPeer("recipient_456")
        assertTrue("Message should be delivered via P2P", deliveredMessages.isNotEmpty())
        
        val deliveredMessage = deliveredMessages.find { it.transactionId == messageTransaction.id }
        assertNotNull("Specific message should be delivered", deliveredMessage)
    }

    @Test
    fun testP2PPeerDiscoveryAndBlockchainSync() = runTest {
        // Given: Multiple peers in the network
        val peer1 = Peer(
            id = "peer_1",
            address = "192.168.1.100:8080",
            publicKey = "peer1_public_key",
            lastSeen = Date(),
            reliability = 0.95
        )
        
        val peer2 = Peer(
            id = "peer_2", 
            address = "192.168.1.101:8080",
            publicKey = "peer2_public_key",
            lastSeen = Date(),
            reliability = 0.88
        )

        // When: Peers are discovered and connected
        p2pManager.addPeer(peer1)
        p2pManager.addPeer(peer2)
        
        val discoveredPeers = p2pManager.discoverPeers()
        assertTrue("Should discover multiple peers", discoveredPeers.size >= 2)

        // Then: Blockchain should sync with discovered peers
        val syncResult = blockchainManager.syncWithPeers(discoveredPeers)
        assertTrue("Blockchain sync should succeed", syncResult.isSuccess)

        // Verify blockchain state is consistent across peers
        val localBlockHeight = blockchainManager.getCurrentBlockHeight()
        val peer1BlockHeight = blockchainManager.getPeerBlockHeight(peer1.id)
        val peer2BlockHeight = blockchainManager.getPeerBlockHeight(peer2.id)

        assertTrue("Block heights should be synchronized", 
            Math.abs(localBlockHeight - peer1BlockHeight) <= 1)
        assertTrue("Block heights should be synchronized", 
            Math.abs(localBlockHeight - peer2BlockHeight) <= 1)
    }

    @Test
    fun testBlockchainConsensusWithP2PNetwork() = runTest {
        // Given: A network of peers participating in consensus
        val peers = (1..5).map { i ->
            Peer(
                id = "consensus_peer_$i",
                address = "192.168.1.${100 + i}:8080",
                publicKey = "peer${i}_public_key",
                lastSeen = Date(),
                reliability = 0.9 + (i * 0.01)
            )
        }

        peers.forEach { p2pManager.addPeer(it) }

        // When: A new block is proposed
        val newBlock = blockchainManager.createBlock(
            transactions = listOf(
                createTestTransaction("tx1", "user1", "user2"),
                createTestTransaction("tx2", "user3", "user4"),
                createTestTransaction("tx3", "user5", "user6")
            )
        )

        // Then: Block should be validated by consensus
        val consensusResult = blockchainManager.proposeBlock(newBlock)
        assertTrue("Block should achieve consensus", consensusResult.isAccepted)
        assertTrue("Consensus should have majority approval", consensusResult.approvalCount >= 3)

        // Verify block is added to blockchain
        val latestBlock = blockchainManager.getLatestBlock()
        assertEquals("New block should be latest", newBlock.hash, latestBlock.hash)

        // Verify all peers have the new block
        delay(2000) // Allow time for block propagation
        
        peers.forEach { peer ->
            val peerLatestBlock = blockchainManager.getPeerLatestBlock(peer.id)
            assertEquals("Peer should have latest block", newBlock.hash, peerLatestBlock?.hash)
        }
    }

    @Test
    fun testP2PMessageRoutingWithBlockchainVerification() = runTest {
        // Given: A message that needs to be routed through multiple peers
        val sourceId = "source_user"
        val targetId = "target_user"
        val messageContent = "Test message for routing"

        // Create routing path through multiple peers
        val routingPeers = listOf(
            Peer("route_peer_1", "192.168.1.200:8080", "route1_key", Date(), 0.95),
            Peer("route_peer_2", "192.168.1.201:8080", "route2_key", Date(), 0.92),
            Peer("route_peer_3", "192.168.1.202:8080", "route3_key", Date(), 0.89)
        )

        routingPeers.forEach { p2pManager.addPeer(it) }

        // When: Message is sent through P2P routing
        val routingResult = p2pManager.routeMessage(
            from = sourceId,
            to = targetId,
            content = messageContent,
            routingPeers = routingPeers
        )

        assertTrue("Message routing should succeed", routingResult.isSuccess)
        assertNotNull("Routing path should be established", routingResult.routingPath)

        // Then: Message should be verifiable on blockchain
        delay(3000) // Allow time for blockchain confirmation

        val blockchainMessages = blockchainManager.getMessagesForUser(targetId)
        val routedMessage = blockchainMessages.find { 
            it.from == sourceId && it.to == targetId 
        }

        assertNotNull("Routed message should be on blockchain", routedMessage)
        
        // Verify routing integrity
        assertTrue("Message should maintain integrity through routing", 
            blockchainManager.verifyMessageIntegrity(routedMessage!!))
    }

    @Test
    fun testBlockchainMessagePruningWithP2PNotification() = runTest {
        // Given: Old messages that should be pruned
        val oldTimestamp = System.currentTimeMillis() - (48 * 60 * 60 * 1000 + 1000) // 48+ hours ago
        
        val oldTransactions = listOf(
            createTestTransaction("old_tx_1", "user1", "user2", oldTimestamp),
            createTestTransaction("old_tx_2", "user3", "user4", oldTimestamp),
            createTestTransaction("old_tx_3", "user5", "user6", oldTimestamp)
        )

        // Store old transactions
        oldTransactions.forEach { tx ->
            blockchainManager.storeMessageTransaction(tx)
        }

        // When: Pruning is triggered
        val pruningResult = blockchainManager.pruneOldMessages()
        assertTrue("Pruning should succeed", pruningResult.isSuccess)
        assertEquals("Should prune 3 messages", 3, pruningResult.prunedCount)

        // Then: P2P network should be notified of pruning
        val networkEvents = p2pManager.getNetworkEvents().first()
        val pruningEvents = networkEvents.filter { it.type == "MESSAGE_PRUNED" }
        assertEquals("Should have pruning notifications", 3, pruningEvents.size)

        // Verify messages are no longer accessible
        oldTransactions.forEach { tx ->
            val retrievedTx = blockchainManager.getTransaction(tx.id)
            assertNull("Pruned transaction should not be retrievable", retrievedTx)
        }
    }

    @Test
    fun testP2PNetworkResilienceWithBlockchainFallback() = runTest {
        // Given: A partially connected P2P network
        val reliablePeer = Peer("reliable_peer", "192.168.1.100:8080", "reliable_key", Date(), 0.98)
        val unreliablePeer = Peer("unreliable_peer", "192.168.1.101:8080", "unreliable_key", Date(), 0.45)
        
        p2pManager.addPeer(reliablePeer)
        p2pManager.addPeer(unreliablePeer)

        // When: Unreliable peer fails during message delivery
        p2pManager.simulatePeerFailure(unreliablePeer.id)
        
        val message = createTestTransaction("resilience_tx", "sender", "recipient")
        val deliveryResult = p2pManager.deliverMessage(message, listOf(reliablePeer, unreliablePeer))

        // Then: Message should still be delivered through reliable peer
        assertTrue("Message delivery should succeed despite peer failure", deliveryResult.isSuccess)
        assertEquals("Should use reliable peer", reliablePeer.id, deliveryResult.successfulPeerId)

        // And: Message should be stored on blockchain as backup
        val blockchainBackup = blockchainManager.getTransaction(message.id)
        assertNotNull("Message should be backed up on blockchain", blockchainBackup)

        // Verify network adapts to peer reliability
        val updatedPeerReliability = p2pManager.getPeerReliability(unreliablePeer.id)
        assertTrue("Unreliable peer score should decrease", updatedPeerReliability < 0.45)
    }

    private fun createTestTransaction(
        id: String, 
        from: String, 
        to: String, 
        timestamp: Long = System.currentTimeMillis()
    ) = MessageTransaction(
        id = id,
        from = from,
        to = to,
        encryptedContent = "encrypted_content_$id",
        messageType = MessageType.TEXT,
        timestamp = timestamp,
        signature = "signature_$id",
        nonce = "nonce_$id"
    )
}