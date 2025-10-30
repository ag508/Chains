package com.chain.messaging.core.cloud

import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Interface for managing encrypted file uploads and downloads to cloud storage
 */
interface CloudStorageManager {
    
    /**
     * Upload a file to cloud storage with encryption
     */
    suspend fun uploadFile(
        file: File,
        service: CloudService,
        expirationHours: Int = 24
    ): Flow<UploadResult>
    
    /**
     * Download and decrypt a file from cloud storage
     */
    suspend fun downloadFile(
        encryptedLink: EncryptedLink,
        outputFile: File
    ): Flow<DownloadResult>
    
    /**
     * Delete a file from cloud storage
     */
    suspend fun deleteFile(encryptedLink: EncryptedLink): Boolean
    
    /**
     * Get storage quota information for a service
     */
    suspend fun getStorageInfo(service: CloudService): StorageInfo?
    
    /**
     * Clean up expired files
     */
    suspend fun cleanupExpiredFiles(): Int
    
    /**
     * List all uploaded files for a service
     */
    suspend fun listFiles(service: CloudService): List<EncryptedLink>
    
    /**
     * Generate a shareable encrypted link
     */
    suspend fun generateShareableLink(encryptedLink: EncryptedLink): String
    
    /**
     * Parse a shareable link back to EncryptedLink
     */
    suspend fun parseShareableLink(shareableLink: String): EncryptedLink?
    
    /**
     * Initialize cloud storage manager
     */
    suspend fun initialize()
    
    /**
     * Initialize user accounts for cloud storage
     */
    suspend fun initializeUserAccounts()
}

/**
 * Storage information for a cloud service
 */
data class StorageInfo(
    val totalSpace: Long,
    val usedSpace: Long,
    val availableSpace: Long,
    val service: CloudService
) {
    val usagePercentage: Float
        get() = if (totalSpace > 0) (usedSpace.toFloat() / totalSpace.toFloat()) * 100f else 0f
}