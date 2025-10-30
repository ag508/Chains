package com.chain.messaging.core.notification

/**
 * Events emitted by the notification system
 */
sealed class NotificationEvent {
    
    /**
     * Message notification was shown
     */
    data class MessageNotificationShown(
        val messageId: String,
        val chatId: String,
        val senderName: String
    ) : NotificationEvent()
    
    /**
     * Missed call notification was shown
     */
    data class MissedCallNotificationShown(
        val callId: String,
        val callerName: String,
        val isVideo: Boolean
    ) : NotificationEvent()
    
    /**
     * System notification was shown
     */
    data class SystemNotificationShown(
        val title: String,
        val message: String
    ) : NotificationEvent()
    
    /**
     * Notification was cleared for specific chat
     */
    data class NotificationCleared(
        val chatId: String
    ) : NotificationEvent()
    
    /**
     * All notifications were cleared
     */
    object AllNotificationsCleared : NotificationEvent()
    
    /**
     * User replied to a message from notification
     */
    data class NotificationReply(
        val chatId: String,
        val replyText: String
    ) : NotificationEvent()
    
    /**
     * User marked chat as read from notification
     */
    data class NotificationMarkRead(
        val chatId: String
    ) : NotificationEvent()
    
    /**
     * User muted chat from notification
     */
    data class NotificationMuteChat(
        val chatId: String
    ) : NotificationEvent()
}