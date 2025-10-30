package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.Reaction
import com.chain.messaging.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting reactions for messages
 * Implements Requirements: 4.7
 */
class GetReactionsUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    
    /**
     * Get all reactions for a specific message
     */
    suspend fun getReactions(messageId: String): List<Reaction> {
        return try {
            messageRepository.getReactions(messageId)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Observe reactions for a specific message
     */
    fun observeReactions(messageId: String): Flow<List<Reaction>> {
        return messageRepository.observeReactions(messageId)
    }
    
    /**
     * Check if a user has reacted to a message with a specific emoji
     */
    suspend fun hasUserReacted(messageId: String, userId: String, emoji: String): Boolean {
        return try {
            messageRepository.hasUserReacted(messageId, userId, emoji)
        } catch (e: Exception) {
            false
        }
    }
}