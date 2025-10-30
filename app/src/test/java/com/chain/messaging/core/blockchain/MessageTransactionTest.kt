package com.chain.messaging.core.blockchain

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MessageTransactionTest {
    
    @Test
    fun `serialize should convert transaction to JSON string`() {
        val transaction = createTestTransaction()
        
        val serialized = transaction.serialize()
        
        assertNotNull(serialized)
        assert(serialized.contains("test_transaction_id"))
        assert(serialized.contains("test_sender"))
        assert(serialized.contains("test_recipient"))
        assert(serialized.contains("encrypted_content"))
        assert(serialized.contains("TEXT"))
    }
    
    @Test
    fun `deserialize should convert JSON string back to transaction`() {
        val originalTransaction = createTestTransaction()
        val serialized = originalTransaction.serialize()
        
        val deserializedTransaction = MessageTransaction.deserialize(serialized)
        
        assertEquals(originalTransaction.id, deserializedTransaction.id)
        assertEquals(originalTransaction.from, deserializedTransaction.from)
        assertEquals(originalTransaction.to, deserializedTransaction.to)
        assertEquals(originalTransaction.encryptedContent, deserializedTransaction.encryptedContent)
        assertEquals(originalTransaction.messageType, deserializedTransaction.messageType)
        assertEquals(originalTransaction.timestamp, deserializedTransaction.timestamp)
        assertEquals(originalTransaction.signature, deserializedTransaction.signature)
        assertEquals(originalTransaction.nonce, deserializedTransaction.nonce)
    }
    
    @Test
    fun `serialize and deserialize should be symmetric`() {
        val originalTransaction = createTestTransaction()
        
        val serialized = originalTransaction.serialize()
        val deserializedTransaction = MessageTransaction.deserialize(serialized)
        val reserializedTransaction = deserializedTransaction.serialize()
        
        assertEquals(serialized, reserializedTransaction)
    }
    
    @Test
    fun `transaction should handle different message types`() {
        val textTransaction = createTestTransaction().copy(messageType = MessageType.TEXT)
        val imageTransaction = createTestTransaction().copy(messageType = MessageType.IMAGE)
        val videoTransaction = createTestTransaction().copy(messageType = MessageType.VIDEO)
        
        val textSerialized = textTransaction.serialize()
        val imageSerialized = imageTransaction.serialize()
        val videoSerialized = videoTransaction.serialize()
        
        val textDeserialized = MessageTransaction.deserialize(textSerialized)
        val imageDeserialized = MessageTransaction.deserialize(imageSerialized)
        val videoDeserialized = MessageTransaction.deserialize(videoSerialized)
        
        assertEquals(MessageType.TEXT, textDeserialized.messageType)
        assertEquals(MessageType.IMAGE, imageDeserialized.messageType)
        assertEquals(MessageType.VIDEO, videoDeserialized.messageType)
    }
    
    private fun createTestTransaction(): MessageTransaction {
        return MessageTransaction(
            id = "test_transaction_id",
            from = "test_sender",
            to = "test_recipient",
            encryptedContent = "encrypted_content",
            messageType = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            signature = "test_signature",
            nonce = "test_nonce",
            gasUsed = 100,
            blockNumber = 12345,
            transactionHash = "test_hash"
        )
    }
}