package com.chain.messaging.domain.usecase

import com.chain.messaging.core.messaging.TypingIndicatorService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TypingIndicatorUseCaseTest {
    
    private lateinit var typingIndicatorUseCase: TypingIndicatorUseCase
    private lateinit var typingIndicatorService: TypingIndicatorService
    
    @Before
    fun setup() {
        typingIndicatorService = mockk()
        typingIndicatorUseCase = TypingIndicatorUseCase(typingIndicatorService)
    }
    
    @Test
    fun `startTyping should call service startTyping`() {
        // Given
        val chatId = "chat123"
        val userId = "user123"
        
        every { typingIndicatorService.startTyping(chatId, userId) } returns Unit
        
        // When
        typingIndicatorUseCase.startTyping(chatId, userId)
        
        // Then
        verify { typingIndicatorService.startTyping(chatId, userId) }
    }
    
    @Test
    fun `stopTyping should call service stopTyping`() {
        // Given
        val chatId = "chat123"
        val userId = "user123"
        
        every { typingIndicatorService.stopTyping(chatId, userId) } returns Unit
        
        // When
        typingIndicatorUseCase.stopTyping(chatId, userId)
        
        // Then
        verify { typingIndicatorService.stopTyping(chatId, userId) }
    }
    
    @Test
    fun `getTypingUsers should return users from service`() {
        // Given
        val chatId = "chat123"
        val typingUsers = setOf("user1", "user2")
        
        every { typingIndicatorService.getTypingUsers(chatId) } returns typingUsers
        
        // When
        val result = typingIndicatorUseCase.getTypingUsers(chatId)
        
        // Then
        assertEquals(typingUsers, result)
        verify { typingIndicatorService.getTypingUsers(chatId) }
    }
    
    @Test
    fun `observeTypingUsers should return flow from service`() = runTest {
        // Given
        val typingUsersMap = mapOf("chat123" to setOf("user1", "user2"))
        val typingUsersFlow = MutableStateFlow(typingUsersMap)
        
        every { typingIndicatorService.typingUsers } returns typingUsersFlow
        
        // When
        val result = typingIndicatorUseCase.observeTypingUsers().first()
        
        // Then
        assertEquals(typingUsersMap, result)
    }
    
    @Test
    fun `setUserOnline should call service setUserOnline`() {
        // Given
        val userId = "user123"
        val isOnline = true
        
        every { typingIndicatorService.setUserOnline(userId, isOnline) } returns Unit
        
        // When
        typingIndicatorUseCase.setUserOnline(userId, isOnline)
        
        // Then
        verify { typingIndicatorService.setUserOnline(userId, isOnline) }
    }
    
    @Test
    fun `isUserOnline should return status from service`() {
        // Given
        val userId = "user123"
        val isOnline = true
        
        every { typingIndicatorService.isUserOnline(userId) } returns isOnline
        
        // When
        val result = typingIndicatorUseCase.isUserOnline(userId)
        
        // Then
        assertEquals(isOnline, result)
        verify { typingIndicatorService.isUserOnline(userId) }
    }
    
    @Test
    fun `observeOnlineUsers should return flow from service`() = runTest {
        // Given
        val onlineUsersMap = mapOf("user1" to true, "user2" to false)
        val onlineUsersFlow = MutableStateFlow(onlineUsersMap)
        
        every { typingIndicatorService.onlineUsers } returns onlineUsersFlow
        
        // When
        val result = typingIndicatorUseCase.observeOnlineUsers().first()
        
        // Then
        assertEquals(onlineUsersMap, result)
    }
}