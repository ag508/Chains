package com.chain.messaging.core.offline

import com.chain.messaging.core.util.TimeUtils
import com.chain.messaging.core.util.isAfter
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConflictResolverImpl @Inject constructor() : ConflictResolver {
    
    override suspend fun resolveConflicts(
        localMessages: List<Message>,
        remoteMessages: List<Message>
    ): ConflictResolution {
        val conflicts = mutableListOf<MessageConflict>()
        val resolvedMessages = mutableListOf<Message>()
        val processedMessageIds = mutableSetOf<String>()
        
        // Create maps for efficient lookup
        val localMessageMap = localMessages.associateBy { it.id }
        val remoteMessageMap = remoteMessages.associateBy { it.id }
        
        // Process messages that exist in both local and remote
        for (localMessage in localMessages) {
            val remoteMessage = remoteMessageMap[localMessage.id]
            
            if (remoteMessage != null) {
                // Message exists in both - check for conflicts
                if (hasConflict(localMessage, remoteMessage)) {
                    val conflict = MessageConflict(
                        localMessage = localMessage,
                        remoteMessage = remoteMessage,
                        conflictType = determineConflictType(localMessage, remoteMessage)
                    )
                    conflicts.add(conflict)
                    
                    val resolution = resolveMessageConflict(localMessage, remoteMessage)
                    resolvedMessages.add(resolution.resolvedMessage)
                } else {
                    // No conflict - use remote version (it's more authoritative)
                    resolvedMessages.add(remoteMessage)
                }
                processedMessageIds.add(localMessage.id)
            } else {
                // Message only exists locally - keep it
                resolvedMessages.add(localMessage)
                processedMessageIds.add(localMessage.id)
            }
        }
        
        // Add remote messages that don't exist locally
        for (remoteMessage in remoteMessages) {
            if (!processedMessageIds.contains(remoteMessage.id)) {
                resolvedMessages.add(remoteMessage)
            }
        }
        
        // Sort messages by timestamp to maintain order
        val sortedMessages = resolvedMessages.sortedBy { it.timestamp }
        
        return ConflictResolution(
            resolvedMessages = sortedMessages,
            conflictsFound = conflicts.size,
            conflictsResolved = conflicts.count { it.conflictType != ConflictType.ORDERING_CONFLICT },
            unresolvedConflicts = conflicts.filter { it.conflictType == ConflictType.ORDERING_CONFLICT }
        )
    }
    
    override suspend fun resolveMessageConflict(
        localMessage: Message,
        remoteMessage: Message
    ): MessageResolution {
        val conflictType = determineConflictType(localMessage, remoteMessage)
        val strategy = determineResolutionStrategy(conflictType, localMessage, remoteMessage)
        
        val resolvedMessage = when (strategy) {
            ResolutionStrategy.PREFER_LOCAL -> localMessage
            ResolutionStrategy.PREFER_REMOTE -> remoteMessage
            ResolutionStrategy.PREFER_LATEST -> {
                if (localMessage.timestamp.isAfter(remoteMessage.timestamp)) {
                    localMessage
                } else {
                    remoteMessage
                }
            }
            ResolutionStrategy.MERGE_CONTENT -> mergeMessages(localMessage, remoteMessage)
            ResolutionStrategy.CREATE_DUPLICATE -> {
                // For now, prefer remote and mark local as duplicate
                remoteMessage.copy(
                    content = "${remoteMessage.content} [Merged from local: ${localMessage.content}]"
                )
            }
            ResolutionStrategy.MANUAL_RESOLUTION -> {
                // For automated resolution, prefer remote
                remoteMessage
            }
        }
        
        return MessageResolution(
            resolvedMessage = resolvedMessage,
            strategy = strategy,
            wasConflict = true
        )
    }
    
    override fun hasConflict(localMessage: Message, remoteMessage: Message): Boolean {
        return localMessage.id == remoteMessage.id && (
            localMessage.content != remoteMessage.content ||
            localMessage.status != remoteMessage.status ||
            localMessage.timestamp != remoteMessage.timestamp
        )
    }
    
    private fun determineConflictType(localMessage: Message, remoteMessage: Message): ConflictType {
        return when {
            localMessage.content != remoteMessage.content -> ConflictType.CONTENT_MISMATCH
            localMessage.timestamp != remoteMessage.timestamp -> ConflictType.TIMESTAMP_CONFLICT
            localMessage.status != remoteMessage.status -> ConflictType.STATUS_CONFLICT
            else -> ConflictType.DUPLICATE_MESSAGE
        }
    }
    
    private fun determineResolutionStrategy(
        conflictType: ConflictType,
        localMessage: Message,
        remoteMessage: Message
    ): ResolutionStrategy {
        return when (conflictType) {
            ConflictType.CONTENT_MISMATCH -> {
                // If local message is still sending, prefer remote
                if (localMessage.status == MessageStatus.SENDING) {
                    ResolutionStrategy.PREFER_REMOTE
                } else {
                    ResolutionStrategy.PREFER_LATEST
                }
            }
            ConflictType.TIMESTAMP_CONFLICT -> ResolutionStrategy.PREFER_REMOTE
            ConflictType.STATUS_CONFLICT -> {
                // Prefer the message with more advanced status
                if (getStatusPriority(remoteMessage.status) > getStatusPriority(localMessage.status)) {
                    ResolutionStrategy.PREFER_REMOTE
                } else {
                    ResolutionStrategy.PREFER_LOCAL
                }
            }
            ConflictType.DUPLICATE_MESSAGE -> ResolutionStrategy.PREFER_REMOTE
            ConflictType.ORDERING_CONFLICT -> ResolutionStrategy.PREFER_LATEST
        }
    }
    
    private fun getStatusPriority(status: MessageStatus): Int {
        return when (status) {
            MessageStatus.SENDING -> 0
            MessageStatus.SENT -> 1
            MessageStatus.DELIVERED -> 2
            MessageStatus.READ -> 3
            MessageStatus.FAILED -> -1
        }
    }
    
    private fun mergeMessages(localMessage: Message, remoteMessage: Message): Message {
        // Simple merge strategy - combine content and use latest timestamp
        val mergedContent = if (localMessage.content != remoteMessage.content) {
            "${remoteMessage.content} [Local: ${localMessage.content}]"
        } else {
            remoteMessage.content
        }
        
        return remoteMessage.copy(
            content = mergedContent,
            timestamp = if (localMessage.timestamp.isAfter(remoteMessage.timestamp)) {
                localMessage.timestamp
            } else {
                remoteMessage.timestamp
            }
        )
    }
}