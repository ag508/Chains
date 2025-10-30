package com.chain.messaging.core.offline

import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Service for managing offline functionality and synchronization
 */
interface OfflineSyncService {
    /**
     * Initialize offline sync service
     */
    suspend fun initialize()
    
    /**
     * Handle a message when offline
     */
    suspend fun handleOfflineMessage(message: Message)
    
    /**
     * Sync all pending messages when connection is restored
     */
    suspend fun syncPendingMessages(): SyncResult
    
    /**
     * Get sync status as a flow
     */
    fun getSyncStatus(): Flow<SyncStatus>
    
    /**
     * Force a manual sync
     */
    suspend fun forcSync(): SyncResult
    
    /**
     * Get offline statistics
     */
    suspend fun getOfflineStats(): OfflineStats
    
    /**
     * Clear all offline data
     */
    suspend fun clearOfflineData()
}

/**
 * Result of synchronization operation
 */
data class SyncResult(
    val success: Boolean,
    val messagesSynced: Int,
    val conflictsResolved: Int,
    val errors: List<SyncError>,
    val duration: Long // in milliseconds
)

/**
 * Current synchronization status
 */
data class SyncStatus(
    val isOnline: Boolean,
    val isSyncing: Boolean,
    val pendingMessages: Int,
    val lastSyncTime: java.time.LocalDateTime?,
    val syncProgress: Float = 0f // 0.0 to 1.0
)

/**
 * Offline usage statistics
 */
data class OfflineStats(
    val totalQueuedMessages: Int,
    val failedMessages: Int,
    val averageRetryCount: Double,
    val oldestQueuedMessage: java.time.LocalDateTime?,
    val totalOfflineTime: Long // in milliseconds
)

/**
 * Synchronization error
 */
data class SyncError(
    val messageId: String,
    val error: String,
    val timestamp: java.time.LocalDateTime = java.time.LocalDateTime.now()
)