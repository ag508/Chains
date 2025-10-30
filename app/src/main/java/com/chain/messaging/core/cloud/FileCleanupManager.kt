package com.chain.messaging.core.cloud

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages file cleanup and optimization for cloud storage
 */
@Singleton
class FileCleanupManager @Inject constructor(
    private val context: Context,
    private val cloudStorageManager: CloudStorageManager
) {
    
    companion object {
        private const val DEFAULT_CLEANUP_DAYS = 30
        private const val CACHE_CLEANUP_DAYS = 7
        private const val TEMP_CLEANUP_HOURS = 24
    }
    
    /**
     * Perform comprehensive cleanup across all services
     */
    suspend fun performFullCleanup(): CleanupResult = withContext(Dispatchers.IO) {
        val results = mutableListOf<ServiceCleanupResult>()
        var totalFilesDeleted = 0
        var totalSpaceFreed = 0L
        
        // Clean up expired cloud files
        val expiredFilesDeleted = cloudStorageManager.cleanupExpiredFiles()
        totalFilesDeleted += expiredFilesDeleted
        
        // Clean up local cache and temp files
        val localCleanup = cleanupLocalFiles()
        totalFilesDeleted += localCleanup.filesDeleted
        totalSpaceFreed += localCleanup.spaceFreed
        
        // Clean up each cloud service
        CloudService.values().forEach { service ->
            try {
                val serviceResult = cleanupService(service)
                results.add(serviceResult)
                totalFilesDeleted += serviceResult.filesDeleted
                totalSpaceFreed += serviceResult.spaceFreed
            } catch (e: Exception) {
                results.add(
                    ServiceCleanupResult(
                        service = service,
                        filesDeleted = 0,
                        spaceFreed = 0L,
                        error = e.message
                    )
                )
            }
        }
        
        CleanupResult(
            totalFilesDeleted = totalFilesDeleted,
            totalSpaceFreed = totalSpaceFreed,
            serviceResults = results
        )
    }
    
    /**
     * Clean up files for a specific service
     */
    suspend fun cleanupService(service: CloudService): ServiceCleanupResult = withContext(Dispatchers.IO) {
        try {
            val files = cloudStorageManager.listFiles(service)
            val cutoffDate = Instant.now().minus(DEFAULT_CLEANUP_DAYS.toLong(), ChronoUnit.DAYS)
            
            var filesDeleted = 0
            var spaceFreed = 0L
            
            files.forEach { file ->
                if (file.expiresAt.isBefore(cutoffDate) || file.isExpired()) {
                    if (cloudStorageManager.deleteFile(file)) {
                        filesDeleted++
                        spaceFreed += file.fileSize
                    }
                }
            }
            
            ServiceCleanupResult(
                service = service,
                filesDeleted = filesDeleted,
                spaceFreed = spaceFreed,
                error = null
            )
        } catch (e: Exception) {
            ServiceCleanupResult(
                service = service,
                filesDeleted = 0,
                spaceFreed = 0L,
                error = e.message
            )
        }
    }
    
    /**
     * Clean up local cache and temporary files
     */
    suspend fun cleanupLocalFiles(): LocalCleanupResult = withContext(Dispatchers.IO) {
        var filesDeleted = 0
        var spaceFreed = 0L
        
        // Clean up cache directory
        val cacheDir = context.cacheDir
        val cacheCleanup = cleanupDirectory(
            cacheDir,
            CACHE_CLEANUP_DAYS * 24 * 3600 * 1000L // Convert to milliseconds
        )
        filesDeleted += cacheCleanup.filesDeleted
        spaceFreed += cacheCleanup.spaceFreed
        
        // Clean up temp directory
        val tempDir = File(cacheDir, "temp")
        if (tempDir.exists()) {
            val tempCleanup = cleanupDirectory(
                tempDir,
                TEMP_CLEANUP_HOURS * 3600 * 1000L // Convert to milliseconds
            )
            filesDeleted += tempCleanup.filesDeleted
            spaceFreed += tempCleanup.spaceFreed
        }
        
        LocalCleanupResult(filesDeleted, spaceFreed)
    }
    
    /**
     * Get cleanup suggestions based on storage analysis
     */
    suspend fun getCleanupSuggestions(): List<CleanupSuggestion> {
        val suggestions = mutableListOf<CleanupSuggestion>()
        
        // Analyze local storage
        val localFiles = analyzeLocalStorage()
        if (localFiles.totalSize > 100 * 1024 * 1024) { // More than 100MB
            suggestions.add(
                CleanupSuggestion(
                    type = CleanupType.LOCAL_CACHE,
                    description = "Clear local cache files (${formatFileSize(localFiles.totalSize)})",
                    potentialSpaceSaved = localFiles.totalSize,
                    priority = Priority.MEDIUM
                )
            )
        }
        
        // Analyze cloud storage
        CloudService.values().forEach { service ->
            try {
                val files = cloudStorageManager.listFiles(service)
                val expiredFiles = files.filter { it.isExpired() }
                val oldFiles = files.filter { 
                    it.expiresAt.isBefore(Instant.now().minus(DEFAULT_CLEANUP_DAYS.toLong(), ChronoUnit.DAYS))
                }
                
                if (expiredFiles.isNotEmpty()) {
                    val totalSize = expiredFiles.sumOf { it.fileSize }
                    suggestions.add(
                        CleanupSuggestion(
                            type = CleanupType.EXPIRED_FILES,
                            description = "Remove ${expiredFiles.size} expired files from ${service.displayName} (${formatFileSize(totalSize)})",
                            potentialSpaceSaved = totalSize,
                            priority = Priority.HIGH
                        )
                    )
                }
                
                if (oldFiles.isNotEmpty()) {
                    val totalSize = oldFiles.sumOf { it.fileSize }
                    suggestions.add(
                        CleanupSuggestion(
                            type = CleanupType.OLD_FILES,
                            description = "Remove ${oldFiles.size} old files from ${service.displayName} (${formatFileSize(totalSize)})",
                            potentialSpaceSaved = totalSize,
                            priority = Priority.MEDIUM
                        )
                    )
                }
            } catch (e: Exception) {
                // Continue with other services
            }
        }
        
        return suggestions.sortedByDescending { it.priority }
    }
    
    /**
     * Optimize storage by compressing and deduplicating files
     */
    suspend fun optimizeStorage(): OptimizationResult = withContext(Dispatchers.IO) {
        // This would implement file compression and deduplication
        // For now, return a placeholder result
        OptimizationResult(
            filesOptimized = 0,
            spaceSaved = 0L,
            compressionRatio = 0f
        )
    }
    
    private fun cleanupDirectory(directory: File, maxAgeMillis: Long): DirectoryCleanupResult {
        var filesDeleted = 0
        var spaceFreed = 0L
        val currentTime = System.currentTimeMillis()
        
        directory.listFiles()?.forEach { file ->
            if (file.isFile && (currentTime - file.lastModified()) > maxAgeMillis) {
                val fileSize = file.length()
                if (file.delete()) {
                    filesDeleted++
                    spaceFreed += fileSize
                }
            } else if (file.isDirectory) {
                val subResult = cleanupDirectory(file, maxAgeMillis)
                filesDeleted += subResult.filesDeleted
                spaceFreed += subResult.spaceFreed
                
                // Remove empty directories
                if (file.listFiles()?.isEmpty() == true) {
                    file.delete()
                }
            }
        }
        
        return DirectoryCleanupResult(filesDeleted, spaceFreed)
    }
    
    private fun analyzeLocalStorage(): LocalStorageAnalysis {
        val cacheDir = context.cacheDir
        var totalSize = 0L
        var fileCount = 0
        
        fun analyzeDirectory(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    totalSize += file.length()
                    fileCount++
                } else if (file.isDirectory) {
                    analyzeDirectory(file)
                }
            }
        }
        
        analyzeDirectory(cacheDir)
        
        return LocalStorageAnalysis(totalSize, fileCount)
    }
    
    private fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
}

/**
 * Result of cleanup operations
 */
data class CleanupResult(
    val totalFilesDeleted: Int,
    val totalSpaceFreed: Long,
    val serviceResults: List<ServiceCleanupResult>
)

/**
 * Result of cleanup for a specific service
 */
data class ServiceCleanupResult(
    val service: CloudService,
    val filesDeleted: Int,
    val spaceFreed: Long,
    val error: String?
)

/**
 * Result of local file cleanup
 */
data class LocalCleanupResult(
    val filesDeleted: Int,
    val spaceFreed: Long
)

/**
 * Result of directory cleanup
 */
private data class DirectoryCleanupResult(
    val filesDeleted: Int,
    val spaceFreed: Long
)

/**
 * Local storage analysis result
 */
private data class LocalStorageAnalysis(
    val totalSize: Long,
    val fileCount: Int
)

/**
 * Cleanup suggestion for users
 */
data class CleanupSuggestion(
    val type: CleanupType,
    val description: String,
    val potentialSpaceSaved: Long,
    val priority: Priority
)

/**
 * Types of cleanup operations
 */
enum class CleanupType {
    LOCAL_CACHE,
    EXPIRED_FILES,
    OLD_FILES,
    DUPLICATE_FILES,
    LARGE_FILES
}

/**
 * Result of storage optimization
 */
data class OptimizationResult(
    val filesOptimized: Int,
    val spaceSaved: Long,
    val compressionRatio: Float
)