package com.chain.messaging.core.blockchain

import android.util.Log
import com.chain.messaging.core.crypto.KeyManager
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Signature
import java.util.*
/**
 * Service for signing blockchain transactions
 */
class TransactionSigner(
    private val keyManager: KeyManager
) {
    private val TAG = "TransactionSigner"
    
    /**
     * Sign a message transaction with the user's private key
     */
    suspend fun signTransaction(transaction: MessageTransaction): MessageTransaction {
        try {
            val transactionData = createTransactionData(transaction)
            val signature = signData(transactionData)
            val transactionHash = generateTransactionHash(transactionData, signature)
            
            return transaction.copy(
                signature = signature,
                transactionHash = transactionHash
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign transaction", e)
            throw e
        }
    }
    
    /**
     * Verify a transaction signature
     */
    fun verifyTransactionSignature(transaction: MessageTransaction, publicKey: ByteArray): Boolean {
        try {
            val transactionData = createTransactionData(transaction.copy(signature = ""))
            return verifySignature(transactionData, transaction.signature, publicKey)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify transaction signature", e)
            return false
        }
    }
    
    private fun createTransactionData(transaction: MessageTransaction): String {
        return "${transaction.id}${transaction.from}${transaction.to}${transaction.encryptedContent}${transaction.messageType}${transaction.timestamp}${transaction.nonce}"
    }
    
    private suspend fun signData(data: String): String {
        val ecPrivateKey = keyManager.getIdentityKeyPair().privateKey
        // Cast ECPrivateKey to PrivateKey for Signature.initSign()
        val privateKey: PrivateKey = ecPrivateKey
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(data.toByteArray())
        
        val signatureBytes = signature.sign()
        return Base64.getEncoder().encodeToString(signatureBytes)
    }
    
    private fun verifySignature(data: String, signatureStr: String, publicKey: ByteArray): Boolean {
        return try {
            val signature = Signature.getInstance("SHA256withECDSA")
            val pubKey = java.security.KeyFactory.getInstance("EC")
                .generatePublic(java.security.spec.X509EncodedKeySpec(publicKey))
            
            signature.initVerify(pubKey)
            signature.update(data.toByteArray())
            
            val signatureBytes = Base64.getDecoder().decode(signatureStr)
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying signature", e)
            false
        }
    }
    
    private fun generateTransactionHash(data: String, signature: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest("$data$signature".toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}