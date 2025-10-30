package com.chain.messaging.presentation.media

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Media picker component for selecting images, videos, and documents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPicker(
    onMediaSelected: (Uri, MediaPickerType) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(true) }
    
    // Camera capture for photos
    val cameraImageUri = remember { 
        createImageUri(context)
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onMediaSelected(cameraImageUri, MediaPickerType.CAMERA_IMAGE)
        }
        showBottomSheet = false
        onDismiss()
    }
    
    // Gallery picker for images
    val galleryImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            onMediaSelected(it, MediaPickerType.GALLERY_IMAGE)
        }
        showBottomSheet = false
        onDismiss()
    }
    
    // Gallery picker for videos
    val galleryVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            onMediaSelected(it, MediaPickerType.GALLERY_VIDEO)
        }
        showBottomSheet = false
        onDismiss()
    }
    
    // Document picker
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            onMediaSelected(it, MediaPickerType.DOCUMENT)
        }
        showBottomSheet = false
        onDismiss()
    }
    
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                onDismiss()
            }
        ) {
            MediaPickerContent(
                onCameraClick = { 
                    cameraLauncher.launch(cameraImageUri)
                },
                onGalleryImageClick = { 
                    galleryImageLauncher.launch("image/*")
                },
                onGalleryVideoClick = { 
                    galleryVideoLauncher.launch("video/*")
                },
                onDocumentClick = { 
                    documentLauncher.launch("*/*")
                }
            )
        }
    }
}

@Composable
private fun MediaPickerContent(
    onCameraClick: () -> Unit,
    onGalleryImageClick: () -> Unit,
    onGalleryVideoClick: () -> Unit,
    onDocumentClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Media",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Camera option
        MediaPickerItem(
            icon = Icons.Default.CameraAlt,
            title = "Camera",
            subtitle = "Take a photo",
            onClick = onCameraClick
        )
        
        // Gallery Images
        MediaPickerItem(
            icon = Icons.Default.Image,
            title = "Gallery",
            subtitle = "Choose from photos",
            onClick = onGalleryImageClick
        )
        
        // Gallery Videos
        MediaPickerItem(
            icon = Icons.Default.VideoLibrary,
            title = "Video",
            subtitle = "Choose from videos",
            onClick = onGalleryVideoClick
        )
        
        // Documents
        MediaPickerItem(
            icon = Icons.Default.AttachFile,
            title = "Document",
            subtitle = "Choose a file",
            onClick = onDocumentClick
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MediaPickerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class MediaPickerType {
    CAMERA_IMAGE,
    GALLERY_IMAGE,
    GALLERY_VIDEO,
    DOCUMENT
}

private fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = File(context.cacheDir, "images")
    storageDir.mkdirs()
    
    val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
    
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}