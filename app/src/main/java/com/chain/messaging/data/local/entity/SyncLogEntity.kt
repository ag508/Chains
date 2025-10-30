package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Database entity for synchronization logs
 */
@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey
    val id: String,
    val deviceId: String,
    val syncType: String,
    val status: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val messagesSynced: Int = 0,
    val keysSynced: Int = 0,
    val settingsSynced: Int = 0,
    val errorMessage: String? = null,
    val retryCount: Int = 0
)