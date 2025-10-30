package com.chain.messaging.core.offline

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.core.util.TimeUtils
import com.chain.messaging.core.util.toLong
import com.chain.messaging.core.util.toLocalDateTime
import com.chain.messaging.data.local.dao.QueuedMessageDao
import com.chain.messaging.data.local.entity.QueuedMessageEntity
import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineMessageQueueImpl @Inject constructor(
    private val queuedMessageDao: QueuedMessageDao,
    private val messagingService: MessagingService,
    private val networkMonitor: NetworkMonitor,
    private val backoffStrategy: BackoffStrategy,
    private val coroutineScope: CoroutineScope
) : OfflineMessageQueue {
    
    private val _queuedMessages = MutableStateFlow<List<QueuedMessage>>(emptyList())
    
    private var isInitialized = false
    private var isOfflineModeEnabled = false

    init {
        // Start monitoring network connectivity
        monitorNetworkAndProcessQueue()
        // Load existing queued messages
        loadQueuedMessages()
    }
    
    override suspend fun initialize() {
        if (!isInitialized) {
            isInitialized = true
            // Load existing queued messages from database
            refreshQueuedMessages()
            // Start background processing
            startBackgroundProcessing()
        }
    }
    
    override suspend fun enableOfflineMode() {
        isOfflineModeEnabled = true
        // Additional offline mode setup if needed
    }
    
    override suspend fun queueMessage(message: Message) {
        val queuedMessage = QueuedMessage(
            id = UUID.randomUUID().toString(),
            message = message,
            queuedAt = TimeUtils.getCurrentLocalDateTime(),
            priority = determinePriority(message)
        )
        
        // Save to database
        queuedMessageDao.insertQueuedMessage(queuedMessage.toEntity())
        
        // Update in-memory list
        refreshQueuedMessages()
    }
    
    override suspend fun getQueuedMessages(): List<QueuedMessage> {
        return queuedMessageDao.getAllQueuedMessages().map { it.toQueuedMessage() }
    }
    
    override suspend fun removeFromQueue(messageId: String) {
        queuedMessageDao.deleteQueuedMessage(messageId)
        refreshQueuedMessages()
    }
    
    override suspend fun clearQueue() {
        queuedMessageDao.deleteAllQueuedMessages()
        refreshQueuedMessages()
    }
    
    override fun getQueuedMessagesFlow(): Flow<List<QueuedMessage>> {
        return _queuedMessages.asStateFlow()
    }
    
    override suspend fun processQueuedMessages() {
        val queuedMessages = getQueuedMessages()
            .filter { !it.hasExceededMaxRetries() }
            .sortedWith(compareBy<QueuedMessage> { it.priority.ordinal }.thenBy { it.queuedAt })
        
        for (queuedMessage in queuedMessages) {
            if (!queuedMessage.canRetry(backoffStrategy)) {
                continue
            }
            
            try {
                // Attempt to send the message
                messagingService.sendMessage(queuedMessage.message)
                
                // If successful, remove from queue
                removeFromQueue(queuedMessage.id)
                
            } catch (e: Exception) {
                // Update retry count
                val updatedMessage = queuedMessage.withIncrementedRetry()
                
                if (updatedMessage.hasExceededMaxRetries()) {
                    // Move to failed messages or remove
                    removeFromQueue(queuedMessage.id)
                    handleFailedMessage(queuedMessage, e)
                } else {
                    // Update in database with new retry count
                    queuedMessageDao.updateQueuedMessage(updatedMessage.toEntity())
                }
            }
        }
        
        refreshQueuedMessages()
    }
    
    override suspend fun getQueueSize(): Int {
        return queuedMessageDao.getQueueSize()
    }
    
    private fun monitorNetworkAndProcessQueue() {
        coroutineScope.launch {
            networkMonitor.isConnected
                .distinctUntilChanged()
                .filter { it } // Only process when connected
                .collect {
                    delay(1000) // Small delay to ensure connection is stable
                    processQueuedMessages()
                }
        }
    }
    
    private fun loadQueuedMessages() {
        coroutineScope.launch {
            refreshQueuedMessages()
        }
    }
    
    private suspend fun refreshQueuedMessages() {
        val messages = getQueuedMessages()
        _queuedMessages.value = messages
    }
    
    private fun determinePriority(message: Message): MessagePriority {
        return when {
            message.type.name.contains("CALL") -> MessagePriority.HIGH
            message.type.name.contains("SYSTEM") -> MessagePriority.HIGH
            message.type.name.contains("IMAGE") || 
            message.type.name.contains("VIDEO") || 
            message.type.name.contains("DOCUMENT") -> MessagePriority.LOW
            else -> MessagePriority.NORMAL
        }
    }
    
    private suspend fun handleFailedMessage(queuedMessage: QueuedMessage, error: Exception) {
        // Log the failure or store in failed messages table
        // This could be extended to notify the user about failed messages
    }
    
    private fun startBackgroundProcessing() {
        coroutineScope.launch {
            while (isInitialized) {
                try {
                    if (networkMonitor.isConnected() && isOfflineModeEnabled) {
                        processQueuedMessages()
                    }
                    delay(30000) // Process every 30 seconds
                } catch (e: Exception) {
                    // Handle processing errors
                    delay(60000) // Wait longer on error
                }
            }
        }
    }
}

// Extension functions for entity conversion
private fun QueuedMessage.toEntity(): QueuedMessageEntity {
    return QueuedMessageEntity(
        id = id,
        messageId = message.id,
        chatId = message.chatId,
        senderId = message.senderId,
        content = message.content,
        messageType = message.type.name,
        queuedAt = queuedAt,
        retryCount = retryCount,
        lastRetryAt = lastRetryAt,
        priority = priority.name,
        maxRetries = maxRetries
    )
}

private fun QueuedMessageEntity.toQueuedMessage(): QueuedMessage {
    return QueuedMessage(
        id = id,
        message = Message(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = enumValueOf(messageType),
            timestamp = TimeUtils.longToDate(queuedAt.toLong()), // Convert LocalDateTime to Date
            status = com.chain.messaging.domain.model.MessageStatus.SENDING
        ),
        queuedAt = queuedAt,
        retryCount = retryCount,
        lastRetryAt = lastRetryAt,
        priority = enumValueOf(priority),
        maxRetries = maxRetries
    )
}