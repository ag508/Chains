package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.CallNotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CallNotification operations
 */
@Dao
interface CallNotificationDao {

    @Query("SELECT * FROM call_notifications ORDER BY timestamp DESC")
    suspend fun getAllCallNotifications(): List<CallNotificationEntity>

    @Query("SELECT * FROM call_notifications WHERE id = :notificationId")
    suspend fun getCallNotificationById(notificationId: String): CallNotificationEntity?

    @Query("SELECT * FROM call_notifications WHERE callId = :callId")
    suspend fun getCallNotificationsByCallId(callId: String): List<CallNotificationEntity>

    @Query("SELECT * FROM call_notifications ORDER BY timestamp DESC")
    fun observeAllCallNotifications(): Flow<List<CallNotificationEntity>>

    @Query("SELECT * FROM call_notifications WHERE callId = :callId")
    fun observeCallNotificationsByCallId(callId: String): Flow<List<CallNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallNotification(notification: CallNotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallNotifications(notifications: List<CallNotificationEntity>)

    @Update
    suspend fun updateCallNotification(notification: CallNotificationEntity)

    @Delete
    suspend fun deleteCallNotification(notification: CallNotificationEntity)

    @Query("DELETE FROM call_notifications WHERE id = :notificationId")
    suspend fun clearCallNotification(notificationId: String)

    @Query("DELETE FROM call_notifications WHERE callId = :callId")
    suspend fun deleteCallNotificationsByCallId(callId: String)

    @Query("DELETE FROM call_notifications")
    suspend fun deleteAllCallNotifications()

    @Query("SELECT COUNT(*) FROM call_notifications")
    suspend fun getCallNotificationCount(): Int

    @Query("SELECT COUNT(*) FROM call_notifications WHERE type = 'MISSED'")
    suspend fun getMissedCallCount(): Int
}
