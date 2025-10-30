package com.chain.messaging.data.repository

import com.chain.messaging.data.local.dao.UserSettingsDao
import com.chain.messaging.data.local.entity.UserSettingsEntity
import com.chain.messaging.domain.model.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SettingsRepositoryImpl
 */
class SettingsRepositoryImplTest {
    
    private lateinit var settingsDao: UserSettingsDao
    private lateinit var repository: SettingsRepositoryImpl
    
    private val testUserId = "test_user_id"
    private val testDisplayName = "Test User"
    
    @Before
    fun setup() {
        settingsDao = mockk()
        repository = SettingsRepositoryImpl(settingsDao)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `getSettings returns null when no settings exist`() = runTest {
        // Given
        coEvery { settingsDao.getSettings(testUserId) } returns null
        
        // When
        val result = repository.getSettings(testUserId)
        
        // Then
        assertNull(result)
        coVerify { settingsDao.getSettings(testUserId) }
    }
    
    @Test
    fun `getSettings returns settings when they exist`() = runTest {
        // Given
        val settingsEntity = createTestSettingsEntity()
        coEvery { settingsDao.getSettings(testUserId) } returns settingsEntity
        
        // When
        val result = repository.getSettings(testUserId)
        
        // Then
        assertNotNull(result)
        assertEquals(testUserId, result?.userId)
        assertEquals(testDisplayName, result?.profile?.displayName)
        coVerify { settingsDao.getSettings(testUserId) }
    }
    
    @Test
    fun `getSettingsFlow returns flow of settings`() = runTest {
        // Given
        val settingsEntity = createTestSettingsEntity()
        every { settingsDao.getSettingsFlow(testUserId) } returns flowOf(settingsEntity)
        
        // When
        val flow = repository.getSettingsFlow(testUserId)
        
        // Then
        flow.collect { result ->
            assertNotNull(result)
            assertEquals(testUserId, result?.userId)
            assertEquals(testDisplayName, result?.profile?.displayName)
        }
        verify { settingsDao.getSettingsFlow(testUserId) }
    }
    
    @Test
    fun `saveSettings successfully saves settings`() = runTest {
        // Given
        val settings = createTestSettings()
        coEvery { settingsDao.insertOrUpdateSettings(any()) } just Runs
        
        // When
        val result = repository.saveSettings(settings)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { settingsDao.insertOrUpdateSettings(any()) }
    }
    
    @Test
    fun `saveSettings returns failure when dao throws exception`() = runTest {
        // Given
        val settings = createTestSettings()
        val exception = RuntimeException("Database error")
        coEvery { settingsDao.insertOrUpdateSettings(any()) } throws exception
        
        // When
        val result = repository.saveSettings(settings)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { settingsDao.insertOrUpdateSettings(any()) }
    }
    
    @Test
    fun `updateProfile successfully updates profile`() = runTest {
        // Given
        val profile = UserProfile(displayName = "Updated Name")
        coEvery { settingsDao.updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
        
        // When
        val result = repository.updateProfile(testUserId, profile)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            settingsDao.updateProfile(
                userId = testUserId,
                displayName = "Updated Name",
                bio = null,
                avatar = null,
                phoneNumber = null,
                email = null,
                showOnlineStatus = true,
                showLastSeen = true,
                updatedAt = any()
            )
        }
    }
    
    @Test
    fun `updatePrivacySettings successfully updates privacy settings`() = runTest {
        // Given
        val privacy = PrivacySettings(readReceipts = false)
        coEvery { settingsDao.updatePrivacySettings(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
        
        // When
        val result = repository.updatePrivacySettings(testUserId, privacy)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            settingsDao.updatePrivacySettings(
                userId = testUserId,
                readReceipts = false,
                typingIndicators = true,
                profilePhotoVisibility = ProfileVisibility.EVERYONE.name,
                lastSeenVisibility = ProfileVisibility.EVERYONE.name,
                onlineStatusVisibility = ProfileVisibility.EVERYONE.name,
                groupInvitePermission = GroupInvitePermission.EVERYONE.name,
                disappearingMessagesDefault = DisappearingMessageTimer.OFF.name,
                screenshotNotifications = true,
                forwardingRestriction = false,
                updatedAt = any()
            )
        }
    }
    
    @Test
    fun `updateNotificationSettings successfully updates notification settings`() = runTest {
        // Given
        val notifications = NotificationSettings(messageNotifications = false)
        coEvery { settingsDao.updateNotificationSettings(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
        
        // When
        val result = repository.updateNotificationSettings(testUserId, notifications)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            settingsDao.updateNotificationSettings(
                userId = testUserId,
                messageNotifications = false,
                callNotifications = true,
                groupNotifications = true,
                soundEnabled = true,
                vibrationEnabled = true,
                ledEnabled = true,
                notificationSound = "default",
                quietHoursEnabled = false,
                quietHoursStart = "22:00",
                quietHoursEnd = "08:00",
                showPreview = true,
                showSenderName = true,
                updatedAt = any()
            )
        }
    }
    
    @Test
    fun `updateAppearanceSettings successfully updates appearance settings`() = runTest {
        // Given
        val appearance = AppearanceSettings(theme = AppTheme.DARK)
        coEvery { settingsDao.updateAppearanceSettings(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
        
        // When
        val result = repository.updateAppearanceSettings(testUserId, appearance)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            settingsDao.updateAppearanceSettings(
                userId = testUserId,
                theme = AppTheme.DARK.name,
                fontSize = FontSize.MEDIUM.name,
                chatWallpaper = null,
                useSystemEmojis = false,
                showAvatarsInGroups = true,
                compactMode = false,
                updatedAt = any()
            )
        }
    }
    
    @Test
    fun `updateAccessibilitySettings successfully updates accessibility settings`() = runTest {
        // Given
        val accessibility = AccessibilitySettings(highContrast = true)
        coEvery { settingsDao.updateAccessibilitySettings(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
        
        // When
        val result = repository.updateAccessibilitySettings(testUserId, accessibility)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            settingsDao.updateAccessibilitySettings(
                userId = testUserId,
                highContrast = true,
                largeText = false,
                reduceMotion = false,
                screenReaderOptimized = false,
                hapticFeedback = true,
                voiceOverEnabled = false,
                updatedAt = any()
            )
        }
    }
    
    @Test
    fun `createDefaultSettings creates and saves default settings`() = runTest {
        // Given
        coEvery { settingsDao.insertOrUpdateSettings(any()) } just Runs
        
        // When
        val result = repository.createDefaultSettings(testUserId, testDisplayName)
        
        // Then
        assertTrue(result.isSuccess)
        val settings = result.getOrNull()
        assertNotNull(settings)
        assertEquals(testUserId, settings?.userId)
        assertEquals(testDisplayName, settings?.profile?.displayName)
        coVerify { settingsDao.insertOrUpdateSettings(any()) }
    }
    
    @Test
    fun `deleteSettings successfully deletes settings`() = runTest {
        // Given
        coEvery { settingsDao.deleteSettings(testUserId) } just Runs
        
        // When
        val result = repository.deleteSettings(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { settingsDao.deleteSettings(testUserId) }
    }
    
    @Test
    fun `settingsExist returns true when settings exist`() = runTest {
        // Given
        coEvery { settingsDao.settingsExist(testUserId) } returns true
        
        // When
        val result = repository.settingsExist(testUserId)
        
        // Then
        assertTrue(result)
        coVerify { settingsDao.settingsExist(testUserId) }
    }
    
    @Test
    fun `settingsExist returns false when settings don't exist`() = runTest {
        // Given
        coEvery { settingsDao.settingsExist(testUserId) } returns false
        
        // When
        val result = repository.settingsExist(testUserId)
        
        // Then
        assertFalse(result)
        coVerify { settingsDao.settingsExist(testUserId) }
    }
    
    private fun createTestSettings(): UserSettings {
        return UserSettings(
            userId = testUserId,
            profile = UserProfile(displayName = testDisplayName),
            privacy = PrivacySettings(),
            notifications = NotificationSettings(),
            appearance = AppearanceSettings(),
            accessibility = AccessibilitySettings()
        )
    }
    
    private fun createTestSettingsEntity(): UserSettingsEntity {
        return UserSettingsEntity(
            userId = testUserId,
            displayName = testDisplayName,
            bio = null,
            avatar = null,
            phoneNumber = null,
            email = null,
            showOnlineStatus = true,
            showLastSeen = true,
            readReceipts = true,
            typingIndicators = true,
            profilePhotoVisibility = ProfileVisibility.EVERYONE.name,
            lastSeenVisibility = ProfileVisibility.EVERYONE.name,
            onlineStatusVisibility = ProfileVisibility.EVERYONE.name,
            groupInvitePermission = GroupInvitePermission.EVERYONE.name,
            disappearingMessagesDefault = DisappearingMessageTimer.OFF.name,
            screenshotNotifications = true,
            forwardingRestriction = false,
            messageNotifications = true,
            callNotifications = true,
            groupNotifications = true,
            soundEnabled = true,
            vibrationEnabled = true,
            ledEnabled = true,
            notificationSound = "default",
            quietHoursEnabled = false,
            quietHoursStart = "22:00",
            quietHoursEnd = "08:00",
            showPreview = true,
            showSenderName = true,
            theme = AppTheme.SYSTEM.name,
            fontSize = FontSize.MEDIUM.name,
            chatWallpaper = null,
            useSystemEmojis = false,
            showAvatarsInGroups = true,
            compactMode = false,
            highContrast = false,
            largeText = false,
            reduceMotion = false,
            screenReaderOptimized = false,
            hapticFeedback = true,
            voiceOverEnabled = false
        )
    }
}