package com.chain.messaging.core.messaging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing typing indicators and online status
 */
@Singleton
class TypingIndicatorService @Inject constructor() {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val typingJobs = ConcurrentHashMap<String, Job>()
    
    // Map of chatId to set of userIds who are typing
    private val _typingUsers = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val typingUsers: StateFlow<Map<String, Set<String>>> = _typingUsers.asStateFlow()
    
    // Map of userId to their online status
    private val _onlineUsers = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val onlineUsers: StateFlow<Map<String, Boolean>> = _onlineUsers.asStateFlow()
    
    // Map of userId to their last seen timestamp
    private val _lastSeenUsers = MutableStateFlow<Map<String, Long>>(emptyMap())
    val lastSeenUsers: StateFlow<Map<String, Long>> = _lastSeenUsers.asStateFlow()
    
    companion object {
        private const val TYPING_TIMEOUT_MS = 3000L // 3 seconds
        private const val ONLINE_TIMEOUT_MS = 30000L // 30 seconds
    }
    
    /**
     * Indicates that a user started typing in a chat
     */
    fun startTyping(chatId: String, userId: String) {
        // Cancel existing typing job for this user in this chat
        val jobKey = "$chatId:$userId"
        typingJobs[jobKey]?.cancel()
        
        // Add user to typing list
        val currentTyping = _typingUsers.value.toMutableMap()
        val chatTypingUsers = currentTyping[chatId]?.toMutableSet() ?: mutableSetOf()
        chatTypingUsers.add(userId)
        currentTyping[chatId] = chatTypingUsers
        _typingUsers.value = currentTyping
        
        // Start timeout job
        typingJobs[jobKey] = scope.launch {
            delay(TYPING_TIMEOUT_MS)
            stopTyping(chatId, userId)
        }
    }
    
    /**
     * Indicates that a user stopped typing in a chat
     */
    fun stopTyping(chatId: String, userId: String) {
        // Cancel typing job
        val jobKey = "$chatId:$userId"
        typingJobs[jobKey]?.cancel()
        typingJobs.remove(jobKey)
        
        // Remove user from typing list
        val currentTyping = _typingUsers.value.toMutableMap()
        val chatTypingUsers = currentTyping[chatId]?.toMutableSet()
        if (chatTypingUsers != null) {
            chatTypingUsers.remove(userId)
            if (chatTypingUsers.isEmpty()) {
                currentTyping.remove(chatId)
            } else {
                currentTyping[chatId] = chatTypingUsers
            }
            _typingUsers.value = currentTyping
        }
    }
    
    /**
     * Gets users currently typing in a chat
     */
    fun getTypingUsers(chatId: String): Set<String> {
        return _typingUsers.value[chatId] ?: emptySet()
    }
    
    /**
     * Sets a user's online status
     */
    fun setUserOnline(userId: String, isOnline: Boolean) {
        val currentOnline = _onlineUsers.value.toMutableMap()
        currentOnline[userId] = isOnline
        _onlineUsers.value = currentOnline
        
        // Update last seen timestamp
        if (isOnline) {
            val currentLastSeen = _lastSeenUsers.value.toMutableMap()
            currentLastSeen[userId] = System.currentTimeMillis()
            _lastSeenUsers.value = currentLastSeen
        }
    }
    
    /**
     * Updates user's last seen timestamp
     */
    fun updateLastSeen(userId: String) {
        val currentLastSeen = _lastSeenUsers.value.toMutableMap()
        currentLastSeen[userId] = System.currentTimeMillis()
        _lastSeenUsers.value = currentLastSeen
    }
    
    /**
     * Gets a user's online status
     */
    fun isUserOnline(userId: String): Boolean {
        return _onlineUsers.value[userId] ?: false
    }
    
    /**
     * Gets a user's last seen timestamp
     */
    fun getUserLastSeen(userId: String): Long? {
        return _lastSeenUsers.value[userId]
    }
    
    /**
     * Cleans up expired typing indicators and online status
     */
    fun cleanup() {
        // This would typically be called periodically to clean up stale data
        val currentTime = System.currentTimeMillis()
        
        // Clean up online status for users who haven't been seen recently
        val currentOnline = _onlineUsers.value.toMutableMap()
        val currentLastSeen = _lastSeenUsers.value
        
        currentOnline.keys.forEach { userId ->
            val lastSeen = currentLastSeen[userId] ?: 0
            if (currentTime - lastSeen > ONLINE_TIMEOUT_MS) {
                currentOnline[userId] = false
            }
        }
        
        _onlineUsers.value = currentOnline
    }
}