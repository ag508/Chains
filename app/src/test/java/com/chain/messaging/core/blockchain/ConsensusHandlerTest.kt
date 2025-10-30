package com.chain.messaging.core.blockchain

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class ConsensusHandlerTest {
    
    private lateinit var consensusHandler: ConsensusHandler
    
    @Before
    fun setup() {
        consensusHandler = ConsensusHandler()
    }
    
    @Test
    fun `initial consensus state should be empty`() {
        val state = consensusHandler.getConsensusState()
        assertTrue(state.isEmpty())
    }
    
    @Test
    fun `handleConsensusUpdate should process valid consensus data`() {
        val consensusData = """{"height":123,"hash":"test_hash","peers":5}"""
        
        consensusHandler.handleConsensusUpdate(consensusData)
        
        // Allow some time for async processing
        Thread.sleep(100)
        
        val state = consensusHandler.getConsensusState()
        assertTrue(state.isNotEmpty())
    }
    
    @Test
    fun `validateBlock should return true for valid block`() {
        val validBlock = createValidBlock()
        
        val isValid = consensusHandler.validateBlock(validBlock)
        
        assertTrue(isValid)
    }
    
    @Test
    fun `validateBlock should return false for block with no transactions`() {
        val invalidBlock = createValidBlock().copy(transactions = emptyList())
        
        val isValid = consensusHandler.validateBlock(invalidBlock)
        
        assertFalse(isValid)
    }
    
    @Test
    fun `validateBlock should return false for block with invalid transaction`() {
        val invalidTransaction = createValidTransaction().copy(id = "") // Invalid: empty ID
        val invalidBlock = createValidBlock().copy(transactions = listOf(invalidTransaction))
        
        val isValid = consensusHandler.validateBlock(invalidBlock)
        
        assertFalse(isValid)
    }
    
    @Test
    fun `validateBlock should return false for block with invalid hash`() {
        val invalidBlock = createValidBlock().copy(hash = "invalid_hash")
        
        val isValid = consensusHandler.validateBlock(invalidBlock)
        
        assertFalse(isValid)
    }
    
    private fun createValidBlock(): Block {
        val transaction = createValidTransaction()
        val blockData = "previous_hash${System.currentTimeMillis()}merkle_rootnonce"
        val hash = java.security.MessageDigest.getInstance("SHA-256")
            .digest(blockData.toByteArray())
            .let { java.util.Base64.getEncoder().encodeToString(it) }
        
        return Block(
            number = 1,
            hash = hash,
            previousHash = "previous_hash",
            timestamp = System.currentTimeMillis(),
            transactions = listOf(transaction),
            merkleRoot = "merkle_root",
            nonce = "nonce",
            difficulty = 1
        )
    }
    
    private fun createValidTransaction(): MessageTransaction {
        return MessageTransaction(
            id = "test_transaction_id",
            from = "test_sender",
            to = "test_recipient",
            encryptedContent = "encrypted_content",
            messageType = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            signature = "valid_signature",
            nonce = "test_nonce"
        )
    }
}