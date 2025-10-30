package com.chain.messaging.presentation.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.usecase.GetUserSettingsUseCase
import com.chain.messaging.domain.usecase.UpdateUserSettingsUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for SettingsViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var getUserSettingsUseCase: GetUserSettingsUseCase
    private lateinit var updateUserSettingsUseCase: UpdateUserSettingsUseCase
    private lateinit var viewModel: SettingsViewModel
    
    private val testUserId = "test_user_id"
    private val testDisplayName = "Test User"
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        getUserSettingsUseCase = mockk()
        updateUserSettingsUseCase = mockk()
        
        // Default mock behavior
        every { getUserSettingsUseCase.asFlow(any()) } returns flowOf(null)
        
        viewModel = SettingsViewModel(getUserSettingsUseCase, updateUserSettingsUseCase)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    
    @Test
    fun `initial state is correct`() {
        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.settings)
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
    }
    
    @Test
    fun `loadSettings updates state with settings`() = runTest {
        // Given
        val expectedSettings = createTestSettings()
        every { getUserSettingsUseCase.asFlow(testUserId) } returns flowOf(expectedSettings)
        
        // When
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(expectedSettings, uiState.settings)
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        verify { getUserSettingsUseCase.asFlow(testUserId) }
    }
    
    @Test
    fun `loadSettings sets loading state initially`() = runTest {
        // Given
        every { getUserSettingsUseCase.asFlow(testUserId) } returns flowOf(null)
        
        // When
        viewModel.loadSettings(testUserId)
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
    }
    
    @Test
    fun `updateProfile successfully updates profile`() = runTest {
        // Given
        val profile = UserProfile(displayName = "Updated Name")
        coEvery { updateUserSettingsUseCase.updateProfile(testUserId, profile) } returns Result.success(Unit)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updateProfile(profile)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        coVerify { updateUserSettingsUseCase.updateProfile(testUserId, profile) }
    }
    
    @Test
    fun `updateProfile handles failure`() = runTest {
        // Given
        val profile = UserProfile(displayName = "Updated Name")
        val exception = RuntimeException("Update failed")
        coEvery { updateUserSettingsUseCase.updateProfile(testUserId, profile) } returns Result.failure(exception)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updateProfile(profile)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("Update failed", uiState.error)
        coVerify { updateUserSettingsUseCase.updateProfile(testUserId, profile) }
    }
    
    @Test
    fun `updatePrivacySettings successfully updates privacy settings`() = runTest {
        // Given
        val privacy = PrivacySettings(readReceipts = false)
        coEvery { updateUserSettingsUseCase.updatePrivacySettings(testUserId, privacy) } returns Result.success(Unit)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updatePrivacySettings(privacy)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        coVerify { updateUserSettingsUseCase.updatePrivacySettings(testUserId, privacy) }
    }
    
    @Test
    fun `updateNotificationSettings successfully updates notification settings`() = runTest {
        // Given
        val notifications = NotificationSettings(messageNotifications = false)
        coEvery { updateUserSettingsUseCase.updateNotificationSettings(testUserId, notifications) } returns Result.success(Unit)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updateNotificationSettings(notifications)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        coVerify { updateUserSettingsUseCase.updateNotificationSettings(testUserId, notifications) }
    }
    
    @Test
    fun `updateAppearanceSettings successfully updates appearance settings`() = runTest {
        // Given
        val appearance = AppearanceSettings(theme = AppTheme.DARK)
        coEvery { updateUserSettingsUseCase.updateAppearanceSettings(testUserId, appearance) } returns Result.success(Unit)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updateAppearanceSettings(appearance)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        coVerify { updateUserSettingsUseCase.updateAppearanceSettings(testUserId, appearance) }
    }
    
    @Test
    fun `updateAccessibilitySettings successfully updates accessibility settings`() = runTest {
        // Given
        val accessibility = AccessibilitySettings(highContrast = true)
        coEvery { updateUserSettingsUseCase.updateAccessibilitySettings(testUserId, accessibility) } returns Result.success(Unit)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updateAccessibilitySettings(accessibility)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        coVerify { updateUserSettingsUseCase.updateAccessibilitySettings(testUserId, accessibility) }
    }
    
    @Test
    fun `createDefaultSettings successfully creates default settings`() = runTest {
        // Given
        val expectedSettings = createTestSettings()
        coEvery { updateUserSettingsUseCase.createDefaultSettings(testUserId, testDisplayName) } returns Result.success(expectedSettings)
        every { getUserSettingsUseCase.asFlow(testUserId) } returns flowOf(expectedSettings)
        
        // When
        viewModel.createDefaultSettings(testUserId, testDisplayName)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        coVerify { updateUserSettingsUseCase.createDefaultSettings(testUserId, testDisplayName) }
    }
    
    @Test
    fun `clearError clears error state`() = runTest {
        // Given - set an error state first
        val profile = UserProfile(displayName = "Updated Name")
        val exception = RuntimeException("Update failed")
        coEvery { updateUserSettingsUseCase.updateProfile(testUserId, profile) } returns Result.failure(exception)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateProfile(profile)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify error is set
        assertNotNull(viewModel.uiState.value.error)
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `updateWallpaper successfully updates wallpaper`() = runTest {
        // Given
        val wallpaperPath = "content://media/wallpaper.jpg"
        val currentSettings = createTestSettings()
        val updatedAppearance = currentSettings.appearance.copy(chatWallpaper = wallpaperPath)
        
        coEvery { updateUserSettingsUseCase.updateAppearanceSettings(testUserId, updatedAppearance) } returns Result.success(Unit)
        every { getUserSettingsUseCase.asFlow(testUserId) } returns flowOf(currentSettings)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updateWallpaper(wallpaperPath)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        coVerify { updateUserSettingsUseCase.updateAppearanceSettings(testUserId, updatedAppearance) }
    }
    
    @Test
    fun `resetWallpaper successfully resets wallpaper to null`() = runTest {
        // Given
        val currentSettings = createTestSettings().copy(
            appearance = AppearanceSettings(chatWallpaper = "some_wallpaper.jpg")
        )
        val updatedAppearance = currentSettings.appearance.copy(chatWallpaper = null)
        
        coEvery { updateUserSettingsUseCase.updateAppearanceSettings(testUserId, updatedAppearance) } returns Result.success(Unit)
        every { getUserSettingsUseCase.asFlow(testUserId) } returns flowOf(currentSettings)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.resetWallpaper()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        coVerify { updateUserSettingsUseCase.updateAppearanceSettings(testUserId, updatedAppearance) }
    }
    
    @Test
    fun `updateWallpaper handles failure`() = runTest {
        // Given
        val wallpaperPath = "content://media/wallpaper.jpg"
        val currentSettings = createTestSettings()
        val updatedAppearance = currentSettings.appearance.copy(chatWallpaper = wallpaperPath)
        val exception = RuntimeException("Failed to update wallpaper")
        
        coEvery { updateUserSettingsUseCase.updateAppearanceSettings(testUserId, updatedAppearance) } returns Result.failure(exception)
        every { getUserSettingsUseCase.asFlow(testUserId) } returns flowOf(currentSettings)
        viewModel.loadSettings(testUserId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updateWallpaper(wallpaperPath)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("Failed to update wallpaper", uiState.error)
        coVerify { updateUserSettingsUseCase.updateAppearanceSettings(testUserId, updatedAppearance) }
    }
    
    @Test
    fun `operations do nothing when user ID is not set`() = runTest {
        // Given - don't load settings (no user ID set)
        val profile = UserProfile(displayName = "Updated Name")
        
        // When
        viewModel.updateProfile(profile)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify(exactly = 0) { updateUserSettingsUseCase.updateProfile(any(), any()) }
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