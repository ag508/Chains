package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "security_events")
data class SecurityEventEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val timestamp: LocalDateTime,
    val severity: String,
    val description: String,
    val metadata: String,
    val userId: String?,
    val deviceId: String?,
    val ipAddress: String?,
    val isAcknowledged: Boolean = false
)