package com.chain.messaging.domain.repository

import com.chain.messaging.domain.model.CallNotification
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for call notification operations
 */
interface CallNotificationRepository {

    /**
     * Get all call notifications
     */
    suspend fun getAllCallNotifications(): List<CallNotification>

    /**
     * Get call notifications as Flow for reactive updates
     */
    fun observeCallNotifications(): Flow<List<CallNotification>>

    /**
     * Save a call notification
     */
    suspend fun saveCallNotification(notification: CallNotification): Result<Unit>

    /**
     * Clear/delete a specific call notification
     */
    suspend fun clearCallNotification(notificationId: String): Result<Unit>

    /**
     * Delete all call notifications
     */
    suspend fun clearAllCallNotifications(): Result<Unit>

    /**
     * Get missed call count
     */
    suspend fun getMissedCallCount(): Int
}
