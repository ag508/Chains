package com.chain.messaging.presentation.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chain.messaging.domain.model.MediaMessage
import com.chain.messaging.domain.model.MediaType

/**
 * Full-screen media viewer for images and videos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewer(
    mediaMessages: List<MediaMessage>,
    initialIndex: Int = 0,
    onBackClick: () -> Unit,
    onShareClick: (MediaMessage) -> Unit = {}
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { mediaMessages.size }
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Media content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val mediaMessage = mediaMessages[page]
            val mediaType = MediaType.fromMimeType(mediaMessage.mimeType)
            
            when (mediaType) {
                MediaType.IMAGE -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mediaMessage.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Full screen image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                MediaType.VIDEO -> {
                    // For now, show a placeholder for video
                    // In a full implementation, you'd use ExoPlayer
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share, // Using share as placeholder
                                contentDescription = "Video player placeholder",
                                modifier = Modifier.size(64.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Video Player",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Tap to play ${mediaMessage.fileName}",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                else -> {
                    // For other media types, show info
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Media type not supported in viewer",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        // Top bar
        TopAppBar(
            title = {
                if (mediaMessages.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} of ${mediaMessages.size}",
                        color = Color.White
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { 
                        onShareClick(mediaMessages[pagerState.currentPage])
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        // Bottom info bar
        if (mediaMessages.isNotEmpty()) {
            val currentMedia = mediaMessages[pagerState.currentPage]
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = currentMedia.fileName,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${formatFileSize(currentMedia.fileSize)} • ${currentMedia.mimeType}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
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