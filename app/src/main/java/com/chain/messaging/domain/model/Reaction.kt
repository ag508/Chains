package com.chain.messaging.domain.model

import java.util.Date

/**
 * Domain model representing a message reaction
 */
data class Reaction(
    val userId: String,
    val emoji: String,
    val timestamp: Date
)