package com.chain.messaging.core.blockchain

import kotlinx.serialization.Serializable

/**
 * Blockchain transaction representing a message
 */
@Serializable
data class MessageTransaction(
    val id: String,
    val from: String,
    val to: String,
    val encryptedContent: String,
    val messageType: MessageType,
    val timestamp: Long,
    val signature: String,
    val nonce: String,
    val gasUsed: Long = 0,
    val blockNumber: Long = 0,
    val transactionHash: String = ""
) {
    /**
     * Serialize transaction for blockchain transmission
     */
    fun serialize(): String {
        return kotlinx.serialization.json.Json.encodeToString(serializer(), this)
    }
    
    companion object {
        /**
         * Deserialize transaction from blockchain data
         */
        fun deserialize(data: String): MessageTransaction {
            return kotlinx.serialization.json.Json.decodeFromString(serializer(), data)
        }
    }
}

/**
 * Transaction hash type alias for clarity
 */
typealias TransactionHash = String