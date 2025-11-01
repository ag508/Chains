package com.chain.messaging.core.notification

import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.NotificationSettings
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.domain.repository.SettingsRepository
import com.chain.messaging.domain.repository.UserRepository
import com.chain.messaging.core.messaging.MessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level notification manager that coordinates notification display
 * with user settings and quiet hours
 * Implements requirement 4.4 for push notifications with customization
 */
@Singleton
class NotificationManager @Inject constructor(
    private val notificationService: NotificationService,
    private val notificationActionHandler: NotificationActionHandler,
    private val messagingService: MessagingService,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) {
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    init {
        observeIncomingMessages()
        observeNotificationActions()
    }
    
    /**
     * Get notification events flow
     */
    fun getNotificationEvents(): Flow<NotificationEvent> {
        return notificationService.notificationEvents
    }
    
    /**
     * Show notification for new message if conditions are met
     */
    suspend fun showMessageNotification(
        message: Message,
        currentUserId: String
    ) {
        // Don't show notification for own messages
        if (message.senderId == currentUserId) return
        
        val settings = settingsRepository.getUserSettings()?.notifications
            ?: return
        
        // Check if notifications are enabled
        if (!settings.messageNotifications) return
        
        // Check quiet hours
        if (isInQuietHours(settings)) return
        
        // Get sender info
        val sender = userRepository.getUserById(message.senderId)
        val senderName = sender?.displayName ?: "Unknown"
        
        // Get chat info and unread count
        val chat = chatRepository.getChatById(message.chatId)
        val chatName = if (message.chatId.startsWith("group_")) {
            chat?.name
        } else null

        val unreadCount = chatRepository.getUnreadMessageCount(message.chatId)
        
        notificationService.showMessageNotification(
            message = message,
            senderName = senderName,
            chatName = chatName,
            unreadCount = unreadCount,
            settings = settings
        )
    }
    
    /**
     * Show missed call notification
     */
    suspend fun showMissedCallNotification(
        callId: String,
        callerId: String,
        isVideo: Boolean,
        currentUserId: String
    ) {
        val settings = settingsRepository.getUserSettings()?.notifications
            ?: return
        
        if (!settings.callNotifications) return
        
        if (isInQuietHours(settings)) return
        
        val caller = userRepository.getUserById(callerId)
        val callerName = caller?.displayName ?: "Unknown"
        
        notificationService.showMissedCallNotification(
            callId = callId,
            callerName = callerName,
            isVideo = isVideo,
            settings = settings
        )
    }
    
    /**
     * Show system notification
     */
    fun showSystemNotification(
        title: String,
        message: String,
        priority: Int = android.app.Notification.PRIORITY_DEFAULT
    ) {
        notificationService.showSystemNotification(title, message, priority)
    }
    
    /**
     * Clear notification for specific chat
     */
    fun clearChatNotification(chatId: String) {
        notificationService.clearChatNotification(chatId)
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        notificationService.clearAllNotifications()
    }
    
    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return notificationService.areNotificationsEnabled()
    }
    
    /**
     * Check if should request notification permission
     */
    fun shouldRequestNotificationPermission(): Boolean {
        return notificationService.shouldRequestNotificationPermission()
    }
    
    private fun observeIncomingMessages() {
        coroutineScope.launch {
            messageRepository.getIncomingMessages()
                .forEach { message ->
                    // This will be handled by the messaging service calling showMessageNotification
                    // when a new message arrives
                }
        }
    }
    
    private fun observeNotificationActions() {
        notificationActionHandler.actionEvents
            .onEach { event ->
                when (event) {
                    is NotificationEvent.NotificationReply -> {
                        handleNotificationReply(event.chatId, event.replyText)
                    }
                    
                    is NotificationEvent.NotificationMarkRead -> {
                        handleMarkAsRead(event.chatId)
                    }
                    
                    is NotificationEvent.NotificationMuteChat -> {
                        handleMuteChat(event.chatId)
                    }
                    
                    else -> {
                        // Other events are handled elsewhere
                    }
                }
            }
            .launchIn(coroutineScope)
    }
    
    private suspend fun handleNotificationReply(chatId: String, replyText: String) {
        try {
            // Send the reply message
            messagingService.sendTextMessage(chatId, replyText)
            
            // Clear the notification since user replied
            clearChatNotification(chatId)
        } catch (e: Exception) {
            // Handle error - maybe show a system notification about failed reply
            showSystemNotification(
                "Reply Failed",
                "Could not send reply. Please open the app to try again.",
                android.app.Notification.PRIORITY_HIGH
            )
        }
    }
    
    private suspend fun handleMarkAsRead(chatId: String) {
        try {
            // Mark messages as read
            chatRepository.markChatAsRead(chatId)

            // Clear the notification
            clearChatNotification(chatId)
        } catch (e: Exception) {
            // Handle error silently or log
        }
    }
    
    private suspend fun handleMuteChat(chatId: String) {
        try {
            // Mute the chat (this would need to be implemented in chat settings)
            // For now, just clear the notification
            clearChatNotification(chatId)
            
            showSystemNotification(
                "Chat Muted",
                "You won't receive notifications from this chat for 1 hour."
            )
        } catch (e: Exception) {
            // Handle error silently or log
        }
    }
    
    private fun isInQuietHours(settings: NotificationSettings): Boolean {
        if (!settings.quietHoursEnabled) return false
        
        try {
            val now = LocalTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val startTime = LocalTime.parse(settings.quietHoursStart, formatter)
            val endTime = LocalTime.parse(settings.quietHoursEnd, formatter)
            
            return if (startTime.isBefore(endTime)) {
                // Same day quiet hours (e.g., 22:00 to 08:00 next day)
                now.isAfter(startTime) || now.isBefore(endTime)
            } else {
                // Overnight quiet hours (e.g., 22:00 to 08:00 next day)
                now.isAfter(startTime) || now.isBefore(endTime)
            }
        } catch (e: Exception) {
            // If parsing fails, assume not in quiet hours
            return false
        }
    }
}