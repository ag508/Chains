package com.chain.messaging.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * Reusable component for displaying profile images with fallback to default icon
 */
@Composable
fun ProfileImageDisplay(
    imagePath: String?,
    size: androidx.compose.ui.unit.Dp = 100.dp,
    modifier: Modifier = Modifier,
    getImageFile: (String) -> File? = { path ->
        try {
            val file = File(path)
            if (file.exists()) file else null
        } catch (e: Exception) {
            null
        }
    }
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (imagePath != null) {
            val imageFile = getImageFile(imagePath)
            if (imageFile?.exists() == true) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback to default icon if image file doesn't exist
                DefaultProfileIcon(size = size)
            }
        } else {
            // Default icon when no avatar is set
            DefaultProfileIcon(size = size)
        }
    }
}

@Composable
private fun DefaultProfileIcon(size: androidx.compose.ui.unit.Dp) {
    Icon(
        Icons.Default.Person,
        contentDescription = "Default Profile Picture",
        modifier = Modifier.size(size * 0.6f),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Profile image with edit functionality
 */
@Composable
fun EditableProfileImage(
    imagePath: String?,
    onImageSelected: (android.net.Uri) -> Unit,
    isLoading: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 100.dp,
    modifier: Modifier = Modifier,
    getImageFile: (String) -> File? = { path ->
        try {
            val file = File(path)
            if (file.exists()) file else null
        } catch (e: Exception) {
            null
        }
    }
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileImageDisplay(
            imagePath = imagePath,
            size = size,
            getImageFile = getImageFile
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ImagePickerButton(
            onImageSelected = onImageSelected,
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Change Photo")
        }
    }
}