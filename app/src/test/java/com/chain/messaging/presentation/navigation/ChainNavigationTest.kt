package com.chain.messaging.presentation.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.presentation.theme.ChainTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for Chain navigation and bottom navigation bar
 * Tests Requirements: 11.1, 11.3
 */
@RunWith(AndroidJUnit4::class)
class ChainNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavigation_displaysAllTabs() {
        composeTestRule.setContent {
            ChainTheme {
                ChainNavigation(navController = rememberNavController())
            }
        }

        // Verify all bottom navigation items are displayed
        composeTestRule.onNodeWithText("Chats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calls").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_chatsTabSelectedByDefault() {
        composeTestRule.setContent {
            ChainTheme {
                ChainNavigation(navController = rememberNavController())
            }
        }

        // Verify Chats tab is selected by default
        composeTestRule.onNodeWithText("Chats").assertIsSelected()
        composeTestRule.onNodeWithText("Calls").assertIsNotSelected()
        composeTestRule.onNodeWithText("Settings").assertIsNotSelected()
    }

    @Test
    fun bottomNavigation_switchBetweenTabs() {
        composeTestRule.setContent {
            ChainTheme {
                ChainNavigation(navController = rememberNavController())
            }
        }

        // Click on Calls tab
        composeTestRule.onNodeWithText("Calls").performClick()
        composeTestRule.onNodeWithText("Calls").assertIsSelected()
        composeTestRule.onNodeWithText("Chats").assertIsNotSelected()

        // Click on Settings tab
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsSelected()
        composeTestRule.onNodeWithText("Calls").assertIsNotSelected()

        // Click back on Chats tab
        composeTestRule.onNodeWithText("Chats").performClick()
        composeTestRule.onNodeWithText("Chats").assertIsSelected()
        composeTestRule.onNodeWithText("Settings").assertIsNotSelected()
    }

    @Test
    fun navigation_showsCorrectScreenContent() {
        composeTestRule.setContent {
            ChainTheme {
                ChainNavigation(navController = rememberNavController())
            }
        }

        // Default screen should show chat list (Chain title)
        composeTestRule.onNodeWithText("Chain").assertIsDisplayed()

        // Navigate to Calls
        composeTestRule.onNodeWithText("Calls").performClick()
        composeTestRule.onNodeWithText("Calls - Coming Soon").assertIsDisplayed()

        // Navigate to Settings
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Settings - Coming Soon").assertIsDisplayed()
    }
}