package com.chain.messaging.core.group

import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing group message history and synchronization in large groups.
 * Handles efficient storage, retrieval, and synchronization of message history.
 */
interface GroupHistoryManager {
    
    /**
     * Synchronizes message history for a user joining a large group.
     * Uses incremental sync to minimize data transfer.
     */
    suspend fun synchronizeHistoryForNewMember(
        groupId: String,
        userId: String,
        fromTimestamp: Long? = null
    ): Result<SyncResult>
    
    /**
     * Gets paginated message history for a group with efficient loading.
     */
    suspend fun getGroupHistory(
        groupId: String,
        userId: String,
        limit: Int = 50,
        beforeTimestamp: Long? = null
    ): Result<List<Message>>
    
    /**
     * Manages message history pruning for large groups to control storage.
     */
    suspend fun pruneGroupHistory(
        groupId: String,
        retentionPeriodMs: Long,
        keepImportantMessages: Boolean = true
    ): Result<PruningResult>
    
    /**
     * Creates and manages message history snapshots for efficient sync.
     */
    suspend fun createHistorySnapshot(
        groupId: String,
        timestamp: Long
    ): Result<HistorySnapshot>
    
    /**
     * Synchronizes from a history snapshot for faster initial sync.
     */
    suspend fun syncFromSnapshot(
        groupId: String,
        userId: String,
        snapshotId: String
    ): Result<SyncResult>
    
    /**
     * Handles message gaps and ensures consistency in large groups.
     */
    suspend fun detectAndFillMessageGaps(
        groupId: String,
        userId: String
    ): Result<GapFillResult>
    
    /**
     * Optimizes message storage for large groups with compression.
     */
    suspend fun optimizeMessageStorage(
        groupId: String,
        compressionLevel: CompressionLevel = CompressionLevel.MEDIUM
    ): Result<OptimizationResult>
    
    /**
     * Observes real-time message history updates.
     */
    fun observeHistoryUpdates(groupId: String): Flow<HistoryUpdate>
    
    /**
     * Gets message history statistics for a group.
     */
    suspend fun getHistoryStatistics(groupId: String): HistoryStatistics
    
    /**
     * Exports group history for backup or migration.
     */
    suspend fun exportGroupHistory(
        groupId: String,
        format: ExportFormat,
        dateRange: DateRange? = null
    ): Result<ExportResult>
}

/**
 * Result of history synchronization operation
 */
data class SyncResult(
    val syncId: String,
    val messagesSynced: Int,
    val bytesTransferred: Long,
    val syncDurationMs: Long,
    val fromTimestamp: Long,
    val toTimestamp: Long,
    val hasMoreData: Boolean
)

/**
 * Result of message history pruning
 */
data class PruningResult(
    val messagesRemoved: Int,
    val bytesFreed: Long,
    val oldestRemainingTimestamp: Long,
    val importantMessagesKept: Int
)

/**
 * History snapshot for efficient synchronization
 */
data class HistorySnapshot(
    val snapshotId: String,
    val groupId: String,
    val timestamp: Long,
    val messageCount: Int,
    val compressedSize: Long,
    val checksum: String
)

/**
 * Result of gap filling operation
 */
data class GapFillResult(
    val gapsDetected: Int,
    val gapsFilled: Int,
    val messagesFetched: Int,
    val inconsistenciesResolved: Int
)

/**
 * Result of storage optimization
 */
data class OptimizationResult(
    val originalSize: Long,
    val optimizedSize: Long,
    val compressionRatio: Float,
    val messagesOptimized: Int
)

/**
 * Real-time history update notification
 */
data class HistoryUpdate(
    val groupId: String,
    val updateType: HistoryUpdateType,
    val messageId: String?,
    val timestamp: Long,
    val affectedUserIds: List<String>
)

/**
 * Group message history statistics
 */
data class HistoryStatistics(
    val groupId: String,
    val totalMessages: Long,
    val totalSizeBytes: Long,
    val oldestMessageTimestamp: Long,
    val newestMessageTimestamp: Long,
    val averageMessageSize: Long,
    val activeParticipants: Int,
    val messagesPerDay: Float
)

/**
 * Export result for group history
 */
data class ExportResult(
    val exportId: String,
    val filePath: String,
    val format: ExportFormat,
    val messageCount: Int,
    val fileSizeBytes: Long,
    val checksum: String
)

/**
 * Date range for history operations
 */
data class DateRange(
    val startTimestamp: Long,
    val endTimestamp: Long
)

/**
 * Types of history updates
 */
enum class HistoryUpdateType {
    MESSAGE_ADDED,
    MESSAGE_UPDATED,
    MESSAGE_DELETED,
    BATCH_SYNC_COMPLETED,
    PRUNING_COMPLETED
}

/**
 * Compression levels for message storage
 */
enum class CompressionLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    MAXIMUM
}

/**
 * Export formats for group history
 */
enum class ExportFormat {
    JSON,
    CSV,
    BINARY,
    ENCRYPTED_ARCHIVE
}