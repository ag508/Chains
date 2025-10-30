package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user settings operations
 */
@Dao
interface UserSettingsDao {
    
    /**
     * Get user settings by user ID
     */
    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    suspend fun getSettings(userId: String): UserSettingsEntity?
    
    /**
     * Get user settings as Flow for reactive updates
     */
    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    fun getSettingsFlow(userId: String): Flow<UserSettingsEntity?>
    
    /**
     * Insert or update user settings
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)
    
    /**
     * Update profile settings
     */
    @Query("""
        UPDATE user_settings 
        SET displayName = :displayName, 
            bio = :bio, 
            avatar = :avatar, 
            phoneNumber = :phoneNumber, 
            email = :email,
            showOnlineStatus = :showOnlineStatus,
            showLastSeen = :showLastSeen,
            updatedAt = :updatedAt
        WHERE userId = :userId
    """)
    suspend fun updateProfile(
        userId: String,
        displayName: String,
        bio: String?,
        avatar: String?,
        phoneNumber: String?,
        email: String?,
        showOnlineStatus: Boolean,
        showLastSeen: Boolean,
        updatedAt: Long
    )
    
    /**
     * Update privacy settings
     */
    @Query("""
        UPDATE user_settings 
        SET readReceipts = :readReceipts,
            typingIndicators = :typingIndicators,
            profilePhotoVisibility = :profilePhotoVisibility,
            lastSeenVisibility = :lastSeenVisibility,
            onlineStatusVisibility = :onlineStatusVisibility,
            groupInvitePermission = :groupInvitePermission,
            disappearingMessagesDefault = :disappearingMessagesDefault,
            screenshotNotifications = :screenshotNotifications,
            forwardingRestriction = :forwardingRestriction,
            updatedAt = :updatedAt
        WHERE userId = :userId
    """)
    suspend fun updatePrivacySettings(
        userId: String,
        readReceipts: Boolean,
        typingIndicators: Boolean,
        profilePhotoVisibility: String,
        lastSeenVisibility: String,
        onlineStatusVisibility: String,
        groupInvitePermission: String,
        disappearingMessagesDefault: String,
        screenshotNotifications: Boolean,
        forwardingRestriction: Boolean,
        updatedAt: Long
    )
    
    /**
     * Update notification settings
     */
    @Query("""
        UPDATE user_settings 
        SET messageNotifications = :messageNotifications,
            callNotifications = :callNotifications,
            groupNotifications = :groupNotifications,
            soundEnabled = :soundEnabled,
            vibrationEnabled = :vibrationEnabled,
            ledEnabled = :ledEnabled,
            notificationSound = :notificationSound,
            quietHoursEnabled = :quietHoursEnabled,
            quietHoursStart = :quietHoursStart,
            quietHoursEnd = :quietHoursEnd,
            showPreview = :showPreview,
            showSenderName = :showSenderName,
            updatedAt = :updatedAt
        WHERE userId = :userId
    """)
    suspend fun updateNotificationSettings(
        userId: String,
        messageNotifications: Boolean,
        callNotifications: Boolean,
        groupNotifications: Boolean,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        ledEnabled: Boolean,
        notificationSound: String,
        quietHoursEnabled: Boolean,
        quietHoursStart: String,
        quietHoursEnd: String,
        showPreview: Boolean,
        showSenderName: Boolean,
        updatedAt: Long
    )
    
    /**
     * Update appearance settings
     */
    @Query("""
        UPDATE user_settings 
        SET theme = :theme,
            fontSize = :fontSize,
            chatWallpaper = :chatWallpaper,
            useSystemEmojis = :useSystemEmojis,
            showAvatarsInGroups = :showAvatarsInGroups,
            compactMode = :compactMode,
            updatedAt = :updatedAt
        WHERE userId = :userId
    """)
    suspend fun updateAppearanceSettings(
        userId: String,
        theme: String,
        fontSize: String,
        chatWallpaper: String?,
        useSystemEmojis: Boolean,
        showAvatarsInGroups: Boolean,
        compactMode: Boolean,
        updatedAt: Long
    )
    
    /**
     * Update accessibility settings
     */
    @Query("""
        UPDATE user_settings 
        SET highContrast = :highContrast,
            largeText = :largeText,
            reduceMotion = :reduceMotion,
            screenReaderOptimized = :screenReaderOptimized,
            hapticFeedback = :hapticFeedback,
            voiceOverEnabled = :voiceOverEnabled,
            updatedAt = :updatedAt
        WHERE userId = :userId
    """)
    suspend fun updateAccessibilitySettings(
        userId: String,
        highContrast: Boolean,
        largeText: Boolean,
        reduceMotion: Boolean,
        screenReaderOptimized: Boolean,
        hapticFeedback: Boolean,
        voiceOverEnabled: Boolean,
        updatedAt: Long
    )
    
    /**
     * Delete user settings
     */
    @Query("DELETE FROM user_settings WHERE userId = :userId")
    suspend fun deleteSettings(userId: String)
    
    /**
     * Check if settings exist for user
     */
    @Query("SELECT COUNT(*) > 0 FROM user_settings WHERE userId = :userId")
    suspend fun settingsExist(userId: String): Boolean
}