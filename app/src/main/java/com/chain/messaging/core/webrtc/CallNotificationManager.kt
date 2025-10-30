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
 * Enhanced call notification manager that integrates with CallManager
 * Implements requirement 6.2 for call status management and notifications
 */
@Singleton
class CallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val callNotificationService: CallNotificationService
) {
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _notificationEvents = MutableSharedFlow<CallNotificationEvent>()
    val notificationEvents: Flow<CallNotificationEvent> = _notificationEvents.asSharedFlow()
    
    companion object {
        private const val CHANNEL_ID = "call_notifications"
        private const val INCOMING_CALL_NOTIFICATION_ID = 1001
        private const val ONGOING_CALL_NOTIFICATION_ID = 1002
    }
    
    init {
        createNotificationChannel()
        observeNotificationEvents()
    }
    
    /**
     * Start observing notification events from the service
     */
    private fun observeNotificationEvents() {
        coroutineScope.launch {
            callNotificationService.notificationEvents.collect { event ->
                _notificationEvents.emit(event)
            }
        }
    }
    
    /**
     * Handle call state events and manage notifications
     */
    suspend fun handleCallStateEvent(event: CallStateEvent) {
        when (event) {
            is CallStateEvent.IncomingCall -> {
                showIncomingCallNotification(event.callId, event.fromPeerId, event.isVideo)
            }
            is CallStateEvent.CallAccepted -> {
                dismissIncomingCallNotification()
                event.callSession.let { session ->
                    showOngoingCallNotification(session.id, session.peerId, session.isVideo, "00:00")
                }
            }
            is CallStateEvent.CallRejected -> {
                dismissIncomingCallNotification()
            }
            is CallStateEvent.CallEnded -> {
                dismissAllCallNotifications()
                showCallEndedNotification(event.callId, event.reason)
            }
            is CallStateEvent.CallInitiated -> {
                // Handle outgoing call initiated
            }
            else -> {
                // Handle other events as needed
            }
        }
    }
    
    /**
     * Show incoming call notification
     */
    fun showIncomingCallNotification(
        callId: String,
        callerName: String,
        isVideo: Boolean
    ) {
        callNotificationService.showIncomingCallNotification(callId, callerName, isVideo)
    }
    
    /**
     * Show ongoing call notification
     */
    fun showOngoingCallNotification(
        callId: String,
        participantName: String,
        isVideo: Boolean,
        duration: String
    ) {
        callNotificationService.showOngoingCallNotification(callId, participantName, isVideo, duration)
    }
    
    /**
     * Show call ended notification
     */
    fun showCallEndedNotification(callId: String, reason: String?) {
        // Get participant name from call session if available
        val participantName = "Unknown" // In real implementation, get from call session
        val duration: String? = null // In real implementation, calculate duration
        
        callNotificationService.showCallEndedNotification(participantName, duration, reason)
    }
    
    /**
     * Show missed call notification
     */
    fun showMissedCallNotification(peerId: String, isVideo: Boolean, timestamp: Long) {
        callNotificationService.showMissedCallNotification(peerId, isVideo, timestamp)
    }
    
    /**
     * Dismiss incoming call notification
     */
    fun dismissIncomingCallNotification() {
        callNotificationService.clearIncomingCallNotification()
    }
    
    /**
     * Dismiss ongoing call notification
     */
    fun dismissOngoingCallNotification() {
        callNotificationService.clearOngoingCallNotification()
    }
    
    /**
     * Dismiss all call notifications
     */
    fun dismissAllCallNotifications() {
        callNotificationService.clearAllCallNotifications()
    }
    
    /**
     * Format call duration for display
     */
    fun formatCallDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%d:%02d", minutes, seconds)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming and ongoing calls"
                setSound(null, null) // Custom ringtone handling
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createCallActionIntent(callId: String, action: String): PendingIntent {
        val intent = Intent("com.chain.messaging.CALL_ACTION").apply {
            putExtra("call_id", callId)
            putExtra("action", action)
        }
        
        return PendingIntent.getBroadcast(
            context,
            callId.hashCode() + action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createFullScreenIntent(callId: String): PendingIntent {
        val intent = Intent("com.chain.messaging.INCOMING_CALL").apply {
            putExtra("call_id", callId)
        }
        
        return PendingIntent.getActivity(
            context,
            callId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}