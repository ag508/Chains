package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chain.messaging.domain.model.*

/**
 * Room entity for storing user settings locally
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val userId: String,
    
    // Profile settings
    val displayName: String,
    val bio: String?,
    val avatar: String?,
    val phoneNumber: String?,
    val email: String?,
    val showOnlineStatus: Boolean,
    val showLastSeen: Boolean,
    
    // Privacy settings
    val readReceipts: Boolean,
    val typingIndicators: Boolean,
    val profilePhotoVisibility: String,
    val lastSeenVisibility: String,
    val onlineStatusVisibility: String,
    val groupInvitePermission: String,
    val disappearingMessagesDefault: String,
    val screenshotNotifications: Boolean,
    val forwardingRestriction: Boolean,
    
    // Notification settings
    val messageNotifications: Boolean,
    val callNotifications: Boolean,
    val groupNotifications: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val ledEnabled: Boolean,
    val notificationSound: String,
    val quietHoursEnabled: Boolean,
    val quietHoursStart: String,
    val quietHoursEnd: String,
    val showPreview: Boolean,
    val showSenderName: Boolean,
    
    // Appearance settings
    val theme: String,
    val fontSize: String,
    val chatWallpaper: String?,
    val useSystemEmojis: Boolean,
    val showAvatarsInGroups: Boolean,
    val compactMode: Boolean,
    
    // Accessibility settings
    val highContrast: Boolean,
    val largeText: Boolean,
    val reduceMotion: Boolean,
    val screenReaderOptimized: Boolean,
    val hapticFeedback: Boolean,
    val voiceOverEnabled: Boolean,
    
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension function to convert UserSettingsEntity to domain UserSettings model
 */
fun UserSettingsEntity.toDomain(): UserSettings {
    return UserSettings(
        userId = userId,
        profile = UserProfile(
            displayName = displayName,
            bio = bio,
            avatar = avatar,
            phoneNumber = phoneNumber,
            email = email,
            showOnlineStatus = showOnlineStatus,
            showLastSeen = showLastSeen
        ),
        privacy = PrivacySettings(
            readReceipts = readReceipts,
            typingIndicators = typingIndicators,
            profilePhotoVisibility = ProfileVisibility.valueOf(profilePhotoVisibility),
            lastSeenVisibility = ProfileVisibility.valueOf(lastSeenVisibility),
            onlineStatusVisibility = ProfileVisibility.valueOf(onlineStatusVisibility),
            groupInvitePermission = GroupInvitePermission.valueOf(groupInvitePermission),
            disappearingMessagesDefault = DisappearingMessageTimer.valueOf(disappearingMessagesDefault),
            screenshotNotifications = screenshotNotifications,
            forwardingRestriction = forwardingRestriction
        ),
        notifications = NotificationSettings(
            messageNotifications = messageNotifications,
            callNotifications = callNotifications,
            groupNotifications = groupNotifications,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            ledEnabled = ledEnabled,
            notificationSound = notificationSound,
            quietHoursEnabled = quietHoursEnabled,
            quietHoursStart = quietHoursStart,
            quietHoursEnd = quietHoursEnd,
            showPreview = showPreview,
            showSenderName = showSenderName
        ),
        appearance = AppearanceSettings(
            theme = AppTheme.valueOf(theme),
            fontSize = FontSize.valueOf(fontSize),
            chatWallpaper = chatWallpaper,
            useSystemEmojis = useSystemEmojis,
            showAvatarsInGroups = showAvatarsInGroups,
            compactMode = compactMode
        ),
        accessibility = AccessibilitySettings(
            highContrast = highContrast,
            largeText = largeText,
            reduceMotion = reduceMotion,
            screenReaderOptimized = screenReaderOptimized,
            hapticFeedback = hapticFeedback,
            voiceOverEnabled = voiceOverEnabled
        ),
        updatedAt = updatedAt
    )
}

/**
 * Extension function to convert domain UserSettings model to UserSettingsEntity
 */
fun UserSettings.toEntity(): UserSettingsEntity {
    return UserSettingsEntity(
        userId = userId,
        displayName = profile.displayName,
        bio = profile.bio,
        avatar = profile.avatar,
        phoneNumber = profile.phoneNumber,
        email = profile.email,
        showOnlineStatus = profile.showOnlineStatus,
        showLastSeen = profile.showLastSeen,
        readReceipts = privacy.readReceipts,
        typingIndicators = privacy.typingIndicators,
        profilePhotoVisibility = privacy.profilePhotoVisibility.name,
        lastSeenVisibility = privacy.lastSeenVisibility.name,
        onlineStatusVisibility = privacy.onlineStatusVisibility.name,
        groupInvitePermission = privacy.groupInvitePermission.name,
        disappearingMessagesDefault = privacy.disappearingMessagesDefault.name,
        screenshotNotifications = privacy.screenshotNotifications,
        forwardingRestriction = privacy.forwardingRestriction,
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
        theme = appearance.theme.name,
        fontSize = appearance.fontSize.name,
        chatWallpaper = appearance.chatWallpaper,
        useSystemEmojis = appearance.useSystemEmojis,
        showAvatarsInGroups = appearance.showAvatarsInGroups,
        compactMode = appearance.compactMode,
        highContrast = accessibility.highContrast,
        largeText = accessibility.largeText,
        reduceMotion = accessibility.reduceMotion,
        screenReaderOptimized = accessibility.screenReaderOptimized,
        hapticFeedback = accessibility.hapticFeedback,
        voiceOverEnabled = accessibility.voiceOverEnabled,
        updatedAt = updatedAt
    )
}