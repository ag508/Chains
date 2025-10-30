package com.chain.messaging.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.core.auth.AuthenticationService
import com.chain.messaging.core.auth.UserIdentity
import com.chain.messaging.domain.model.*
import com.chain.messaging.domain.usecase.GetUserSettingsUseCase
import com.chain.messaging.domain.usecase.UpdateUserSettingsUseCase
import com.chain.messaging.presentation.settings.AppearanceSettingsScreen
import com.chain.messaging.presentation.settings.SettingsViewModel
import com.chain.messaging.presentation.theme.ChainTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WallpaperPickerIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var getUserSettingsUseCase: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettingsUseCase: UpdateUserSettingsUseCase

    @Inject
    lateinit var authenticationService: AuthenticationService

    private lateinit var viewModel: SettingsViewModel

    private val testUser = UserIdentity(
        userId = "test_user_id",
        displayName = "Test User",
        publicKey = "test_public_key"
    )

    private val testSettings = UserSettings(
        userId = "test_user_id",
        profile = UserProfile(
            displayName = "Test User",
            bio = "Test bio",
            avatar = null
        ),
        privacy = PrivacySettings(),
        notifications = NotificationSettings(),
        appearance = AppearanceSettings(
            theme = AppTheme.SYSTEM,
            fontSize = FontSize.MEDIUM,
            chatWallpaper = null,
            useSystemEmojis = false,
            showAvatarsInGroups = true,
            compactMode = false
        ),
        accessibility = AccessibilitySettings()
    )

    @Before
    fun setup() {
        hiltRule.inject()

        // Mock authentication service
        every { authenticationService.getCurrentUser() } returns testUser

        // Mock use cases
        every { getUserSettingsUseCase.asFlow("test_user_id") } returns flowOf(testSettings)
        coEvery { updateUserSettingsUseCase.updateAppearanceSettings(any(), any()) } returns Result.success(Unit)

        viewModel = SettingsViewModel(
            getUserSettingsUseCase,
            updateUserSettingsUseCase,
            authenticationService,
            mockk(relaxed = true)
        )
    }

    @Test
    fun wallpaperPicker_opensWhenButtonClicked() {
        composeTestRule.setContent {
            ChainTheme {
                AppearanceSettingsScreen(
                    onNavigateBack = { },
                    viewModel = viewModel
                )
            }
        }

        // Wait for settings to load
        composeTestRule.waitForIdle()

        // Click on wallpaper picker button
        composeTestRule
            .onNodeWithText("Choose Wallpaper")
            .performClick()

        // Wallpaper picker dialog should appear
        composeTestRule
            .onNodeWithText("Choose Chat Wallpaper")
            .assertIsDisplayed()
    }

    @Test
    fun wallpaperPicker_selectsPredefinedWallpaper() {
        composeTestRule.setContent {
            ChainTheme {
                AppearanceSettingsScreen(
                    onNavigateBack = { },
                    viewModel = viewModel
                )
            }
        }

        // Wait for settings to load
        composeTestRule.waitForIdle()

        // Click on wallpaper picker button
        composeTestRule
            .onNodeWithText("Choose Wallpaper")
            .performClick()

        // Select a predefined wallpaper (e.g., solid blue)
        composeTestRule
            .onAllNodes(hasClickAction())
            .filter(hasContentDescription("Selected").not())
            .onFirst()
            .performClick()

        // Verify that updateAppearanceSettings was called
        coVerify {
            updateUserSettingsUseCase.updateAppearanceSettings(
                "test_user_id",
                any()
            )
        }
    }

    @Test
    fun wallpaperPicker_resetsWallpaperToDefault() {
        // Set up settings with existing wallpaper
        val settingsWithWallpaper = testSettings.copy(
            appearance = testSettings.appearance.copy(chatWallpaper = "solid_blue")
        )
        
        every { getUserSettingsUseCase.asFlow("test_user_id") } returns flowOf(settingsWithWallpaper)

        composeTestRule.setContent {
            ChainTheme {
                AppearanceSettingsScreen(
                    onNavigateBack = { },
                    viewModel = viewModel
                )
            }
        }

        // Wait for settings to load
        composeTestRule.waitForIdle()

        // Click reset button
        composeTestRule
            .onNodeWithText("Reset to Default")
            .performClick()

        // Verify that wallpaper was reset (set to null)
        coVerify {
            updateUserSettingsUseCase.updateAppearanceSettings(
                "test_user_id",
                match { it.chatWallpaper == null }
            )
        }
    }

    @Test
    fun wallpaperPicker_showsCurrentWallpaperInfo() {
        // Set up settings with existing wallpaper
        val settingsWithWallpaper = testSettings.copy(
            appearance = testSettings.appearance.copy(chatWallpaper = "solid_blue")
        )
        
        every { getUserSettingsUseCase.asFlow("test_user_id") } returns flowOf(settingsWithWallpaper)

        composeTestRule.setContent {
            ChainTheme {
                AppearanceSettingsScreen(
                    onNavigateBack = { },
                    viewModel = viewModel
                )
            }
        }

        // Wait for settings to load
        composeTestRule.waitForIdle()

        // Should show current wallpaper info
        composeTestRule
            .onNodeWithText("Current: Solid_blue", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun wallpaperPicker_cancelsWithoutChanges() {
        composeTestRule.setContent {
            ChainTheme {
                AppearanceSettingsScreen(
                    onNavigateBack = { },
                    viewModel = viewModel
                )
            }
        }

        // Wait for settings to load
        composeTestRule.waitForIdle()

        // Click on wallpaper picker button
        composeTestRule
            .onNodeWithText("Choose Wallpaper")
            .performClick()

        // Cancel the dialog
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        // Dialog should disappear
        composeTestRule
            .onNodeWithText("Choose Chat Wallpaper")
            .assertDoesNotExist()

        // No update should have been called
        coVerify(exactly = 0) {
            updateUserSettingsUseCase.updateAppearanceSettings(any(), any())
        }
    }
}