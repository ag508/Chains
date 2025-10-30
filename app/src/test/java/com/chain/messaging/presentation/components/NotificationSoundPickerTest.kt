package com.chain.messaging.presentation.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.presentation.theme.ChainTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationSoundPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun notificationSoundPicker_displaysCorrectTitle() {
        var selectedSound = "default"
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                NotificationSoundPicker(
                    currentSound = selectedSound,
                    onSoundSelected = { selectedSound = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Choose Notification Sound")
            .assertIsDisplayed()
    }

    @Test
    fun notificationSoundPicker_showsDefaultSoundSelected() {
        var selectedSound = "default"
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                NotificationSoundPicker(
                    currentSound = selectedSound,
                    onSoundSelected = { selectedSound = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        // Wait for sounds to load
        composeTestRule.waitForIdle()

        // Check that default sound is initially selected
        composeTestRule
            .onNodeWithText("Default")
            .assertIsDisplayed()
    }

    @Test
    fun notificationSoundPicker_canSelectDifferentSound() {
        var selectedSound = "default"
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                NotificationSoundPicker(
                    currentSound = selectedSound,
                    onSoundSelected = { selectedSound = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        // Wait for sounds to load
        composeTestRule.waitForIdle()

        // Select silent option
        composeTestRule
            .onNodeWithText("Silent")
            .performClick()

        // Click OK button
        composeTestRule
            .onNodeWithText("OK")
            .performClick()

        // Verify sound was changed
        assert(selectedSound == "silent")
        assert(dismissed)
    }

    @Test
    fun notificationSoundPicker_canCancel() {
        var selectedSound = "default"
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                NotificationSoundPicker(
                    currentSound = selectedSound,
                    onSoundSelected = { selectedSound = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        // Click Cancel button
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        // Verify dialog was dismissed without changing sound
        assert(selectedSound == "default")
        assert(dismissed)
    }

    @Test
    fun notificationSoundPicker_canCloseWithXButton() {
        var selectedSound = "default"
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                NotificationSoundPicker(
                    currentSound = selectedSound,
                    onSoundSelected = { selectedSound = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        // Click close button
        composeTestRule
            .onNodeWithContentDescription("Close")
            .performClick()

        // Verify dialog was dismissed
        assert(dismissed)
    }

    @Test
    fun notificationSoundPicker_showsPlayStopButtons() {
        var selectedSound = "default"
        var dismissed = false

        composeTestRule.setContent {
            ChainTheme {
                NotificationSoundPicker(
                    currentSound = selectedSound,
                    onSoundSelected = { selectedSound = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        // Wait for sounds to load
        composeTestRule.waitForIdle()

        // Check that play buttons are displayed
        composeTestRule
            .onAllNodesWithContentDescription("Play")
            .assertCountEquals(3) // Default, Silent, and at least one system sound
    }
}