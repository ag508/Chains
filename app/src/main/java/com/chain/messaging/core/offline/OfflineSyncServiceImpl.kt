package com.chain.messaging.core.offline

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.repository.MessageRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineSyncServiceImpl @Inject constructor(
    private val offlineMessageQueue: OfflineMessageQueue,
    private val networkMonitor: NetworkMonitor,
    private val messagingService: MessagingService,
    private val messageRepository: MessageRepository,
    private val conflictResolver: ConflictResolver,
    private val coroutineScope: CoroutineScope
) : OfflineSyncService {
    
    private val _syncStatus = MutableStateFlow(
        SyncStatus(
            isOnline = false,
            isSyncing = false,
            pendingMessages = 0,
            lastSyncTime = null
        )
    )
    
    private var lastSyncTime: LocalDateTime? = null
    private var offlineStartTime: LocalDateTime? = null
    
    override suspend fun initialize() {
        // Monitor network status and update sync status
        coroutineScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                updateSyncStatus { status ->
                    status.copy(isOnline = isConnected)
                }
                
                if (isConnected) {
                    if (offlineStartTime != null) {
                        offlineStartTime = null
                    }
                    // Automatically sync when connection is restored
                    syncPendingMessages()
                } else {
                    if (offlineStartTime == null) {
                        offlineStartTime = LocalDateTime.now()
                    }
                }
            }
        }
        
        // Monitor queued messages count
        coroutineScope.launch {
            offlineMessageQueue.getQueuedMessagesFlow().collect { queuedMessages ->
                updateSyncStatus { status ->
                    status.copy(pendingMessages = queuedMessages.size)
                }
            }
        }
    }
    
    override suspend fun handleOfflineMessage(message: Message) {
        offlineMessageQueue.queueMessage(message)
    }
    
    override suspend fun syncPendingMessages(): SyncResult {
        val startTime = System.currentTimeMillis()
        val errors = mutableListOf<SyncError>()
        var messagesSynced = 0
        var conflictsResolved = 0
        
        updateSyncStatus { it.copy(isSyncing = true, syncProgress = 0f) }
        
        try {
            // Check if we're online
            if (!networkMonitor.isNetworkAvailable()) {
                return SyncResult(
                    success = false,
                    messagesSynced = 0,
                    conflictsResolved = 0,
                    errors = listOf(SyncError("", "Network not available")),
                    duration = System.currentTimeMillis() - startTime
                )
            }
            
            // Process queued messages
            val queuedMessages = offlineMessageQueue.getQueuedMessages()
            val totalMessages = queuedMessages.size
            
            for ((index, queuedMessage) in queuedMessages.withIndex()) {
                try {
                    // Update progress
                    val progress = if (totalMessages > 0) index.toFloat() / totalMessages else 1f
                    updateSyncStatus { it.copy(syncProgress = progress) }
                    
                    // Try to send the message
                    messagingService.sendMessage(queuedMessage.message)
                    
                    // Remove from queue if successful
                    offlineMessageQueue.removeFromQueue(queuedMessage.id)
                    messagesSynced++
                    
                } catch (e: Exception) {
                    errors.add(SyncError(queuedMessage.message.id, e.message ?: "Unknown error"))
                }
            }
            
            // Sync message history and resolve conflicts
            val conflictResult = syncMessageHistory()
            conflictsResolved = conflictResult.conflictsResolved
            
            lastSyncTime = LocalDateTime.now()
            
            return SyncResult(
                success = errors.isEmpty(),
                messagesSynced = messagesSynced,
                conflictsResolved = conflictsResolved,
                errors = errors,
                duration = System.currentTimeMillis() - startTime
            )
            
        } finally {
            updateSyncStatus { 
                it.copy(
                    isSyncing = false, 
                    syncProgress = 1f,
                    lastSyncTime = lastSyncTime
                ) 
            }
        }
    }
    
    override fun getSyncStatus(): Flow<SyncStatus> {
        return _syncStatus.asStateFlow()
    }
    
    override suspend fun forcSync(): SyncResult {
        return syncPendingMessages()
    }
    
    override suspend fun getOfflineStats(): OfflineStats {
        val queuedMessages = offlineMessageQueue.getQueuedMessages()
        val totalOfflineTime = offlineStartTime?.let { start ->
            java.time.Duration.between(start, LocalDateTime.now()).toMillis()
        } ?: 0L
        
        return OfflineStats(
            totalQueuedMessages = queuedMessages.size,
            failedMessages = queuedMessages.count { it.hasExceededMaxRetries() },
            averageRetryCount = queuedMessages.map { it.retryCount }.average().takeIf { !it.isNaN() } ?: 0.0,
            oldestQueuedMessage = queuedMessages.minByOrNull { it.queuedAt }?.queuedAt,
            totalOfflineTime = totalOfflineTime
        )
    }
    
    override suspend fun clearOfflineData() {
        offlineMessageQueue.clearQueue()
        offlineStartTime = null
        lastSyncTime = null
        updateSyncStatus { 
            it.copy(
                pendingMessages = 0,
                lastSyncTime = null,
                syncProgress = 0f
            ) 
        }
    }
    
    private suspend fun syncMessageHistory(): ConflictResolution {
        // Get local messages that might have conflicts
        val localMessages = messageRepository.getRecentMessages(limit = 100)
        
        // Get remote messages from blockchain (this would be implemented in MessagingService)
        val remoteMessages = try {
            messagingService.getRecentMessages(limit = 100)
        } catch (e: Exception) {
            emptyList()
        }
        
        // Resolve conflicts
        val conflictResolution = conflictResolver.resolveConflicts(localMessages, remoteMessages)
        
        // Update local database with resolved messages
        for (resolvedMessage in conflictResolution.resolvedMessages) {
            messageRepository.updateMessage(resolvedMessage)
        }
        
        return conflictResolution
    }
    
    private fun updateSyncStatus(update: (SyncStatus) -> SyncStatus) {
        _syncStatus.value = update(_syncStatus.value)
    }
}