package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.chain.messaging.core.util.TimeUtils
import com.chain.messaging.core.util.toLong
import com.chain.messaging.core.util.toDate
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import java.util.Date

/**
 * Room entity for storing messages locally.
 */
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["chatId"]),
        Index(value = ["senderId"]),
        Index(value = ["timestamp"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val type: String,
    val timestamp: Long,
    val status: String,
    val replyTo: String?,
    val isEncrypted: Boolean,
    val disappearingMessageTimer: Long?,
    val expiresAt: Long?,
    val isDisappearing: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension function to convert MessageEntity to domain Message model
 */
fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        type = MessageType.valueOf(type),
        timestamp = timestamp.toDate(),
        status = MessageStatus.valueOf(status),
        replyTo = replyTo,
        reactions = emptyList(), // Reactions will be loaded separately
        isEncrypted = isEncrypted,
        disappearingMessageTimer = disappearingMessageTimer,
        expiresAt = expiresAt?.toDate(),
        isDisappearing = isDisappearing
    )
}

/**
 * Extension function to convert domain Message model to MessageEntity
 */
fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        type = type.name,
        timestamp = timestamp.toLong(),
        status = status.name,
        replyTo = replyTo,
        isEncrypted = isEncrypted,
        disappearingMessageTimer = disappearingMessageTimer,
        expiresAt = expiresAt?.toLong(),
        isDisappearing = isDisappearing,
        updatedAt = TimeUtils.getCurrentTimestamp()
    )
}