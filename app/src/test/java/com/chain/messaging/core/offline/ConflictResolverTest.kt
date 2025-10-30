package com.chain.messaging.core.offline

import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

class ConflictResolverTest {
    
    private lateinit var conflictResolver: ConflictResolverImpl
    
    private val baseMessage = Message(
        id = "test-message-1",
        chatId = "test-chat",
        senderId = "test-sender",
        content = "Original content",
        type = MessageType.TEXT,
        timestamp = LocalDateTime.now(),
        status = MessageStatus.SENT
    )
    
    @Before
    fun setup() {
        conflictResolver = ConflictResolverImpl()
    }
    
    @Test
    fun `hasConflict should return false for identical messages`() = runTest {
        // Given
        val localMessage = baseMessage
        val remoteMessage = baseMessage.copy()
        
        // When
        val result = conflictResolver.hasConflict(localMessage, remoteMessage)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `hasConflict should return true for messages with different content`() = runTest {
        // Given
        val localMessage = baseMessage
        val remoteMessage = baseMessage.copy(content = "Different content")
        
        // When
        val result = conflictResolver.hasConflict(localMessage, remoteMessage)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `hasConflict should return true for messages with different status`() = runTest {
        // Given
        val localMessage = baseMessage.copy(status = MessageStatus.SENDING)
        val remoteMessage = baseMessage.copy(status = MessageStatus.DELIVERED)
        
        // When
        val result = conflictResolver.hasConflict(localMessage, remoteMessage)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `resolveMessageConflict should prefer remote when local is sending`() = runTest {
        // Given
        val localMessage = baseMessage.copy(
            status = MessageStatus.SENDING,
            content = "Local content"
        )
        val remoteMessage = baseMessage.copy(
            status = MessageStatus.DELIVERED,
            content = "Remote content"
        )
        
        // When
        val result = conflictResolver.resolveMessageConflict(localMessage, remoteMessage)
        
        // Then
        assertEquals("Remote content", result.resolvedMessage.content)
        assertEquals(MessageStatus.DELIVERED, result.resolvedMessage.status)
        assertEquals(ResolutionStrategy.PREFER_REMOTE, result.strategy)
    }
    
    @Test
    fun `resolveMessageConflict should prefer latest timestamp for content conflicts`() = runTest {
        // Given
        val earlierTime = LocalDateTime.now().minusMinutes(5)
        val laterTime = LocalDateTime.now()
        
        val localMessage = baseMessage.copy(
            timestamp = laterTime,
            content = "Newer local content",
            status = MessageStatus.SENT
        )
        val remoteMessage = baseMessage.copy(
            timestamp = earlierTime,
            content = "Older remote content",
            status = MessageStatus.SENT
        )
        
        // When
        val result = conflictResolver.resolveMessageConflict(localMessage, remoteMessage)
        
        // Then
        assertEquals("Newer local content", result.resolvedMessage.content)
        assertEquals(ResolutionStrategy.PREFER_LATEST, result.strategy)
    }
    
    @Test
    fun `resolveMessageConflict should prefer higher status priority`() = runTest {
        // Given
        val localMessage = baseMessage.copy(status = MessageStatus.SENT)
        val remoteMessage = baseMessage.copy(status = MessageStatus.READ)
        
        // When
        val result = conflictResolver.resolveMessageConflict(localMessage, remoteMessage)
        
        // Then
        assertEquals(MessageStatus.READ, result.resolvedMessage.status)
        assertEquals(ResolutionStrategy.PREFER_REMOTE, result.strategy)
    }
    
    @Test
    fun `resolveConflicts should handle mixed local and remote messages`() = runTest {
        // Given
        val localOnlyMessage = baseMessage.copy(id = "local-only")
        val remoteOnlyMessage = baseMessage.copy(id = "remote-only")
        val conflictedLocalMessage = baseMessage.copy(
            id = "conflicted",
            content = "Local version"
        )
        val conflictedRemoteMessage = baseMessage.copy(
            id = "conflicted",
            content = "Remote version"
        )
        
        val localMessages = listOf(localOnlyMessage, conflictedLocalMessage)
        val remoteMessages = listOf(remoteOnlyMessage, conflictedRemoteMessage)
        
        // When
        val result = conflictResolver.resolveConflicts(localMessages, remoteMessages)
        
        // Then
        assertEquals(3, result.resolvedMessages.size)
        assertEquals(1, result.conflictsFound)
        assertEquals(1, result.conflictsResolved)
        
        // Check that all messages are included
        val messageIds = result.resolvedMessages.map { it.id }
        assertTrue(messageIds.contains("local-only"))
        assertTrue(messageIds.contains("remote-only"))
        assertTrue(messageIds.contains("conflicted"))
        
        // Check that conflicted message uses remote version
        val conflictedResolved = result.resolvedMessages.find { it.id == "conflicted" }
        assertEquals("Remote version", conflictedResolved?.content)
    }
    
    @Test
    fun `resolveConflicts should maintain message order by timestamp`() = runTest {
        // Given
        val time1 = LocalDateTime.now().minusMinutes(10)
        val time2 = LocalDateTime.now().minusMinutes(5)
        val time3 = LocalDateTime.now()
        
        val message1 = baseMessage.copy(id = "msg1", timestamp = time2)
        val message2 = baseMessage.copy(id = "msg2", timestamp = time1)
        val message3 = baseMessage.copy(id = "msg3", timestamp = time3)
        
        val localMessages = listOf(message1, message2)
        val remoteMessages = listOf(message3)
        
        // When
        val result = conflictResolver.resolveConflicts(localMessages, remoteMessages)
        
        // Then
        assertEquals(3, result.resolvedMessages.size)
        assertEquals("msg2", result.resolvedMessages[0].id) // Earliest
        assertEquals("msg1", result.resolvedMessages[1].id) // Middle
        assertEquals("msg3", result.resolvedMessages[2].id) // Latest
    }
    
    @Test
    fun `resolveConflicts should handle empty lists`() = runTest {
        // When
        val result = conflictResolver.resolveConflicts(emptyList(), emptyList())
        
        // Then
        assertEquals(0, result.resolvedMessages.size)
        assertEquals(0, result.conflictsFound)
        assertEquals(0, result.conflictsResolved)
    }
}