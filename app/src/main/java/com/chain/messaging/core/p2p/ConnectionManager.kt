package com.chain.messaging.core.p2p

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * Manages P2P connections with reliability scoring and connection pooling
 */
class ConnectionManager {
    
    private val TAG = "ConnectionManager"
    
    private val activeConnections = ConcurrentHashMap<String, Connection>()
    private val peerReliability = ConcurrentHashMap<String, PeerReliabilityScore>()
    private val connectionPool = ConcurrentHashMap<String, MutableList<Connection>>()
    
    private val _connectionEvents = MutableSharedFlow<ConnectionEvent>()
    val connectionEvents: SharedFlow<ConnectionEvent> = _connectionEvents.asSharedFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private var isInitialized = false
    
    /**
     * Initialize the connection manager
     */
    suspend fun initialize() {
        if (!isInitialized) {
            isInitialized = true
            Log.i(TAG, "Connection manager initialized")
        }
    }
    
    /**
     * Start the connection manager
     */
    fun start() {
        if (isRunning) return
        
        isRunning = true
        coroutineScope.launch {
            startMaintenanceLoop()
        }
        Log.i(TAG, "Connection manager started")
    }
    
    /**
     * Stop the connection manager
     */
    fun stop() {
        isRunning = false
        
        // Close all active connections
        activeConnections.values.forEach { connection ->
            coroutineScope.launch {
                closeConnection(connection.connectionId)
            }
        }
        
        Log.i(TAG, "Connection manager stopped")
    }
    
    /**
     * Establish connection to a peer
     */
    suspend fun connectToPeer(peer: Peer): Connection? {
        try {
            // Check if already connected
            activeConnections.values.find { it.peerId == peer.id && it.isActive }?.let {
                Log.d(TAG, "Already connected to peer: ${peer.id}")
                return it
            }
            
            // Create new connection
            val connection = createConnection(peer)
            activeConnections[connection.connectionId] = connection
            
            // Add to connection pool
            connectionPool.getOrPut(peer.id) { ArrayList() }.add(connection)
            
            // Update reliability score
            updateReliabilityScore(peer.id, true)
            
            _connectionEvents.emit(ConnectionEvent.Connected(connection, peer))
            Log.i(TAG, "Connected to peer: ${peer.id}")
            
            return connection
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to peer: ${peer.id}", e)
            updateReliabilityScore(peer.id, false)
            _connectionEvents.emit(ConnectionEvent.ConnectionFailed(peer, e.message ?: "Unknown error"))
            return null
        }
    }
    
    /**
     * Disconnect from a peer
     */
    suspend fun disconnectFromPeer(peerId: String) {
        val connections = activeConnections.values.filter { it.peerId == peerId }
        
        connections.forEach { connection ->
            closeConnection(connection.connectionId)
        }
        
        connectionPool.remove(peerId)
        Log.i(TAG, "Disconnected from peer: $peerId")
    }
    
    /**
     * Get active connection to a peer
     */
    fun getConnection(peerId: String): Connection? {
        return activeConnections.values.find { it.peerId == peerId && it.isActive }
    }
    
    /**
     * Get all active connections
     */
    fun getActiveConnections(): List<Connection> {
        return activeConnections.values.filter { it.isActive }
    }
    
    /**
     * Get peer reliability score
     */
    fun getPeerReliability(peerId: String): Double {
        return peerReliability[peerId]?.score ?: 0.0
    }
    
    /**
     * Get connection statistics
     */
    fun getConnectionStats(): ConnectionStats {
        val active = activeConnections.values.count { it.isActive }
        val total = activeConnections.size
        val averageLatency = if (active > 0) {
            activeConnections.values.filter { it.isActive }.map { it.latency }.average().toLong()
        } else 0L
        
        return ConnectionStats(
            activeConnections = active,
            totalConnections = total,
            averageLatency = averageLatency,
            totalBytesSent = activeConnections.values.sumOf { it.bytesSent },
            totalBytesReceived = activeConnections.values.sumOf { it.bytesReceived }
        )
    }
    
    /**
     * Select best peers for message routing
     */
    fun selectBestPeers(availablePeers: List<Peer>, count: Int): List<Peer> {
        return availablePeers
            .map { peer -> peer to getPeerReliability(peer.id) }
            .sortedByDescending { it.second }
            .take(count)
            .map { it.first }
    }
    
    /**
     * Update connection statistics
     */
    fun updateConnectionStats(connectionId: String, bytesSent: Long, bytesReceived: Long, latency: Long) {
        activeConnections[connectionId]?.let { connection ->
            val updatedConnection = connection.copy(
                bytesSent = connection.bytesSent + bytesSent,
                bytesReceived = connection.bytesReceived + bytesReceived,
                latency = latency
            )
            activeConnections[connectionId] = updatedConnection
        }
    }
    
    private suspend fun createConnection(peer: Peer): Connection {
        // Simulate connection establishment
        delay(100) // Network delay
        
        val connectionId = UUID.randomUUID().toString()
        val latency = measureLatency(peer)
        
        return Connection(
            peerId = peer.id,
            connectionId = connectionId,
            establishedAt = Date(),
            isActive = true,
            latency = latency
        )
    }
    
    private suspend fun closeConnection(connectionId: String) {
        activeConnections[connectionId]?.let { connection ->
            val closedConnection = connection.copy(isActive = false)
            activeConnections[connectionId] = closedConnection
            
            _connectionEvents.emit(ConnectionEvent.Disconnected(connection))
            Log.d(TAG, "Closed connection: $connectionId")
        }
    }
    
    private suspend fun measureLatency(peer: Peer): Long {
        val startTime = System.currentTimeMillis()
        
        // Simulate ping
        delay(Random.nextLong(10, 100))
        
        return System.currentTimeMillis() - startTime
    }
    
    private fun updateReliabilityScore(peerId: String, success: Boolean) {
        val current = peerReliability.getOrPut(peerId) {
            PeerReliabilityScore(peerId, 0.5, 0, 0)
        }
        
        val newScore = if (success) {
            current.copy(
                score = minOf(1.0, current.score + RELIABILITY_INCREMENT),
                successCount = current.successCount + 1
            )
        } else {
            current.copy(
                score = maxOf(0.0, current.score - RELIABILITY_DECREMENT),
                failureCount = current.failureCount + 1
            )
        }
        
        peerReliability[peerId] = newScore
    }
    
    private suspend fun startMaintenanceLoop() {
        while (isRunning) {
            try {
                performConnectionMaintenance()
                delay(MAINTENANCE_INTERVAL_MS)
            } catch (e: Exception) {
                Log.e(TAG, "Error in connection maintenance", e)
            }
        }
    }
    
    private suspend fun performConnectionMaintenance() {
        // Check connection health
        val unhealthyConnections = activeConnections.values.filter { connection ->
            connection.isActive && (System.currentTimeMillis() - connection.establishedAt.time) > CONNECTION_TIMEOUT_MS
        }
        
        unhealthyConnections.forEach { connection ->
            try {
                val isHealthy = checkConnectionHealth(connection)
                if (!isHealthy) {
                    closeConnection(connection.connectionId)
                    updateReliabilityScore(connection.peerId, false)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Health check failed for connection: ${connection.connectionId}", e)
                closeConnection(connection.connectionId)
            }
        }
        
        // Clean up old inactive connections
        val cutoffTime = System.currentTimeMillis() - CLEANUP_THRESHOLD_MS
        val toRemove = activeConnections.entries.filter { (_, connection) ->
            !connection.isActive && connection.establishedAt.time < cutoffTime
        }.map { it.key }
        
        toRemove.forEach { connectionId ->
            activeConnections.remove(connectionId)
        }
        
        if (toRemove.isNotEmpty()) {
            Log.d(TAG, "Cleaned up ${toRemove.size} old connections")
        }
    }
    
    private suspend fun checkConnectionHealth(connection: Connection): Boolean {
        // Simulate health check
        delay(50)
        return Random.nextDouble() > 0.1 // 90% success rate
    }
    
    companion object {
        private const val RELIABILITY_INCREMENT = 0.1
        private const val RELIABILITY_DECREMENT = 0.2
        private const val MAINTENANCE_INTERVAL_MS = 30000L // 30 seconds
        private const val CONNECTION_TIMEOUT_MS = 300000L // 5 minutes
        private const val CLEANUP_THRESHOLD_MS = 3600000L // 1 hour
    }
}

/**
 * Connection events
 */
sealed class ConnectionEvent {
    data class Connected(val connection: Connection, val peer: Peer) : ConnectionEvent()
    data class Disconnected(val connection: Connection) : ConnectionEvent()
    data class ConnectionFailed(val peer: Peer, val reason: String) : ConnectionEvent()
    data class LatencyUpdated(val connectionId: String, val latency: Long) : ConnectionEvent()
}

/**
 * Peer reliability scoring
 */
data class PeerReliabilityScore(
    val peerId: String,
    val score: Double,
    val successCount: Int,
    val failureCount: Int
)

/**
 * Connection statistics
 */
data class ConnectionStats(
    val activeConnections: Int,
    val totalConnections: Int,
    val averageLatency: Long,
    val totalBytesSent: Long,
    val totalBytesReceived: Long
)