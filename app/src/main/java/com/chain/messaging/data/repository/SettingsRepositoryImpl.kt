package com.chain.messaging.data.repository

import com.chain.messaging.data.local.dao.UserSettingsDao
import com.chain.messaging.data.local.entity.toDomain
import com.chain.messaging.data.local.entity.toEntity
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: UserSettingsDao
) : SettingsRepository {
    
    override suspend fun getSettings(userId: String): UserSettings? {
        return try {
            settingsDao.getSettings(userId)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }
    
    override fun getSettingsFlow(userId: String): Flow<UserSettings?> {
        return settingsDao.getSettingsFlow(userId).map { entity ->
            entity?.toDomain()
        }
    }
    
    override suspend fun saveSettings(settings: UserSettings): Result<Unit> {
        return try {
            settingsDao.insertOrUpdateSettings(settings.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateProfile(userId: String, profile: UserProfile): Result<Unit> {
        return try {
            settingsDao.updateProfile(
                userId = userId,
                displayName = profile.displayName,
                bio = profile.bio,
                avatar = profile.avatar,
                phoneNumber = profile.phoneNumber,
                email = profile.email,
                showOnlineStatus = profile.showOnlineStatus,
                showLastSeen = profile.showLastSeen,
                updatedAt = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updatePrivacySettings(userId: String, privacy: PrivacySettings): Result<Unit> {
        return try {
            settingsDao.updatePrivacySettings(
                userId = userId,
                readReceipts = privacy.readReceipts,
                typingIndicators = privacy.typingIndicators,
                profilePhotoVisibility = privacy.profilePhotoVisibility.name,
                lastSeenVisibility = privacy.lastSeenVisibility.name,
                onlineStatusVisibility = privacy.onlineStatusVisibility.name,
                groupInvitePermission = privacy.groupInvitePermission.name,
                disappearingMessagesDefault = privacy.disappearingMessagesDefault.name,
                screenshotNotifications = privacy.screenshotNotifications,
                forwardingRestriction = privacy.forwardingRestriction,
                updatedAt = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateNotificationSettings(userId: String, notifications: NotificationSettings): Result<Unit> {
        return try {
            settingsDao.updateNotificationSettings(
                userId = userId,
                messageNotifications = notifications.messageNotifications,
                callNotifications = notifications.callNotifications,
                groupNotifications = notifications.groupNotifications,
                soundEnabled = notifications.soundEnabled,
                vibrationEnabled = notifications.vibrationEnabled,
                ledEnabled = notifications.ledEnabled,
                notificationSound = notifications.notificationSound,
                quietHoursEnabled = notifications.quietHoursEnabled,
                quietHoursStart = notifications.quietHoursStart,
                quietHoursEnd = notifications.quietHoursEnd,
                showPreview = notifications.showPreview,
                showSenderName = notifications.showSenderName,
                updatedAt = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateAppearanceSettings(userId: String, appearance: AppearanceSettings): Result<Unit> {
        return try {
            settingsDao.updateAppearanceSettings(
                userId = userId,
                theme = appearance.theme.name,
                fontSize = appearance.fontSize.name,
                chatWallpaper = appearance.chatWallpaper,
                useSystemEmojis = appearance.useSystemEmojis,
                showAvatarsInGroups = appearance.showAvatarsInGroups,
                compactMode = appearance.compactMode,
                updatedAt = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateAccessibilitySettings(userId: String, accessibility: AccessibilitySettings): Result<Unit> {
        return try {
            settingsDao.updateAccessibilitySettings(
                userId = userId,
                highContrast = accessibility.highContrast,
                largeText = accessibility.largeText,
                reduceMotion = accessibility.reduceMotion,
                screenReaderOptimized = accessibility.screenReaderOptimized,
                hapticFeedback = accessibility.hapticFeedback,
                voiceOverEnabled = accessibility.voiceOverEnabled,
                updatedAt = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createDefaultSettings(userId: String, displayName: String): Result<UserSettings> {
        return try {
            val defaultSettings = UserSettings(
                userId = userId,
                profile = UserProfile(
                    displayName = displayName
                ),
                privacy = PrivacySettings(),
                notifications = NotificationSettings(),
                appearance = AppearanceSettings(),
                accessibility = AccessibilitySettings()
            )
            
            settingsDao.insertOrUpdateSettings(defaultSettings.toEntity())
            Result.success(defaultSettings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteSettings(userId: String): Result<Unit> {
        return try {
            settingsDao.deleteSettings(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun settingsExist(userId: String): Boolean {
        return try {
            settingsDao.settingsExist(userId)
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getUserSettings(): UserSettings? {
        // For now, we'll need to get the current user ID from authentication service
        // This is a placeholder implementation that would need the current user context
        return try {
            // TODO: Get current user ID from AuthenticationService
            // For now, return null as we don't have user context
            null
        } catch (e: Exception) {
            null
        }
    }
}