package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import com.chain.messaging.core.util.TimeUtils
import com.chain.messaging.core.util.toLong
import com.chain.messaging.core.util.toDate
import com.chain.messaging.domain.model.Reaction
import java.util.Date

/**
 * Room entity for storing message reactions locally.
 */
@Entity(
    tableName = "reactions",
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["userId"]),
        Index(value = ["messageId", "userId", "emoji"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReactionEntity(
    @PrimaryKey
    val id: String,
    val messageId: String,
    val userId: String,
    val emoji: String,
    val timestamp: Long,
    val createdAt: Long = TimeUtils.getCurrentTimestamp()
)

/**
 * Extension function to convert ReactionEntity to domain Reaction model
 */
fun ReactionEntity.toDomain(): Reaction {
    return Reaction(
        userId = userId,
        emoji = emoji,
        timestamp = timestamp.toDate()
    )
}

/**
 * Extension function to convert domain Reaction model to ReactionEntity
 */
fun Reaction.toEntity(messageId: String, id: String = java.util.UUID.randomUUID().toString()): ReactionEntity {
    return ReactionEntity(
        id = id,
        messageId = messageId,
        userId = userId,
        emoji = emoji,
        timestamp = timestamp.toLong()
    )
}