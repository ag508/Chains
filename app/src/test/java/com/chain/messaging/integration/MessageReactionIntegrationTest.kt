package com.chain.messaging.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.core.auth.AuthenticationService
import com.chain.messaging.core.auth.UserIdentity
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.data.local.ChainDatabase
import com.chain.messaging.data.repository.MessageRepositoryImpl
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.model.Reaction
import com.chain.messaging.domain.usecase.AddReactionUseCase
import com.chain.messaging.domain.usecase.GetReactionsUseCase
import com.chain.messaging.domain.usecase.RemoveReactionUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration test for message reaction functionality
 * Tests the complete flow from use cases through repository to database
 */
class MessageReactionIntegrationTest {
    
    private lateinit var database: ChainDatabase
    private lateinit var messageRepository: MessageRepositoryImpl
    private lateinit var addReactionUseCase: AddReactionUseCase
    private lateinit var removeReactionUseCase: RemoveReactionUseCase
    private lateinit var getReactionsUseCase: GetReactionsUseCase
    
    private lateinit var mockBlockchainManager: BlockchainManager
    private lateinit var mockEncryptionService: SignalEncryptionService
    private lateinit var mockAuthService: AuthenticationService
    
    @Before
    fun setUp() {
        // Create in-memory database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ChainDatabase::class.java
        ).allowMainThreadQueries().build()
        
        // Create mocks
        mockBlockchainManager = mockk()
        mockEncryptionService = mockk()
        mockAuthService = mockk()
        
        // Setup auth service mock
        every { mockAuthService.getCurrentUser() } returns UserIdentity(
            userId = "user123",
            publicKey = "publicKey123",
            displayName = "Test User"
        )
        
        // Create repository
        messageRepository = MessageRepositoryImpl(
            messageDao = database.messageDao(),
            reactionDao = database.reactionDao(),
            blockchainManager = mockBlockchainManager,
            encryptionService = mockEncryptionService,
            authenticationService = mockAuthService
        )
        
        // Create use cases
        addReactionUseCase = AddReactionUseCase(messageRepository)
        removeReactionUseCase = RemoveReactionUseCase(messageRepository)
        getReactionsUseCase = GetReactionsUseCase(messageRepository)
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun `should add reaction to message successfully`() = runTest {
        // Given
        val message = createTestMessage()
        messageRepository.saveMessage(message)
        
        val userId = "user456"
        val emoji = "👍"
        
        // When
        val result = addReactionUseCase(message.id, userId, emoji)
        
        // Then
        assertTrue(result.isSuccess)
        
        val reactions = getReactionsUseCase.getReactions(message.id)
        assertEquals(1, reactions.size)
        assertEquals(userId, reactions[0].userId)
        assertEquals(emoji, reactions[0].emoji)
    }
    
    @Test
    fun `should remove reaction when user reacts with same emoji again`() = runTest {
        // Given
        val message = createTestMessage()
        messageRepository.saveMessage(message)
        
        val userId = "user456"
        val emoji = "👍"
        
        // Add reaction first
        addReactionUseCase(message.id, userId, emoji)
        
        // When - add same reaction again (should remove it)
        val result = addReactionUseCase(message.id, userId, emoji)
        
        // Then
        assertTrue(result.isSuccess)
        
        val reactions = getReactionsUseCase.getReactions(message.id)
        assertTrue(reactions.isEmpty())
    }
    
    @Test
    fun `should allow multiple users to react with same emoji`() = runTest {
        // Given
        val message = createTestMessage()
        messageRepository.saveMessage(message)
        
        val user1 = "user1"
        val user2 = "user2"
        val emoji = "👍"
        
        // When
        addReactionUseCase(message.id, user1, emoji)
        addReactionUseCase(message.id, user2, emoji)
        
        // Then
        val reactions = getReactionsUseCase.getReactions(message.id)
        assertEquals(2, reactions.size)
        assertTrue(reactions.any { it.userId == user1 && it.emoji == emoji })
        assertTrue(reactions.any { it.userId == user2 && it.emoji == emoji })
    }
    
    @Test
    fun `should allow user to react with multiple emojis`() = runTest {
        // Given
        val message = createTestMessage()
        messageRepository.saveMessage(message)
        
        val userId = "user456"
        val emoji1 = "👍"
        val emoji2 = "❤️"
        
        // When
        addReactionUseCase(message.id, userId, emoji1)
        addReactionUseCase(message.id, userId, emoji2)
        
        // Then
        val reactions = getReactionsUseCase.getReactions(message.id)
        assertEquals(2, reactions.size)
        assertTrue(reactions.any { it.userId == userId && it.emoji == emoji1 })
        assertTrue(reactions.any { it.userId == userId && it.emoji == emoji2 })
    }
    
    @Test
    fun `should remove specific reaction`() = runTest {
        // Given
        val message = createTestMessage()
        messageRepository.saveMessage(message)
        
        val userId = "user456"
        val emoji1 = "👍"
        val emoji2 = "❤️"
        
        // Add multiple reactions
        addReactionUseCase(message.id, userId, emoji1)
        addReactionUseCase(message.id, userId, emoji2)
        
        // When - remove one specific reaction
        val result = removeReactionUseCase(message.id, userId, emoji1)
        
        // Then
        assertTrue(result.isSuccess)
        
        val reactions = getReactionsUseCase.getReactions(message.id)
        assertEquals(1, reactions.size)
        assertEquals(emoji2, reactions[0].emoji)
    }
    
    @Test
    fun `should check if user has reacted correctly`() = runTest {
        // Given
        val message = createTestMessage()
        messageRepository.saveMessage(message)
        
        val userId = "user456"
        val emoji = "👍"
        
        // Initially user has not reacted
        assertFalse(getReactionsUseCase.hasUserReacted(message.id, userId, emoji))
        
        // When user adds reaction
        addReactionUseCase(message.id, userId, emoji)
        
        // Then user has reacted
        assertTrue(getReactionsUseCase.hasUserReacted(message.id, userId, emoji))
        
        // When user removes reaction
        removeReactionUseCase(message.id, userId, emoji)
        
        // Then user has not reacted again
        assertFalse(getReactionsUseCase.hasUserReacted(message.id, userId, emoji))
    }
    
    @Test
    fun `should observe reactions changes`() = runTest {
        // Given
        val message = createTestMessage()
        messageRepository.saveMessage(message)
        
        val userId = "user456"
        val emoji = "👍"
        
        // When observing reactions
        val reactionsFlow = getReactionsUseCase.observeReactions(message.id)
        
        // Initially no reactions
        var reactions = reactionsFlow.first()
        assertTrue(reactions.isEmpty())
        
        // Add reaction
        addReactionUseCase(message.id, userId, emoji)
        
        // Should observe the new reaction
        reactions = reactionsFlow.first()
        assertEquals(1, reactions.size)
        assertEquals(userId, reactions[0].userId)
        assertEquals(emoji, reactions[0].emoji)
    }
    
    @Test
    fun `should load messages with reactions`() = runTest {
        // Given
        val message = createTestMessage()
        messageRepository.saveMessage(message)
        
        val userId = "user456"
        val emoji = "👍"
        
        // Add reaction
        addReactionUseCase(message.id, userId, emoji)
        
        // When loading messages
        val messagesFlow = messageRepository.observeMessages(message.chatId)
        val messages = messagesFlow.first()
        
        // Then message should include reactions
        assertEquals(1, messages.size)
        val loadedMessage = messages[0]
        assertEquals(1, loadedMessage.reactions.size)
        assertEquals(userId, loadedMessage.reactions[0].userId)
        assertEquals(emoji, loadedMessage.reactions[0].emoji)
    }
    
    private fun createTestMessage(): Message {
        return Message(
            id = "message123",
            chatId = "chat456",
            senderId = "user123",
            content = "Test message",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENT,
            reactions = emptyList()
        )
    }
}