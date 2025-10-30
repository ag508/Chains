package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.chain.messaging.domain.model.CallNotification
import com.chain.messaging.domain.model.CallNotificationType

/**
 * Room entity for storing call notifications locally.
 */
@Entity(
    tableName = "call_notifications",
    indices = [
        Index(value = ["callId"]),
        Index(value = ["peerId"]),
        Index(value = ["timestamp"])
    ]
)
data class CallNotificationEntity(
    @PrimaryKey
    val id: String,
    val callId: String,
    val type: String,
    val peerName: String,
    val peerId: String,
    val timestamp: Long,
    val isVideo: Boolean,
    val duration: Long?
)

/**
 * Extension function to convert CallNotificationEntity to domain CallNotification model
 */
fun CallNotificationEntity.toDomain(): CallNotification {
    return CallNotification(
        id = id,
        callId = callId,
        type = CallNotificationType.valueOf(type),
        peerName = peerName,
        peerId = peerId,
        timestamp = timestamp,
        isVideo = isVideo,
        duration = duration
    )
}

/**
 * Extension function to convert domain CallNotification to CallNotificationEntity
 */
fun CallNotification.toEntity(): CallNotificationEntity {
    return CallNotificationEntity(
        id = id,
        callId = callId,
        type = type.name,
        peerName = peerName,
        peerId = peerId,
        timestamp = timestamp,
        isVideo = isVideo,
        duration = duration
    )
}
