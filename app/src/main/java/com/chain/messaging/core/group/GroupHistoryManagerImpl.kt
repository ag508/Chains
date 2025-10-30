package com.chain.messaging.core.group

import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Implementation of GroupHistoryManager that provides efficient message history
 * management and synchronization for large groups.
 */
@Singleton
class GroupHistoryManagerImpl @Inject constructor(
    private val messageRepository: MessageRepository
) : GroupHistoryManager {
    
    private val historyUpdateFlow = MutableSharedFlow<HistoryUpdate>()
    private val activeSnapshots = ConcurrentHashMap<String, HistorySnapshot>()
    private val syncProgress = ConcurrentHashMap<String, SyncProgress>()
    
    companion object {
        private const val DEFAULT_SYNC_BATCH_SIZE = 1000
        private const val MAX_SYNC_BATCH_SIZE = 5000
        private const val SNAPSHOT_VALIDITY_MS = 3600_000L // 1 hour
        private const val DEFAULT_RETENTION_DAYS = 365L
    }
    
    override suspend fun synchronizeHistoryForNewMember(
        groupId: String,
        userId: String,
        fromTimestamp: Long?
    ): Result<SyncResult> {
        return try {
            val syncId = UUID.randomUUID().toString()
            val startTime = System.currentTimeMillis()
            
            // Determine sync starting point
            val syncFromTimestamp = fromTimestamp ?: (startTime - (30L * 24 * 60 * 60 * 1000)) // 30 days ago
            
            // Get messages in batches for efficient sync
            var messagesSynced = 0
            var bytesTransferred = 0L
            var currentTimestamp = syncFromTimestamp
            var hasMoreData = true
            
            while (hasMoreData) {
                val batch = messageRepository.getMessages(
                    chatId = groupId,
                    limit = DEFAULT_SYNC_BATCH_SIZE,
                    offset = messagesSynced
                )
                
                if (batch.isEmpty()) {
                    hasMoreData = false
                    break
                }
                
                // Process batch for the new member
                batch.forEach { message ->
                    // In a real implementation, this would decrypt and re-encrypt for the new member
                    bytesTransferred += message.content.length
                    messagesSynced++
                }
                
                currentTimestamp = batch.lastOrNull()?.timestamp?.time ?: currentTimestamp
                
                // Update sync progress
                updateSyncProgress(syncId, messagesSynced, bytesTransferred)
                
                // Check if we've reached the current time
                if (currentTimestamp >= startTime) {
                    hasMoreData = false
                }
            }
            
            val syncDuration = System.currentTimeMillis() - startTime
            
            val result = SyncResult(
                syncId = syncId,
                messagesSynced = messagesSynced,
                bytesTransferred = bytesTransferred,
                syncDurationMs = syncDuration,
                fromTimestamp = syncFromTimestamp,
                toTimestamp = currentTimestamp,
                hasMoreData = false
            )
            
            // Emit history update
            emitHistoryUpdate(
                groupId,
                HistoryUpdateType.BATCH_SYNC_COMPLETED,
                null,
                listOf(userId)
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGroupHistory(
        groupId: String,
        userId: String,
        limit: Int,
        beforeTimestamp: Long?
    ): Result<List<Message>> {
        return try {
            val messages = if (beforeTimestamp != null) {
                // Get messages before specific timestamp
                messageRepository.getMessages(groupId, limit, 0)
                    .filter { it.timestamp.time < beforeTimestamp }
                    .take(limit)
            } else {
                messageRepository.getMessages(groupId, limit, 0)
            }
            
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun pruneGroupHistory(
        groupId: String,
        retentionPeriodMs: Long,
        keepImportantMessages: Boolean
    ): Result<PruningResult> {
        return try {
            val cutoffTimestamp = System.currentTimeMillis() - retentionPeriodMs
            val allMessages = messageRepository.getMessages(groupId, Int.MAX_VALUE, 0)
            
            val messagesToRemove = allMessages.filter { message ->
                val shouldRemove = message.timestamp.time < cutoffTimestamp
                
                if (keepImportantMessages && shouldRemove) {
                    // Keep important messages (system messages, pinned messages, etc.)
                    !isImportantMessage(message)
                } else {
                    shouldRemove
                }
            }
            
            val importantMessagesKept = allMessages.count { message ->
                message.timestamp.time < cutoffTimestamp && isImportantMessage(message)
            }
            
            // Calculate bytes freed (approximate)
            val bytesFreed = messagesToRemove.sumOf { it.content.length.toLong() }
            
            // Remove messages
            val messageIds = messagesToRemove.map { it.id }
            messageRepository.deleteMessages(messageIds)
            
            val oldestRemaining = allMessages
                .filterNot { messagesToRemove.contains(it) }
                .minByOrNull { it.timestamp.time }
                ?.timestamp?.time ?: System.currentTimeMillis()
            
            val result = PruningResult(
                messagesRemoved = messagesToRemove.size,
                bytesFreed = bytesFreed,
                oldestRemainingTimestamp = oldestRemaining,
                importantMessagesKept = importantMessagesKept
            )
            
            // Emit history update
            emitHistoryUpdate(
                groupId,
                HistoryUpdateType.PRUNING_COMPLETED,
                null,
                emptyList()
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createHistorySnapshot(
        groupId: String,
        timestamp: Long
    ): Result<HistorySnapshot> {
        return try {
            val snapshotId = UUID.randomUUID().toString()
            
            // Get all messages up to timestamp
            val messages = messageRepository.getMessages(groupId, Int.MAX_VALUE, 0)
                .filter { it.timestamp.time <= timestamp }
            
            // Calculate compressed size (simulated)
            val originalSize = messages.sumOf { it.content.length.toLong() }
            val compressedSize = (originalSize * 0.3).toLong() // Assume 70% compression
            
            // Generate checksum (simplified)
            val checksum = messages.joinToString("") { it.id }.hashCode().toString()
            
            val snapshot = HistorySnapshot(
                snapshotId = snapshotId,
                groupId = groupId,
                timestamp = timestamp,
                messageCount = messages.size,
                compressedSize = compressedSize,
                checksum = checksum
            )
            
            activeSnapshots[snapshotId] = snapshot
            
            Result.success(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncFromSnapshot(
        groupId: String,
        userId: String,
        snapshotId: String
    ): Result<SyncResult> {
        return try {
            val snapshot = activeSnapshots[snapshotId]
                ?: return Result.failure(IllegalArgumentException("Snapshot not found"))
            
            // Validate snapshot age
            if (System.currentTimeMillis() - snapshot.timestamp > SNAPSHOT_VALIDITY_MS) {
                return Result.failure(IllegalStateException("Snapshot is too old"))
            }
            
            val startTime = System.currentTimeMillis()
            
            // Simulate snapshot-based sync (in real implementation, this would load from compressed snapshot)
            val messages = messageRepository.getMessages(groupId, snapshot.messageCount, 0)
                .filter { it.timestamp.time <= snapshot.timestamp }
            
            val syncDuration = System.currentTimeMillis() - startTime
            
            val result = SyncResult(
                syncId = UUID.randomUUID().toString(),
                messagesSynced = messages.size,
                bytesTransferred = snapshot.compressedSize,
                syncDurationMs = syncDuration,
                fromTimestamp = 0L,
                toTimestamp = snapshot.timestamp,
                hasMoreData = false
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun detectAndFillMessageGaps(
        groupId: String,
        userId: String
    ): Result<GapFillResult> {
        return try {
            val userMessages = messageRepository.getMessages(groupId, Int.MAX_VALUE, 0)
            val allMessages = messageRepository.getMessages(groupId, Int.MAX_VALUE, 0)
            
            // Detect gaps by comparing message sequences
            val gaps = detectMessageGaps(userMessages, allMessages)
            
            var messagesFetched = 0
            var gapsFilled = 0
            
            gaps.forEach { gap ->
                // Fill gap by fetching missing messages
                val missingMessages = allMessages.filter { message ->
                    message.timestamp.time >= gap.startTimestamp &&
                    message.timestamp.time <= gap.endTimestamp
                }
                
                messagesFetched += missingMessages.size
                if (missingMessages.isNotEmpty()) {
                    gapsFilled++
                }
            }
            
            val result = GapFillResult(
                gapsDetected = gaps.size,
                gapsFilled = gapsFilled,
                messagesFetched = messagesFetched,
                inconsistenciesResolved = gapsFilled
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun optimizeMessageStorage(
        groupId: String,
        compressionLevel: CompressionLevel
    ): Result<OptimizationResult> {
        return try {
            val messages = messageRepository.getMessages(groupId, Int.MAX_VALUE, 0)
            val originalSize = messages.sumOf { it.content.length.toLong() }
            
            // Simulate compression based on level
            val compressionRatio = when (compressionLevel) {
                CompressionLevel.NONE -> 1.0f
                CompressionLevel.LOW -> 0.8f
                CompressionLevel.MEDIUM -> 0.6f
                CompressionLevel.HIGH -> 0.4f
                CompressionLevel.MAXIMUM -> 0.2f
            }
            
            val optimizedSize = (originalSize * compressionRatio).toLong()
            
            val result = OptimizationResult(
                originalSize = originalSize,
                optimizedSize = optimizedSize,
                compressionRatio = compressionRatio,
                messagesOptimized = messages.size
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeHistoryUpdates(groupId: String): Flow<HistoryUpdate> {
        return historyUpdateFlow
    }
    
    override suspend fun getHistoryStatistics(groupId: String): HistoryStatistics {
        val messages = messageRepository.getMessages(groupId, Int.MAX_VALUE, 0)
        
        val totalMessages = messages.size.toLong()
        val totalSize = messages.sumOf { it.content.length.toLong() }
        val oldestTimestamp = messages.minByOrNull { it.timestamp.time }?.timestamp?.time ?: 0L
        val newestTimestamp = messages.maxByOrNull { it.timestamp.time }?.timestamp?.time ?: 0L
        val averageSize = if (totalMessages > 0) totalSize / totalMessages else 0L
        
        // Calculate messages per day
        val daysDiff = if (oldestTimestamp > 0) {
            ((newestTimestamp - oldestTimestamp) / (24 * 60 * 60 * 1000)).coerceAtLeast(1)
        } else {
            1L
        }
        val messagesPerDay = totalMessages.toFloat() / daysDiff
        
        return HistoryStatistics(
            groupId = groupId,
            totalMessages = totalMessages,
            totalSizeBytes = totalSize,
            oldestMessageTimestamp = oldestTimestamp,
            newestMessageTimestamp = newestTimestamp,
            averageMessageSize = averageSize,
            activeParticipants = messages.map { it.senderId }.distinct().size,
            messagesPerDay = messagesPerDay
        )
    }
    
    override suspend fun exportGroupHistory(
        groupId: String,
        format: ExportFormat,
        dateRange: DateRange?
    ): Result<ExportResult> {
        return try {
            val messages = messageRepository.getMessages(groupId, Int.MAX_VALUE, 0)
                .let { allMessages ->
                    dateRange?.let { range ->
                        allMessages.filter { message ->
                            message.timestamp.time >= range.startTimestamp &&
                            message.timestamp.time <= range.endTimestamp
                        }
                    } ?: allMessages
                }
            
            val exportId = UUID.randomUUID().toString()
            val filePath = "exports/${groupId}_${exportId}.${format.name.lowercase()}"
            
            // Simulate export file size calculation
            val baseSize = messages.sumOf { it.content.length.toLong() }
            val fileSizeBytes = when (format) {
                ExportFormat.JSON -> (baseSize * 1.5).toLong()
                ExportFormat.CSV -> (baseSize * 1.2).toLong()
                ExportFormat.BINARY -> (baseSize * 0.8).toLong()
                ExportFormat.ENCRYPTED_ARCHIVE -> (baseSize * 0.6).toLong()
            }
            
            val checksum = messages.joinToString("") { it.id }.hashCode().toString()
            
            val result = ExportResult(
                exportId = exportId,
                filePath = filePath,
                format = format,
                messageCount = messages.size,
                fileSizeBytes = fileSizeBytes,
                checksum = checksum
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun isImportantMessage(message: Message): Boolean {
        // Define criteria for important messages
        return message.type.name == "SYSTEM" || 
               message.content.contains("@everyone") ||
               message.content.startsWith("!important")
    }
    
    private fun detectMessageGaps(userMessages: List<Message>, allMessages: List<Message>): List<MessageGap> {
        val gaps = mutableListOf<MessageGap>()
        val userTimestamps = userMessages.map { it.timestamp.time }.sorted()
        val allTimestamps = allMessages.map { it.timestamp.time }.sorted()
        
        var userIndex = 0
        var allIndex = 0
        
        while (allIndex < allTimestamps.size && userIndex < userTimestamps.size) {
            val userTime = userTimestamps[userIndex]
            val allTime = allTimestamps[allIndex]
            
            if (userTime == allTime) {
                userIndex++
                allIndex++
            } else if (userTime > allTime) {
                // Gap detected - user is missing this message
                val gapStart = allTime
                var gapEnd = allTime
                
                // Find end of gap
                while (allIndex < allTimestamps.size && 
                       (userIndex >= userTimestamps.size || allTimestamps[allIndex] < userTimestamps[userIndex])) {
                    gapEnd = allTimestamps[allIndex]
                    allIndex++
                }
                
                gaps.add(MessageGap(gapStart, gapEnd))
            } else {
                userIndex++
            }
        }
        
        return gaps
    }
    
    private suspend fun updateSyncProgress(syncId: String, messagesSynced: Int, bytesTransferred: Long) {
        syncProgress[syncId] = SyncProgress(messagesSynced, bytesTransferred, System.currentTimeMillis())
    }
    
    private suspend fun emitHistoryUpdate(
        groupId: String,
        updateType: HistoryUpdateType,
        messageId: String?,
        affectedUserIds: List<String>
    ) {
        val update = HistoryUpdate(
            groupId = groupId,
            updateType = updateType,
            messageId = messageId,
            timestamp = System.currentTimeMillis(),
            affectedUserIds = affectedUserIds
        )
        
        historyUpdateFlow.emit(update)
    }
    
    private data class MessageGap(
        val startTimestamp: Long,
        val endTimestamp: Long
    )
    
    private data class SyncProgress(
        val messagesSynced: Int,
        val bytesTransferred: Long,
        val lastUpdateTime: Long
    )
}