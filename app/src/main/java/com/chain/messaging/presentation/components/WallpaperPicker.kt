package com.chain.messaging.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Wallpaper picker component that provides predefined wallpapers and custom image selection
 */
@Composable
fun WallpaperPicker(
    currentWallpaper: String?,
    onWallpaperSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Gallery picker launcher for custom wallpaper
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { 
            // Convert URI to string path for storage
            onWallpaperSelected(it.toString())
        }
        onDismiss()
    }
    
    // Predefined wallpaper options
    val predefinedWallpapers = listOf(
        WallpaperOption("default", "Default", null),
        WallpaperOption("solid_blue", "Solid Blue", Color(0xFF2196F3)),
        WallpaperOption("solid_green", "Solid Green", Color(0xFF4CAF50)),
        WallpaperOption("solid_purple", "Solid Purple", Color(0xFF9C27B0)),
        WallpaperOption("gradient_blue", "Blue Gradient", Color(0xFF1976D2)),
        WallpaperOption("gradient_sunset", "Sunset Gradient", Color(0xFFFF5722))
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choose Chat Wallpaper")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select a wallpaper for your chat background:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Predefined wallpapers
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(predefinedWallpapers) { wallpaper ->
                        WallpaperPreview(
                            wallpaper = wallpaper,
                            isSelected = currentWallpaper == wallpaper.id,
                            onClick = { 
                                onWallpaperSelected(if (wallpaper.id == "default") null else wallpaper.id)
                                onDismiss()
                            }
                        )
                    }
                }
                
                Divider()
                
                // Custom wallpaper option
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Custom Image")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Preview component for wallpaper options
 */
@Composable
private fun WallpaperPreview(
    wallpaper: WallpaperOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        when {
            wallpaper.id == "default" -> {
                // Default wallpaper (no background)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Default",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            wallpaper.color != null -> {
                // Solid color or gradient wallpaper
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            when (wallpaper.id) {
                                "gradient_blue" -> androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
                                )
                                "gradient_sunset" -> androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFF5722), Color(0xFFFFAB40))
                                )
                                else -> androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(wallpaper.color, wallpaper.color)
                                )
                            }
                        )
                )
            }
        }
        
        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Simple wallpaper picker button that shows the picker dialog when clicked
 */
@Composable
fun WallpaperPickerButton(
    currentWallpaper: String?,
    onWallpaperSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    
    Button(
        onClick = { showPicker = true },
        modifier = modifier,
        enabled = enabled
    ) {
        content()
    }
    
    if (showPicker) {
        WallpaperPicker(
            currentWallpaper = currentWallpaper,
            onWallpaperSelected = onWallpaperSelected,
            onDismiss = { showPicker = false }
        )
    }
}

/**
 * Data class for wallpaper options
 */
private data class WallpaperOption(
    val id: String,
    val name: String,
    val color: Color?
)