package com.chain.messaging.presentation.components

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * UI tests for ImagePicker component
 */
@RunWith(AndroidJUnit4::class)
class ImagePickerTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun imagePickerButton_displaysCorrectly() {
        // Given
        var selectedUri: Uri? = null
        
        // When
        composeTestRule.setContent {
            ImagePickerButton(
                onImageSelected = { uri -> selectedUri = uri }
            ) {
                androidx.compose.material3.Text("Pick Image")
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Pick Image").assertIsDisplayed()
    }
    
    @Test
    fun imagePickerButton_isClickable() {
        // Given
        var selectedUri: Uri? = null
        
        // When
        composeTestRule.setContent {
            ImagePickerButton(
                onImageSelected = { uri -> selectedUri = uri }
            ) {
                androidx.compose.material3.Text("Pick Image")
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Pick Image").assertHasClickAction()
    }
    
    @Test
    fun imagePickerButton_canBeDisabled() {
        // Given
        var selectedUri: Uri? = null
        
        // When
        composeTestRule.setContent {
            ImagePickerButton(
                onImageSelected = { uri -> selectedUri = uri },
                enabled = false
            ) {
                androidx.compose.material3.Text("Pick Image")
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Pick Image").assertIsNotEnabled()
    }
    
    @Test
    fun imagePicker_showsDialogWhenButtonClicked() {
        // Given
        var selectedUri: Uri? = null
        
        // When
        composeTestRule.setContent {
            ImagePickerButton(
                onImageSelected = { uri -> selectedUri = uri }
            ) {
                androidx.compose.material3.Text("Pick Image")
            }
        }
        
        // Click the button
        composeTestRule.onNodeWithText("Pick Image").performClick()
        
        // Then
        composeTestRule.onNodeWithText("Select Profile Photo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose from Gallery").assertIsDisplayed()
        composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }
    
    @Test
    fun imagePicker_dismissesWhenCancelClicked() {
        // Given
        var selectedUri: Uri? = null
        
        // When
        composeTestRule.setContent {
            ImagePickerButton(
                onImageSelected = { uri -> selectedUri = uri }
            ) {
                androidx.compose.material3.Text("Pick Image")
            }
        }
        
        // Click the button to show dialog
        composeTestRule.onNodeWithText("Pick Image").performClick()
        composeTestRule.onNodeWithText("Select Profile Photo").assertIsDisplayed()
        
        // Click cancel
        composeTestRule.onNodeWithText("Cancel").performClick()
        
        // Then
        composeTestRule.onNodeWithText("Select Profile Photo").assertDoesNotExist()
    }
}