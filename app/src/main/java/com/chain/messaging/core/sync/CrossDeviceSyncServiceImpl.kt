package com.chain.messaging.core.sync

import android.content.Context
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.core.util.TimeUtils
import com.chain.messaging.core.util.toLong
import com.chain.messaging.data.local.dao.DeviceDao
import com.chain.messaging.data.local.dao.MessageDao
import com.chain.messaging.data.local.dao.SyncLogDao
import com.chain.messaging.data.local.entity.SyncLogEntity
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.data.local.entity.RegisteredDeviceEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrossDeviceSyncServiceImpl @Inject constructor(
    private val context: Context,
    private val deviceManager: DeviceManager,
    private val deviceDao: DeviceDao,
    private val syncLogDao: SyncLogDao,
    private val messageDao: MessageDao,
    private val messageRepository: MessageRepository,
    private val keyManager: KeyManager,
    private val blockchainManager: BlockchainManager
) : CrossDeviceSyncService {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _syncStatus = MutableStateFlow(createInitialSyncStatus())
    private val _syncProgress = MutableStateFlow(SyncProgress(SyncPhase.INITIALIZING, 0f, "Initializing"))
    
    private var isInitialized = false
    private var autoSyncEnabled = true
    
    override suspend fun initialize() {
        if (isInitialized) return
        
        try {
            // Load auto-sync preference
            val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            autoSyncEnabled = prefs.getBoolean("auto_sync_enabled", true)
            
            // Start monitoring for sync opportunities
            startSyncMonitoring()
            
            // Update sync status
            updateSyncStatus()
            
            isInitialized = true
        } catch (e: Exception) {
            _syncProgress.value = SyncProgress(SyncPhase.ERROR, 0f, "Initialization failed: ${e.message}")
        }
    }
    
    override suspend fun syncMessageHistory(): SyncResult {
        val syncId = UUID.randomUUID().toString()
        val currentDevice = deviceManager.getCurrentDevice()
        
        return try {
            logSyncStart(syncId, currentDevice.deviceId, SyncType.MESSAGES_ONLY)
            _syncProgress.value = SyncProgress(SyncPhase.SYNCING_MESSAGES, 0f, "Starting message sync")
            
            val trustedDevices = deviceDao.getTrustedDevices()
                .filter { !it.isCurrentDevice }
            
            if (trustedDevices.isEmpty()) {
                return SyncResult.Success("No trusted devices to sync with")
            }
            
            var totalMessagesSynced = 0
            val errors = mutableListOf<String>()
            
            for ((index, device) in trustedDevices.withIndex()) {
                try {
                    val progress = (index.toFloat() / trustedDevices.size) * 0.8f
                    _syncProgress.value = SyncProgress(
                        SyncPhase.SYNCING_MESSAGES, 
                        progress, 
                        "Syncing with ${device.deviceName}"
                    )
                    
                    val result = syncMessagesWithDevice(device.deviceId)
                    if (result is SyncResult.Success) {
                        totalMessagesSynced += result.data?.messages?.size ?: 0
                    } else if (result is SyncResult.Error) {
                        errors.add("${device.deviceName}: ${result.message}")
                    }
                } catch (e: Exception) {
                    errors.add("${device.deviceName}: ${e.message}")
                }
            }
            
            _syncProgress.value = SyncProgress(SyncPhase.FINALIZING, 0.9f, "Finalizing message sync")
            
            logSyncComplete(syncId, totalMessagesSynced, 0, 0, errors.firstOrNull())
            
            if (errors.isEmpty()) {
                SyncResult.Success("Synced $totalMessagesSynced messages")
            } else {
                SyncResult.PartialSuccess("Synced $totalMessagesSynced messages with ${errors.size} errors", errors)
            }
            
        } catch (e: Exception) {
            logSyncError(syncId, e.message ?: "Unknown error")
            SyncResult.Error("Message sync failed: ${e.message}")
        } finally {
            _syncProgress.value = SyncProgress(SyncPhase.COMPLETED, 1f, "Message sync completed")
            updateSyncStatus()
        }
    }
    
    override suspend fun syncEncryptionKeys(): SyncResult {
        val syncId = UUID.randomUUID().toString()
        val currentDevice = deviceManager.getCurrentDevice()
        
        return try {
            logSyncStart(syncId, currentDevice.deviceId, SyncType.KEYS_ONLY)
            _syncProgress.value = SyncProgress(SyncPhase.SYNCING_KEYS, 0f, "Starting key sync")
            
            val trustedDevices = deviceDao.getTrustedDevices()
                .filter { !it.isCurrentDevice }
            
            if (trustedDevices.isEmpty()) {
                return SyncResult.Success("No trusted devices to sync keys with")
            }
            
            var totalKeysSynced = 0
            val errors = mutableListOf<String>()
            
            for ((index, device) in trustedDevices.withIndex()) {
                try {
                    val progress = (index.toFloat() / trustedDevices.size) * 0.8f
                    _syncProgress.value = SyncProgress(
                        SyncPhase.SYNCING_KEYS, 
                        progress, 
                        "Syncing keys with ${device.deviceName}"
                    )
                    
                    val result = syncKeysWithDevice(device.deviceId)
                    if (result is SyncResult.Success) {
                        totalKeysSynced += result.data?.encryptionKeys?.size ?: 0
                    } else if (result is SyncResult.Error) {
                        errors.add("${device.deviceName}: ${result.message}")
                    }
                } catch (e: Exception) {
                    errors.add("${device.deviceName}: ${e.message}")
                }
            }
            
            _syncProgress.value = SyncProgress(SyncPhase.FINALIZING, 0.9f, "Finalizing key sync")
            
            logSyncComplete(syncId, 0, totalKeysSynced, 0, errors.firstOrNull())
            
            if (errors.isEmpty()) {
                SyncResult.Success("Synced $totalKeysSynced keys")
            } else {
                SyncResult.PartialSuccess("Synced $totalKeysSynced keys with ${errors.size} errors", errors)
            }
            
        } catch (e: Exception) {
            logSyncError(syncId, e.message ?: "Unknown error")
            SyncResult.Error("Key sync failed: ${e.message}")
        } finally {
            _syncProgress.value = SyncProgress(SyncPhase.COMPLETED, 1f, "Key sync completed")
            updateSyncStatus()
        }
    }
    
    override suspend fun syncUserSettings(): SyncResult {
        val syncId = UUID.randomUUID().toString()
        val currentDevice = deviceManager.getCurrentDevice()
        
        return try {
            logSyncStart(syncId, currentDevice.deviceId, SyncType.SETTINGS_ONLY)
            _syncProgress.value = SyncProgress(SyncPhase.SYNCING_SETTINGS, 0f, "Starting settings sync")
            
            val trustedDevices = deviceDao.getTrustedDevices()
                .filter { !it.isCurrentDevice }
            
            if (trustedDevices.isEmpty()) {
                return SyncResult.Success("No trusted devices to sync settings with")
            }
            
            var totalSettingsSynced = 0
            val errors = mutableListOf<String>()
            
            for ((index, device) in trustedDevices.withIndex()) {
                try {
                    val progress = (index.toFloat() / trustedDevices.size) * 0.8f
                    _syncProgress.value = SyncProgress(
                        SyncPhase.SYNCING_SETTINGS, 
                        progress, 
                        "Syncing settings with ${device.deviceName}"
                    )
                    
                    val result = syncSettingsWithDevice(device.deviceId)
                    if (result is SyncResult.Success) {
                        totalSettingsSynced += result.data?.userSettings?.size ?: 0
                    } else if (result is SyncResult.Error) {
                        errors.add("${device.deviceName}: ${result.message}")
                    }
                } catch (e: Exception) {
                    errors.add("${device.deviceName}: ${e.message}")
                }
            }
            
            _syncProgress.value = SyncProgress(SyncPhase.FINALIZING, 0.9f, "Finalizing settings sync")
            
            logSyncComplete(syncId, 0, 0, totalSettingsSynced, errors.firstOrNull())
            
            if (errors.isEmpty()) {
                SyncResult.Success("Synced $totalSettingsSynced settings")
            } else {
                SyncResult.PartialSuccess("Synced $totalSettingsSynced settings with ${errors.size} errors", errors)
            }
            
        } catch (e: Exception) {
            logSyncError(syncId, e.message ?: "Unknown error")
            SyncResult.Error("Settings sync failed: ${e.message}")
        } finally {
            _syncProgress.value = SyncProgress(SyncPhase.COMPLETED, 1f, "Settings sync completed")
            updateSyncStatus()
        }
    }
    
    override suspend fun performFullSync(): SyncResult {
        val syncId = UUID.randomUUID().toString()
        val currentDevice = deviceManager.getCurrentDevice()
        
        return try {
            logSyncStart(syncId, currentDevice.deviceId, SyncType.FULL)
            _syncProgress.value = SyncProgress(SyncPhase.DISCOVERING_DEVICES, 0f, "Discovering devices")
            
            val trustedDevices = deviceDao.getTrustedDevices()
                .filter { !it.isCurrentDevice }
            
            if (trustedDevices.isEmpty()) {
                return SyncResult.Success("No trusted devices to sync with")
            }
            
            val results = mutableListOf<SyncResult>()
            
            // Sync keys first (20% of progress)
            _syncProgress.value = SyncProgress(SyncPhase.SYNCING_KEYS, 0.1f, "Syncing encryption keys")
            results.add(syncEncryptionKeys())
            
            // Sync messages (60% of progress)
            _syncProgress.value = SyncProgress(SyncPhase.SYNCING_MESSAGES, 0.3f, "Syncing message history")
            results.add(syncMessageHistory())
            
            // Sync settings (20% of progress)
            _syncProgress.value = SyncProgress(SyncPhase.SYNCING_SETTINGS, 0.9f, "Syncing user settings")
            results.add(syncUserSettings())
            
            _syncProgress.value = SyncProgress(SyncPhase.FINALIZING, 0.95f, "Finalizing full sync")
            
            val errors = results.filterIsInstance<SyncResult.Error>()
            val partialErrors = results.filterIsInstance<SyncResult.PartialSuccess>()
            
            val totalMessages = results.sumOf { it.data?.messages?.size ?: 0 }
            val totalKeys = results.sumOf { it.data?.encryptionKeys?.size ?: 0 }
            val totalSettings = results.sumOf { it.data?.userSettings?.size ?: 0 }
            
            logSyncComplete(syncId, totalMessages, totalKeys, totalSettings, 
                (errors + partialErrors).firstOrNull()?.message)
            
            when {
                errors.isNotEmpty() -> SyncResult.Error("Full sync failed: ${errors.first().message}")
                partialErrors.isNotEmpty() -> SyncResult.PartialSuccess(
                    "Full sync completed with some errors", 
                    partialErrors.flatMap { it.errors }
                )
                else -> SyncResult.Success("Full sync completed successfully")
            }
            
        } catch (e: Exception) {
            logSyncError(syncId, e.message ?: "Unknown error")
            SyncResult.Error("Full sync failed: ${e.message}")
        } finally {
            _syncProgress.value = SyncProgress(SyncPhase.COMPLETED, 1f, "Full sync completed")
            updateSyncStatus()
        }
    }
    
    override fun getSyncStatus(): Flow<CrossDeviceSyncStatus> = _syncStatus.asStateFlow()
    
    override suspend fun requestSyncFromDevice(deviceId: String): SyncResult {
        return try {
            val device = deviceDao.getDeviceById(deviceId)
                ?: return SyncResult.Error("Device not found")
            
            if (!device.isTrusted) {
                return SyncResult.Error("Device is not trusted")
            }
            
            val request = SyncRequest(
                requestId = UUID.randomUUID().toString(),
                fromDeviceId = deviceManager.getCurrentDevice().deviceId,
                syncType = SyncType.INCREMENTAL,
                timestamp = LocalDateTime.now(),
                lastSyncTime = device.lastSyncAt
            )
            
            // Send sync request through blockchain
            val response = sendSyncRequest(deviceId, request)
            
            if (response.success) {
                response.data?.let { data ->
                    processSyncData(data)
                }
                SyncResult.Success("Sync request completed")
            } else {
                SyncResult.Error(response.error ?: "Sync request failed")
            }
            
        } catch (e: Exception) {
            SyncResult.Error("Failed to request sync: ${e.message}")
        }
    }
    
    override suspend fun handleSyncRequest(request: SyncRequest): SyncResponse {
        return try {
            val device = deviceDao.getDeviceById(request.fromDeviceId)
            
            if (device == null || !device.isTrusted) {
                return SyncResponse(
                    requestId = request.requestId,
                    success = false,
                    error = "Device not trusted"
                )
            }
            
            val syncData = when (request.syncType) {
                SyncType.FULL -> gatherFullSyncData()
                SyncType.INCREMENTAL -> gatherIncrementalSyncData(request.lastSyncTime)
                SyncType.MESSAGES_ONLY -> gatherMessageSyncData(request.lastSyncTime)
                SyncType.KEYS_ONLY -> gatherKeySyncData()
                SyncType.SETTINGS_ONLY -> gatherSettingsSyncData()
            }
            
            SyncResponse(
                requestId = request.requestId,
                success = true,
                data = syncData,
                nextSyncTime = LocalDateTime.now().plusHours(1)
            )
            
        } catch (e: Exception) {
            SyncResponse(
                requestId = request.requestId,
                success = false,
                error = e.message
            )
        }
    }
    
    override fun getSyncProgress(): Flow<SyncProgress> = _syncProgress.asStateFlow()
    
    override suspend fun setAutoSyncEnabled(enabled: Boolean) {
        autoSyncEnabled = enabled
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_sync_enabled", enabled).apply()
        
        if (enabled) {
            startSyncMonitoring()
        }
        
        updateSyncStatus()
    }
    
    override suspend fun isAutoSyncEnabled(): Boolean = autoSyncEnabled
    
    override suspend fun startSync() {
        if (!isInitialized) {
            initialize()
        }
        
        _syncProgress.value = SyncProgress(SyncPhase.INITIALIZING, 0f, "Starting synchronization")
        
        // Perform full sync
        performFullSync()
    }
    
    override suspend fun shutdown() {
        scope.cancel()
        _syncProgress.value = SyncProgress(SyncPhase.COMPLETED, 1f, "Sync service shutdown")
    }
    
    override suspend fun resumeSync() {
        if (autoSyncEnabled) {
            startSyncMonitoring()
            _syncProgress.value = SyncProgress(SyncPhase.INITIALIZING, 0f, "Resuming synchronization")
        }
    }
    
    // Private helper methods
    
    private suspend fun syncMessagesWithDevice(deviceId: String): SyncResult {
        // Implementation would sync messages with specific device
        // This is a simplified version
        return try {
            val messages = messageDao.getRecentMessages(100) // Get recent messages
            val syncData = SyncData(messages = messages.map { it.toMessage() })
            SyncResult.Success("Messages synced", syncData)
        } catch (e: Exception) {
            SyncResult.Error("Failed to sync messages: ${e.message}")
        }
    }
    
    private suspend fun syncKeysWithDevice(deviceId: String): SyncResult {
        // Implementation would sync encryption keys with specific device
        return try {
            val keys = keyManager.exportKeys() // Hypothetical method
            val syncData = SyncData(encryptionKeys = keys)
            SyncResult.Success("Keys synced", syncData)
        } catch (e: Exception) {
            SyncResult.Error("Failed to sync keys: ${e.message}")
        }
    }
    
    private suspend fun syncSettingsWithDevice(deviceId: String): SyncResult {
        // Implementation would sync user settings with specific device
        return try {
            val settings = gatherUserSettings()
            val syncData = SyncData(userSettings = settings)
            SyncResult.Success("Settings synced", syncData)
        } catch (e: Exception) {
            SyncResult.Error("Failed to sync settings: ${e.message}")
        }
    }
    
    private suspend fun sendSyncRequest(deviceId: String, request: SyncRequest): SyncResponse {
        return try {
            // Get device information for encryption
            val targetDevice = deviceDao.getDeviceById(deviceId)
                ?: return SyncResponse(
                    requestId = request.requestId,
                    success = false,
                    error = "Target device not found"
                )
            
            // Serialize the sync request
            val requestJson = serializeSyncRequest(request)
            
            // Send the sync request through blockchain as a system message
            val transactionHash = blockchainManager.sendMessage(
                recipientId = deviceId,
                encryptedContent = requestJson,
                messageType = "SYNC_REQUEST"
            )
            
            // Wait for response with timeout (30 seconds)
            val response = waitForSyncResponse(request.requestId, timeoutMs = 30_000)
            
            if (response != null) {
                // Update device sync status
                updateDeviceSyncStatus(deviceId, true)
                response
            } else {
                SyncResponse(
                    requestId = request.requestId,
                    success = false,
                    error = "Sync request timed out"
                )
            }
            
        } catch (e: Exception) {
            SyncResponse(
                requestId = request.requestId,
                success = false,
                error = "Failed to send sync request: ${e.message}"
            )
        }
    }
    
    private suspend fun processSyncData(data: SyncData) {
        // Process received sync data
        data.messages.forEach { message ->
            messageRepository.saveMessage(message)
        }
        
        data.encryptionKeys.forEach { (keyId, key) ->
            keyManager.importKey(keyId, key)
        }
        
        data.userSettings.forEach { (key, value) ->
            saveUserSetting(key, value)
        }
    }
    
    private suspend fun gatherFullSyncData(): SyncData {
        return SyncData(
            messages = messageDao.getAllMessages().map { it.toMessage() },
            encryptionKeys = keyManager.exportKeys(),
            userSettings = gatherUserSettings()
        )
    }
    
    private suspend fun gatherIncrementalSyncData(since: LocalDateTime?): SyncData {
        val cutoff = since ?: LocalDateTime.now().minusDays(7)
        val cutoffTimestamp = cutoff.toLong()
        return SyncData(
            messages = messageDao.getMessagesSince(cutoffTimestamp).map { it.toMessage() },
            encryptionKeys = keyManager.exportRecentKeys(cutoff),
            userSettings = gatherUserSettings()
        )
    }
    
    private suspend fun gatherMessageSyncData(since: LocalDateTime?): SyncData {
        val cutoff = since ?: LocalDateTime.now().minusDays(7)
        val cutoffTimestamp = cutoff.toLong()
        return SyncData(
            messages = messageDao.getMessagesSince(cutoffTimestamp).map { it.toMessage() }
        )
    }
    
    private suspend fun gatherKeySyncData(): SyncData {
        return SyncData(
            encryptionKeys = keyManager.exportKeys()
        )
    }
    
    private suspend fun gatherSettingsSyncData(): SyncData {
        return SyncData(
            userSettings = gatherUserSettings()
        )
    }
    
    private fun gatherUserSettings(): Map<String, Any> {
        val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        return prefs.all
    }
    
    private fun saveUserSetting(key: String, value: Any) {
        val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
        }
        
        editor.apply()
    }
    
    private fun startSyncMonitoring() {
        if (!autoSyncEnabled) return
        
        scope.launch {
            // Monitor for sync opportunities every 30 minutes
            while (autoSyncEnabled) {
                delay(30 * 60 * 1000) // 30 minutes
                
                try {
                    val devicesNeedingSync = deviceDao.getDevicesNeedingSync()
                    if (devicesNeedingSync.isNotEmpty()) {
                        performFullSync()
                    }
                } catch (e: Exception) {
                    // Log error but continue monitoring
                }
            }
        }
    }
    
    private suspend fun updateSyncStatus() {
        val trustedDevices = deviceDao.getTrustedDeviceCount()
        val pendingSyncs = deviceDao.getPendingSyncCount()
        val lastSync = syncLogDao.getLastSuccessfulSync(deviceManager.getCurrentDevice().deviceId)
        val errors = syncLogDao.getFailedSyncs().take(5).map { 
            SyncError(it.errorMessage ?: "Unknown error", it.startTime)
        }
        
        _syncStatus.value = CrossDeviceSyncStatus(
            isEnabled = autoSyncEnabled,
            lastSyncTime = lastSync?.endTime,
            connectedDevices = trustedDevices, // Simplified
            trustedDevices = trustedDevices,
            pendingSyncs = pendingSyncs,
            syncErrors = errors
        )
    }
    
    private fun createInitialSyncStatus(): CrossDeviceSyncStatus {
        return CrossDeviceSyncStatus(
            isEnabled = false,
            lastSyncTime = null,
            connectedDevices = 0,
            trustedDevices = 0,
            pendingSyncs = 0,
            syncErrors = emptyList()
        )
    }
    
    private suspend fun logSyncStart(syncId: String, deviceId: String, syncType: SyncType) {
        val syncLog = SyncLogEntity(
            id = syncId,
            deviceId = deviceId,
            syncType = syncType.name,
            status = "IN_PROGRESS",
            startTime = LocalDateTime.now()
        )
        syncLogDao.insertSyncLog(syncLog)
    }
    
    private suspend fun logSyncComplete(
        syncId: String, 
        messagesSynced: Int, 
        keysSynced: Int, 
        settingsSynced: Int,
        error: String?
    ) {
        val syncLog = syncLogDao.getRecentSyncLogs(1).find { it.id == syncId }
        syncLog?.let {
            val updatedLog = it.copy(
                status = if (error == null) "SUCCESS" else "ERROR",
                endTime = LocalDateTime.now(),
                messagesSynced = messagesSynced,
                keysSynced = keysSynced,
                settingsSynced = settingsSynced,
                errorMessage = error
            )
            syncLogDao.updateSyncLog(updatedLog)
        }
    }
    
    private suspend fun logSyncError(syncId: String, error: String) {
        logSyncComplete(syncId, 0, 0, 0, error)
    }
    
    /**
     * Serialize sync request to JSON format
     */
    private fun serializeSyncRequest(request: SyncRequest): String {
        return """
        {
            "requestId": "${request.requestId}",
            "fromDeviceId": "${request.fromDeviceId}",
            "syncType": "${request.syncType}",
            "timestamp": "${request.timestamp}",
            "lastSyncTime": ${if (request.lastSyncTime != null) "\"${request.lastSyncTime}\"" else "null"}
        }
        """.trimIndent()
    }
    
    /**
     * Wait for sync response from the target device
     */
    private suspend fun waitForSyncResponse(requestId: String, timeoutMs: Long): SyncResponse? {
        val startTime = System.currentTimeMillis()
        
        // Subscribe to incoming messages to catch the response
        val currentUserId = deviceManager.getCurrentDevice().deviceId
        
        return try {
            // Use withTimeoutOrNull to handle timeout properly
            withTimeoutOrNull(timeoutMs) {
                blockchainManager.subscribeToMessages(currentUserId)
                    .firstOrNull { incomingMessage ->
                        if (incomingMessage.type == "SYNC_RESPONSE") {
                            val response = parseSyncResponse(incomingMessage.encryptedContent)
                            response?.requestId == requestId
                        } else {
                            false
                        }
                    }
                    ?.let { incomingMessage ->
                        parseSyncResponse(incomingMessage.encryptedContent)
                    }
            }
        } catch (e: Exception) {
            null // Error occurred
        }
    }
    
    /**
     * Parse sync response from JSON
     */
    private fun parseSyncResponse(responseJson: String): SyncResponse? {
        return try {
            // Simple JSON parsing - in production, use a proper JSON library like Gson or Moshi
            val lines = responseJson.lines().map { it.trim() }
            
            val requestId = extractJsonValue(lines, "requestId")
            val successStr = extractJsonValue(lines, "success")
            val success = when (successStr?.lowercase()) {
                "true" -> true
                "false" -> false
                else -> false
            }
            val error = extractJsonValue(lines, "error")
            val nextSyncTimeStr = extractJsonValue(lines, "nextSyncTime")
            
            val nextSyncTime = nextSyncTimeStr?.let { 
                try {
                    LocalDateTime.parse(it)
                } catch (e: Exception) {
                    null
                }
            }
            
            SyncResponse(
                requestId = requestId ?: "",
                success = success,
                error = error,
                data = if (success) parseSyncData(responseJson) else null,
                nextSyncTime = nextSyncTime
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse sync data from response JSON
     */
    private fun parseSyncData(responseJson: String): SyncData? {
        return try {
            // Simplified parsing - extract basic sync data
            // In production, use proper JSON serialization
            SyncData(
                messages = emptyList(), // Would parse actual message data
                encryptionKeys = emptyMap(), // Would parse actual key data
                userSettings = emptyMap() // Would parse actual settings data
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extract JSON value by key (simple implementation)
     */
    private fun extractJsonValue(lines: List<String>, key: String): String? {
        val line = lines.find { it.contains("\"$key\"") } ?: return null
        
        // Try quoted string pattern first
        val quotedPattern = "\"$key\":\\s*\"([^\"]*)\""
        Regex(quotedPattern).find(line)?.let { 
            return it.groupValues[1]
        }
        
        // Try unquoted value pattern (for booleans, numbers, null)
        val unquotedPattern = "\"$key\":\\s*([^,}\\s]+)"
        Regex(unquotedPattern).find(line)?.let {
            return it.groupValues[1]
        }
        
        return null
    }
    
    /**
     * Update device sync status after successful sync
     */
    private suspend fun updateDeviceSyncStatus(deviceId: String, success: Boolean) {
        try {
            val device = deviceDao.getDeviceById(deviceId)
            device?.let { existingDevice ->
                val updatedDevice = existingDevice.copy(
                    lastSyncAt = LocalDateTime.now(),
                    syncStatus = if (success) com.chain.messaging.core.sync.SyncStatus.SYNCED.name 
                                else com.chain.messaging.core.sync.SyncStatus.ERROR.name
                )
                deviceDao.updateDevice(updatedDevice)
            }
        } catch (e: Exception) {
            // Log error but don't fail the sync operation
        }
    }
}

/**
 * Result of a synchronization operation
 */
sealed class SyncResult {
    data class Success(val message: String, val syncData: SyncData? = null) : SyncResult()
    data class PartialSuccess(val message: String, val errors: List<String>) : SyncResult()
    data class Error(val message: String) : SyncResult()
    
    val data: SyncData?
        get() = when (this) {
            is Success -> syncData
            else -> null
        }
}

/**
 * Synchronization error information
 */
data class SyncError(
    val message: String,
    val timestamp: LocalDateTime
)

// Extension function to convert MessageEntity to Message
private fun com.chain.messaging.data.local.entity.MessageEntity.toMessage(): Message {
    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        type = com.chain.messaging.domain.model.MessageType.valueOf(type),
        timestamp = Date(timestamp),
        status = com.chain.messaging.domain.model.MessageStatus.valueOf(status),
        replyTo = replyTo
    )
}

// Extension function to convert RegisteredDevice to RegisteredDeviceEntity
private fun RegisteredDevice.toEntity(): RegisteredDeviceEntity {
    return RegisteredDeviceEntity(
        deviceId = deviceInfo.deviceId,
        deviceName = deviceInfo.deviceName,
        deviceType = deviceInfo.deviceType.name,
        platform = deviceInfo.platform,
        platformVersion = deviceInfo.platformVersion,
        appVersion = deviceInfo.appVersion,
        publicKey = deviceInfo.publicKey,
        registeredAt = registeredAt,
        isTrusted = isTrusted,
        lastSyncAt = lastSyncAt,
        syncStatus = syncStatus.name,
        isCurrentDevice = deviceInfo.isCurrentDevice,
        lastSeen = deviceInfo.lastSeen
    )
}