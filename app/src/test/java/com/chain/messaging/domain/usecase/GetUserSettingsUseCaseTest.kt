package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.repository.SettingsRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetUserSettingsUseCase
 */
class GetUserSettingsUseCaseTest {
    
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetUserSettingsUseCase
    
    private val testUserId = "test_user_id"
    
    @Before
    fun setup() {
        settingsRepository = mockk()
        useCase = GetUserSettingsUseCase(settingsRepository)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `invoke returns settings when they exist`() = runTest {
        // Given
        val expectedSettings = createTestSettings()
        coEvery { settingsRepository.getSettings(testUserId) } returns expectedSettings
        
        // When
        val result = useCase(testUserId)
        
        // Then
        assertEquals(expectedSettings, result)
        coVerify { settingsRepository.getSettings(testUserId) }
    }
    
    @Test
    fun `invoke returns null when settings don't exist`() = runTest {
        // Given
        coEvery { settingsRepository.getSettings(testUserId) } returns null
        
        // When
        val result = useCase(testUserId)
        
        // Then
        assertNull(result)
        coVerify { settingsRepository.getSettings(testUserId) }
    }
    
    @Test
    fun `asFlow returns flow of settings`() = runTest {
        // Given
        val expectedSettings = createTestSettings()
        every { settingsRepository.getSettingsFlow(testUserId) } returns flowOf(expectedSettings)
        
        // When
        val flow = useCase.asFlow(testUserId)
        
        // Then
        flow.collect { result ->
            assertEquals(expectedSettings, result)
        }
        verify { settingsRepository.getSettingsFlow(testUserId) }
    }
    
    @Test
    fun `asFlow returns flow of null when settings don't exist`() = runTest {
        // Given
        every { settingsRepository.getSettingsFlow(testUserId) } returns flowOf(null)
        
        // When
        val flow = useCase.asFlow(testUserId)
        
        // Then
        flow.collect { result ->
            assertNull(result)
        }
        verify { settingsRepository.getSettingsFlow(testUserId) }
    }
    
    private fun createTestSettings(): UserSettings {
        return UserSettings(
            userId = testUserId,
            profile = UserProfile(displayName = "Test User"),
            privacy = PrivacySettings(),
            notifications = NotificationSettings(),
            appearance = AppearanceSettings(),
            accessibility = AccessibilitySettings()
        )
    }
}