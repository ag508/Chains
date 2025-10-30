package com.chain.messaging.presentation.components

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

/**
 * Data class representing a notification sound option
 */
data class NotificationSound(
    val id: String,
    val name: String,
    val uri: Uri?
)

/**
 * Notification sound picker dialog component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSoundPicker(
    currentSound: String,
    onSoundSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var sounds by remember { mutableStateOf<List<NotificationSound>>(emptyList()) }
    var selectedSound by remember { mutableStateOf(currentSound) }
    var playingSound by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    
    // Load available notification sounds
    LaunchedEffect(Unit) {
        sounds = loadNotificationSounds(context)
    }
    
    // Cleanup media player when dialog is dismissed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose Notification Sound",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sound list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sounds) { sound ->
                        NotificationSoundItem(
                            sound = sound,
                            isSelected = selectedSound == sound.id,
                            isPlaying = playingSound == sound.id,
                            onSelect = { selectedSound = sound.id },
                            onPlay = { 
                                if (playingSound == sound.id) {
                                    // Stop playing
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    playingSound = null
                                } else {
                                    // Stop any currently playing sound
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    
                                    // Play new sound
                                    try {
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(context, sound.uri ?: getDefaultNotificationUri(context))
                                            prepare()
                                            start()
                                            setOnCompletionListener {
                                                playingSound = null
                                                release()
                                                mediaPlayer = null
                                            }
                                        }
                                        playingSound = sound.id
                                    } catch (e: Exception) {
                                        playingSound = null
                                    }
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSoundSelected(selectedSound)
                            onDismiss()
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

/**
 * Individual sound item in the picker
 */
@Composable
private fun NotificationSoundItem(
    sound: NotificationSound,
    isSelected: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            IconButton(onClick = onPlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play"
                )
            }
        }
    }
}

/**
 * Load available notification sounds from the system
 */
private fun loadNotificationSounds(context: Context): List<NotificationSound> {
    val sounds = mutableListOf<NotificationSound>()
    
    // Add default/silent options
    sounds.add(NotificationSound("default", "Default", getDefaultNotificationUri(context)))
    sounds.add(NotificationSound("silent", "Silent", null))
    
    try {
        // Get system notification sounds
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION)
        val cursor = ringtoneManager.cursor
        
        while (cursor.moveToNext()) {
            val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = ringtoneManager.getRingtoneUri(cursor.position)
            
            sounds.add(NotificationSound(id, title, uri))
        }
        cursor.close()
    } catch (e: Exception) {
        // Fallback to basic sounds if system access fails
        sounds.addAll(
            listOf(
                NotificationSound("notification1", "Notification 1", getDefaultNotificationUri(context)),
                NotificationSound("notification2", "Notification 2", getDefaultNotificationUri(context)),
                NotificationSound("notification3", "Notification 3", getDefaultNotificationUri(context))
            )
        )
    }
    
    return sounds
}

/**
 * Get the default notification URI
 */
private fun getDefaultNotificationUri(context: Context): Uri {
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
}