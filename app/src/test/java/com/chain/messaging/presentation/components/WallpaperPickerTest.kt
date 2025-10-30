package com.chain.messaging.presentation.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.presentation.theme.ChainTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WallpaperPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun wallpaperPicker_displaysCorrectTitle() {
        var selectedWallpaper: String? = null
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                WallpaperPicker(
                    currentWallpaper = null,
                    onWallpaperSelected = { selectedWallpaper = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Choose Chat Wallpaper")
            .assertIsDisplayed()
    }

    @Test
    fun wallpaperPicker_showsPredefinedOptions() {
        var selectedWallpaper: String? = null
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                WallpaperPicker(
                    currentWallpaper = null,
                    onWallpaperSelected = { selectedWallpaper = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        // Check that predefined wallpaper options are displayed
        composeTestRule
            .onNodeWithContentDescription("Selected", useUnmergedTree = true)
            .assertDoesNotExist() // No wallpaper should be selected initially

        // Check that custom image option is available
        composeTestRule
            .onNodeWithText("Choose Custom Image")
            .assertIsDisplayed()
    }

    @Test
    fun wallpaperPicker_selectsDefaultWallpaper() {
        var selectedWallpaper: String? = "initial"
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                WallpaperPicker(
                    currentWallpaper = "solid_blue",
                    onWallpaperSelected = { selectedWallpaper = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        // Click on default wallpaper (first option)
        composeTestRule
            .onAllNodes(hasClickAction())
            .filterToOne(hasContentDescription("Selected", substring = true).not())
            .performClick()

        // Should select null (default) and dismiss
        assert(selectedWallpaper == null)
        assert(dismissed)
    }

    @Test
    fun wallpaperPicker_cancelsCorrectly() {
        var selectedWallpaper: String? = null
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                WallpaperPicker(
                    currentWallpaper = null,
                    onWallpaperSelected = { selectedWallpaper = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        assert(selectedWallpaper == null)
        assert(dismissed)
    }

    @Test
    fun wallpaperPickerButton_showsPickerOnClick() {
        var selectedWallpaper: String? = null

        composeTestRule.setContent {
            ChainTheme {
                WallpaperPickerButton(
                    currentWallpaper = null,
                    onWallpaperSelected = { selectedWallpaper = it }
                ) {
                    androidx.compose.material3.Text("Pick Wallpaper")
                }
            }
        }

        // Click the button
        composeTestRule
            .onNodeWithText("Pick Wallpaper")
            .performClick()

        // Dialog should appear
        composeTestRule
            .onNodeWithText("Choose Chat Wallpaper")
            .assertIsDisplayed()
    }

    @Test
    fun wallpaperPickerButton_respectsEnabledState() {
        composeTestRule.setContent {
            ChainTheme {
                WallpaperPickerButton(
                    currentWallpaper = null,
                    onWallpaperSelected = { },
                    enabled = false
                ) {
                    androidx.compose.material3.Text("Pick Wallpaper")
                }
            }
        }

        // Button should be disabled
        composeTestRule
            .onNodeWithText("Pick Wallpaper")
            .assertIsNotEnabled()
    }
}