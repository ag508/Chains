package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Database entity for registered devices
 */
@Entity(tableName = "registered_devices")
data class RegisteredDeviceEntity(
    @PrimaryKey
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val platform: String,
    val platformVersion: String,
    val appVersion: String,
    val publicKey: String,
    val lastSeen: LocalDateTime,
    val registeredAt: LocalDateTime,
    val isTrusted: Boolean,
    val lastSyncAt: LocalDateTime? = null,
    val syncStatus: String,
    val isCurrentDevice: Boolean = false
)