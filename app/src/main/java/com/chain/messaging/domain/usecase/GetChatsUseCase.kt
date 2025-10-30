package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all chats for the current user.
 * Demonstrates Clean Architecture pattern with business logic separation.
 */
class GetChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    
    suspend fun execute(): List<Chat> {
        return chatRepository.getChats()
    }
    
    fun observeChats(): Flow<List<Chat>> {
        return chatRepository.observeChats()
    }
}