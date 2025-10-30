package com.chain.messaging.core.p2p

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Handles message routing through multiple peers for redundancy and reliability
 */
class MessageRouter(
    private val connectionManager: ConnectionManager,
    private val dhtPeerDiscovery: DHTPeerDiscovery
) {
    
    private val TAG = "MessageRouter"
    
    private val messageCache = ConcurrentHashMap<String, CachedMessage>()
    private val routingTable = ConcurrentHashMap<String, List<String>>() // destination -> peer IDs
    
    private val _routingEvents = MutableSharedFlow<RoutingEvent>()
    val routingEvents: SharedFlow<RoutingEvent> = _routingEvents.asSharedFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private var isInitialized = false
    
    /**
     * Initialize the message router
     */
    suspend fun initialize() {
        if (!isInitialized) {
            isInitialized = true
            Log.i(TAG, "Message router initialized")
        }
    }
    
    /**
     * Start the message router
     */
    fun start() {
        if (isRunning) return
        
        isRunning = true
        coroutineScope.launch {
            startRoutingMaintenance()
        }
        Log.i(TAG, "Message router started")
    }
    
    /**
     * Stop the message router
     */
    fun stop() {
        isRunning = false
        Log.i(TAG, "Message router stopped")
    }
    
    /**
     * Route a message to its destination with redundancy
     */
    suspend fun routeMessage(message: Message): RoutingResult {
        try {
            // Cache the message to prevent loops
            cacheMessage(message)
            
            val result = if (message.to == null) {
                // Broadcast message
                broadcastMessage(message)
            } else {
                // Direct message
                routeDirectMessage(message)
            }
            
            _routingEvents.emit(RoutingEvent.MessageRouted(message, result))
            return result
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to route message: ${message.id}", e)
            val failureResult = RoutingResult.Failed(e.message ?: "Unknown error")
            _routingEvents.emit(RoutingEvent.RoutingFailed(message, failureResult))
            return failureResult
        }
    }
    
    /**
     * Handle incoming message from a peer
     */
    suspend fun handleIncomingMessage(message: Message, fromPeerId: String): Boolean {
        // Check if we've seen this message before (loop prevention)
        if (isMessageCached(message.id)) {
            Log.d(TAG, "Ignoring duplicate message: ${message.id}")
            return false
        }
        
        // Cache the message
        cacheMessage(message)
        
        // Check TTL
        if (message.ttl <= 0) {
            Log.d(TAG, "Message TTL expired: ${message.id}")
            return false
        }
        
        // If message is for us, deliver it
        if (message.to == getCurrentNodeId() || message.to == null) {
            _routingEvents.emit(RoutingEvent.MessageReceived(message, fromPeerId))
            return true
        }
        
        // Forward the message
        val forwardedMessage = message.copy(ttl = message.ttl - 1)
        val result = routeMessage(forwardedMessage)
        
        return result is RoutingResult.Success
    }
    
    /**
     * Update routing table with peer information
     */
    fun updateRoutingTable(destination: String, peerIds: List<String>) {
        routingTable[destination] = peerIds
        Log.d(TAG, "Updated routing table for $destination: ${peerIds.size} peers")
    }
    
    /**
     * Get routing statistics
     */
    fun getRoutingStats(): RoutingStats {
        val cachedMessages = messageCache.size
        val routingEntries = routingTable.size
        val activeRoutes = routingTable.values.sumOf { it.size }
        
        return RoutingStats(
            cachedMessages = cachedMessages,
            routingEntries = routingEntries,
            activeRoutes = activeRoutes
        )
    }
    
    private suspend fun routeDirectMessage(message: Message): RoutingResult {
        val destination = message.to!!
        
        // Find best peers for routing
        val candidatePeers = findBestPeersForDestination(destination)
        
        if (candidatePeers.isEmpty()) {
            return RoutingResult.Failed("No peers available for destination: $destination")
        }
        
        // Try to send through multiple peers for redundancy
        val results = mutableListOf<SendResult>()
        val redundancyCount = minOf(REDUNDANCY_FACTOR, candidatePeers.size)
        
        candidatePeers.take(redundancyCount).forEach { peer ->
            coroutineScope.launch {
                val result = sendMessageToPeer(message, peer)
                synchronized(results) {
                    results.add(result)
                }
            }
        }
        
        // Wait for at least one success or all failures
        var attempts = 0
        while (attempts < ROUTING_TIMEOUT_MS / 100 && results.size < redundancyCount) {
            delay(100)
            attempts++
        }
        
        val successCount = results.count { it is SendResult.Success }
        val failureCount = results.count { it is SendResult.Failed }
        
        return if (successCount > 0) {
            RoutingResult.Success(successCount, failureCount)
        } else {
            RoutingResult.Failed("All routing attempts failed")
        }
    }
    
    private suspend fun broadcastMessage(message: Message): RoutingResult {
        val connectedPeers = connectionManager.getActiveConnections().map { it.peerId }
        
        if (connectedPeers.isEmpty()) {
            return RoutingResult.Failed("No connected peers for broadcast")
        }
        
        val results = mutableListOf<SendResult>()
        
        connectedPeers.forEach { peerId ->
            coroutineScope.launch {
                val peer = dhtPeerDiscovery.getAllPeers().find { it.id == peerId }
                if (peer != null) {
                    val result = sendMessageToPeer(message, peer)
                    synchronized(results) {
                        results.add(result)
                    }
                }
            }
        }
        
        // Wait for all broadcast attempts
        var attempts = 0
        while (attempts < ROUTING_TIMEOUT_MS / 100 && results.size < connectedPeers.size) {
            delay(100)
            attempts++
        }
        
        val successCount = results.count { it is SendResult.Success }
        val failureCount = results.count { it is SendResult.Failed }
        
        return RoutingResult.Success(successCount, failureCount)
    }
    
    private suspend fun sendMessageToPeer(message: Message, peer: Peer): SendResult {
        return try {
            val connection = connectionManager.getConnection(peer.id)
                ?: connectionManager.connectToPeer(peer)
                ?: return SendResult.Failed("Could not establish connection to ${peer.id}")
            
            // Simulate message sending
            delay(Random.nextLong(10, 100))
            
            // Update connection stats
            val messageSize = message.payload.length.toLong()
            connectionManager.updateConnectionStats(connection.connectionId, messageSize, 0, 50)
            
            Log.d(TAG, "Sent message ${message.id} to peer ${peer.id}")
            SendResult.Success(peer.id)
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send message to peer ${peer.id}", e)
            SendResult.Failed("Send failed: ${e.message}")
        }
    }
    
    private fun findBestPeersForDestination(destination: String): List<Peer> {
        // First, check routing table for known routes
        val knownPeers = routingTable[destination]?.mapNotNull { peerId ->
            dhtPeerDiscovery.getAllPeers().find { it.id == peerId }
        } ?: emptyList()
        
        if (knownPeers.isNotEmpty()) {
            return connectionManager.selectBestPeers(knownPeers, REDUNDANCY_FACTOR)
        }
        
        // Fall back to closest peers from DHT
        val closestPeers = dhtPeerDiscovery.findClosestPeers(destination, REDUNDANCY_FACTOR * 2)
        return connectionManager.selectBestPeers(closestPeers, REDUNDANCY_FACTOR)
    }
    
    private fun cacheMessage(message: Message) {
        val cachedMessage = CachedMessage(
            message = message,
            cachedAt = System.currentTimeMillis()
        )
        messageCache[message.id] = cachedMessage
    }
    
    private fun isMessageCached(messageId: String): Boolean {
        return messageCache.containsKey(messageId)
    }
    
    private fun getCurrentNodeId(): String {
        return dhtPeerDiscovery.getDHTStats().localNodeId
    }
    
    private suspend fun startRoutingMaintenance() {
        while (isRunning) {
            try {
                cleanupMessageCache()
                updateRoutingInformation()
                delay(MAINTENANCE_INTERVAL_MS)
            } catch (e: Exception) {
                Log.e(TAG, "Error in routing maintenance", e)
            }
        }
    }
    
    private fun cleanupMessageCache() {
        val cutoffTime = System.currentTimeMillis() - MESSAGE_CACHE_TTL_MS
        val toRemove = messageCache.entries.filter { (_, cached) ->
            cached.cachedAt < cutoffTime
        }.map { it.key }
        
        toRemove.forEach { messageId ->
            messageCache.remove(messageId)
        }
        
        if (toRemove.isNotEmpty()) {
            Log.d(TAG, "Cleaned up ${toRemove.size} cached messages")
        }
    }
    
    private suspend fun updateRoutingInformation() {
        // Update routing table based on peer discovery information
        val allPeers = dhtPeerDiscovery.getAllPeers()
        
        // Group peers by their network segments or other routing criteria
        val peerGroups = allPeers.groupBy { peer ->
            // Simple grouping by address prefix (in real implementation, use more sophisticated routing)
            peer.address.substringBeforeLast(".")
        }
        
        peerGroups.forEach { (segment, peers) ->
            val peerIds = peers.map { it.id }
            routingTable[segment] = peerIds
        }
    }
    
    companion object {
        private const val REDUNDANCY_FACTOR = 3
        private const val ROUTING_TIMEOUT_MS = 5000L
        private const val MAINTENANCE_INTERVAL_MS = 60000L // 1 minute
        private const val MESSAGE_CACHE_TTL_MS = 300000L // 5 minutes
    }
}

/**
 * Cached message for loop prevention
 */
data class CachedMessage(
    val message: Message,
    val cachedAt: Long
)

/**
 * Routing result
 */
sealed class RoutingResult {
    data class Success(val successCount: Int, val failureCount: Int) : RoutingResult()
    data class Failed(val reason: String) : RoutingResult()
}

/**
 * Send result for individual peer
 */
sealed class SendResult {
    data class Success(val peerId: String) : SendResult()
    data class Failed(val reason: String) : SendResult()
}

/**
 * Routing events
 */
sealed class RoutingEvent {
    data class MessageRouted(val message: Message, val result: RoutingResult) : RoutingEvent()
    data class MessageReceived(val message: Message, val fromPeerId: String) : RoutingEvent()
    data class RoutingFailed(val message: Message, val result: RoutingResult.Failed) : RoutingEvent()
}

/**
 * Routing statistics
 */
data class RoutingStats(
    val cachedMessages: Int,
    val routingEntries: Int,
    val activeRoutes: Int
)