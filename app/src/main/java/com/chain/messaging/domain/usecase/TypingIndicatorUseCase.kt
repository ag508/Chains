package com.chain.messaging.domain.usecase

import com.chain.messaging.core.messaging.TypingIndicatorService
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Use case for managing typing indicators
 */
class TypingIndicatorUseCase @Inject constructor(
    private val typingIndicatorService: TypingIndicatorService
) {
    
    /**
     * Starts typing indicator for a user in a chat
     */
    fun startTyping(chatId: String, userId: String) {
        typingIndicatorService.startTyping(chatId, userId)
    }
    
    /**
     * Stops typing indicator for a user in a chat
     */
    fun stopTyping(chatId: String, userId: String) {
        typingIndicatorService.stopTyping(chatId, userId)
    }
    
    /**
     * Gets users currently typing in a chat
     */
    fun getTypingUsers(chatId: String): Set<String> {
        return typingIndicatorService.getTypingUsers(chatId)
    }
    
    /**
     * Observes typing users across all chats
     */
    fun observeTypingUsers(): StateFlow<Map<String, Set<String>>> {
        return typingIndicatorService.typingUsers
    }
    
    /**
     * Sets user online status
     */
    fun setUserOnline(userId: String, isOnline: Boolean) {
        typingIndicatorService.setUserOnline(userId, isOnline)
    }
    
    /**
     * Checks if user is online
     */
    fun isUserOnline(userId: String): Boolean {
        return typingIndicatorService.isUserOnline(userId)
    }
    
    /**
     * Observes online users
     */
    fun observeOnlineUsers(): StateFlow<Map<String, Boolean>> {
        return typingIndicatorService.onlineUsers
    }
}