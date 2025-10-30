package com.chain.messaging.core.offline

import com.chain.messaging.domain.model.Message
import java.time.LocalDateTime

/**
 * Interface for resolving message synchronization conflicts
 */
interface ConflictResolver {
    /**
     * Resolve conflicts between local and remote messages
     */
    suspend fun resolveConflicts(
        localMessages: List<Message>,
        remoteMessages: List<Message>
    ): ConflictResolution
    
    /**
     * Resolve conflict for a single message
     */
    suspend fun resolveMessageConflict(
        localMessage: Message,
        remoteMessage: Message
    ): MessageResolution
    
    /**
     * Detect if two messages are in conflict
     */
    fun hasConflict(localMessage: Message, remoteMessage: Message): Boolean
}

/**
 * Result of conflict resolution
 */
data class ConflictResolution(
    val resolvedMessages: List<Message>,
    val conflictsFound: Int,
    val conflictsResolved: Int,
    val unresolvedConflicts: List<MessageConflict>
)

/**
 * Resolution for a single message conflict
 */
data class MessageResolution(
    val resolvedMessage: Message,
    val strategy: ResolutionStrategy,
    val wasConflict: Boolean
)

/**
 * Represents a message conflict
 */
data class MessageConflict(
    val localMessage: Message,
    val remoteMessage: Message,
    val conflictType: ConflictType,
    val detectedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Types of message conflicts
 */
enum class ConflictType {
    CONTENT_MISMATCH,    // Same message ID but different content
    TIMESTAMP_CONFLICT,  // Same message but different timestamps
    STATUS_CONFLICT,     // Different message status
    DUPLICATE_MESSAGE,   // Same message exists in both local and remote
    ORDERING_CONFLICT    // Messages have conflicting order
}

/**
 * Strategies for resolving conflicts
 */
enum class ResolutionStrategy {
    PREFER_LOCAL,        // Use local version
    PREFER_REMOTE,       // Use remote version
    PREFER_LATEST,       // Use version with latest timestamp
    MERGE_CONTENT,       // Merge content from both versions
    CREATE_DUPLICATE,    // Keep both versions as separate messages
    MANUAL_RESOLUTION    // Requires user intervention
}