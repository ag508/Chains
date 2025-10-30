package com.chain.messaging.core.sync

import java.time.LocalDateTime

/**
 * Information about a user device
 */
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val deviceType: DeviceType,
    val platform: String, // Android, iOS, Windows, etc.
    val platformVersion: String,
    val appVersion: String,
    val publicKey: String, // Device's public key for encryption
    val lastSeen: LocalDateTime = LocalDateTime.now(),
    val isCurrentDevice: Boolean = false
)

/**
 * A registered device with additional metadata
 */
data class RegisteredDevice(
    val deviceInfo: DeviceInfo,
    val registeredAt: LocalDateTime,
    val isTrusted: Boolean = false,
    val lastSyncAt: LocalDateTime? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    val isOnline: Boolean
        get() = deviceInfo.lastSeen.isAfter(LocalDateTime.now().minusMinutes(5))
}

/**
 * Types of devices
 */
enum class DeviceType {
    MOBILE,
    TABLET,
    DESKTOP,
    WEB,
    UNKNOWN
}

/**
 * Device synchronization status
 */
enum class SyncStatus {
    PENDING,     // Device registered but not synced
    SYNCING,     // Currently synchronizing
    SYNCED,      // Fully synchronized
    ERROR,       // Sync error occurred
    OFFLINE      // Device is offline
}