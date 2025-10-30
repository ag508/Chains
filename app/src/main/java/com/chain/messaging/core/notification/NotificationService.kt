package com.chain.messaging.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.chain.messaging.R
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.model.NotificationSettings
import com.chain.messaging.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core notification service for managing all app notifications
 * Implements requirement 4.4 for push notifications and notification actions
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _notificationEvents = MutableSharedFlow<NotificationEvent>()
    val notificationEvents: Flow<NotificationEvent> = _notificationEvents.asSharedFlow()
    
    companion object {
        // Notification channels
        private const val CHANNEL_ID_MESSAGES = "messages"
        private const val CHANNEL_ID_CALLS = "calls"
        private const val CHANNEL_ID_GROUPS = "groups"
        private const val CHANNEL_ID_SYSTEM = "system"
        
        // Notification IDs
        private const val NOTIFICATION_ID_MESSAGE_BASE = 2000
        private const val NOTIFICATION_ID_GROUP_BASE = 3000
        private const val NOTIFICATION_ID_SYSTEM_BASE = 4000
        
        // Actions
        private const val ACTION_REPLY = "ACTION_REPLY"
        private const val ACTION_MARK_READ = "ACTION_MARK_READ"
        private const val ACTION_MUTE_CHAT = "ACTION_MUTE_CHAT"
        
        // Remote input key
        private const val KEY_TEXT_REPLY = "key_text_reply"
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Show notification for new message
     */
    fun showMessageNotification(
        message: Message,
        senderName: String,
        chatName: String?,
        unreadCount: Int,
        settings: NotificationSettings
    ) {
        if (!settings.messageNotifications) return
        
        val channelId = if (message.chatId.startsWith("group_")) {
            if (!settings.groupNotifications) return
            CHANNEL_ID_GROUPS
        } else {
            CHANNEL_ID_MESSAGES
        }
        
        val notificationId = generateNotificationId(message.chatId)
        
        val contentTitle = chatName ?: senderName
        val contentText = when (message.type) {
            MessageType.TEXT -> if (settings.showPreview) message.content else "New message"
            MessageType.IMAGE -> "📷 Photo"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.AUDIO -> "🎵 Voice message"
            MessageType.DOCUMENT -> "📄 Document"
            else -> "New message"
        }
        
        val fullText = if (settings.showSenderName && chatName != null) {
            "$senderName: $contentText"
        } else {
            contentText
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(contentTitle)
            .setContentText(fullText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(createChatIntent(message.chatId))
            .setGroup(message.chatId)
            .apply {
                if (unreadCount > 1) {
                    setNumber(unreadCount)
                    setSubText("$unreadCount new messages")
                }
                
                // Combine notification defaults
                var defaults = 0
                if (settings.soundEnabled) {
                    defaults = defaults or NotificationCompat.DEFAULT_SOUND
                }
                if (settings.vibrationEnabled) {
                    defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
                }
                if (settings.ledEnabled) {
                    defaults = defaults or NotificationCompat.DEFAULT_LIGHTS
                }
                if (defaults != 0) {
                    setDefaults(defaults)
                }
                
                // Add reply action for direct messages
                if (!message.chatId.startsWith("group_")) {
                    addAction(createReplyAction(message.chatId, senderName))
                }
                
                // Add mark as read action
                addAction(createMarkReadAction(message.chatId))
                
                // Add mute action
                addAction(createMuteAction(message.chatId))
            }
            .build()
        
        notificationManager.notify(notificationId, notification)
        
        coroutineScope.launch {
            _notificationEvents.emit(
                NotificationEvent.MessageNotificationShown(
                    messageId = message.id,
                    chatId = message.chatId,
                    senderName = senderName
                )
            )
        }
    }
    
    /**
     * Show notification for missed call
     */
    fun showMissedCallNotification(
        callId: String,
        callerName: String,
        isVideo: Boolean,
        settings: NotificationSettings
    ) {
        if (!settings.callNotifications) return
        
        val callType = if (isVideo) "video call" else "voice call"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CALLS)
            .setSmallIcon(R.drawable.ic_call_missed)
            .setContentTitle("Missed $callType")
            .setContentText("From $callerName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
            .setAutoCancel(true)
            .setContentIntent(createCallHistoryIntent())
            .apply {
                // Combine notification defaults for missed call
                var defaults = 0
                if (settings.soundEnabled) {
                    defaults = defaults or NotificationCompat.DEFAULT_SOUND
                }
                if (settings.vibrationEnabled) {
                    defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
                }
                if (defaults != 0) {
                    setDefaults(defaults)
                }
            }
            .build()
        
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        
        coroutineScope.launch {
            _notificationEvents.emit(
                NotificationEvent.MissedCallNotificationShown(
                    callId = callId,
                    callerName = callerName,
                    isVideo = isVideo
                )
            )
        }
    }
    
    /**
     * Show system notification (e.g., security alerts, sync status)
     */
    fun showSystemNotification(
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        autoCancel: Boolean = true
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SYSTEM)
            .setSmallIcon(R.drawable.ic_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(autoCancel)
            .setContentIntent(createMainIntent())
            .build()
        
        val notificationId = NOTIFICATION_ID_SYSTEM_BASE + System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        
        coroutineScope.launch {
            _notificationEvents.emit(
                NotificationEvent.SystemNotificationShown(title, message)
            )
        }
    }
    
    /**
     * Clear notification for specific chat
     */
    fun clearChatNotification(chatId: String) {
        val notificationId = generateNotificationId(chatId)
        notificationManager.cancel(notificationId)
        
        coroutineScope.launch {
            _notificationEvents.emit(
                NotificationEvent.NotificationCleared(chatId)
            )
        }
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        notificationManager.cancelAll()
        
        coroutineScope.launch {
            _notificationEvents.emit(NotificationEvent.AllNotificationsCleared)
        }
    }
    
    /**
     * Check if notifications are enabled for the app
     */
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    /**
     * Request notification permission (Android 13+)
     */
    fun shouldRequestNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
               !areNotificationsEnabled()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_MESSAGES,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new messages"
                    enableVibration(true)
                    enableLights(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_CALLS,
                    "Calls",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for calls and missed calls"
                    enableVibration(true)
                    enableLights(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_GROUPS,
                    "Group Messages",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for group messages"
                    enableVibration(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_SYSTEM,
                    "System",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "System notifications and alerts"
                }
            )
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
    
    private fun generateNotificationId(chatId: String): Int {
        return if (chatId.startsWith("group_")) {
            NOTIFICATION_ID_GROUP_BASE + chatId.hashCode()
        } else {
            NOTIFICATION_ID_MESSAGE_BASE + chatId.hashCode()
        }
    }
    
    private fun createChatIntent(chatId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("chat_id", chatId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        return PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createMainIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createCallHistoryIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("show_call_history", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        return PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createReplyAction(chatId: String, senderName: String): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Reply to $senderName")
            .build()
        
        val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra("chat_id", chatId)
        }
        
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            chatId.hashCode(),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            R.drawable.ic_reply,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()
    }
    
    private fun createMarkReadAction(chatId: String): NotificationCompat.Action {
        val markReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_READ
            putExtra("chat_id", chatId)
        }
        
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            chatId.hashCode() + 1,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            R.drawable.ic_mark_read,
            "Mark as read",
            markReadPendingIntent
        ).build()
    }
    
    private fun createMuteAction(chatId: String): NotificationCompat.Action {
        val muteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MUTE_CHAT
            putExtra("chat_id", chatId)
        }
        
        val mutePendingIntent = PendingIntent.getBroadcast(
            context,
            chatId.hashCode() + 2,
            muteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            R.drawable.ic_mute,
            "Mute",
            mutePendingIntent
        ).build()
    }
    
    /**
     * Initialize notification service
     */
    suspend fun initialize() {
        // Notification service is already initialized in constructor
    }
    
    /**
     * Shutdown notification service
     */
    suspend fun shutdown() {
        clearAllNotifications()
    }
}