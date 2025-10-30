package com.chain.messaging.core.security

import com.chain.messaging.domain.model.SecurityAlert
import com.chain.messaging.domain.model.SecuritySeverity

/**
 * Service for sending security alert notifications to users
 */
interface AlertNotificationService {
    
    /**
     * Initializes the notification service
     */
    suspend fun initialize() {
        // Default implementation - can be overridden
    }
    
    /**
     * Sends a security alert notification
     */
    suspend fun sendAlert(alert: SecurityAlert)
    
    /**
     * Sends a batch of security alerts
     */
    suspend fun sendAlerts(alerts: List<SecurityAlert>)
    
    /**
     * Cancels a specific alert notification
     */
    suspend fun cancelAlert(alertId: String)
    
    /**
     * Updates notification preferences
     */
    suspend fun updateNotificationPreferences(preferences: AlertNotificationPreferences)
}

/**
 * Notification preferences for security alerts
 */
data class AlertNotificationPreferences(
    val enablePushNotifications: Boolean = true,
    val enableInAppAlerts: Boolean = true,
    val enableEmailAlerts: Boolean = false,
    val minimumSeverity: SecuritySeverity = SecuritySeverity.MEDIUM,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00"
)