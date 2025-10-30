package com.chain.messaging.core.blockchain

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
/**
 * Implementation of BlockchainManager using WebSocket for blockchain communication
 */
@Singleton
class BlockchainManagerImpl @Inject constructor(
    private val transactionSigner: TransactionSigner,
    private val consensusHandler: ConsensusHandler,
    private val authenticationService: com.chain.messaging.core.auth.AuthenticationService
) : BlockchainManager {
    
    private val TAG = "BlockchainManager"
    
    private var webSocket: WebSocket? = null
    private var currentNodeUrl: String? = null
    private var isConnectedState = false
    
    private val messageSubscriptions = ConcurrentHashMap<String, MutableSharedFlow<IncomingMessage>>()
    private val transactionPool = TransactionPool()
    private val messagePruner = MessagePruner()
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var networkStatus = NetworkStatus(
        isConnected = false,
        nodeUrl = null,
        blockHeight = 0,
        peerCount = 0,
        lastSyncTime = 0
    )
    
    override suspend fun initialize() {
        Log.d(TAG, "Initializing BlockchainManager")
        // Initialize transaction pool and message pruner
        transactionPool.initialize()
        messagePruner.initialize()
        Log.d(TAG, "BlockchainManager initialized successfully")
    }
    
    override suspend fun connectAsUser() {
        val currentUser = authenticationService.getCurrentUser()
        if (currentUser != null) {
            Log.d(TAG, "Connecting as user: ${currentUser.userId}")
            // Use default node URL or get from config
            val nodeUrl = "wss://blockchain-node.chain-messaging.com"
            connect(nodeUrl)
        } else {
            throw IllegalStateException("No authenticated user found")
        }
    }
    
    override suspend fun shutdown() {
        Log.d(TAG, "Shutting down BlockchainManager")
        disconnect()
        coroutineScope.cancel()
        Log.d(TAG, "BlockchainManager shutdown complete")
    }
    
    override suspend fun reconnect() {
        Log.d(TAG, "Reconnecting to blockchain")
        currentNodeUrl?.let { nodeUrl ->
            disconnect()
            delay(1000) // Brief delay before reconnection
            connect(nodeUrl)
        } ?: throw IllegalStateException("No previous connection to reconnect to")
    }
    
    override suspend fun connect(nodeUrl: String) {
        try {
            disconnect() // Disconnect from any existing connection
            
            currentNodeUrl = nodeUrl
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            val request = Request.Builder()
                .url(nodeUrl)
                .build()
            
            webSocket = client.newWebSocket(request, createWebSocketListener())
            
            // Wait for connection to establish
            delay(2000)
            
            if (isConnectedState) {
                Log.i(TAG, "Successfully connected to blockchain node: $nodeUrl")
                startSynchronization()
                messagePruner.start()
                startRetryLoop()
            } else {
                throw Exception("Failed to establish WebSocket connection")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to blockchain node: $nodeUrl", e)
            throw e
        }
    }
    
    override suspend fun sendMessage(message: EncryptedMessage): String {
        if (!isConnected()) {
            throw IllegalStateException("Not connected to blockchain network")
        }
        
        try {
            val transaction = createMessageTransaction(message)
            val signedTransaction = transactionSigner.signTransaction(transaction)
            
            // Add to transaction pool
            transactionPool.addTransaction(signedTransaction)
            
            // Broadcast transaction to network
            val broadcastMessage = createBroadcastMessage("SEND_TRANSACTION", signedTransaction.serialize())
            webSocket?.send(broadcastMessage)
            
            Log.d(TAG, "Sent message transaction: ${signedTransaction.id}")
            return signedTransaction.transactionHash
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            throw e
        }
    }
    
    override suspend fun sendMessage(
        recipientId: String,
        encryptedContent: String,
        messageType: String
    ): String {
        val message = EncryptedMessage(
            content = encryptedContent,
            type = MessageType.valueOf(messageType.uppercase()),
            keyId = "default",
            timestamp = System.currentTimeMillis()
        )
        return sendMessage(message)
    }
    
    override fun subscribeToMessages(userId: String): Flow<IncomingMessage> {
        val flow = messageSubscriptions.getOrPut(userId) {
            MutableSharedFlow<IncomingMessage>(replay = 0, extraBufferCapacity = 100)
        }
        return flow.asSharedFlow()
    }
    
    override fun getNetworkStatus(): NetworkStatus {
        return networkStatus.copy()
    }
    
    override suspend fun pruneOldMessages(olderThan: Date) {
        try {
            // Force prune messages locally
            val prunedMessages = messagePruner.forceProneOlderThan(olderThan)
            
            if (isConnected() && prunedMessages.isNotEmpty()) {
                // Send pruning request to blockchain network
                val pruneMessage = createBroadcastMessage("PRUNE_MESSAGES", olderThan.time.toString())
                webSocket?.send(pruneMessage)
                Log.d(TAG, "Requested pruning of ${prunedMessages.size} messages older than: $olderThan")
            } else if (prunedMessages.isNotEmpty()) {
                Log.d(TAG, "Pruned ${prunedMessages.size} messages locally (not connected to network)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prune old messages", e)
        }
    }
    
    override suspend fun disconnect() {
        try {
            messagePruner.stop()
            webSocket?.close(1000, "Client disconnect")
            webSocket = null
            isConnectedState = false
            currentNodeUrl = null
            
            networkStatus = networkStatus.copy(
                isConnected = false,
                nodeUrl = null
            )
            
            Log.i(TAG, "Disconnected from blockchain network")
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect", e)
        }
    }
    
    override fun isConnected(): Boolean {
        return isConnectedState && webSocket != null
    }
    
    override suspend fun sendDeletionTransaction(messageId: String) {
        try {
            if (!isConnected()) {
                Log.w(TAG, "Cannot send deletion transaction - not connected to blockchain")
                return
            }
            
            val deletionMessage = createBroadcastMessage("DELETE_MESSAGE", messageId)
            webSocket?.send(deletionMessage)
            Log.d(TAG, "Sent deletion transaction for message: $messageId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send deletion transaction for message: $messageId", e)
            throw e
        }
    }
    
    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnectedState = true
                networkStatus = networkStatus.copy(
                    isConnected = true,
                    nodeUrl = currentNodeUrl,
                    lastSyncTime = System.currentTimeMillis()
                )
                Log.i(TAG, "WebSocket connection opened")
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                coroutineScope.launch {
                    handleIncomingMessage(text)
                }
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                coroutineScope.launch {
                    handleIncomingMessage(bytes.utf8())
                }
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket closing: $code $reason")
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnectedState = false
                networkStatus = networkStatus.copy(isConnected = false)
                Log.i(TAG, "WebSocket closed: $code $reason")
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnectedState = false
                networkStatus = networkStatus.copy(isConnected = false)
                Log.e(TAG, "WebSocket failure", t)
                
                // Attempt reconnection with exponential backoff
                coroutineScope.launch {
                    attemptReconnection()
                }
            }
        }
    }
    
    private suspend fun handleIncomingMessage(message: String) {
        try {
            val messageData = parseIncomingMessage(message)
            
            when (messageData.type) {
                "NEW_MESSAGE" -> {
                    val incomingMessage = parseIncomingMessageData(messageData.data)
                    deliverMessageToSubscribers(incomingMessage)
                }
                "TRANSACTION_CONFIRMED" -> {
                    val confirmationData = parseTransactionConfirmation(messageData.data)
                    transactionPool.confirmTransaction(confirmationData.transactionId, confirmationData.blockNumber)
                    messagePruner.markMessageDelivered(confirmationData.transactionId)
                    Log.d(TAG, "Transaction confirmed: ${confirmationData.transactionId}")
                }
                "NETWORK_STATUS" -> {
                    updateNetworkStatus(messageData.data)
                }
                "CONSENSUS_UPDATE" -> {
                    consensusHandler.handleConsensusUpdate(messageData.data)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling incoming message", e)
        }
    }
    
    private fun deliverMessageToSubscribers(message: IncomingMessage) {
        messageSubscriptions[message.to]?.tryEmit(message)
    }
    
    private suspend fun createMessageTransaction(message: EncryptedMessage): MessageTransaction {
        return MessageTransaction(
            id = UUID.randomUUID().toString(),
            from = getCurrentUserId(),
            to = extractRecipientFromMessage(message),
            encryptedContent = message.content,
            messageType = message.type,
            timestamp = message.timestamp,
            signature = "",
            nonce = generateNonce()
        )
    }
    
    private fun createBroadcastMessage(type: String, data: String): String {
        return """{"type":"$type","data":"$data","timestamp":${System.currentTimeMillis()}}"""
    }
    
    private fun parseIncomingMessage(message: String): IncomingMessageData {
        // Simple JSON parsing - in production, use proper JSON library
        val parts = message.split("\"")
        return IncomingMessageData(
            type = parts.getOrNull(3) ?: "",
            data = parts.getOrNull(7) ?: ""
        )
    }
    
    private fun parseIncomingMessageData(data: String): IncomingMessage {
        return MessageTransaction.deserialize(data).let { tx ->
            IncomingMessage(
                transactionHash = tx.transactionHash,
                senderId = tx.from,
                recipientId = tx.to,
                encryptedContent = tx.encryptedContent,
                type = tx.messageType.name,
                timestamp = tx.timestamp,
                blockNumber = tx.blockNumber
            )
        }
    }
    
    private fun updateNetworkStatus(data: String) {
        // Parse network status data and update
        networkStatus = networkStatus.copy(
            lastSyncTime = System.currentTimeMillis()
        )
    }
    
    private suspend fun startSynchronization() {
        coroutineScope.launch {
            while (isConnected()) {
                try {
                    val syncMessage = createBroadcastMessage("SYNC_REQUEST", "")
                    webSocket?.send(syncMessage)
                    delay(30000) // Sync every 30 seconds
                } catch (e: Exception) {
                    Log.e(TAG, "Error during synchronization", e)
                }
            }
        }
    }
    
    private suspend fun attemptReconnection() {
        var retryCount = 0
        val maxRetries = 5
        
        while (retryCount < maxRetries && !isConnected()) {
            try {
                delay((1000 * Math.pow(2.0, retryCount.toDouble())).toLong()) // Exponential backoff
                currentNodeUrl?.let { connect(it) }
                if (isConnected()) {
                    Log.i(TAG, "Reconnection successful after $retryCount retries")
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "Reconnection attempt $retryCount failed", e)
            }
            retryCount++
        }
        
        Log.e(TAG, "Failed to reconnect after $maxRetries attempts")
    }
    
    private suspend fun getCurrentUserId(): String {
        val currentUser = authenticationService.getCurrentUser()
        return currentUser?.userId ?: "anonymous_user"
    }
    
    private fun extractRecipientFromMessage(message: EncryptedMessage): String {
        // TODO: Extract recipient from encrypted message metadata
        return "recipient_id"
    }
    
    private fun generateNonce(): String {
        return UUID.randomUUID().toString()
    }
    
    private fun parseTransactionConfirmation(data: String): TransactionConfirmation {
        // Simple parsing - in production use proper JSON library
        return TransactionConfirmation(
            transactionId = data.split("\"")[3],
            blockNumber = data.split("block\":")[1].split(",")[0].toLong()
        )
    }
    
    private suspend fun startRetryLoop() {
        coroutineScope.launch {
            while (isConnected()) {
                try {
                    val transactionsToRetry = transactionPool.getTransactionsForRetry()
                    transactionsToRetry.forEach { transaction ->
                        try {
                            val broadcastMessage = createBroadcastMessage("SEND_TRANSACTION", transaction.serialize())
                            webSocket?.send(broadcastMessage)
                            transactionPool.incrementRetryCount(transaction.id)
                            Log.d(TAG, "Retried transaction: ${transaction.id}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to retry transaction: ${transaction.id}", e)
                            transactionPool.failTransaction(transaction.id, "Retry failed: ${e.message}")
                        }
                    }
                    delay(30000) // Check for retries every 30 seconds
                } catch (e: Exception) {
                    Log.e(TAG, "Error in retry loop", e)
                }
            }
        }
    }
    
    /**
     * Get transaction pool state for monitoring
     */
    fun getTransactionPoolState() = transactionPool.poolState
    
    /**
     * Get pruning statistics
     */
    fun getPruningStats() = messagePruner.getPruningStats()
    
    private data class IncomingMessageData(
        val type: String,
        val data: String
    )
    
    private data class TransactionConfirmation(
        val transactionId: String,
        val blockNumber: Long
    )
}