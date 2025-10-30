package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.Reaction
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetReactionsUseCaseTest {
    
    private lateinit var messageRepository: MessageRepository
    private lateinit var getReactionsUseCase: GetReactionsUseCase
    
    @Before
    fun setUp() {
        messageRepository = mockk()
        getReactionsUseCase = GetReactionsUseCase(messageRepository)
    }
    
    @Test
    fun `getReactions should return reactions from repository`() = runTest {
        // Given
        val messageId = "message123"
        val reactions = listOf(
            Reaction("user1", "👍", Date()),
            Reaction("user2", "❤️", Date())
        )
        coEvery { messageRepository.getReactions(messageId) } returns reactions
        
        // When
        val result = getReactionsUseCase.getReactions(messageId)
        
        // Then
        assertEquals(reactions, result)
        coVerify { messageRepository.getReactions(messageId) }
    }
    
    @Test
    fun `getReactions should return empty list when repository throws exception`() = runTest {
        // Given
        val messageId = "message123"
        coEvery { messageRepository.getReactions(messageId) } throws RuntimeException("Database error")
        
        // When
        val result = getReactionsUseCase.getReactions(messageId)
        
        // Then
        assertTrue(result.isEmpty())
        coVerify { messageRepository.getReactions(messageId) }
    }
    
    @Test
    fun `observeReactions should return flow from repository`() = runTest {
        // Given
        val messageId = "message123"
        val reactions = listOf(
            Reaction("user1", "👍", Date()),
            Reaction("user2", "❤️", Date())
        )
        val flow = flowOf(reactions)
        every { messageRepository.observeReactions(messageId) } returns flow
        
        // When
        val result = getReactionsUseCase.observeReactions(messageId)
        
        // Then
        assertEquals(flow, result)
    }
    
    @Test
    fun `hasUserReacted should return true when user has reacted`() = runTest {
        // Given
        val messageId = "message123"
        val userId = "user456"
        val emoji = "👍"
        coEvery { messageRepository.hasUserReacted(messageId, userId, emoji) } returns true
        
        // When
        val result = getReactionsUseCase.hasUserReacted(messageId, userId, emoji)
        
        // Then
        assertTrue(result)
        coVerify { messageRepository.hasUserReacted(messageId, userId, emoji) }
    }
    
    @Test
    fun `hasUserReacted should return false when user has not reacted`() = runTest {
        // Given
        val messageId = "message123"
        val userId = "user456"
        val emoji = "👍"
        coEvery { messageRepository.hasUserReacted(messageId, userId, emoji) } returns false
        
        // When
        val result = getReactionsUseCase.hasUserReacted(messageId, userId, emoji)
        
        // Then
        assertFalse(result)
        coVerify { messageRepository.hasUserReacted(messageId, userId, emoji) }
    }
    
    @Test
    fun `hasUserReacted should return false when repository throws exception`() = runTest {
        // Given
        val messageId = "message123"
        val userId = "user456"
        val emoji = "👍"
        coEvery { messageRepository.hasUserReacted(messageId, userId, emoji) } throws RuntimeException("Database error")
        
        // When
        val result = getReactionsUseCase.hasUserReacted(messageId, userId, emoji)
        
        // Then
        assertFalse(result)
        coVerify { messageRepository.hasUserReacted(messageId, userId, emoji) }
    }
}