package com.chain.messaging.domain.repository

import com.chain.messaging.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user settings operations
 */
interface SettingsRepository {
    
    /**
     * Get user settings
     */
    suspend fun getSettings(userId: String): UserSettings?
    
    /**
     * Get user settings as Flow for reactive updates
     */
    fun getSettingsFlow(userId: String): Flow<UserSettings?>
    
    /**
     * Save complete user settings
     */
    suspend fun saveSettings(settings: UserSettings): Result<Unit>
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(userId: String, profile: UserProfile): Result<Unit>
    
    /**
     * Update privacy settings
     */
    suspend fun updatePrivacySettings(userId: String, privacy: PrivacySettings): Result<Unit>
    
    /**
     * Update notification settings
     */
    suspend fun updateNotificationSettings(userId: String, notifications: NotificationSettings): Result<Unit>
    
    /**
     * Update appearance settings
     */
    suspend fun updateAppearanceSettings(userId: String, appearance: AppearanceSettings): Result<Unit>
    
    /**
     * Update accessibility settings
     */
    suspend fun updateAccessibilitySettings(userId: String, accessibility: AccessibilitySettings): Result<Unit>
    
    /**
     * Create default settings for new user
     */
    suspend fun createDefaultSettings(userId: String, displayName: String): Result<UserSettings>
    
    /**
     * Delete user settings
     */
    suspend fun deleteSettings(userId: String): Result<Unit>
    
    /**
     * Check if settings exist for user
     */
    suspend fun settingsExist(userId: String): Boolean
    
    /**
     * Get user settings for current user (convenience method)
     */
    suspend fun getUserSettings(): UserSettings?
}