package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chain.messaging.core.util.TimeUtils
import com.chain.messaging.core.util.toLong
import com.chain.messaging.core.util.toDate
import com.chain.messaging.domain.model.User
import com.chain.messaging.domain.model.UserStatus
import java.util.Date

/**
 * Room entity for storing user data locally.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val publicKey: String,
    val displayName: String,
    val avatar: String?,
    val status: String,
    val lastSeen: Long,
    val createdAt: Long = TimeUtils.getCurrentTimestamp(),
    val updatedAt: Long = TimeUtils.getCurrentTimestamp()
)

/**
 * Extension function to convert UserEntity to domain User model
 */
fun UserEntity.toDomain(): User {
    return User(
        id = id,
        publicKey = publicKey,
        displayName = displayName,
        avatar = avatar,
        status = UserStatus.valueOf(status),
        lastSeen = lastSeen.toDate(),
        devices = emptyList() // Devices will be loaded separately if needed
    )
}

/**
 * Extension function to convert domain User model to UserEntity
 */
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        publicKey = publicKey,
        displayName = displayName,
        avatar = avatar,
        status = status.name,
        lastSeen = lastSeen.toLong(),
        updatedAt = TimeUtils.getCurrentTimestamp()
    )
}