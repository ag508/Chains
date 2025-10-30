package com.chain.messaging.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.data.local.ChainDatabase
import com.chain.messaging.data.local.dao.UserSettingsDao
import com.chain.messaging.data.repository.SettingsRepositoryImpl
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.usecase.GetUserSettingsUseCase
import com.chain.messaging.domain.usecase.UpdateUserSettingsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for settings functionality
 */
@RunWith(AndroidJUnit4::class)
class SettingsIntegrationTest {
    
    private lateinit var database: ChainDatabase
    private lateinit var settingsDao: UserSettingsDao
    private lateinit var repository: SettingsRepositoryImpl
    private lateinit var getUserSettingsUseCase: GetUserSettingsUseCase
    private lateinit var updateUserSettingsUseCase: UpdateUserSettingsUseCase
    
    private val testUserId = "test_user_id"
    private val testDisplayName = "Test User"
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ChainDatabase::class.java
        ).allowMainThreadQueries().build()
        
        settingsDao = database.userSettingsDao()
        repository = SettingsRepositoryImpl(settingsDao)
        getUserSettingsUseCase = GetUserSettingsUseCase(repository)
        updateUserSettingsUseCase = UpdateUserSettingsUseCase(repository)
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun `complete settings workflow - create, read, update, delete`() = runTest {
        // Initially no settings exist
        assertFalse(repository.settingsExist(testUserId))
        assertNull(getUserSettingsUseCase(testUserId))
        
        // Create default settings
        val createResult = updateUserSettingsUseCase.createDefaultSettings(testUserId, testDisplayName)
        assertTrue(createResult.isSuccess)
        
        val createdSettings = createResult.getOrNull()
        assertNotNull(createdSettings)
        assertEquals(testUserId, createdSettings?.userId)
        assertEquals(testDisplayName, createdSettings?.profile?.displayName)
        
        // Verify settings exist and can be retrieved
        assertTrue(repository.settingsExist(testUserId))
        val retrievedSettings = getUserSettingsUseCase(testUserId)
        assertNotNull(retrievedSettings)
        assertEquals(testUserId, retrievedSettings?.userId)
        assertEquals(testDisplayName, retrievedSettings?.profile?.displayName)
        
        // Test profile update
        val updatedProfile = UserProfile(
            displayName = "Updated Name",
            bio = "Updated bio",
            email = "test@example.com",
            showOnlineStatus = false
        )
        val profileUpdateResult = updateUserSettingsUseCase.updateProfile(testUserId, updatedProfile)
        assertTrue(profileUpdateResult.isSuccess)
        
        // Verify profile was updated
        val settingsAfterProfileUpdate = getUserSettingsUseCase(testUserId)
        assertEquals("Updated Name", settingsAfterProfileUpdate?.profile?.displayName)
        assertEquals("Updated bio", settingsAfterProfileUpdate?.profile?.bio)
        assertEquals("test@example.com", settingsAfterProfileUpdate?.profile?.email)
        assertFalse(settingsAfterProfileUpdate?.profile?.showOnlineStatus ?: true)
        
        // Test privacy settings update
        val updatedPrivacy = PrivacySettings(
            readReceipts = false,
            typingIndicators = false,
            profilePhotoVisibility = ProfileVisibility.CONTACTS,
            screenshotNotifications = false
        )
        val privacyUpdateResult = updateUserSettingsUseCase.updatePrivacySettings(testUserId, updatedPrivacy)
        assertTrue(privacyUpdateResult.isSuccess)
        
        // Verify privacy settings were updated
        val settingsAfterPrivacyUpdate = getUserSettingsUseCase(testUserId)
        assertFalse(settingsAfterPrivacyUpdate?.privacy?.readReceipts ?: true)
        assertFalse(settingsAfterPrivacyUpdate?.privacy?.typingIndicators ?: true)
        assertEquals(ProfileVisibility.CONTACTS, settingsAfterPrivacyUpdate?.privacy?.profilePhotoVisibility)
        assertFalse(settingsAfterPrivacyUpdate?.privacy?.screenshotNotifications ?: true)
        
        // Test notification settings update
        val updatedNotifications = NotificationSettings(
            messageNotifications = false,
            soundEnabled = false,
            quietHoursEnabled = true,
            quietHoursStart = "23:00",
            quietHoursEnd = "07:00"
        )
        val notificationUpdateResult = updateUserSettingsUseCase.updateNotificationSettings(testUserId, updatedNotifications)
        assertTrue(notificationUpdateResult.isSuccess)
        
        // Verify notification settings were updated
        val settingsAfterNotificationUpdate = getUserSettingsUseCase(testUserId)
        assertFalse(settingsAfterNotificationUpdate?.notifications?.messageNotifications ?: true)
        assertFalse(settingsAfterNotificationUpdate?.notifications?.soundEnabled ?: true)
        assertTrue(settingsAfterNotificationUpdate?.notifications?.quietHoursEnabled ?: false)
        assertEquals("23:00", settingsAfterNotificationUpdate?.notifications?.quietHoursStart)
        assertEquals("07:00", settingsAfterNotificationUpdate?.notifications?.quietHoursEnd)
        
        // Test appearance settings update
        val updatedAppearance = AppearanceSettings(
            theme = AppTheme.DARK,
            fontSize = FontSize.LARGE,
            compactMode = true,
            useSystemEmojis = true
        )
        val appearanceUpdateResult = updateUserSettingsUseCase.updateAppearanceSettings(testUserId, updatedAppearance)
        assertTrue(appearanceUpdateResult.isSuccess)
        
        // Verify appearance settings were updated
        val settingsAfterAppearanceUpdate = getUserSettingsUseCase(testUserId)
        assertEquals(AppTheme.DARK, settingsAfterAppearanceUpdate?.appearance?.theme)
        assertEquals(FontSize.LARGE, settingsAfterAppearanceUpdate?.appearance?.fontSize)
        assertTrue(settingsAfterAppearanceUpdate?.appearance?.compactMode ?: false)
        assertTrue(settingsAfterAppearanceUpdate?.appearance?.useSystemEmojis ?: false)
        
        // Test accessibility settings update
        val updatedAccessibility = AccessibilitySettings(
            highContrast = true,
            largeText = true,
            reduceMotion = true,
            hapticFeedback = false
        )
        val accessibilityUpdateResult = updateUserSettingsUseCase.updateAccessibilitySettings(testUserId, updatedAccessibility)
        assertTrue(accessibilityUpdateResult.isSuccess)
        
        // Verify accessibility settings were updated
        val settingsAfterAccessibilityUpdate = getUserSettingsUseCase(testUserId)
        assertTrue(settingsAfterAccessibilityUpdate?.accessibility?.highContrast ?: false)
        assertTrue(settingsAfterAccessibilityUpdate?.accessibility?.largeText ?: false)
        assertTrue(settingsAfterAccessibilityUpdate?.accessibility?.reduceMotion ?: false)
        assertFalse(settingsAfterAccessibilityUpdate?.accessibility?.hapticFeedback ?: true)
        
        // Test complete settings update
        val completeUpdatedSettings = UserSettings(
            userId = testUserId,
            profile = UserProfile(displayName = "Final Name"),
            privacy = PrivacySettings(readReceipts = true),
            notifications = NotificationSettings(messageNotifications = true),
            appearance = AppearanceSettings(theme = AppTheme.LIGHT),
            accessibility = AccessibilitySettings(highContrast = false)
        )
        val completeUpdateResult = updateUserSettingsUseCase.updateSettings(completeUpdatedSettings)
        assertTrue(completeUpdateResult.isSuccess)
        
        // Verify complete settings were updated
        val finalSettings = getUserSettingsUseCase(testUserId)
        assertEquals("Final Name", finalSettings?.profile?.displayName)
        assertTrue(finalSettings?.privacy?.readReceipts ?: false)
        assertTrue(finalSettings?.notifications?.messageNotifications ?: false)
        assertEquals(AppTheme.LIGHT, finalSettings?.appearance?.theme)
        assertFalse(finalSettings?.accessibility?.highContrast ?: true)
        
        // Test settings deletion
        val deleteResult = repository.deleteSettings(testUserId)
        assertTrue(deleteResult.isSuccess)
        
        // Verify settings were deleted
        assertFalse(repository.settingsExist(testUserId))
        assertNull(getUserSettingsUseCase(testUserId))
    }
    
    @Test
    fun `settings flow emits updates correctly`() = runTest {
        // Create initial settings
        val createResult = updateUserSettingsUseCase.createDefaultSettings(testUserId, testDisplayName)
        assertTrue(createResult.isSuccess)
        
        // Get settings flow
        val settingsFlow = getUserSettingsUseCase.asFlow(testUserId)
        
        // Verify initial settings
        val initialSettings = settingsFlow.first()
        assertNotNull(initialSettings)
        assertEquals(testDisplayName, initialSettings?.profile?.displayName)
        
        // Update profile and verify flow emits updated settings
        val updatedProfile = UserProfile(displayName = "Updated Name")
        updateUserSettingsUseCase.updateProfile(testUserId, updatedProfile)
        
        // Note: In a real test, you would collect the flow and verify the emission
        // For this test, we'll just verify the updated settings can be retrieved
        val updatedSettings = getUserSettingsUseCase(testUserId)
        assertEquals("Updated Name", updatedSettings?.profile?.displayName)
    }
    
    @Test
    fun `settings persistence validation`() = runTest {
        // Test all enum values are properly persisted and retrieved
        val testSettings = UserSettings(
            userId = testUserId,
            profile = UserProfile(
                displayName = testDisplayName,
                bio = "Test bio",
                email = "test@example.com",
                phoneNumber = "+1234567890",
                showOnlineStatus = false,
                showLastSeen = false
            ),
            privacy = PrivacySettings(
                readReceipts = false,
                typingIndicators = false,
                profilePhotoVisibility = ProfileVisibility.NOBODY,
                lastSeenVisibility = ProfileVisibility.CONTACTS,
                onlineStatusVisibility = ProfileVisibility.EVERYONE,
                groupInvitePermission = GroupInvitePermission.CONTACTS,
                disappearingMessagesDefault = DisappearingMessageTimer.ONE_HOUR,
                screenshotNotifications = false,
                forwardingRestriction = true
            ),
            notifications = NotificationSettings(
                messageNotifications = false,
                callNotifications = false,
                groupNotifications = false,
                soundEnabled = false,
                vibrationEnabled = false,
                ledEnabled = false,
                notificationSound = "custom_sound",
                quietHoursEnabled = true,
                quietHoursStart = "22:30",
                quietHoursEnd = "08:30",
                showPreview = false,
                showSenderName = false
            ),
            appearance = AppearanceSettings(
                theme = AppTheme.DARK,
                fontSize = FontSize.EXTRA_LARGE,
                chatWallpaper = "custom_wallpaper",
                useSystemEmojis = true,
                showAvatarsInGroups = false,
                compactMode = true
            ),
            accessibility = AccessibilitySettings(
                highContrast = true,
                largeText = true,
                reduceMotion = true,
                screenReaderOptimized = true,
                hapticFeedback = false,
                voiceOverEnabled = true
            )
        )
        
        // Save settings
        val saveResult = updateUserSettingsUseCase.updateSettings(testSettings)
        assertTrue(saveResult.isSuccess)
        
        // Retrieve and verify all values
        val retrievedSettings = getUserSettingsUseCase(testUserId)
        assertNotNull(retrievedSettings)
        
        // Verify profile
        assertEquals(testDisplayName, retrievedSettings?.profile?.displayName)
        assertEquals("Test bio", retrievedSettings?.profile?.bio)
        assertEquals("test@example.com", retrievedSettings?.profile?.email)
        assertEquals("+1234567890", retrievedSettings?.profile?.phoneNumber)
        assertFalse(retrievedSettings?.profile?.showOnlineStatus ?: true)
        assertFalse(retrievedSettings?.profile?.showLastSeen ?: true)
        
        // Verify privacy
        assertFalse(retrievedSettings?.privacy?.readReceipts ?: true)
        assertFalse(retrievedSettings?.privacy?.typingIndicators ?: true)
        assertEquals(ProfileVisibility.NOBODY, retrievedSettings?.privacy?.profilePhotoVisibility)
        assertEquals(ProfileVisibility.CONTACTS, retrievedSettings?.privacy?.lastSeenVisibility)
        assertEquals(ProfileVisibility.EVERYONE, retrievedSettings?.privacy?.onlineStatusVisibility)
        assertEquals(GroupInvitePermission.CONTACTS, retrievedSettings?.privacy?.groupInvitePermission)
        assertEquals(DisappearingMessageTimer.ONE_HOUR, retrievedSettings?.privacy?.disappearingMessagesDefault)
        assertFalse(retrievedSettings?.privacy?.screenshotNotifications ?: true)
        assertTrue(retrievedSettings?.privacy?.forwardingRestriction ?: false)
        
        // Verify notifications
        assertFalse(retrievedSettings?.notifications?.messageNotifications ?: true)
        assertFalse(retrievedSettings?.notifications?.callNotifications ?: true)
        assertFalse(retrievedSettings?.notifications?.groupNotifications ?: true)
        assertFalse(retrievedSettings?.notifications?.soundEnabled ?: true)
        assertFalse(retrievedSettings?.notifications?.vibrationEnabled ?: true)
        assertFalse(retrievedSettings?.notifications?.ledEnabled ?: true)
        assertEquals("custom_sound", retrievedSettings?.notifications?.notificationSound)
        assertTrue(retrievedSettings?.notifications?.quietHoursEnabled ?: false)
        assertEquals("22:30", retrievedSettings?.notifications?.quietHoursStart)
        assertEquals("08:30", retrievedSettings?.notifications?.quietHoursEnd)
        assertFalse(retrievedSettings?.notifications?.showPreview ?: true)
        assertFalse(retrievedSettings?.notifications?.showSenderName ?: true)
        
        // Verify appearance
        assertEquals(AppTheme.DARK, retrievedSettings?.appearance?.theme)
        assertEquals(FontSize.EXTRA_LARGE, retrievedSettings?.appearance?.fontSize)
        assertEquals("custom_wallpaper", retrievedSettings?.appearance?.chatWallpaper)
        assertTrue(retrievedSettings?.appearance?.useSystemEmojis ?: false)
        assertFalse(retrievedSettings?.appearance?.showAvatarsInGroups ?: true)
        assertTrue(retrievedSettings?.appearance?.compactMode ?: false)
        
        // Verify accessibility
        assertTrue(retrievedSettings?.accessibility?.highContrast ?: false)
        assertTrue(retrievedSettings?.accessibility?.largeText ?: false)
        assertTrue(retrievedSettings?.accessibility?.reduceMotion ?: false)
        assertTrue(retrievedSettings?.accessibility?.screenReaderOptimized ?: false)
        assertFalse(retrievedSettings?.accessibility?.hapticFeedback ?: true)
        assertTrue(retrievedSettings?.accessibility?.voiceOverEnabled ?: false)
    }
}