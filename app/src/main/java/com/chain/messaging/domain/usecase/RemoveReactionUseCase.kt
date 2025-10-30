package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.repository.MessageRepository
import javax.inject.Inject

/**
 * Use case for removing reactions from messages
 * Implements Requirements: 4.7
 */
class RemoveReactionUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    
    /**
     * Remove a specific reaction from a message
     */
    suspend operator fun invoke(
        messageId: String,
        userId: String,
        emoji: String
    ): Result<Unit> {
        return try {
            messageRepository.removeReaction(messageId, userId, emoji)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}