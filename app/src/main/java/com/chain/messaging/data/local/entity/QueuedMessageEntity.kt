package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "queued_messages")
data class QueuedMessageEntity(
    @PrimaryKey
    val id: String,
    val messageId: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val messageType: String,
    val queuedAt: LocalDateTime,
    val retryCount: Int = 0,
    val lastRetryAt: LocalDateTime? = null,
    val priority: String = "NORMAL",
    val maxRetries: Int = 5
)