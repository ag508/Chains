package com.chain.messaging.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for encrypting and decrypting files using AES-GCM encryption
 */
@Singleton
class FileEncryption @Inject constructor() {
    
    companion object {
        private const val KEYSTORE_ALIAS_PREFIX = "ChainFileEncryption_"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val KEY_LENGTH = 256
        private const val BUFFER_SIZE = 8192
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }
    
    private val secureRandom = SecureRandom()
    
    /**
     * Encrypt a file and return the encryption key
     */
    fun encryptFile(sourceFile: File, destFile: File): String {
        // Generate a random encryption key for this file
        val keyBytes = ByteArray(KEY_LENGTH / 8)
        secureRandom.nextBytes(keyBytes)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        
        FileInputStream(sourceFile).use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                // Write IV to the beginning of the file
                outputStream.write(iv)
                
                // Encrypt and write the file content
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val encryptedBytes = if (bytesRead == BUFFER_SIZE) {
                        cipher.update(buffer)
                    } else {
                        cipher.update(buffer, 0, bytesRead)
                    }
                    encryptedBytes?.let { outputStream.write(it) }
                }
                
                // Write final block
                val finalBytes = cipher.doFinal()
                outputStream.write(finalBytes)
            }
        }
        
        // Return the encryption key as Base64 string
        return Base64.encodeToString(keyBytes, Base64.DEFAULT)
    }
    
    /**
     * Decrypt a file using the provided encryption key
     */
    fun decryptFile(encryptedFile: File, destFile: File, encryptionKey: String): Boolean {
        return try {
            val keyBytes = Base64.decode(encryptionKey, Base64.DEFAULT)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            
            FileInputStream(encryptedFile).use { inputStream ->
                // Read IV from the beginning of the file
                val iv = ByteArray(GCM_IV_LENGTH)
                inputStream.read(iv)
                
                val cipher = Cipher.getInstance(TRANSFORMATION)
                val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
                
                FileOutputStream(destFile).use { outputStream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        val decryptedBytes = if (bytesRead == BUFFER_SIZE) {
                            cipher.update(buffer)
                        } else {
                            cipher.update(buffer, 0, bytesRead)
                        }
                        decryptedBytes?.let { outputStream.write(it) }
                    }
                    
                    // Write final block
                    val finalBytes = cipher.doFinal()
                    outputStream.write(finalBytes)
                }
            }
            true
        } catch (e: Exception) {
            // Clean up partial file on failure
            if (destFile.exists()) {
                destFile.delete()
            }
            false
        }
    }
    
    /**
     * Generate a secure encryption key for file encryption
     */
    fun generateFileEncryptionKey(): String {
        val keyBytes = ByteArray(KEY_LENGTH / 8)
        secureRandom.nextBytes(keyBytes)
        return Base64.encodeToString(keyBytes, Base64.DEFAULT)
    }
    
    /**
     * Check if a file appears to be encrypted
     */
    fun isFileEncrypted(file: File): Boolean {
        return try {
            if (file.length() < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
                false
            } else {
                // Check if file starts with what looks like an IV
                FileInputStream(file).use { inputStream ->
                    val header = ByteArray(GCM_IV_LENGTH)
                    inputStream.read(header)
                    // This is a simple heuristic - in practice you might want a more robust check
                    header.any { it != 0.toByte() }
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the size of encrypted file (including IV and tag overhead)
     */
    fun getEncryptedFileSize(originalSize: Long): Long {
        return originalSize + GCM_IV_LENGTH + GCM_TAG_LENGTH
    }
    
    /**
     * Get the original file size from encrypted file size
     */
    fun getOriginalFileSize(encryptedSize: Long): Long {
        return maxOf(0, encryptedSize - GCM_IV_LENGTH - GCM_TAG_LENGTH)
    }
    
    /**
     * Securely delete a file by overwriting it before deletion
     */
    fun secureDelete(file: File): Boolean {
        return try {
            if (!file.exists()) {
                return true
            }
            
            val fileSize = file.length()
            val randomData = ByteArray(BUFFER_SIZE)
            
            FileOutputStream(file).use { outputStream ->
                var remaining = fileSize
                while (remaining > 0) {
                    val bytesToWrite = minOf(remaining, BUFFER_SIZE.toLong()).toInt()
                    secureRandom.nextBytes(randomData)
                    outputStream.write(randomData, 0, bytesToWrite)
                    remaining -= bytesToWrite
                }
                outputStream.flush()
            }
            
            file.delete()
        } catch (e: Exception) {
            // Fallback to regular deletion
            file.delete()
        }
    }
    
    /**
     * Encrypt file in place (overwrites original file)
     */
    fun encryptFileInPlace(file: File): String? {
        return try {
            val tempFile = File(file.parent, "${file.name}.tmp")
            val encryptionKey = encryptFile(file, tempFile)
            
            if (file.delete() && tempFile.renameTo(file)) {
                encryptionKey
            } else {
                tempFile.delete()
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Decrypt file in place (overwrites encrypted file)
     */
    fun decryptFileInPlace(file: File, encryptionKey: String): Boolean {
        return try {
            val tempFile = File(file.parent, "${file.name}.tmp")
            val success = decryptFile(file, tempFile, encryptionKey)
            
            if (success && file.delete() && tempFile.renameTo(file)) {
                true
            } else {
                tempFile.delete()
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}