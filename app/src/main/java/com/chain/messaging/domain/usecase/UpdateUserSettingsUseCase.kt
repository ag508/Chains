package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating user settings
 */
class UpdateUserSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    
    /**
     * Update complete user settings
     */
    suspend fun updateSettings(settings: UserSettings): Result<Unit> {
        return settingsRepository.saveSettings(settings)
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(userId: String, profile: UserProfile): Result<Unit> {
        return settingsRepository.updateProfile(userId, profile)
    }
    
    /**
     * Update privacy settings
     */
    suspend fun updatePrivacySettings(userId: String, privacy: PrivacySettings): Result<Unit> {
        return settingsRepository.updatePrivacySettings(userId, privacy)
    }
    
    /**
     * Update notification settings
     */
    suspend fun updateNotificationSettings(userId: String, notifications: NotificationSettings): Result<Unit> {
        return settingsRepository.updateNotificationSettings(userId, notifications)
    }
    
    /**
     * Update appearance settings
     */
    suspend fun updateAppearanceSettings(userId: String, appearance: AppearanceSettings): Result<Unit> {
        return settingsRepository.updateAppearanceSettings(userId, appearance)
    }
    
    /**
     * Update accessibility settings
     */
    suspend fun updateAccessibilitySettings(userId: String, accessibility: AccessibilitySettings): Result<Unit> {
        return settingsRepository.updateAccessibilitySettings(userId, accessibility)
    }
    
    /**
     * Create default settings for new user
     */
    suspend fun createDefaultSettings(userId: String, displayName: String): Result<UserSettings> {
        return settingsRepository.createDefaultSettings(userId, displayName)
    }
}