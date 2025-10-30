package com.chain.messaging.core.p2p

import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Interface for P2P network management including peer discovery and message routing
 */
interface P2PManager {
    /**
     * Discover peers in the network
     */
    suspend fun discoverPeers(): List<Peer>
    
    /**
     * Connect to a specific peer
     */
    suspend fun connectToPeer(peerId: String): Connection
    
    /**
     * Broadcast a message to the network
     */
    suspend fun broadcastMessage(message: Message): Unit
    
    /**
     * Subscribe to network events
     */
    fun subscribeToNetwork(): Flow<NetworkEvent>
    
    /**
     * Maintain active connections
     */
    suspend fun maintainConnections()
    
    /**
     * Get list of connected peers
     */
    fun getConnectedPeers(): List<Peer>
    
    /**
     * Disconnect from a peer
     */
    suspend fun disconnectFromPeer(peerId: String)
    
    /**
     * Get network statistics
     */
    fun getNetworkStats(): NetworkStats
    
    /**
     * Start the P2P network manager
     */
    suspend fun start()
    
    /**
     * Stop the P2P network manager
     */
    suspend fun stop()
    
    /**
     * Initialize P2P manager
     */
    suspend fun initialize()
    
    /**
     * Shutdown P2P manager
     */
    suspend fun shutdown()
    
    /**
     * Reconnect P2P manager
     */
    suspend fun reconnect()
    
    /**
     * Check if P2P is connected
     */
    fun isConnected(): Boolean
}

/**
 * Represents a peer in the P2P network
 */
data class Peer(
    val id: String,
    val address: String,
    val publicKey: String,
    val lastSeen: Date,
    val reliability: Double,
    val connectionCount: Int = 0,
    val latency: Long = 0,
    val isConnected: Boolean = false
)

/**
 * Represents a connection to a peer
 */
data class Connection(
    val peerId: String,
    val connectionId: String,
    val establishedAt: Date,
    val isActive: Boolean,
    val latency: Long,
    val bytesSent: Long = 0,
    val bytesReceived: Long = 0
)

/**
 * Message for P2P transmission
 */
data class Message(
    val id: String,
    val type: MessageType,
    val payload: String,
    val from: String,
    val to: String? = null, // null for broadcast
    val timestamp: Long,
    val ttl: Int = 10 // Time to live for routing
)

/**
 * Network events
 */
sealed class NetworkEvent {
    data class PeerDiscovered(val peer: Peer) : NetworkEvent()
    data class PeerConnected(val peer: Peer) : NetworkEvent()
    data class PeerDisconnected(val peer: Peer) : NetworkEvent()
    data class MessageReceived(val message: Message, val fromPeer: String) : NetworkEvent()
    data class MessageSent(val message: Message, val toPeer: String) : NetworkEvent()
    data class NetworkError(val error: String, val peerId: String? = null) : NetworkEvent()
}

/**
 * Message types for P2P communication
 */
enum class MessageType {
    CHAT_MESSAGE,
    PEER_DISCOVERY,
    PEER_ANNOUNCEMENT,
    BLOCKCHAIN_SYNC,
    TRANSACTION_BROADCAST,
    HEARTBEAT,
    ROUTING_UPDATE
}

/**
 * Network statistics
 */
data class NetworkStats(
    val connectedPeers: Int,
    val totalPeersDiscovered: Int,
    val messagesSent: Long,
    val messagesReceived: Long,
    val bytesTransferred: Long,
    val averageLatency: Long,
    val networkReliability: Double
)