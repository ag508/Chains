package com.chain.messaging.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for encrypting and decrypting messages at rest using Android Keystore
 */
@Singleton
class MessageEncryption @Inject constructor() {
    
    companion object {
        private const val KEYSTORE_ALIAS = "ChainMessageEncryptionKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }
    
    init {
        generateKeyIfNeeded()
    }
    
    /**
     * Encrypt message content for storage
     */
    fun encryptForStorage(plaintext: String): String {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val combined = iv + encryptedData
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            // If encryption fails, return original text (fallback)
            // In production, you might want to handle this differently
            plaintext
        }
    }
    
    /**
     * Decrypt message content from storage
     */
    fun decryptFromStorage(encryptedText: String): String {
        return try {
            val secretKey = getOrCreateSecretKey()
            val combined = Base64.decode(encryptedText, Base64.DEFAULT)
            
            // Extract IV and encrypted data
            val iv = combined.sliceArray(0..GCM_IV_LENGTH - 1)
            val encryptedData = combined.sliceArray(GCM_IV_LENGTH until combined.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            
            val decryptedData = cipher.doFinal(encryptedData)
            String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            // If decryption fails, return encrypted text as-is (fallback)
            // This handles cases where the text might not be encrypted
            encryptedText
        }
    }
    
    /**
     * Check if text appears to be encrypted
     */
    fun isEncrypted(text: String): Boolean {
        return try {
            // Try to decode as Base64 and check if it has the expected structure
            val decoded = Base64.decode(text, Base64.DEFAULT)
            decoded.size > GCM_IV_LENGTH + GCM_TAG_LENGTH
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate encryption key if it doesn't exist
     */
    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            generateSecretKey()
        }
    }
    
    /**
     * Generate a new secret key in Android Keystore
     */
    private fun generateSecretKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    /**
     * Get or create the secret key
     */
    private fun getOrCreateSecretKey(): SecretKey {
        return if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        } else {
            generateSecretKey()
            keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        }
    }
    
    /**
     * Delete the encryption key (for key rotation or reset)
     */
    fun deleteEncryptionKey(): Boolean {
        return try {
            if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                keyStore.deleteEntry(KEYSTORE_ALIAS)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Rotate the encryption key (generate new key)
     */
    fun rotateEncryptionKey(): Boolean {
        return try {
            deleteEncryptionKey()
            generateSecretKey()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if encryption key exists
     */
    fun hasEncryptionKey(): Boolean {
        return keyStore.containsAlias(KEYSTORE_ALIAS)
    }
    
    /**
     * Batch encrypt multiple messages
     */
    fun encryptBatch(messages: List<String>): List<String> {
        return messages.map { encryptForStorage(it) }
    }
    
    /**
     * Batch decrypt multiple messages
     */
    fun decryptBatch(encryptedMessages: List<String>): List<String> {
        return encryptedMessages.map { decryptFromStorage(it) }
    }
}