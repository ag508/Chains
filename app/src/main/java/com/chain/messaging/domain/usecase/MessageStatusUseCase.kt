package com.chain.messaging.domain.usecase

import com.chain.messaging.core.messaging.MessageStatusTracker
import com.chain.messaging.domain.model.MessageStatus
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Use case for managing message status tracking
 */
class MessageStatusUseCase @Inject constructor(
    private val messageStatusTracker: MessageStatusTracker
) {
    
    /**
     * Gets the current status of a message
     */
    fun getMessageStatus(messageId: String): MessageStatus? {
        return messageStatusTracker.getMessageStatus(messageId)
    }
    
    /**
     * Marks a message as delivered
     */
    fun markAsDelivered(messageId: String, chatId: String) {
        messageStatusTracker.markAsDelivered(messageId, chatId)
    }
    
    /**
     * Marks a message as read
     */
    fun markAsRead(messageId: String, chatId: String) {
        messageStatusTracker.markAsRead(messageId, chatId)
    }
    
    /**
     * Marks a message as failed
     */
    fun markAsFailed(messageId: String) {
        messageStatusTracker.markAsFailed(messageId)
    }
    
    /**
     * Observes message status updates
     */
    fun observeStatusUpdates(): StateFlow<Map<String, MessageStatus>> {
        return messageStatusTracker.statusUpdates
    }
    
    /**
     * Observes delivery receipts
     */
    fun observeDeliveryReceipts(): StateFlow<Map<String, Map<String, Long>>> {
        return messageStatusTracker.deliveryReceipts
    }
    
    /**
     * Observes read receipts
     */
    fun observeReadReceipts(): StateFlow<Map<String, Map<String, Long>>> {
        return messageStatusTracker.readReceipts
    }
}