package com.chain.messaging.core.p2p

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

/**
 * Implementation of P2PManager that coordinates DHT discovery, connection management, and message routing
 */
class P2PManagerImpl : P2PManager {
    
    private val TAG = "P2PManager"
    
    private val dhtPeerDiscovery = DHTPeerDiscovery()
    private val connectionManager = ConnectionManager()
    private val messageRouter = MessageRouter(connectionManager, dhtPeerDiscovery)
    
    private val _networkEvents = MutableSharedFlow<NetworkEvent>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var isRunning = false
    private var isInitialized = false
    
    override suspend fun initialize() {
        if (!isInitialized) {
            isInitialized = true
            // Initialize all components
            dhtPeerDiscovery.initialize()
            connectionManager.initialize()
            messageRouter.initialize()
            Log.i(TAG, "P2P Manager initialized")
        }
    }

    override suspend fun start() {
        if (isRunning) return
        
        if (!isInitialized) {
            initialize()
        }
        
        isRunning = true
        
        // Start all components
        dhtPeerDiscovery.start()
        connectionManager.start()
        messageRouter.start()
        
        // Subscribe to component events
        subscribeToComponentEvents()
        
        Log.i(TAG, "P2P Manager started")
    }
    
    override suspend fun stop() {
        if (!isRunning) return
        
        isRunning = false
        
        // Stop all components
        messageRouter.stop()
        connectionManager.stop()
        dhtPeerDiscovery.stop()
        
        Log.i(TAG, "P2P Manager stopped")
    }
    
    override suspend fun shutdown() {
        stop()
        isInitialized = false
        coroutineScope.cancel()
        Log.i(TAG, "P2P Manager shutdown complete")
    }
    
    override suspend fun reconnect() {
        Log.i(TAG, "Reconnecting P2P Manager")
        stop()
        delay(1000) // Brief delay before reconnection
        start()
    }
    
    override fun isConnected(): Boolean {
        return isRunning && getConnectedPeers().isNotEmpty()
    }
    
    override suspend fun discoverPeers(): List<Peer> {
        return try {
            val allPeers = dhtPeerDiscovery.getAllPeers()
            Log.d(TAG, "Discovered ${allPeers.size} peers")
            allPeers
        } catch (e: Exception) {
            Log.e(TAG, "Failed to discover peers", e)
            emptyList()
        }
    }
    
    override suspend fun connectToPeer(peerId: String): Connection {
        val peer = dhtPeerDiscovery.getAllPeers().find { it.id == peerId }
            ?: throw IllegalArgumentException("Peer not found: $peerId")
        
        return connectionManager.connectToPeer(peer)
            ?: throw RuntimeException("Failed to connect to peer: $peerId")
    }
    
    override suspend fun broadcastMessage(message: Message) {
        try {
            val result = messageRouter.routeMessage(message)
            when (result) {
                is RoutingResult.Success -> {
                    Log.d(TAG, "Broadcast message ${message.id}: ${result.successCount} successes, ${result.failureCount} failures")
                    _networkEvents.emit(NetworkEvent.MessageSent(message, "broadcast"))
                }
                is RoutingResult.Failed -> {
                    Log.w(TAG, "Failed to broadcast message ${message.id}: ${result.reason}")
                    _networkEvents.emit(NetworkEvent.NetworkError("Broadcast failed: ${result.reason}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error broadcasting message", e)
            _networkEvents.emit(NetworkEvent.NetworkError("Broadcast error: ${e.message}"))
        }
    }
    
    override fun subscribeToNetwork(): Flow<NetworkEvent> {
        return _networkEvents.asSharedFlow()
    }
    
    override suspend fun maintainConnections() {
        try {
            val connectedPeers = getConnectedPeers()
            Log.d(TAG, "Maintaining ${connectedPeers.size} connections")
            
            // The connection manager handles maintenance automatically
            // This method can be used for additional maintenance logic if needed
            
        } catch (e: Exception) {
            Log.e(TAG, "Error maintaining connections", e)
        }
    }
    
    override fun getConnectedPeers(): List<Peer> {
        val activeConnections = connectionManager.getActiveConnections()
        return activeConnections.mapNotNull { connection ->
            dhtPeerDiscovery.getAllPeers().find { it.id == connection.peerId }
        }
    }
    
    override suspend fun disconnectFromPeer(peerId: String) {
        try {
            connectionManager.disconnectFromPeer(peerId)
            Log.d(TAG, "Disconnected from peer: $peerId")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from peer: $peerId", e)
        }
    }
    
    override fun getNetworkStats(): NetworkStats {
        val connectionStats = connectionManager.getConnectionStats()
        val dhtStats = dhtPeerDiscovery.getDHTStats()
        val routingStats = messageRouter.getRoutingStats()
        
        return NetworkStats(
            connectedPeers = connectionStats.activeConnections,
            totalPeersDiscovered = dhtStats.totalPeers,
            messagesSent = 0, // Would be tracked separately in a real implementation
            messagesReceived = 0, // Would be tracked separately in a real implementation
            bytesTransferred = connectionStats.totalBytesSent + connectionStats.totalBytesReceived,
            averageLatency = connectionStats.averageLatency,
            networkReliability = calculateNetworkReliability()
        )
    }
    
    /**
     * Add a peer to the DHT
     */
    fun addPeer(peer: Peer) {
        dhtPeerDiscovery.addPeer(peer)
        Log.d(TAG, "Added peer to network: ${peer.id}")
    }
    
    /**
     * Remove a peer from the DHT
     */
    fun removePeer(peerId: String) {
        dhtPeerDiscovery.removePeer(peerId)
        coroutineScope.launch {
            disconnectFromPeer(peerId)
        }
        Log.d(TAG, "Removed peer from network: $peerId")
    }
    
    /**
     * Send a direct message to a specific peer
     */
    suspend fun sendDirectMessage(message: Message): RoutingResult {
        return messageRouter.routeMessage(message)
    }
    
    /**
     * Handle incoming message from the network
     */
    suspend fun handleIncomingMessage(message: Message, fromPeerId: String): Boolean {
        return messageRouter.handleIncomingMessage(message, fromPeerId)
    }
    
    /**
     * Perform peer lookup using DHT
     */
    suspend fun lookupPeers(targetId: String): List<Peer> {
        return dhtPeerDiscovery.lookupPeers(targetId)
    }
    
    /**
     * Get detailed network information
     */
    fun getDetailedNetworkInfo(): DetailedNetworkInfo {
        val connectionStats = connectionManager.getConnectionStats()
        val dhtStats = dhtPeerDiscovery.getDHTStats()
        val routingStats = messageRouter.getRoutingStats()
        
        return DetailedNetworkInfo(
            dhtStats = dhtStats,
            connectionStats = connectionStats,
            routingStats = routingStats,
            isRunning = isRunning
        )
    }
    
    private fun subscribeToComponentEvents() {
        // Subscribe to DHT discovery events
        coroutineScope.launch {
            dhtPeerDiscovery.discoveryEvents.collect { event ->
                when (event) {
                    is DiscoveryEvent.PeerAdded -> {
                        _networkEvents.emit(NetworkEvent.PeerDiscovered(event.peer))
                    }
                    is DiscoveryEvent.PeerRemoved -> {
                        _networkEvents.emit(NetworkEvent.PeerDisconnected(event.peer))
                    }
                    is DiscoveryEvent.LookupCompleted -> {
                        Log.d(TAG, "DHT lookup completed for ${event.targetId}: ${event.foundPeers.size} peers")
                    }
                }
            }
        }
        
        // Subscribe to connection events
        coroutineScope.launch {
            connectionManager.connectionEvents.collect { event ->
                when (event) {
                    is ConnectionEvent.Connected -> {
                        _networkEvents.emit(NetworkEvent.PeerConnected(event.peer))
                    }
                    is ConnectionEvent.Disconnected -> {
                        val peer = dhtPeerDiscovery.getAllPeers().find { it.id == event.connection.peerId }
                        if (peer != null) {
                            _networkEvents.emit(NetworkEvent.PeerDisconnected(peer))
                        }
                    }
                    is ConnectionEvent.ConnectionFailed -> {
                        _networkEvents.emit(NetworkEvent.NetworkError("Connection failed to ${event.peer.id}: ${event.reason}"))
                    }
                    is ConnectionEvent.LatencyUpdated -> {
                        Log.d(TAG, "Connection latency updated: ${event.connectionId} -> ${event.latency}ms")
                    }
                }
            }
        }
        
        // Subscribe to routing events
        coroutineScope.launch {
            messageRouter.routingEvents.collect { event ->
                when (event) {
                    is RoutingEvent.MessageReceived -> {
                        _networkEvents.emit(NetworkEvent.MessageReceived(event.message, event.fromPeerId))
                    }
                    is RoutingEvent.MessageRouted -> {
                        Log.d(TAG, "Message routed: ${event.message.id}")
                    }
                    is RoutingEvent.RoutingFailed -> {
                        _networkEvents.emit(NetworkEvent.NetworkError("Routing failed for ${event.message.id}: ${event.result.reason}"))
                    }
                }
            }
        }
    }
    
    private fun calculateNetworkReliability(): Double {
        val connectedPeers = getConnectedPeers()
        if (connectedPeers.isEmpty()) return 0.0
        
        val averageReliability = connectedPeers.map { peer ->
            connectionManager.getPeerReliability(peer.id)
        }.average()
        
        return averageReliability
    }
}

/**
 * Detailed network information for monitoring and debugging
 */
data class DetailedNetworkInfo(
    val dhtStats: DHTStats,
    val connectionStats: ConnectionStats,
    val routingStats: RoutingStats,
    val isRunning: Boolean
)