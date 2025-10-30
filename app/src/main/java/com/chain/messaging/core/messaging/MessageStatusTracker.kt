package com.chain.messaging.core.messaging

import com.chain.messaging.domain.model.MessageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks message status changes and provides real-time updates
 */
@Singleton
class MessageStatusTracker @Inject constructor() {
    
    // Map of messageId to its current status
    private val messageStatuses = ConcurrentHashMap<String, MessageStatus>()
    
    // StateFlow for observing status changes
    private val _statusUpdates = MutableStateFlow<Map<String, MessageStatus>>(emptyMap())
    val statusUpdates: StateFlow<Map<String, MessageStatus>> = _statusUpdates.asStateFlow()
    
    // Map of chatId to delivery receipts (messageId to timestamp)
    private val _deliveryReceipts = MutableStateFlow<Map<String, Map<String, Long>>>(emptyMap())
    val deliveryReceipts: StateFlow<Map<String, Map<String, Long>>> = _deliveryReceipts.asStateFlow()
    
    // Map of chatId to read receipts (messageId to timestamp)
    private val _readReceipts = MutableStateFlow<Map<String, Map<String, Long>>>(emptyMap())
    val readReceipts: StateFlow<Map<String, Map<String, Long>>> = _readReceipts.asStateFlow()
    
    /**
     * Updates the status of a message
     */
    fun updateMessageStatus(messageId: String, status: MessageStatus) {
        messageStatuses[messageId] = status
        
        // Update StateFlow
        val currentStatuses = _statusUpdates.value.toMutableMap()
        currentStatuses[messageId] = status
        _statusUpdates.value = currentStatuses
    }
    
    /**
     * Gets the current status of a message
     */
    fun getMessageStatus(messageId: String): MessageStatus? {
        return messageStatuses[messageId]
    }
    
    /**
     * Marks a message as sent (successfully transmitted to blockchain)
     */
    fun markAsSent(messageId: String) {
        updateMessageStatus(messageId, MessageStatus.SENT)
    }
    
    /**
     * Marks a message as delivered (received by recipient's device)
     */
    fun markAsDelivered(messageId: String, chatId: String, timestamp: Long = System.currentTimeMillis()) {
        updateMessageStatus(messageId, MessageStatus.DELIVERED)
        
        // Update delivery receipts
        val currentReceipts = _deliveryReceipts.value.toMutableMap()
        val chatReceipts = currentReceipts[chatId]?.toMutableMap() ?: mutableMapOf()
        chatReceipts[messageId] = timestamp
        currentReceipts[chatId] = chatReceipts
        _deliveryReceipts.value = currentReceipts
    }
    
    /**
     * Marks a message as read (viewed by recipient)
     */
    fun markAsRead(messageId: String, chatId: String, timestamp: Long = System.currentTimeMillis()) {
        updateMessageStatus(messageId, MessageStatus.READ)
        
        // Update read receipts
        val currentReceipts = _readReceipts.value.toMutableMap()
        val chatReceipts = currentReceipts[chatId]?.toMutableMap() ?: mutableMapOf()
        chatReceipts[messageId] = timestamp
        currentReceipts[chatId] = chatReceipts
        _readReceipts.value = currentReceipts
    }
    
    /**
     * Marks a message as failed
     */
    fun markAsFailed(messageId: String) {
        updateMessageStatus(messageId, MessageStatus.FAILED)
    }
    
    /**
     * Gets delivery receipt timestamp for a message
     */
    fun getDeliveryReceipt(messageId: String, chatId: String): Long? {
        return _deliveryReceipts.value[chatId]?.get(messageId)
    }
    
    /**
     * Gets read receipt timestamp for a message
     */
    fun getReadReceipt(messageId: String, chatId: String): Long? {
        return _readReceipts.value[chatId]?.get(messageId)
    }
    
    /**
     * Processes incoming delivery receipt
     */
    fun processDeliveryReceipt(messageId: String, chatId: String, timestamp: Long) {
        markAsDelivered(messageId, chatId, timestamp)
    }
    
    /**
     * Processes incoming read receipt
     */
    fun processReadReceipt(messageId: String, chatId: String, timestamp: Long) {
        markAsRead(messageId, chatId, timestamp)
    }
    
    /**
     * Clears status tracking for old messages
     */
    fun cleanup(olderThanTimestamp: Long) {
        // Remove old message statuses to prevent memory leaks
        val messagesToRemove = mutableListOf<String>()
        
        messageStatuses.forEach { (messageId, _) ->
            // In a real implementation, you'd check the message timestamp
            // For now, we'll keep all messages
        }
        
        messagesToRemove.forEach { messageId ->
            messageStatuses.remove(messageId)
        }
        
        // Update StateFlow
        _statusUpdates.value = messageStatuses.toMap()
    }
}