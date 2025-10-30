package com.chain.messaging.presentation.media

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chain.messaging.domain.model.MediaMessage
import com.chain.messaging.domain.model.MediaType

/**
 * Component for previewing media content in messages
 */
@Composable
fun MediaPreview(
    mediaMessage: MediaMessage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val mediaType = MediaType.fromMimeType(mediaMessage.mimeType)
    
    Card(
        modifier = modifier
            .clickable { onClick() }
            .widthIn(max = 250.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        when (mediaType) {
            MediaType.IMAGE -> ImagePreview(
                mediaMessage = mediaMessage,
                onClick = onClick
            )
            MediaType.VIDEO -> VideoPreview(
                mediaMessage = mediaMessage,
                onClick = onClick
            )
            MediaType.AUDIO -> AudioPreview(
                mediaMessage = mediaMessage,
                onClick = onClick
            )
            MediaType.DOCUMENT -> DocumentPreview(
                mediaMessage = mediaMessage,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun ImagePreview(
    mediaMessage: MediaMessage,
    onClick: () -> Unit
) {
    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(mediaMessage.uri)
                .crossfade(true)
                .build(),
            contentDescription = "Image message",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        
        // File size overlay
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = formatFileSize(mediaMessage.fileSize),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun VideoPreview(
    mediaMessage: MediaMessage,
    onClick: () -> Unit
) {
    Box {
        // Use thumbnail if available, otherwise show video icon
        if (mediaMessage.thumbnailUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mediaMessage.thumbnailUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Video thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = "Video",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Play button overlay
        Surface(
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(50)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play video",
                modifier = Modifier
                    .size(48.dp)
                    .padding(12.dp),
                tint = Color.White
            )
        }
        
        // Duration and file size overlay
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "${formatDuration(mediaMessage.duration)} • ${formatFileSize(mediaMessage.fileSize)}",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun AudioPreview(
    mediaMessage: MediaMessage,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(50)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play audio",
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Voice Message",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatDuration(mediaMessage.duration)} • ${formatFileSize(mediaMessage.fileSize)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.AudioFile,
            contentDescription = "Audio file",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DocumentPreview(
    mediaMessage: MediaMessage,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Document",
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mediaMessage.fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatFileSize(mediaMessage.fileSize),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Download",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> "%.1f GB".format(gb)
        mb >= 1 -> "%.1f MB".format(mb)
        kb >= 1 -> "%.1f KB".format(kb)
        else -> "$bytes B"
    }
}

private fun formatDuration(durationMs: Long?): String {
    if (durationMs == null) return "0:00"
    
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))
    
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}