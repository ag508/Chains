package com.chain.messaging.data.local.storage

import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

class MessageCacheTest {
    
    private lateinit var messageCache: MessageCache
    
    private val testMessage1 = Message(
        id = "msg1",
        chatId = "chat1",
        senderId = "user1",
        content = "Test message 1",
        type = MessageType.TEXT,
        timestamp = Date(1000),
        status = MessageStatus.SENT
    )
    
    private val testMessage2 = Message(
        id = "msg2",
        chatId = "chat1",
        senderId = "user2",
        content = "Test message 2",
        type = MessageType.TEXT,
        timestamp = Date(2000),
        status = MessageStatus.SENT
    )
    
    private val testMessage3 = Message(
        id = "msg3",
        chatId = "chat2",
        senderId = "user1",
        content = "Test message 3",
        type = MessageType.TEXT,
        timestamp = Date(3000),
        status = MessageStatus.SENT
    )
    
    @Before
    fun setup() {
        messageCache = MessageCache()
    }
    
    @Test
    fun `putMessage should add message to cache`() = runTest {
        // When
        messageCache.putMessage(testMessage1)
        
        // Then
        val cachedMessage = messageCache.getMessage(testMessage1.id)
        assertEquals(testMessage1, cachedMessage)
        assertTrue(messageCache.isMessageCached(testMessage1.id))
    }
    
    @Test
    fun `putMessage should add message to chat cache`() = runTest {
        // When
        messageCache.putMessage(testMessage1)
        messageCache.putMessage(testMessage2)
        
        // Then
        assertTrue(messageCache.hasCachedMessages("chat1"))
        assertEquals(2, messageCache.getCachedMessageCount("chat1"))
        
        val chatMessages = messageCache.getMessages("chat1", 10, 0)
        assertEquals(2, chatMessages.size)
        // Messages should be in chronological order (newest first)
        assertEquals(testMessage2.id, chatMessages[0].id)
        assertEquals(testMessage1.id, chatMessages[1].id)
    }
    
    @Test
    fun `getMessages should return paginated results`() = runTest {
        // Given
        messageCache.putMessage(testMessage1)
        messageCache.putMessage(testMessage2)
        
        // When
        val firstPage = messageCache.getMessages("chat1", 1, 0)
        val secondPage = messageCache.getMessages("chat1", 1, 1)
        
        // Then
        assertEquals(1, firstPage.size)
        assertEquals(testMessage2.id, firstPage[0].id) // Newest first
        
        assertEquals(1, secondPage.size)
        assertEquals(testMessage1.id, secondPage[0].id)
    }
    
    @Test
    fun `getMessages should return empty list for non-existent chat`() = runTest {
        // When
        val messages = messageCache.getMessages("non-existent", 10, 0)
        
        // Then
        assertTrue(messages.isEmpty())
        assertFalse(messageCache.hasCachedMessages("non-existent"))
    }
    
    @Test
    fun `getMessages should handle offset beyond available messages`() = runTest {
        // Given
        messageCache.putMessage(testMessage1)
        
        // When
        val messages = messageCache.getMessages("chat1", 10, 5)
        
        // Then
        assertTrue(messages.isEmpty())
    }
    
    @Test
    fun `updateMessageStatus should update cached message`() = runTest {
        // Given
        messageCache.putMessage(testMessage1)
        
        // When
        messageCache.updateMessageStatus(testMessage1.id, MessageStatus.READ)
        
        // Then
        val updatedMessage = messageCache.getMessage(testMessage1.id)
        assertEquals(MessageStatus.READ, updatedMessage?.status)
    }
    
    @Test
    fun `updateMessageStatus should handle non-existent message`() = runTest {
        // When
        messageCache.updateMessageStatus("non-existent", MessageStatus.READ)
        
        // Then
        // Should not throw exception
        assertNull(messageCache.getMessage("non-existent"))
    }
    
    @Test
    fun `removeMessage should remove from cache and chat cache`() = runTest {
        // Given
        messageCache.putMessage(testMessage1)
        messageCache.putMessage(testMessage2)
        
        // When
        messageCache.removeMessage(testMessage1.id)
        
        // Then
        assertNull(messageCache.getMessage(testMessage1.id))
        assertFalse(messageCache.isMessageCached(testMessage1.id))
        assertEquals(1, messageCache.getCachedMessageCount("chat1"))
        
        val remainingMessages = messageCache.getMessages("chat1", 10, 0)
        assertEquals(1, remainingMessages.size)
        assertEquals(testMessage2.id, remainingMessages[0].id)
    }
    
    @Test
    fun `removeChatMessages should remove all messages for chat`() = runTest {
        // Given
        messageCache.putMessage(testMessage1)
        messageCache.putMessage(testMessage2)
        messageCache.putMessage(testMessage3) // Different chat
        
        // When
        messageCache.removeChatMessages("chat1")
        
        // Then
        assertNull(messageCache.getMessage(testMessage1.id))
        assertNull(messageCache.getMessage(testMessage2.id))
        assertNotNull(messageCache.getMessage(testMessage3.id)) // Should remain
        
        assertFalse(messageCache.hasCachedMessages("chat1"))
        assertTrue(messageCache.hasCachedMessages("chat2"))
    }
    
    @Test
    fun `clearCache should remove all messages`() = runTest {
        // Given
        messageCache.putMessage(testMessage1)
        messageCache.putMessage(testMessage2)
        messageCache.putMessage(testMessage3)
        
        // When
        messageCache.clearCache()
        
        // Then
        assertNull(messageCache.getMessage(testMessage1.id))
        assertNull(messageCache.getMessage(testMessage2.id))
        assertNull(messageCache.getMessage(testMessage3.id))
        
        assertFalse(messageCache.hasCachedMessages("chat1"))
        assertFalse(messageCache.hasCachedMessages("chat2"))
        
        val stats = messageCache.getCacheStats()
        assertEquals(0, stats.totalMessages)
        assertEquals(0, stats.totalChats)
    }
    
    @Test
    fun `getCacheStats should return correct statistics`() = runTest {
        // Given
        messageCache.putMessage(testMessage1)
        messageCache.putMessage(testMessage2)
        messageCache.putMessage(testMessage3)
        
        // When
        val stats = messageCache.getCacheStats()
        
        // Then
        assertEquals(3, stats.totalMessages)
        assertEquals(2, stats.totalChats) // chat1 and chat2
        assertTrue(stats.maxCacheSize > 0)
        assertTrue(stats.maxChatCacheSize > 0)
    }
    
    @Test
    fun `preloadChatMessages should add messages to cache in correct order`() = runTest {
        // Given
        val messages = listOf(testMessage1, testMessage2)
        
        // When
        messageCache.preloadChatMessages("chat1", messages)
        
        // Then
        assertEquals(2, messageCache.getCachedMessageCount("chat1"))
        
        val cachedMessages = messageCache.getMessages("chat1", 10, 0)
        assertEquals(2, cachedMessages.size)
        // Should be sorted by timestamp (newest first)
        assertEquals(testMessage2.id, cachedMessages[0].id)
        assertEquals(testMessage1.id, cachedMessages[1].id)
    }
    
    @Test
    fun `preloadChatMessages should not add duplicate messages`() = runTest {
        // Given
        messageCache.putMessage(testMessage1)
        val messages = listOf(testMessage1, testMessage2)
        
        // When
        messageCache.preloadChatMessages("chat1", messages)
        
        // Then
        assertEquals(2, messageCache.getCachedMessageCount("chat1"))
        
        val cachedMessages = messageCache.getMessages("chat1", 10, 0)
        assertEquals(2, cachedMessages.size)
    }
    
    @Test
    fun `removeMessage should clean up empty chat cache`() = runTest {
        // Given
        messageCache.putMessage(testMessage1)
        
        // When
        messageCache.removeMessage(testMessage1.id)
        
        // Then
        assertFalse(messageCache.hasCachedMessages("chat1"))
        assertEquals(0, messageCache.getCachedMessageCount("chat1"))
        
        val stats = messageCache.getCacheStats()
        assertEquals(0, stats.totalChats)
    }
}