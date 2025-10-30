package com.chain.messaging.core.privacy

import android.content.Context
import android.content.SharedPreferences
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.core.blockchain.BlockchainManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DisappearingMessageManager that handles automatic message deletion.
 */
@Singleton
class DisappearingMessageManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    private val blockchainManager: BlockchainManager,
    private val screenshotDetector: ScreenshotDetector
) : DisappearingMessageManager {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        "disappearing_messages", Context.MODE_PRIVATE
    )
    
    private val cleanupScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var cleanupJob: Job? = null
    
    private val _messagesAboutToExpire = MutableStateFlow<List<Message>>(emptyList())
    
    companion object {
        private const val CLEANUP_INTERVAL_MS = 30_000L // 30 seconds
        private const val TIMER_PREFIX = "timer_"
    }
    
    override suspend fun initialize() {
        // Start the cleanup service
        startCleanupService()
        
        // Initialize screenshot detection for disappearing messages
        screenshotDetector.startMonitoring()
        
        // Initial update of messages about to expire
        updateMessagesAboutToExpire()
    }
    
    override suspend fun setDisappearingMessageTimer(chatId: String, timerDuration: Long?) {
        if (timerDuration != null) {
            preferences.edit()
                .putLong(TIMER_PREFIX + chatId, timerDuration)
                .apply()
        } else {
            preferences.edit()
                .remove(TIMER_PREFIX + chatId)
                .apply()
        }
    }
    
    override suspend fun getDisappearingMessageTimer(chatId: String): Long? {
        val timer = preferences.getLong(TIMER_PREFIX + chatId, -1L)
        return if (timer == -1L) null else timer
    }
    
    override suspend fun processMessageForExpiration(message: Message, chatId: String): Message {
        val timer = getDisappearingMessageTimer(chatId)
        
        return if (timer != null) {
            val expiresAt = Date(System.currentTimeMillis() + timer)
            message.copy(
                disappearingMessageTimer = timer,
                expiresAt = expiresAt,
                isDisappearing = true
            )
        } else {
            message
        }
    }
    
    override suspend fun startCleanupService() {
        stopCleanupService() // Stop any existing service
        
        cleanupJob = cleanupScope.launch {
            while (isActive) {
                try {
                    cleanupExpiredMessages()
                    updateMessagesAboutToExpire()
                    delay(CLEANUP_INTERVAL_MS)
                } catch (e: Exception) {
                    // Log error but continue cleanup service
                    delay(CLEANUP_INTERVAL_MS)
                }
            }
        }
    }
    
    override suspend fun stopCleanupService() {
        cleanupJob?.cancel()
        cleanupJob = null
        
        // Stop screenshot detection when cleanup service stops
        screenshotDetector.stopMonitoring()
    }
    
    override suspend fun cleanupExpiredMessages(): Int {
        val currentTime = System.currentTimeMillis()
        val expiredMessages = messageRepository.getExpiredMessages(currentTime)
        
        var deletedCount = 0
        expiredMessages.forEach { message ->
            try {
                // Delete from local database
                messageRepository.deleteMessage(message.id)
                
                // Notify blockchain to remove from all devices
                deleteMessageFromAllDevices(message.id)
                
                deletedCount++
            } catch (e: Exception) {
                // Log error but continue with other messages
            }
        }
        
        return deletedCount
    }
    
    override suspend fun deleteMessageFromAllDevices(messageId: String) {
        try {
            // Send deletion transaction to blockchain
            blockchainManager.sendDeletionTransaction(messageId)
        } catch (e: Exception) {
            // Handle blockchain communication error
            throw e
        }
    }
    
    override fun observeMessagesAboutToExpire(warningThresholdMs: Long): Flow<List<Message>> {
        return _messagesAboutToExpire.asStateFlow()
    }
    
    override fun getAvailableTimerOptions(): List<Long> {
        return DisappearingMessageTimers.ALL_OPTIONS
    }
    
    private suspend fun updateMessagesAboutToExpire(warningThresholdMs: Long = 60000) {
        val currentTime = System.currentTimeMillis()
        val warningTime = currentTime + warningThresholdMs
        
        val messagesAboutToExpire = messageRepository.getMessagesExpiringBefore(warningTime)
            .filter { it.isDisappearing && it.expiresAt != null }
        
        _messagesAboutToExpire.value = messagesAboutToExpire
    }
}

