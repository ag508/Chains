package com.chain.messaging.core.blockchain

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
import org.signal.libsignal.protocol.ecc.Curve
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TransactionSignerTest {
    
    @Mock
    private lateinit var keyManager: KeyManager
    
    private lateinit var transactionSigner: TransactionSigner
    private lateinit var testKeyPair: IdentityKeyPair
    
    @Before
    fun setup() {
        testKeyPair = IdentityKeyPair.generate()
        transactionSigner = TransactionSigner(keyManager)
        
        whenever(keyManager.getIdentityKeyPair()).thenReturn(testKeyPair)
    }
    
    @Test
    fun `signTransaction should add signature and hash to transaction`() = runTest {
        val transaction = createTestTransaction()
        
        val signedTransaction = transactionSigner.signTransaction(transaction)
        
        // Verify signature was added
        assertTrue(signedTransaction.signature.isNotEmpty())
        assertNotEquals("", signedTransaction.signature)
        
        // Verify transaction hash was generated
        assertTrue(signedTransaction.transactionHash.isNotEmpty())
        assertNotEquals("", signedTransaction.transactionHash)
        
        // Verify other fields remain unchanged
        assertEquals(transaction.id, signedTransaction.id)
        assertEquals(transaction.from, signedTransaction.from)
        assertEquals(transaction.to, signedTransaction.to)
        assertEquals(transaction.encryptedContent, signedTransaction.encryptedContent)
        assertEquals(transaction.messageType, signedTransaction.messageType)
        assertEquals(transaction.timestamp, signedTransaction.timestamp)
        assertEquals(transaction.nonce, signedTransaction.nonce)
    }
    
    @Test
    fun `signTransaction should generate different signatures for different transactions`() = runTest {
        val transaction1 = createTestTransaction()
        val transaction2 = createTestTransaction().copy(id = "different_id")
        
        val signedTransaction1 = transactionSigner.signTransaction(transaction1)
        val signedTransaction2 = transactionSigner.signTransaction(transaction2)
        
        assertNotEquals(signedTransaction1.signature, signedTransaction2.signature)
        assertNotEquals(signedTransaction1.transactionHash, signedTransaction2.transactionHash)
    }
    
    @Test
    fun `verifyTransactionSignature should return true for valid signature`() = runTest {
        val transaction = createTestTransaction()
        val signedTransaction = transactionSigner.signTransaction(transaction)
        
        val publicKeyBytes = testKeyPair.publicKey.serialize()
        val isValid = transactionSigner.verifyTransactionSignature(signedTransaction, publicKeyBytes)
        
        assertTrue(isValid)
    }
    
    @Test
    fun `verifyTransactionSignature should return false for invalid signature`() {
        val transaction = createTestTransaction().copy(signature = "invalid_signature")
        val publicKeyBytes = testKeyPair.publicKey.serialize()
        
        val isValid = transactionSigner.verifyTransactionSignature(transaction, publicKeyBytes)
        
        assertFalse(isValid)
    }
    
    @Test
    fun `verifyTransactionSignature should return false for wrong public key`() = runTest {
        val transaction = createTestTransaction()
        val signedTransaction = transactionSigner.signTransaction(transaction)
        
        // Use different key pair for verification
        val wrongKeyPair = IdentityKeyPair.generate()
        val wrongPublicKeyBytes = wrongKeyPair.publicKey.serialize()
        
        val isValid = transactionSigner.verifyTransactionSignature(signedTransaction, wrongPublicKeyBytes)
        
        assertFalse(isValid)
    }
    
    @Test
    fun `verifyTransactionSignature should return false for tampered transaction`() = runTest {
        val transaction = createTestTransaction()
        val signedTransaction = transactionSigner.signTransaction(transaction)
        
        // Tamper with the transaction content
        val tamperedTransaction = signedTransaction.copy(encryptedContent = "tampered_content")
        val publicKeyBytes = testKeyPair.publicKey.serialize()
        
        val isValid = transactionSigner.verifyTransactionSignature(tamperedTransaction, publicKeyBytes)
        
        assertFalse(isValid)
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
}