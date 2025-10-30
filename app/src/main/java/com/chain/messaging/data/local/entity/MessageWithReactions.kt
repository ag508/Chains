package com.chain.messaging.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.chain.messaging.domain.model.Message

/**
 * Data class representing a message with its reactions
 */
data class MessageWithReactions(
    @Embedded val message: MessageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val reactions: List<ReactionEntity>
)

/**
 * Extension function to convert MessageWithReactions to domain Message model
 */
fun MessageWithReactions.toDomain(): Message {
    return message.toDomain().copy(
        reactions = reactions.map { it.toDomain() }
    )
}