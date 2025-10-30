package com.chain.messaging.core.privacy

import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Interface for managing disappearing messages functionality.
 */
interface DisappearingMessageManager {
    
    /**
     * Sets up a disappearing message timer for a chat.
     * @param chatId The chat ID to configure
     * @param timerDuration Timer duration in milliseconds, null to disable
     */
    suspend fun setDisappearingMessageTimer(chatId: String, timerDuration: Long?)
    
    /**
     * Gets the current disappearing message timer for a chat.
     * @param chatId The chat ID
     * @return Timer duration in milliseconds, null if disabled
     */
    suspend fun getDisappearingMessageTimer(chatId: String): Long?
    
    /**
     * Processes a message to set expiration if disappearing messages are enabled.
     * @param message The message to process
     * @param chatId The chat ID
     * @return Updated message with expiration set if applicable
     */
    suspend fun processMessageForExpiration(message: Message, chatId: String): Message
    
    /**
     * Starts the automatic cleanup service for expired messages.
     */
    suspend fun startCleanupService()
    
    /**
     * Stops the automatic cleanup service.
     */
    suspend fun stopCleanupService()
    
    /**
     * Manually triggers cleanup of expired messages.
     * @return Number of messages deleted
     */
    suspend fun cleanupExpiredMessages(): Int
    
    /**
     * Deletes a specific message from all devices.
     * @param messageId The message ID to delete
     */
    suspend fun deleteMessageFromAllDevices(messageId: String)
    
    /**
     * Observes messages that are about to expire (within warning threshold).
     * @param warningThresholdMs Warning threshold in milliseconds
     * @return Flow of messages about to expire
     */
    fun observeMessagesAboutToExpire(warningThresholdMs: Long = 60000): Flow<List<Message>>
    
    /**
     * Gets available timer options for disappearing messages.
     * @return List of timer durations in milliseconds
     */
    fun getAvailableTimerOptions(): List<Long>
    
    /**
     * Initialize disappearing message manager
     */
    suspend fun initialize()
}

/**
 * Predefined timer options for disappearing messages.
 */
object DisappearingMessageTimers {
    const val FIVE_SECONDS = 5_000L
    const val TEN_SECONDS = 10_000L
    const val THIRTY_SECONDS = 30_000L
    const val ONE_MINUTE = 60_000L
    const val FIVE_MINUTES = 300_000L
    const val TEN_MINUTES = 600_000L
    const val THIRTY_MINUTES = 1_800_000L
    const val ONE_HOUR = 3_600_000L
    const val SIX_HOURS = 21_600_000L
    const val TWELVE_HOURS = 43_200_000L
    const val ONE_DAY = 86_400_000L
    const val ONE_WEEK = 604_800_000L
    
    val ALL_OPTIONS = listOf(
        FIVE_SECONDS,
        TEN_SECONDS,
        THIRTY_SECONDS,
        ONE_MINUTE,
        FIVE_MINUTES,
        TEN_MINUTES,
        THIRTY_MINUTES,
        ONE_HOUR,
        SIX_HOURS,
        TWELVE_HOURS,
        ONE_DAY,
        ONE_WEEK
    )
}