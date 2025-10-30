package com.chain.messaging.data.local.storage

import com.chain.messaging.data.local.dao.MessageDao
import com.chain.messaging.data.local.dao.MessageSearchDao
import com.chain.messaging.data.local.dao.ReactionDao
import com.chain.messaging.data.local.dao.MediaDao
import com.chain.messaging.data.local.entity.MessageEntity
import com.chain.messaging.data.local.entity.ReactionEntity
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.model.Reaction
import com.chain.messaging.core.security.MessageEncryption
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class MessageStorageServiceTest {
    
    private lateinit var messageDao: MessageDao
    private lateinit var messageSearchDao: MessageSearchDao
    private lateinit var reactionDao: ReactionDao
    private lateinit var mediaDao: MediaDao
    private lateinit var messageEncryption: MessageEncryption
    private lateinit var messageCache: MessageCache
    private lateinit var messageStorageService: MessageStorageService
    
    private val testMessage = Message(
        id = "msg1",
        chatId = "chat1",
        senderId = "user1",
        content = "Test message",
        type = MessageType.TEXT,
        timestamp = Date(),
        status = MessageStatus.SENT,
        reactions = listOf(
            Reaction("user2", "👍", Date())
        ),
        isEncrypted = true
    )
    
    @Before
    fun setup() {
        messageDao = mockk()
        messageSearchDao = mockk()
        reactionDao = mockk()
        mediaDao = mockk()
        messageEncryption = mockk()
        messageCache = mockk()
        
        messageStorageService = MessageStorageService(
            messageDao,
            messageSearchDao,
            reactionDao,
            mediaDao,
            messageEncryption,
            messageCache
        )
    }
    
    @Test
    fun `storeMessage should encrypt content and store message with reactions`() = runTest {
        // Given
        val encryptedContent = "encrypted_content"
        every { messageEncryption.encryptForStorage(testMessage.content) } returns encryptedContent
        coEvery { messageDao.insertMessage(any()) } just Runs
        coEvery { reactionDao.insertReaction(any()) } just Runs
        coEvery { messageCache.putMessage(any()) } just Runs
        
        // When
        val result = messageStorageService.storeMessage(testMessage)
        
        // Then
        assertTrue(result.isSuccess)
        verify { messageEncryption.encryptForStorage(testMessage.content) }
        coVerify { messageDao.insertMessage(any()) }
        coVerify { reactionDao.insertReaction(any()) }
        coVerify { messageCache.putMessage(testMessage) }
    }
    
    @Test
    fun `storeMessage should handle unencrypted messages`() = runTest {
        // Given
        val unencryptedMessage = testMessage.copy(isEncrypted = false)
        coEvery { messageDao.insertMessage(any()) } just Runs
        coEvery { reactionDao.insertReaction(any()) } just Runs
        coEvery { messageCache.putMessage(any()) } just Runs
        
        // When
        val result = messageStorageService.storeMessage(unencryptedMessage)
        
        // Then
        assertTrue(result.isSuccess)
        verify(exactly = 0) { messageEncryption.encryptForStorage(any()) }
        coVerify { messageDao.insertMessage(any()) }
    }
    
    @Test
    fun `getMessages should check cache first`() = runTest {
        // Given
        val cachedMessages = listOf(testMessage)
        every { messageCache.getMessages("chat1", 20, 0) } returns cachedMessages
        
        // When
        val result = messageStorageService.getMessages("chat1", 20, 0)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(cachedMessages, result.getOrNull())
        verify { messageCache.getMessages("chat1", 20, 0) }
        coVerify(exactly = 0) { messageDao.getMessagesByChatId(any(), any(), any()) }
    }
    
    @Test
    fun `getMessages should fetch from database when cache is empty`() = runTest {
        // Given
        val messageEntity = MessageEntity(
            id = testMessage.id,
            chatId = testMessage.chatId,
            senderId = testMessage.senderId,
            content = "encrypted_content",
            type = testMessage.type.name,
            timestamp = testMessage.timestamp.time,
            status = testMessage.status.name,
            replyTo = null,
            isEncrypted = true,
            disappearingMessageTimer = null,
            expiresAt = null,
            isDisappearing = false
        )
        val reactionEntity = ReactionEntity(
            id = "reaction1",
            messageId = testMessage.id,
            userId = "user2",
            emoji = "👍",
            timestamp = Date().time
        )
        
        every { messageCache.getMessages("chat1", 20, 0) } returns emptyList()
        coEvery { messageDao.getMessagesByChatId("chat1", 20, 0) } returns listOf(messageEntity)
        coEvery { reactionDao.getReactionsByMessageId(testMessage.id) } returns listOf(reactionEntity)
        every { messageEncryption.decryptFromStorage("encrypted_content") } returns testMessage.content
        coEvery { messageCache.putMessage(any()) } just Runs
        
        // When
        val result = messageStorageService.getMessages("chat1", 20, 0)
        
        // Then
        assertTrue(result.isSuccess)
        val messages = result.getOrNull()!!
        assertEquals(1, messages.size)
        assertEquals(testMessage.content, messages[0].content)
        assertEquals(1, messages[0].reactions.size)
        
        verify { messageEncryption.decryptFromStorage("encrypted_content") }
        coVerify { messageCache.putMessage(any()) }
    }
    
    @Test
    fun `searchMessages should use full-text search`() = runTest {
        // Given
        val query = "test query"
        val messageEntity = MessageEntity(
            id = testMessage.id,
            chatId = testMessage.chatId,
            senderId = testMessage.senderId,
            content = "encrypted_content",
            type = testMessage.type.name,
            timestamp = testMessage.timestamp.time,
            status = testMessage.status.name,
            replyTo = null,
            isEncrypted = true,
            disappearingMessageTimer = null,
            expiresAt = null,
            isDisappearing = false
        )
        
        coEvery { messageSearchDao.searchMessages(query, 50, 0) } returns listOf(messageEntity)
        coEvery { reactionDao.getReactionsByMessageId(testMessage.id) } returns emptyList()
        every { messageEncryption.decryptFromStorage("encrypted_content") } returns testMessage.content
        
        // When
        val result = messageStorageService.searchMessages(query)
        
        // Then
        assertTrue(result.isSuccess)
        val messages = result.getOrNull()!!
        assertEquals(1, messages.size)
        assertEquals(testMessage.content, messages[0].content)
        
        coVerify { messageSearchDao.searchMessages(query, 50, 0) }
    }
    
    @Test
    fun `searchMessages should search in specific chat when chatId provided`() = runTest {
        // Given
        val query = "test query"
        val chatId = "chat1"
        
        coEvery { messageSearchDao.searchMessagesInChat(query, chatId, 50, 0) } returns emptyList()
        
        // When
        val result = messageStorageService.searchMessages(query, chatId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { messageSearchDao.searchMessagesInChat(query, chatId, 50, 0) }
        coVerify(exactly = 0) { messageSearchDao.searchMessages(any(), any(), any()) }
    }
    
    @Test
    fun `updateMessageStatus should update database and cache`() = runTest {
        // Given
        val messageIds = listOf("msg1", "msg2")
        val newStatus = MessageStatus.READ
        
        coEvery { messageDao.updateMessageStatus(messageIds, newStatus.name) } just Runs
        coEvery { messageCache.updateMessageStatus(any(), newStatus) } just Runs
        
        // When
        val result = messageStorageService.updateMessageStatus(messageIds, newStatus)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { messageDao.updateMessageStatus(messageIds, newStatus.name) }
        coVerify { messageCache.updateMessageStatus("msg1", newStatus) }
        coVerify { messageCache.updateMessageStatus("msg2", newStatus) }
    }
    
    @Test
    fun `deleteMessages should remove from database and cache`() = runTest {
        // Given
        val messageIds = listOf("msg1", "msg2")
        
        coEvery { messageDao.deleteMessagesByIds(messageIds) } just Runs
        coEvery { messageCache.removeMessage(any()) } just Runs
        
        // When
        val result = messageStorageService.deleteMessages(messageIds)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { messageDao.deleteMessagesByIds(messageIds) }
        coVerify { messageCache.removeMessage("msg1") }
        coVerify { messageCache.removeMessage("msg2") }
    }
    
    @Test
    fun `cleanupExpiredMessages should remove expired messages from cache and database`() = runTest {
        // Given
        val expiredMessage = MessageEntity(
            id = "expired1",
            chatId = "chat1",
            senderId = "user1",
            content = "expired",
            type = "TEXT",
            timestamp = System.currentTimeMillis(),
            status = "SENT",
            replyTo = null,
            isEncrypted = false,
            disappearingMessageTimer = null,
            expiresAt = System.currentTimeMillis() - 1000,
            isDisappearing = true
        )
        
        coEvery { messageDao.getExpiredMessages(any()) } returns listOf(expiredMessage)
        coEvery { messageDao.deleteExpiredMessages(any()) } returns 1
        coEvery { messageCache.removeMessage(any()) } just Runs
        
        // When
        val result = messageStorageService.cleanupExpiredMessages()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
        coVerify { messageCache.removeMessage("expired1") }
        coVerify { messageDao.deleteExpiredMessages(any()) }
    }
    
    @Test
    fun `storeMessage should handle encryption failure gracefully`() = runTest {
        // Given
        every { messageEncryption.encryptForStorage(any()) } throws RuntimeException("Encryption failed")
        
        // When
        val result = messageStorageService.storeMessage(testMessage)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }
    
    @Test
    fun `getSearchSuggestions should return suggestions from search dao`() = runTest {
        // Given
        val partialQuery = "test"
        val suggestions = listOf("test message", "test query")
        
        coEvery { messageSearchDao.getSearchSuggestions(partialQuery) } returns suggestions
        
        // When
        val result = messageStorageService.getSearchSuggestions(partialQuery)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(suggestions, result.getOrNull())
        coVerify { messageSearchDao.getSearchSuggestions(partialQuery) }
    }
}