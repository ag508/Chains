package com.chain.messaging.core.sync

import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Service for synchronizing data across user devices
 */
interface CrossDeviceSyncService {
    /**
     * Initialize cross-device synchronization
     */
    suspend fun initialize()
    
    /**
     * Sync message history with other devices
     */
    suspend fun syncMessageHistory(): SyncResult
    
    /**
     * Sync encryption keys with other devices
     */
    suspend fun syncEncryptionKeys(): SyncResult
    
    /**
     * Sync user settings and preferences
     */
    suspend fun syncUserSettings(): SyncResult
    
    /**
     * Perform full synchronization with all trusted devices
     */
    suspend fun performFullSync(): SyncResult
    
    /**
     * Get synchronization status
     */
    fun getSyncStatus(): Flow<CrossDeviceSyncStatus>
    
    /**
     * Request sync from a specific device
     */
    suspend fun requestSyncFromDevice(deviceId: String): SyncResult
    
    /**
     * Handle incoming sync request from another device
     */
    suspend fun handleSyncRequest(request: SyncRequest): SyncResponse
    
    /**
     * Get sync progress for UI
     */
    fun getSyncProgress(): Flow<SyncProgress>
    
    /**
     * Enable or disable automatic synchronization
     */
    suspend fun setAutoSyncEnabled(enabled: Boolean)
    
    /**
     * Check if auto sync is enabled
     */
    suspend fun isAutoSyncEnabled(): Boolean
    
    /**
     * Start synchronization
     */
    suspend fun startSync()
    
    /**
     * Shutdown sync service
     */
    suspend fun shutdown()
    
    /**
     * Resume synchronization
     */
    suspend fun resumeSync()
}

/**
 * Cross-device synchronization status
 */
data class CrossDeviceSyncStatus(
    val isEnabled: Boolean,
    val lastSyncTime: java.time.LocalDateTime?,
    val connectedDevices: Int,
    val trustedDevices: Int,
    val pendingSyncs: Int,
    val syncErrors: List<SyncError>
)

/**
 * Synchronization progress information
 */
data class SyncProgress(
    val phase: SyncPhase,
    val progress: Float, // 0.0 to 1.0
    val currentOperation: String,
    val estimatedTimeRemaining: Long? = null // in milliseconds
)

/**
 * Phases of synchronization
 */
enum class SyncPhase {
    INITIALIZING,
    DISCOVERING_DEVICES,
    SYNCING_KEYS,
    SYNCING_MESSAGES,
    SYNCING_SETTINGS,
    FINALIZING,
    COMPLETED,
    ERROR
}

/**
 * Synchronization request from another device
 */
data class SyncRequest(
    val requestId: String,
    val fromDeviceId: String,
    val syncType: SyncType,
    val timestamp: java.time.LocalDateTime,
    val lastSyncTime: java.time.LocalDateTime? = null
)

/**
 * Response to a synchronization request
 */
data class SyncResponse(
    val requestId: String,
    val success: Boolean,
    val data: SyncData? = null,
    val error: String? = null,
    val nextSyncTime: java.time.LocalDateTime? = null
)

/**
 * Types of synchronization
 */
enum class SyncType {
    FULL,           // Complete synchronization
    INCREMENTAL,    // Only changes since last sync
    MESSAGES_ONLY,  // Only message history
    KEYS_ONLY,      // Only encryption keys
    SETTINGS_ONLY   // Only user settings
}

/**
 * Data payload for synchronization
 */
data class SyncData(
    val messages: List<Message> = emptyList(),
    val encryptionKeys: Map<String, String> = emptyMap(),
    val userSettings: Map<String, Any> = emptyMap(),
    val deviceInfo: DeviceInfo? = null
)