package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chain.messaging.core.util.toLong
import com.chain.messaging.core.util.toDate
import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatSettings
import com.chain.messaging.domain.model.ChatType
import java.util.Date

/**
 * Room entity for storing chat data locally.
 */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val name: String,
    val participants: String, // JSON string of participant IDs
    val admins: String, // JSON string of admin IDs
    val isNotificationsEnabled: Boolean,
    val disappearingMessagesTimer: Long?,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val unreadCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Extension function to convert ChatEntity to domain Chat model
 */
fun ChatEntity.toDomain(participantsList: List<String>, adminsList: List<String>): Chat {
    return Chat(
        id = id,
        type = ChatType.valueOf(type),
        name = name,
        participants = participantsList,
        admins = adminsList,
        settings = ChatSettings(
            isNotificationsEnabled = isNotificationsEnabled,
            disappearingMessagesTimer = disappearingMessagesTimer,
            isArchived = isArchived,
            isPinned = isPinned,
            isMuted = isMuted
        ),
        lastMessage = null, // Will be loaded separately
        unreadCount = unreadCount,
        createdAt = createdAt.toDate(),
        updatedAt = updatedAt.toDate()
    )
}

/**
 * Extension function to convert domain Chat model to ChatEntity
 */
fun Chat.toEntity(participantsJson: String, adminsJson: String): ChatEntity {
    return ChatEntity(
        id = id,
        type = type.name,
        name = name,
        participants = participantsJson,
        admins = adminsJson,
        isNotificationsEnabled = settings.isNotificationsEnabled,
        disappearingMessagesTimer = settings.disappearingMessagesTimer,
        isArchived = settings.isArchived,
        isPinned = settings.isPinned,
        isMuted = settings.isMuted,
        unreadCount = unreadCount,
        createdAt = createdAt.toLong(),
        updatedAt = updatedAt.toLong()
    )
}