package com.chain.messaging.core.security

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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertNotificationServiceImpl @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat
) : AlertNotificationService {
    
    companion object {
        private const val SECURITY_CHANNEL_ID = "security_alerts"
        private const val SECURITY_CHANNEL_NAME = "Security Alerts"
        private const val SECURITY_CHANNEL_DESCRIPTION = "Important security notifications"
        private const val NOTIFICATION_ID_BASE = 10000
    }
    
    private var notificationPreferences = AlertNotificationPreferences()
    
    init {
        createNotificationChannel()
    }
    
    override suspend fun sendAlert(alert: SecurityAlert) {
        if (!shouldSendNotification(alert)) {
            return
        }
        
        if (notificationPreferences.enablePushNotifications) {
            sendPushNotification(alert)
        }
        
        // In-app alerts would be handled by the UI layer
        // Email alerts would be handled by a separate email service
    }
    
    override suspend fun sendAlerts(alerts: List<SecurityAlert>) {
        for (alert in alerts) {
            sendAlert(alert)
        }
    }
    
    override suspend fun cancelAlert(alertId: String) {
        val notificationId = alertId.hashCode()
        notificationManager.cancel(notificationId)
    }
    
    override suspend fun updateNotificationPreferences(preferences: AlertNotificationPreferences) {
        this.notificationPreferences = preferences
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SECURITY_CHANNEL_ID,
                SECURITY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = SECURITY_CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun sendPushNotification(alert: SecurityAlert) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("alert_id", alert.id)
            putExtra("show_security_alerts", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, SECURITY_CHANNEL_ID)
            .setSmallIcon(getNotificationIcon(alert.severity))
            .setContentTitle(alert.title)
            .setContentText(alert.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
            .setPriority(getNotificationPriority(alert.severity))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .apply {
                // Add action buttons for high severity alerts
                if (alert.severity >= SecuritySeverity.HIGH && alert.recommendedActions.isNotEmpty()) {
                    val actionIntent = Intent(context, SecurityActionReceiver::class.java).apply {
                        putExtra("alert_id", alert.id)
                        putExtra("action", alert.recommendedActions.first())
                    }
                    val actionPendingIntent = PendingIntent.getBroadcast(
                        context,
                        alert.id.hashCode() + 1,
                        actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    addAction(
                        R.drawable.ic_security,
                        "Take Action",
                        actionPendingIntent
                    )
                }
                
                // Add dismiss action
                val dismissIntent = Intent(context, SecurityActionReceiver::class.java).apply {
                    putExtra("alert_id", alert.id)
                    putExtra("action", "dismiss")
                }
                val dismissPendingIntent = PendingIntent.getBroadcast(
                    context,
                    alert.id.hashCode() + 2,
                    dismissIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                addAction(
                    R.drawable.ic_close,
                    "Dismiss",
                    dismissPendingIntent
                )
            }
            .build()
        
        val notificationId = alert.id.hashCode()
        notificationManager.notify(notificationId, notification)
    }
    
    private fun shouldSendNotification(alert: SecurityAlert): Boolean {
        // Check minimum severity
        if (alert.severity < notificationPreferences.minimumSeverity) {
            return false
        }
        
        // Check quiet hours
        if (notificationPreferences.quietHoursEnabled && isInQuietHours()) {
            // Only send critical alerts during quiet hours
            return alert.severity == SecuritySeverity.CRITICAL
        }
        
        return true
    }
    
    private fun isInQuietHours(): Boolean {
        if (!notificationPreferences.quietHoursEnabled) {
            return false
        }
        
        val now = LocalTime.now()
        val startTime = LocalTime.parse(notificationPreferences.quietHoursStart, DateTimeFormatter.ofPattern("HH:mm"))
        val endTime = LocalTime.parse(notificationPreferences.quietHoursEnd, DateTimeFormatter.ofPattern("HH:mm"))
        
        return if (startTime.isBefore(endTime)) {
            // Same day quiet hours (e.g., 14:00 to 18:00)
            now.isAfter(startTime) && now.isBefore(endTime)
        } else {
            // Overnight quiet hours (e.g., 22:00 to 08:00)
            now.isAfter(startTime) || now.isBefore(endTime)
        }
    }
    
    private fun getNotificationIcon(severity: SecuritySeverity): Int {
        return when (severity) {
            SecuritySeverity.CRITICAL -> R.drawable.ic_security_critical
            SecuritySeverity.HIGH -> R.drawable.ic_security_high
            SecuritySeverity.MEDIUM -> R.drawable.ic_security_medium
            SecuritySeverity.LOW -> R.drawable.ic_security_low
        }
    }
    
    private fun getNotificationPriority(severity: SecuritySeverity): Int {
        return when (severity) {
            SecuritySeverity.CRITICAL -> NotificationCompat.PRIORITY_MAX
            SecuritySeverity.HIGH -> NotificationCompat.PRIORITY_HIGH
            SecuritySeverity.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            SecuritySeverity.LOW -> NotificationCompat.PRIORITY_LOW
        }
    }
}

// Placeholder for security action receiver
class SecurityActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle security alert actions
        val alertId = intent?.getStringExtra("alert_id")
        val action = intent?.getStringExtra("action")
        
        // Implementation would handle the specific action
        // e.g., navigate to security settings, acknowledge alert, etc.
    }
}