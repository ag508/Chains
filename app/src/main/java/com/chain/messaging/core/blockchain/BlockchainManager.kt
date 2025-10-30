package com.chain.messaging.core.blockchain

import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Interface for managing blockchain connectivity and message transactions
 */
interface BlockchainManager {
    /**
     * Connect to a blockchain node
     */
    suspend fun connect(nodeUrl: String)
    
    /**
     * Send an encrypted message as a blockchain transaction
     */
    suspend fun sendMessage(message: EncryptedMessage): String
    
    /**
     * Send a message with specific recipient and type
     */
    suspend fun sendMessage(
        recipientId: String,
        encryptedContent: String,
        messageType: String
    ): String
    
    /**
     * Subscribe to incoming messages for a specific user
     */
    fun subscribeToMessages(userId: String): Flow<IncomingMessage>
    
    /**
     * Get current network status
     */
    fun getNetworkStatus(): NetworkStatus
    
    /**
     * Prune old messages from the blockchain (after 48 hours)
     */
    suspend fun pruneOldMessages(olderThan: Date)
    
    /**
     * Disconnect from the blockchain network
     */
    suspend fun disconnect()
    
    /**
     * Check if connected to the blockchain network
     */
    fun isConnected(): Boolean
    
    /**
     * Send a deletion transaction for disappearing messages
     */
    suspend fun sendDeletionTransaction(messageId: String)
    
    /**
     * Initialize blockchain manager
     */
    suspend fun initialize()
    
    /**
     * Connect as authenticated user
     */
    suspend fun connectAsUser()
    
    /**
     * Shutdown blockchain manager
     */
    suspend fun shutdown()
    
    /**
     * Reconnect to blockchain
     */
    suspend fun reconnect()
}

/**
 * Encrypted message data structure for blockchain transmission
 */
data class EncryptedMessage(
    val content: String,
    val type: MessageType,
    val keyId: String,
    val timestamp: Long
)

/**
 * Incoming message from the blockchain
 */
data class IncomingMessage(
    val transactionHash: String,
    val senderId: String,
    val recipientId: String,
    val encryptedContent: String,
    val type: String,
    val timestamp: Long,
    val blockNumber: Long
) {
    // Legacy compatibility
    val from: String get() = senderId
    val to: String get() = recipientId
}

/**
 * Network status information
 */
data class NetworkStatus(
    val isConnected: Boolean,
    val nodeUrl: String?,
    val blockHeight: Long,
    val peerCount: Int,
    val lastSyncTime: Long
)

/**
 * Message types supported by the blockchain
 */
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    LOCATION,
    CONTACT,
    POLL,
    SYSTEM
}