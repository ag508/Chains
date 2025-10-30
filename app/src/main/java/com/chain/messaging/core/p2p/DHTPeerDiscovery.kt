package com.chain.messaging.core.p2p

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.random.Random

/**
 * Distributed Hash Table implementation for peer discovery
 */
class DHTPeerDiscovery {
    
    private val TAG = "DHTPeerDiscovery"
    
    private val routingTable = ConcurrentHashMap<String, Peer>()
    private val localNodeId = generateNodeId()
    private val kBuckets = Array(160) { mutableListOf<Peer>() } // 160 bits for SHA-1
    
    private val _discoveryEvents = MutableSharedFlow<DiscoveryEvent>()
    val discoveryEvents: SharedFlow<DiscoveryEvent> = _discoveryEvents.asSharedFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private var isInitialized = false
    
    /**
     * Initialize the DHT peer discovery
     */
    suspend fun initialize() {
        if (!isInitialized) {
            isInitialized = true
            Log.i(TAG, "DHT peer discovery initialized with node ID: $localNodeId")
        }
    }
    
    /**
     * Start the DHT peer discovery
     */
    fun start() {
        if (isRunning) return
        
        isRunning = true
        coroutineScope.launch {
            startDiscoveryLoop()
        }
        Log.i(TAG, "DHT peer discovery started with node ID: $localNodeId")
    }
    
    /**
     * Stop the DHT peer discovery
     */
    fun stop() {
        isRunning = false
        Log.i(TAG, "DHT peer discovery stopped")
    }
    
    /**
     * Add a peer to the routing table
     */
    fun addPeer(peer: Peer) {
        val distance = calculateDistance(localNodeId, peer.id)
        val bucketIndex = getBucketIndex(distance)
        
        synchronized(kBuckets[bucketIndex]) {
            val bucket = kBuckets[bucketIndex]
            
            // Remove if already exists
            bucket.removeIf { it.id == peer.id }
            
            // Add to front (most recently seen)
            bucket.add(0, peer)
            
            // Maintain bucket size (K = 20)
            if (bucket.size > K_BUCKET_SIZE) {
                bucket.removeAt(bucket.size - 1)
            }
        }
        
        routingTable[peer.id] = peer
        coroutineScope.launch {
            _discoveryEvents.emit(DiscoveryEvent.PeerAdded(peer))
        }
        
        Log.d(TAG, "Added peer to DHT: ${peer.id} (bucket $bucketIndex)")
    }
    
    /**
     * Remove a peer from the routing table
     */
    fun removePeer(peerId: String) {
        routingTable.remove(peerId)?.let { peer ->
            val distance = calculateDistance(localNodeId, peerId)
            val bucketIndex = getBucketIndex(distance)
            
            synchronized(kBuckets[bucketIndex]) {
                kBuckets[bucketIndex].removeIf { it.id == peerId }
            }
            
            coroutineScope.launch {
                _discoveryEvents.emit(DiscoveryEvent.PeerRemoved(peer))
            }
            
            Log.d(TAG, "Removed peer from DHT: $peerId")
        }
    }
    
    /**
     * Find the closest peers to a target ID
     */
    fun findClosestPeers(targetId: String, count: Int = ALPHA): List<Peer> {
        val allPeers = routingTable.values.toList()
        
        return allPeers
            .map { peer -> peer to calculateDistance(targetId, peer.id) }
            .sortedBy { it.second }
            .take(count)
            .map { it.first }
    }
    
    /**
     * Get all known peers
     */
    fun getAllPeers(): List<Peer> {
        return routingTable.values.toList()
    }
    
    /**
     * Get peers from a specific bucket
     */
    fun getPeersFromBucket(bucketIndex: Int): List<Peer> {
        return if (bucketIndex in 0 until kBuckets.size) {
            synchronized(kBuckets[bucketIndex]) {
                kBuckets[bucketIndex].toList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Perform iterative peer lookup
     */
    suspend fun lookupPeers(targetId: String): List<Peer> {
        val contacted = mutableSetOf<String>()
        val candidates = mutableListOf<Peer>()
        
        // Start with closest known peers
        candidates.addAll(findClosestPeers(targetId, ALPHA))
        
        repeat(MAX_LOOKUP_ITERATIONS) { iteration ->
            val toContact = candidates
                .filter { it.id !in contacted }
                .take(ALPHA)
            
            if (toContact.isEmpty()) return@repeat
            
            toContact.forEach { peer ->
                contacted.add(peer.id)
                try {
                    // In a real implementation, this would send a FIND_NODE RPC
                    val foundPeers = simulateFindNodeRPC(peer, targetId)
                    foundPeers.forEach { foundPeer ->
                        if (foundPeer.id !in contacted) {
                            candidates.add(foundPeer)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to contact peer ${peer.id} during lookup", e)
                }
            }
            
            // Sort by distance to target
            candidates.sortBy { calculateDistance(targetId, it.id) }
            
            Log.d(TAG, "Lookup iteration $iteration: found ${candidates.size} candidates")
        }
        
        return candidates.take(K_BUCKET_SIZE)
    }
    
    /**
     * Get DHT statistics
     */
    fun getDHTStats(): DHTStats {
        val totalPeers = routingTable.size
        val activeBuckets = kBuckets.count { it.isNotEmpty() }
        val averageBucketSize = if (activeBuckets > 0) {
            kBuckets.sumOf { it.size }.toDouble() / activeBuckets
        } else 0.0
        
        return DHTStats(
            totalPeers = totalPeers,
            activeBuckets = activeBuckets,
            averageBucketSize = averageBucketSize,
            localNodeId = localNodeId
        )
    }
    
    private suspend fun startDiscoveryLoop() {
        while (isRunning) {
            try {
                performPeriodicMaintenance()
                delay(MAINTENANCE_INTERVAL_MS)
            } catch (e: Exception) {
                Log.e(TAG, "Error in discovery loop", e)
            }
        }
    }
    
    private suspend fun performPeriodicMaintenance() {
        // Refresh buckets by looking up random IDs
        val randomBucket = kBuckets.indices.random()
        if (kBuckets[randomBucket].isNotEmpty()) {
            val randomId = generateRandomIdInBucket(randomBucket)
            lookupPeers(randomId)
        }
        
        // Ping least recently seen peers to check if they're still alive
        pingLeastRecentlySeenPeers()
    }
    
    private suspend fun pingLeastRecentlySeenPeers() {
        kBuckets.forEach { bucket ->
            synchronized(bucket) {
                if (bucket.isNotEmpty()) {
                    val leastRecent = bucket.last()
                    if (System.currentTimeMillis() - leastRecent.lastSeen.time > PEER_TIMEOUT_MS) {
                        // In a real implementation, this would send a PING RPC
                        val isAlive = simulatePingRPC(leastRecent)
                        if (!isAlive) {
                            bucket.remove(leastRecent)
                            routingTable.remove(leastRecent.id)
                            coroutineScope.launch {
                                _discoveryEvents.emit(DiscoveryEvent.PeerRemoved(leastRecent))
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun calculateDistance(id1: String, id2: String): String {
        val hash1 = id1.toByteArray()
        val hash2 = id2.toByteArray()
        val xor = ByteArray(minOf(hash1.size, hash2.size))
        
        for (i in xor.indices) {
            xor[i] = (hash1[i].toInt() xor hash2[i].toInt()).toByte()
        }
        
        return Base64.getEncoder().encodeToString(xor)
    }
    
    private fun getBucketIndex(distance: String): Int {
        val distanceBytes = Base64.getDecoder().decode(distance)
        
        for (i in distanceBytes.indices) {
            val byte = distanceBytes[i].toInt() and 0xFF
            if (byte != 0) {
                // Find the position of the most significant bit
                var msb = 7
                var temp = byte
                while (temp and (1 shl msb) == 0 && msb >= 0) {
                    msb--
                }
                return (i * 8) + (7 - msb)
            }
        }
        
        return 159 // Maximum distance
    }
    
    private fun generateNodeId(): String {
        val random = ByteArray(20) // 160 bits
        Random.nextBytes(random)
        return Base64.getEncoder().encodeToString(random)
    }
    
    private fun generateRandomIdInBucket(bucketIndex: Int): String {
        // Generate a random ID that would fall into the specified bucket
        val random = ByteArray(20)
        Random.nextBytes(random)
        return Base64.getEncoder().encodeToString(random)
    }
    
    private suspend fun simulateFindNodeRPC(peer: Peer, targetId: String): List<Peer> {
        // Simulate network delay
        delay(50)
        
        // Return some random peers (in real implementation, this would be an actual RPC)
        return (1..3).map { i ->
            Peer(
                id = "simulated_peer_${peer.id}_$i",
                address = "192.168.1.$i:8080",
                publicKey = "simulated_key_$i",
                lastSeen = Date(),
                reliability = 0.8 + (Random.nextDouble() * 0.2)
            )
        }
    }
    
    private suspend fun simulatePingRPC(peer: Peer): Boolean {
        // Simulate network delay
        delay(30)
        
        // Simulate 90% success rate
        return Random.nextDouble() > 0.1
    }
    
    companion object {
        private const val K_BUCKET_SIZE = 20
        private const val ALPHA = 3
        private const val MAX_LOOKUP_ITERATIONS = 10
        private const val MAINTENANCE_INTERVAL_MS = 60000L // 1 minute
        private const val PEER_TIMEOUT_MS = 300000L // 5 minutes
    }
}

/**
 * DHT discovery events
 */
sealed class DiscoveryEvent {
    data class PeerAdded(val peer: Peer) : DiscoveryEvent()
    data class PeerRemoved(val peer: Peer) : DiscoveryEvent()
    data class LookupCompleted(val targetId: String, val foundPeers: List<Peer>) : DiscoveryEvent()
}

/**
 * DHT statistics
 */
data class DHTStats(
    val totalPeers: Int,
    val activeBuckets: Int,
    val averageBucketSize: Double,
    val localNodeId: String
)