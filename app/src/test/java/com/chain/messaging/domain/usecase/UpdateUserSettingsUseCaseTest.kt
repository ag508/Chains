package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.SettingsRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UpdateUserSettingsUseCase
 */
class UpdateUserSettingsUseCaseTest {
    
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: UpdateUserSettingsUseCase
    
    private val testUserId = "test_user_id"
    private val testDisplayName = "Test User"
    
    @Before
    fun setup() {
        settingsRepository = mockk()
        useCase = UpdateUserSettingsUseCase(settingsRepository)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `updateSettings successfully updates complete settings`() = runTest {
        // Given
        val settings = createTestSettings()
        coEvery { settingsRepository.saveSettings(settings) } returns Result.success(Unit)
        
        // When
        val result = useCase.updateSettings(settings)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { settingsRepository.saveSettings(settings) }
    }
    
    @Test
    fun `updateSettings returns failure when repository fails`() = runTest {
        // Given
        val settings = createTestSettings()
        val exception = RuntimeException("Update failed")
        coEvery { settingsRepository.saveSettings(settings) } returns Result.failure(exception)
        
        // When
        val result = useCase.updateSettings(settings)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { settingsRepository.saveSettings(settings) }
    }
    
    @Test
    fun `updateProfile successfully updates profile`() = runTest {
        // Given
        val profile = UserProfile(displayName = "Updated Name")
        coEvery { settingsRepository.updateProfile(testUserId, profile) } returns Result.success(Unit)
        
        // When
        val result = useCase.updateProfile(testUserId, profile)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { settingsRepository.updateProfile(testUserId, profile) }
    }
    
    @Test
    fun `updatePrivacySettings successfully updates privacy settings`() = runTest {
        // Given
        val privacy = PrivacySettings(readReceipts = false)
        coEvery { settingsRepository.updatePrivacySettings(testUserId, privacy) } returns Result.success(Unit)
        
        // When
        val result = useCase.updatePrivacySettings(testUserId, privacy)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { settingsRepository.updatePrivacySettings(testUserId, privacy) }
    }
    
    @Test
    fun `updateNotificationSettings successfully updates notification settings`() = runTest {
        // Given
        val notifications = NotificationSettings(messageNotifications = false)
        coEvery { settingsRepository.updateNotificationSettings(testUserId, notifications) } returns Result.success(Unit)
        
        // When
        val result = useCase.updateNotificationSettings(testUserId, notifications)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { settingsRepository.updateNotificationSettings(testUserId, notifications) }
    }
    
    @Test
    fun `updateAppearanceSettings successfully updates appearance settings`() = runTest {
        // Given
        val appearance = AppearanceSettings(theme = AppTheme.DARK)
        coEvery { settingsRepository.updateAppearanceSettings(testUserId, appearance) } returns Result.success(Unit)
        
        // When
        val result = useCase.updateAppearanceSettings(testUserId, appearance)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { settingsRepository.updateAppearanceSettings(testUserId, appearance) }
    }
    
    @Test
    fun `updateAccessibilitySettings successfully updates accessibility settings`() = runTest {
        // Given
        val accessibility = AccessibilitySettings(highContrast = true)
        coEvery { settingsRepository.updateAccessibilitySettings(testUserId, accessibility) } returns Result.success(Unit)
        
        // When
        val result = useCase.updateAccessibilitySettings(testUserId, accessibility)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { settingsRepository.updateAccessibilitySettings(testUserId, accessibility) }
    }
    
    @Test
    fun `createDefaultSettings successfully creates default settings`() = runTest {
        // Given
        val expectedSettings = createTestSettings()
        coEvery { settingsRepository.createDefaultSettings(testUserId, testDisplayName) } returns Result.success(expectedSettings)
        
        // When
        val result = useCase.createDefaultSettings(testUserId, testDisplayName)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedSettings, result.getOrNull())
        coVerify { settingsRepository.createDefaultSettings(testUserId, testDisplayName) }
    }
    
    @Test
    fun `createDefaultSettings returns failure when repository fails`() = runTest {
        // Given
        val exception = RuntimeException("Creation failed")
        coEvery { settingsRepository.createDefaultSettings(testUserId, testDisplayName) } returns Result.failure(exception)
        
        // When
        val result = useCase.createDefaultSettings(testUserId, testDisplayName)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { settingsRepository.createDefaultSettings(testUserId, testDisplayName) }
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
}