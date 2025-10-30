package com.chain.messaging.domain.model

import java.util.Date

/**
 * Domain model representing a user in the Chain messaging system.
 * This represents the core business entity for user identity.
 */
data class User(
    val id: String,
    val publicKey: String,
    val displayName: String,
    val avatar: String? = null,
    val status: UserStatus = UserStatus.OFFLINE,
    val lastSeen: Date = Date(),
    val devices: List<Device> = emptyList()
)

enum class UserStatus {
    ONLINE,
    OFFLINE,
    AWAY,
    BUSY
}

data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val lastActive: Date,
    val isCurrentDevice: Boolean = false
)

enum class DeviceType {
    ANDROID,
    IOS,
    DESKTOP,
    WEB
}

/**
 * Extension property to check if user is online
 */
val User.isOnline: Boolean
    get() = status == UserStatus.ONLINE