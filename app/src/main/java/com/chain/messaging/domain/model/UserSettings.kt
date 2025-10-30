package com.chain.messaging.domain.model

/**
 * Domain model for user settings and preferences
 */
data class UserSettings(
    val userId: String,
    val profile: UserProfile,
    val privacy: PrivacySettings,
    val notifications: NotificationSettings,
    val appearance: AppearanceSettings,
    val accessibility: AccessibilitySettings,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * User profile settings
 */
data class UserProfile(
    val displayName: String,
    val bio: String? = null,
    val avatar: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true
)

/**
 * Privacy settings
 */
data class PrivacySettings(
    val readReceipts: Boolean = true,
    val typingIndicators: Boolean = true,
    val profilePhotoVisibility: ProfileVisibility = ProfileVisibility.EVERYONE,
    val lastSeenVisibility: ProfileVisibility = ProfileVisibility.EVERYONE,
    val onlineStatusVisibility: ProfileVisibility = ProfileVisibility.EVERYONE,
    val groupInvitePermission: GroupInvitePermission = GroupInvitePermission.EVERYONE,
    val disappearingMessagesDefault: DisappearingMessageTimer = DisappearingMessageTimer.OFF,
    val screenshotNotifications: Boolean = true,
    val forwardingRestriction: Boolean = false
)

/**
 * Notification settings
 */
data class NotificationSettings(
    val messageNotifications: Boolean = true,
    val callNotifications: Boolean = true,
    val groupNotifications: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ledEnabled: Boolean = true,
    val notificationSound: String = "default",
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00",
    val showPreview: Boolean = true,
    val showSenderName: Boolean = true
)

/**
 * Appearance settings
 */
data class AppearanceSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val fontSize: FontSize = FontSize.MEDIUM,
    val chatWallpaper: String? = null,
    val useSystemEmojis: Boolean = false,
    val showAvatarsInGroups: Boolean = true,
    val compactMode: Boolean = false
)

/**
 * Accessibility settings
 */
data class AccessibilitySettings(
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val reduceMotion: Boolean = false,
    val screenReaderOptimized: Boolean = false,
    val hapticFeedback: Boolean = true,
    val voiceOverEnabled: Boolean = false
)

/**
 * Profile visibility options
 */
enum class ProfileVisibility {
    EVERYONE,
    CONTACTS,
    NOBODY
}

/**
 * Group invite permission options
 */
enum class GroupInvitePermission {
    EVERYONE,
    CONTACTS,
    NOBODY
}

/**
 * App theme options
 */
enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Font size options
 */
enum class FontSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

/**
 * Disappearing message timer options
 */
enum class DisappearingMessageTimer {
    OFF,
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
}