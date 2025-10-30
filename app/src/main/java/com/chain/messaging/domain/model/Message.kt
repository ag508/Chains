package com.chain.messaging.domain.model

import java.util.Date

/**
 * Domain model representing a message in the Chain messaging system.
 */
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val type: MessageType,
    val timestamp: Date,
    val status: MessageStatus,
    val replyTo: String? = null,
    val reactions: List<Reaction> = emptyList(),
    val isEncrypted: Boolean = true,
    val disappearingMessageTimer: Long? = null, // Timer in milliseconds, null means no expiration
    val expiresAt: Date? = null, // When the message should be deleted
    val isDisappearing: Boolean = false
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    LOCATION,
    CONTACT,
    POLL,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}