package com.chain.messaging.core.webrtc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chain.messaging.R
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
 * Service for managing call notifications
 * Implements requirement 6.2 for call status management and notifications
 */
@Singleton
class CallNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _notificationEvents = MutableSharedFlow<CallNotificationEvent>()
    val notificationEvents: Flow<CallNotificationEvent> = _notificationEvents.asSharedFlow()
    
    companion object {
        private const val CHANNEL_ID_INCOMING_CALLS = "incoming_calls"
        private const val CHANNEL_ID_ONGOING_CALLS = "ongoing_calls"
        private const val NOTIFICATION_ID_INCOMING_CALL = 1001
        private const val NOTIFICATION_ID_ONGOING_CALL = 1002
        
        private const val ACTION_ACCEPT_CALL = "ACTION_ACCEPT_CALL"
        private const val ACTION_REJECT_CALL = "ACTION_REJECT_CALL"
        private const val ACTION_END_CALL = "ACTION_END_CALL"
        
        private const val EXTRA_CALL_ID = "extra_call_id"
        private const val EXTRA_PEER_ID = "extra_peer_id"
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Show incoming call notification
     */
    fun showIncomingCallNotification(
        callId: String,
        peerName: String,
        isVideo: Boolean
    ) {
        val callType = if (isVideo) "Video call" else "Voice call"
        
        val acceptIntent = createCallActionIntent(ACTION_ACCEPT_CALL, callId, peerName)
        val rejectIntent = createCallActionIntent(ACTION_REJECT_CALL, callId, peerName)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_INCOMING_CALLS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Incoming $callType")
            .setContentText("$peerName is calling")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(createFullScreenIntent(callId, peerName, isVideo), true)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Reject",
                PendingIntent.getBroadcast(
                    context,
                    callId.hashCode(),
                    rejectIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Accept",
                PendingIntent.getBroadcast(
                    context,
                    callId.hashCode() + 1,
                    acceptIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_INCOMING_CALL, notification)
        
        coroutineScope.launch {
            _notificationEvents.emit(
                CallNotificationEvent.IncomingCallShown(callId, peerName, isVideo)
            )
        }
    }
    
    /**
     * Show ongoing call notification
     */
    fun showOngoingCallNotification(
        callId: String,
        peerName: String,
        isVideo: Boolean,
        duration: String
    ) {
        val callType = if (isVideo) "Video call" else "Voice call"
        
        val endCallIntent = createCallActionIntent(ACTION_END_CALL, callId, peerName)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ONGOING_CALLS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$callType in progress")
            .setContentText("$peerName • $duration")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "End call",
                PendingIntent.getBroadcast(
                    context,
                    callId.hashCode(),
                    endCallIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_ONGOING_CALL, notification)
    }
    
    /**
     * Show call ended notification
     */
    fun showCallEndedNotification(
        peerName: String,
        duration: String?,
        reason: String?
    ) {
        val contentText = when {
            duration != null -> "Call duration: $duration"
            reason != null -> "Call ended: $reason"
            else -> "Call ended"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ONGOING_CALLS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Call with $peerName")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        
        // Clear ongoing call notification
        clearOngoingCallNotification()
    }
    
    /**
     * Show call missed notification
     */
    fun showMissedCallNotification(
        peerName: String,
        isVideo: Boolean,
        timestamp: Long
    ) {
        val callType = if (isVideo) "video call" else "voice call"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_INCOMING_CALLS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Missed $callType")
            .setContentText("$peerName called")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setWhen(timestamp)
            .setShowWhen(true)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    /**
     * Clear incoming call notification
     */
    fun clearIncomingCallNotification() {
        notificationManager.cancel(NOTIFICATION_ID_INCOMING_CALL)
        
        coroutineScope.launch {
            _notificationEvents.emit(CallNotificationEvent.IncomingCallCleared)
        }
    }
    
    /**
     * Clear ongoing call notification
     */
    fun clearOngoingCallNotification() {
        notificationManager.cancel(NOTIFICATION_ID_ONGOING_CALL)
    }
    
    /**
     * Clear all call notifications
     */
    fun clearAllCallNotifications() {
        clearIncomingCallNotification()
        clearOngoingCallNotification()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val incomingCallsChannel = NotificationChannel(
                CHANNEL_ID_INCOMING_CALLS,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming calls"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val ongoingCallsChannel = NotificationChannel(
                CHANNEL_ID_ONGOING_CALLS,
                "Ongoing Calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for ongoing calls"
                setShowBadge(false)
            }
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(incomingCallsChannel)
            manager.createNotificationChannel(ongoingCallsChannel)
        }
    }
    
    private fun createCallActionIntent(action: String, callId: String, peerName: String): Intent {
        return Intent(action).apply {
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_PEER_ID, peerName)
        }
    }
    
    private fun createFullScreenIntent(callId: String, peerName: String, isVideo: Boolean): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_PEER_ID, peerName)
            putExtra("is_video", isVideo)
        }
        
        return PendingIntent.getActivity(
            context,
            callId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/**
 * Call notification events
 */
sealed class CallNotificationEvent {
    data class IncomingCallShown(
        val callId: String,
        val peerName: String,
        val isVideo: Boolean
    ) : CallNotificationEvent()
    
    object IncomingCallCleared : CallNotificationEvent()
    
    data class OngoingCallShown(
        val callId: String,
        val peerName: String,
        val isVideo: Boolean,
        val duration: String
    ) : CallNotificationEvent()
    
    data class CallEndedShown(
        val peerName: String,
        val duration: String?,
        val reason: String?
    ) : CallNotificationEvent()
    
    data class MissedCallShown(
        val peerName: String,
        val isVideo: Boolean,
        val timestamp: Long
    ) : CallNotificationEvent()
}

/**
 * Broadcast receiver for call actions
 */
class CallActionReceiver : android.content.BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle call notification actions
        if (context == null || intent == null) return
        
        val action = intent.action
        val callId = intent.getStringExtra("call_id") ?: return
        
        when (action) {
            "ACCEPT_CALL" -> {
                // Handle call acceptance
                val acceptIntent = Intent(context, com.chain.messaging.presentation.MainActivity::class.java).apply {
                    putExtra("action", "accept_call")
                    putExtra("call_id", callId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(acceptIntent)
            }
            "DECLINE_CALL" -> {
                // Handle call decline
                val declineIntent = Intent(context, com.chain.messaging.presentation.MainActivity::class.java).apply {
                    putExtra("action", "decline_call")
                    putExtra("call_id", callId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(declineIntent)
            }
            "END_CALL" -> {
                // Handle call end
                val endIntent = Intent(context, com.chain.messaging.presentation.MainActivity::class.java).apply {
                    putExtra("action", "end_call")
                    putExtra("call_id", callId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(endIntent)
            }
        }
    }
}