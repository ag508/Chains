package com.chain.messaging.domain.model

import java.util.Date

/**
 * Domain model representing a chat/conversation in the Chain messaging system.
 */
data class Chat(
    val id: String,
    val type: ChatType,
    val name: String,
    val participants: List<String>,
    val admins: List<String> = emptyList(),
    val settings: ChatSettings = ChatSettings(),
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessageAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val isPinned: Boolean = false
) {
    // Legacy constructor for Date compatibility
    constructor(
        id: String,
        type: ChatType,
        name: String,
        participants: List<String>,
        admins: List<String> = emptyList(),
        settings: ChatSettings = ChatSettings(),
        lastMessage: Message? = null,
        unreadCount: Int = 0,
        createdAt: Date,
        updatedAt: Date
    ) : this(
        id = id,
        type = type,
        name = name,
        participants = participants,
        admins = admins,
        settings = settings,
        lastMessage = lastMessage,
        unreadCount = unreadCount,
        createdAt = createdAt.time,
        updatedAt = updatedAt.time,
        lastMessageAt = updatedAt.time,
        isArchived = false,
        isPinned = false
    )
}

enum class ChatType {
    DIRECT,
    GROUP
}

data class ChatSettings(
    val isNotificationsEnabled: Boolean = true,
    val disappearingMessagesTimer: Long? = null, // in milliseconds
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false
)

/**
 * Extended model for group chats with additional group-specific properties.
 */
data class GroupChat(
    val chat: Chat,
    val maxMembers: Int = 100000,
    val inviteLink: String? = null,
    val permissions: GroupPermissions = GroupPermissions(),
    val description: String? = null
)

data class GroupPermissions(
    val canAddMembers: Boolean = true,
    val canEditGroupInfo: Boolean = false,
    val canSendMessages: Boolean = true,
    val adminOnly: Boolean = false
)