package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.Reaction
import com.chain.messaging.domain.repository.MessageRepository
import javax.inject.Inject

/**
 * Use case for adding reactions to messages
 * Implements Requirements: 4.7
 */
class AddReactionUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    
    /**
     * Add or remove a reaction to/from a message
     * If the user has already reacted with the same emoji, it removes the reaction
     * Otherwise, it adds the new reaction
     */
    suspend operator fun invoke(
        messageId: String,
        userId: String,
        emoji: String
    ): Result<Unit> {
        return try {
            messageRepository.addReaction(messageId, userId, emoji)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}