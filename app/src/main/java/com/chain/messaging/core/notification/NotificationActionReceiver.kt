package com.chain.messaging.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Broadcast receiver for handling notification actions
 * Implements requirement 4.4 for notification actions (reply, mark as read)
 */
class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_REPLY = "ACTION_REPLY"
        const val ACTION_MARK_READ = "ACTION_MARK_READ"
        const val ACTION_MUTE_CHAT = "ACTION_MUTE_CHAT"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val chatId = intent.getStringExtra("chat_id") ?: return
        
        // Get dependencies through EntryPoint since this is a manifest-registered receiver
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationActionReceiverEntryPoint::class.java
        )
        val notificationActionHandler = entryPoint.getNotificationActionHandler()
        
        when (intent.action) {
            ACTION_REPLY -> {
                val replyText = getMessageText(intent)
                if (replyText != null) {
                    notificationActionHandler.handleReply(chatId, replyText)
                }
            }
            
            ACTION_MARK_READ -> {
                notificationActionHandler.handleMarkRead(chatId)
            }
            
            ACTION_MUTE_CHAT -> {
                notificationActionHandler.handleMuteChat(chatId)
            }
        }
    }
    
    private fun getMessageText(intent: Intent): String? {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        return remoteInput?.getCharSequence(KEY_TEXT_REPLY)?.toString()
    }
}

/**
 * Entry point for accessing dependencies in NotificationActionReceiver
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationActionReceiverEntryPoint {
    fun getNotificationActionHandler(): NotificationActionHandler
}

/**
 * Handler for notification actions
 */
@Singleton
class NotificationActionHandler @Inject constructor() {
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _actionEvents = MutableSharedFlow<NotificationEvent>()
    val actionEvents = _actionEvents.asSharedFlow()
    
    fun handleReply(chatId: String, replyText: String) {
        coroutineScope.launch {
            _actionEvents.emit(
                NotificationEvent.NotificationReply(chatId, replyText)
            )
        }
    }
    
    fun handleMarkRead(chatId: String) {
        coroutineScope.launch {
            _actionEvents.emit(
                NotificationEvent.NotificationMarkRead(chatId)
            )
        }
    }
    
    fun handleMuteChat(chatId: String) {
        coroutineScope.launch {
            _actionEvents.emit(
                NotificationEvent.NotificationMuteChat(chatId)
            )
        }
    }
}