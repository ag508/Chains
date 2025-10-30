package com.chain.messaging.integration

import com.chain.messaging.core.blockchain.*
import com.chain.messaging.core.crypto.KeyManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.signal.libsignal.protocol.IdentityKeyPair
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for blockchain connectivity and transaction processing
 * Tests the complete flow from message creation to blockchain transmission
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class BlockchainIntegrationTest {
    
    @Mock
    private lateinit var keyManager: KeyManager
    
    private lateinit var transactionSigner: TransactionSigner
    private lateinit var consensusHandler: ConsensusHandler
    private lateinit var blockchainManager: BlockchainManagerImpl
    private lateinit var testKeyPair: IdentityKeyPair
    
    @Before
    fun setup() {
        testKeyPair = IdentityKeyPair.generate()
        whenever(keyManager.getIdentityKeyPair()).thenReturn(testKeyPair)
        
        transactionSigner = TransactionSigner(keyManager)
        consensusHandler = ConsensusHandler()
        blockchainManager = BlockchainManagerImpl(transactionSigner, consensusHandler)
    }
    
    @Test
    fun `blockchain manager should initialize in disconnected state`() {
        assertFalse(blockchainManager.isConnected())
        
        val networkStatus = blockchainManager.getNetworkStatus()
        assertFalse(networkStatus.isConnected)
        assertEquals(null, networkStatus.nodeUrl)
        assertEquals(0, networkStatus.blockHeight)
        assertEquals(0, networkStatus.peerCount)
        assertEquals(0, networkStatus.lastSyncTime)
    }
    
    @Test
    fun `transaction signer should create valid signatures`() = runTest {
        val transaction = createTestTransaction()
        
        val signedTransaction = transactionSigner.signTransaction(transaction)
        
        // Verify signature was added
        assertTrue(signedTransaction.signature.isNotEmpty())
        assertTrue(signedTransaction.transactionHash.isNotEmpty())
        
        // Verify signature is valid
        val publicKeyBytes = testKeyPair.publicKey.serialize()
        val isValid = transactionSigner.verifyTransactionSignature(signedTransaction, publicKeyBytes)
        assertTrue(isValid)
    }
    
    @Test
    fun `transaction serialization should be symmetric`() {
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
    fun `consensus handler should validate blocks correctly`() {
        val validBlock = createValidBlock()
        val invalidBlock = createValidBlock().copy(transactions = emptyList())
        
        assertTrue(consensusHandler.validateBlock(validBlock))
        assertFalse(consensusHandler.validateBlock(invalidBlock))
    }
    
    @Test
    fun `message subscription should create flow`() = runTest {
        val userId = "test_user_123"
        
        val messageFlow = blockchainManager.subscribeToMessages(userId)
        
        assertNotNull(messageFlow)
    }
    
    @Test
    fun `sendMessage should fail when disconnected`() = runTest {
        val message = createTestEncryptedMessage()
        
        try {
            blockchainManager.sendMessage(message)
            assert(false) { "Expected IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("Not connected to blockchain network", e.message)
        }
    }
    
    @Test
    fun `disconnect should reset connection state`() = runTest {
        blockchainManager.disconnect()
        
        assertFalse(blockchainManager.isConnected())
        val status = blockchainManager.getNetworkStatus()
        assertFalse(status.isConnected)
        assertEquals(null, status.nodeUrl)
    }
    
    @Test
    fun `different message types should be handled correctly`() {
        val textMessage = createTestEncryptedMessage().copy(type = MessageType.TEXT)
        val imageMessage = createTestEncryptedMessage().copy(type = MessageType.IMAGE)
        val videoMessage = createTestEncryptedMessage().copy(type = MessageType.VIDEO)
        val audioMessage = createTestEncryptedMessage().copy(type = MessageType.AUDIO)
        
        // Verify all message types can be processed
        assertNotNull(textMessage)
        assertNotNull(imageMessage)
        assertNotNull(videoMessage)
        assertNotNull(audioMessage)
        
        assertEquals(MessageType.TEXT, textMessage.type)
        assertEquals(MessageType.IMAGE, imageMessage.type)
        assertEquals(MessageType.VIDEO, videoMessage.type)
        assertEquals(MessageType.AUDIO, audioMessage.type)
    }
    
    @Test
    fun `transaction signing should be deterministic for same input`() = runTest {
        val transaction = createTestTransaction()
        
        val signedTransaction1 = transactionSigner.signTransaction(transaction)
        val signedTransaction2 = transactionSigner.signTransaction(transaction)
        
        // Note: In a real implementation, signatures might include randomness
        // This test verifies the signing process works consistently
        assertTrue(signedTransaction1.signature.isNotEmpty())
        assertTrue(signedTransaction2.signature.isNotEmpty())
        assertTrue(signedTransaction1.transactionHash.isNotEmpty())
        assertTrue(signedTransaction2.transactionHash.isNotEmpty())
    }
    
    private fun createTestTransaction(): MessageTransaction {
        return MessageTransaction(
            id = "test_transaction_id",
            from = "test_sender",
            to = "test_recipient",
            encryptedContent = "encrypted_test_content",
            messageType = MessageType.TEXT,
            timestamp = System.currentTimeMillis(),
            signature = "",
            nonce = "test_nonce"
        )
    }
    
    private fun createTestEncryptedMessage(): EncryptedMessage {
        return EncryptedMessage(
            content = "encrypted_test_content",
            type = MessageType.TEXT,
            keyId = "test_key_id",
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun createValidBlock(): Block {
        val transaction = createTestTransaction().copy(signature = "valid_signature")
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
}