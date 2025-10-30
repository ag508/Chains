package com.chain.messaging.core.cloud

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles client-side file encryption and decryption for cloud storage
 */
@Singleton
class FileEncryption @Inject constructor() {
    
    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_LENGTH = 256
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 16
    }
    
    /**
     * Initialize file encryption
     */
    suspend fun initialize() {
        // Initialize encryption components if needed
        // For now, this is a no-op as AES/GCM doesn't require special initialization
    }
    
    /**
     * Generate a new encryption key
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_LENGTH)
        return keyGenerator.generateKey()
    }
    
    /**
     * Encrypt a file and return the encrypted file path and key
     */
    fun encryptFile(inputFile: File, outputFile: File): EncryptionResult {
        return try {
            val key = generateKey()
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH * 8, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
            
            FileInputStream(inputFile).use { input ->
                FileOutputStream(outputFile).use { output ->
                    // Write IV to the beginning of the file
                    output.write(iv)
                    
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val encryptedData = cipher.update(buffer, 0, bytesRead)
                        if (encryptedData != null) {
                            output.write(encryptedData)
                        }
                    }
                    
                    // Write final block
                    val finalData = cipher.doFinal()
                    output.write(finalData)
                }
            }
            
            val keyString = android.util.Base64.encodeToString(key.encoded, android.util.Base64.NO_WRAP)
            val checksum = calculateChecksum(inputFile)
            
            EncryptionResult.Success(keyString, checksum)
        } catch (e: Exception) {
            EncryptionResult.Error("Encryption failed: ${e.message}", e)
        }
    }
    
    /**
     * Decrypt a file using the provided key
     */
    fun decryptFile(encryptedFile: File, outputFile: File, keyString: String): DecryptionResult {
        return try {
            val keyBytes = android.util.Base64.decode(keyString, android.util.Base64.NO_WRAP)
            val key = SecretKeySpec(keyBytes, ALGORITHM)
            
            FileInputStream(encryptedFile).use { input ->
                // Read IV from the beginning of the file
                val iv = ByteArray(IV_LENGTH)
                input.read(iv)
                
                val cipher = Cipher.getInstance(TRANSFORMATION)
                val gcmSpec = GCMParameterSpec(TAG_LENGTH * 8, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
                
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val decryptedData = cipher.update(buffer, 0, bytesRead)
                        if (decryptedData != null) {
                            output.write(decryptedData)
                        }
                    }
                    
                    // Write final block
                    val finalData = cipher.doFinal()
                    output.write(finalData)
                }
            }
            
            val checksum = calculateChecksum(outputFile)
            DecryptionResult.Success(checksum)
        } catch (e: Exception) {
            DecryptionResult.Error("Decryption failed: ${e.message}", e)
        }
    }
    
    /**
     * Calculate SHA-256 checksum of a file
     */
    private fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        
        FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        
        return android.util.Base64.encodeToString(digest.digest(), android.util.Base64.NO_WRAP)
    }
    
    /**
     * Verify file integrity using checksum
     */
    fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        return try {
            val actualChecksum = calculateChecksum(file)
            actualChecksum == expectedChecksum
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Result of file encryption operation
 */
sealed class EncryptionResult {
    data class Success(val encryptionKey: String, val checksum: String) : EncryptionResult()
    data class Error(val message: String, val cause: Throwable? = null) : EncryptionResult()
}

/**
 * Result of file decryption operation
 */
sealed class DecryptionResult {
    data class Success(val checksum: String) : DecryptionResult()
    data class Error(val message: String, val cause: Throwable? = null) : DecryptionResult()
}