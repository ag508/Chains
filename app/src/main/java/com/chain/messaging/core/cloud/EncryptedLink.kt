package com.chain.messaging.core.cloud

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Represents an encrypted link to a file stored in cloud storage
 */
@Serializable
data class EncryptedLink(
    val url: String,
    val encryptionKey: String,
    val service: CloudService,
    @Serializable(with = InstantSerializer::class)
    val expiresAt: Instant,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val checksum: String
) {
    /**
     * Check if the link has expired
     */
    fun isExpired(): Boolean {
        return Instant.now().isAfter(expiresAt)
    }
}

/**
 * Represents the result of a file upload operation
 */
sealed class UploadResult {
    data class Success(val encryptedLink: EncryptedLink) : UploadResult()
    data class Error(val message: String, val cause: Throwable? = null) : UploadResult()
    data class Progress(val bytesUploaded: Long, val totalBytes: Long) : UploadResult()
}

/**
 * Represents the result of a file download operation
 */
sealed class DownloadResult {
    data class Success(val filePath: String) : DownloadResult()
    data class Error(val message: String, val cause: Throwable? = null) : DownloadResult()
    data class Progress(val bytesDownloaded: Long, val totalBytes: Long) : DownloadResult()
}

/**
 * Configuration for file expiration
 */
data class ExpirationConfig(
    val defaultExpirationHours: Int = 24,
    val maxExpirationHours: Int = 168, // 7 days
    val autoCleanupEnabled: Boolean = true
)