package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.ReactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Reaction operations
 */
@Dao
interface ReactionDao {
    
    /**
     * Get reaction by ID
     */
    @Query("SELECT * FROM reactions WHERE id = :reactionId")
    suspend fun getReactionById(reactionId: String): ReactionEntity?
    
    /**
     * Get all reactions for a message
     */
    @Query("SELECT * FROM reactions WHERE messageId = :messageId ORDER BY timestamp ASC")
    suspend fun getReactionsByMessageId(messageId: String): List<ReactionEntity>
    
    /**
     * Get all reactions for a message as Flow
     */
    @Query("SELECT * FROM reactions WHERE messageId = :messageId ORDER BY timestamp ASC")
    fun observeReactionsByMessageId(messageId: String): Flow<List<ReactionEntity>>
    
    /**
     * Get reactions by user for a message
     */
    @Query("SELECT * FROM reactions WHERE messageId = :messageId AND userId = :userId ORDER BY timestamp ASC")
    suspend fun getReactionsByUserAndMessage(messageId: String, userId: String): List<ReactionEntity>
    
    /**
     * Get specific reaction by user, message, and emoji
     */
    @Query("SELECT * FROM reactions WHERE messageId = :messageId AND userId = :userId AND emoji = :emoji")
    suspend fun getSpecificReaction(messageId: String, userId: String, emoji: String): ReactionEntity?
    
    /**
     * Get reaction count for a message
     */
    @Query("SELECT COUNT(*) FROM reactions WHERE messageId = :messageId")
    suspend fun getReactionCount(messageId: String): Int
    
    /**
     * Get reaction count by emoji for a message
     */
    @Query("SELECT COUNT(*) FROM reactions WHERE messageId = :messageId AND emoji = :emoji")
    suspend fun getReactionCountByEmoji(messageId: String, emoji: String): Int
    
    /**
     * Get unique emojis used in reactions for a message
     */
    @Query("SELECT DISTINCT emoji FROM reactions WHERE messageId = :messageId ORDER BY emoji")
    suspend fun getUniqueEmojisForMessage(messageId: String): List<String>
    
    /**
     * Get reaction summary for a message (emoji and count)
     */
    @Query("SELECT emoji, COUNT(*) as count FROM reactions WHERE messageId = :messageId GROUP BY emoji ORDER BY count DESC, emoji")
    suspend fun getReactionSummary(messageId: String): List<ReactionSummary>
    
    /**
     * Get all reactions by a user
     */
    @Query("SELECT * FROM reactions WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getReactionsByUser(userId: String): List<ReactionEntity>
    
    /**
     * Insert reaction
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: ReactionEntity)
    
    /**
     * Insert multiple reactions
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReactions(reactions: List<ReactionEntity>)
    
    /**
     * Update reaction
     */
    @Update
    suspend fun updateReaction(reaction: ReactionEntity)
    
    /**
     * Delete reaction
     */
    @Delete
    suspend fun deleteReaction(reaction: ReactionEntity)
    
    /**
     * Delete reaction by ID
     */
    @Query("DELETE FROM reactions WHERE id = :reactionId")
    suspend fun deleteReactionById(reactionId: String)
    
    /**
     * Delete specific reaction by user, message, and emoji
     */
    @Query("DELETE FROM reactions WHERE messageId = :messageId AND userId = :userId AND emoji = :emoji")
    suspend fun deleteSpecificReaction(messageId: String, userId: String, emoji: String)
    
    /**
     * Delete all reactions for a message
     */
    @Query("DELETE FROM reactions WHERE messageId = :messageId")
    suspend fun deleteReactionsByMessageId(messageId: String)
    
    /**
     * Delete all reactions by a user
     */
    @Query("DELETE FROM reactions WHERE userId = :userId")
    suspend fun deleteReactionsByUserId(userId: String)
    
    /**
     * Check if user has reacted to a message with specific emoji
     */
    @Query("SELECT COUNT(*) > 0 FROM reactions WHERE messageId = :messageId AND userId = :userId AND emoji = :emoji")
    suspend fun hasUserReacted(messageId: String, userId: String, emoji: String): Boolean
    
    /**
     * Check if user has any reaction to a message
     */
    @Query("SELECT COUNT(*) > 0 FROM reactions WHERE messageId = :messageId AND userId = :userId")
    suspend fun hasUserReactedToMessage(messageId: String, userId: String): Boolean
    
    /**
     * Get total reaction count
     */
    @Query("SELECT COUNT(*) FROM reactions")
    suspend fun getTotalReactionCount(): Int
}

/**
 * Data class for reaction summary
 */
data class ReactionSummary(
    val emoji: String,
    val count: Int
)