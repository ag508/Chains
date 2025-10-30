package com.chain.messaging.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for notification channels
 * Handles creation and customization of notification channels
 */
@Singleton
class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val CHANNEL_ID_MESSAGES = "messages"
        const val CHANNEL_ID_CALLS = "calls"
        const val CHANNEL_ID_GROUPS = "groups"
        const val CHANNEL_ID_SYSTEM = "system"
        const val CHANNEL_ID_SECURITY = "security"
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * Create all notification channels
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                createMessagesChannel(),
                createCallsChannel(),
                createGroupsChannel(),
                createSystemChannel(),
                createSecurityChannel()
            )
            
            notificationManager.createNotificationChannels(channels)
        }
    }
    
    /**
     * Update notification channel settings
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateChannelSettings(
        channelId: String,
        soundUri: Uri? = null,
        vibrationPattern: LongArray? = null,
        enableLights: Boolean = true,
        lightColor: Int = android.graphics.Color.BLUE
    ) {
        val channel = notificationManager.getNotificationChannel(channelId)
        channel?.let {
            soundUri?.let { uri ->
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                it.setSound(uri, audioAttributes)
            }
            
            vibrationPattern?.let { pattern ->
                it.vibrationPattern = pattern
                it.enableVibration(true)
            }
            
            it.enableLights(enableLights)
            if (enableLights) {
                it.lightColor = lightColor
            }
            
            notificationManager.createNotificationChannel(it)
        }
    }
    
    /**
     * Get notification channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotificationChannel(channelId: String): NotificationChannel? {
        return notificationManager.getNotificationChannel(channelId)
    }
    
    /**
     * Delete notification channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteNotificationChannel(channelId: String) {
        notificationManager.deleteNotificationChannel(channelId)
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMessagesChannel(): NotificationChannel {
        return NotificationChannel(
            CHANNEL_ID_MESSAGES,
            "Messages",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new direct messages"
            enableVibration(true)
            enableLights(true)
            lightColor = android.graphics.Color.BLUE
            vibrationPattern = longArrayOf(0, 250, 250, 250)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createCallsChannel(): NotificationChannel {
        return NotificationChannel(
            CHANNEL_ID_CALLS,
            "Calls",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for incoming calls and missed calls"
            enableVibration(true)
            enableLights(true)
            lightColor = android.graphics.Color.GREEN
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
            )
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createGroupsChannel(): NotificationChannel {
        return NotificationChannel(
            CHANNEL_ID_GROUPS,
            "Group Messages",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for group messages"
            enableVibration(true)
            enableLights(true)
            lightColor = android.graphics.Color.CYAN
            vibrationPattern = longArrayOf(0, 150, 100, 150)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSystemChannel(): NotificationChannel {
        return NotificationChannel(
            CHANNEL_ID_SYSTEM,
            "System",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "System notifications and status updates"
            enableVibration(false)
            enableLights(false)
            setSound(null, null)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSecurityChannel(): NotificationChannel {
        return NotificationChannel(
            CHANNEL_ID_SECURITY,
            "Security Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Security alerts and warnings"
            enableVibration(true)
            enableLights(true)
            lightColor = android.graphics.Color.RED
            vibrationPattern = longArrayOf(0, 300, 100, 300, 100, 300)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        }
    }
}