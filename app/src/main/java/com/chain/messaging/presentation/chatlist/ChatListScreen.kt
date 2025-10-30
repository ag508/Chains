package com.chain.messaging.presentation.chatlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatType
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chat List Screen showing all conversations with previews
 * Implements Requirements: 11.1, 11.3 - main navigation and chat list with search, filtering, sorting, and swipe actions
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    onChatClick: (String) -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar with Search and Sort
        TopAppBar(
            title = {
                if (!showSearchBar) {
                    Text(
                        text = "Chain",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            actions = {
                if (!showSearchBar) {
                    IconButton(onClick = { showSearchBar = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search chats"
                        )
                    }
                    
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort chats"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recent") },
                                onClick = {
                                    viewModel.onSortChanged(ChatSortOption.RECENT)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Schedule, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Name") },
                                onClick = {
                                    viewModel.onSortChanged(ChatSortOption.NAME)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.SortByAlpha, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Unread") },
                                onClick = {
                                    viewModel.onSortChanged(ChatSortOption.UNREAD)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Circle, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // Search Bar
        AnimatedVisibility(visible = showSearchBar) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Search chats...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    } else {
                        IconButton(onClick = { showSearchBar = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
        }
        
        // Filter Chips
        if (uiState.availableFilters.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.availableFilters) { filter ->
                    FilterChip(
                        onClick = { viewModel.onFilterToggled(filter) },
                        label = { Text(filter.displayName) },
                        selected = uiState.activeFilters.contains(filter),
                        leadingIcon = if (uiState.activeFilters.contains(filter)) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
        }
        
        // Chat List Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { viewModel.onRefresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            uiState.chats.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No conversations yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = uiState.filteredChats,
                        key = { chat -> chat.id }
                    ) { chat ->
                        SwipeableChatListItem(
                            chat = chat,
                            onClick = { 
                                viewModel.onChatSelected(chat.id)
                                onChatClick(chat.id) 
                            },
                            onArchive = { viewModel.onArchiveChat(chat.id) },
                            onDelete = { viewModel.onDeleteChat(chat.id) },
                            onPin = { viewModel.onPinChat(chat.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeableChatListItem(
    chat: Chat,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pinAction = SwipeAction(
        icon = {
            Icon(
                imageVector = Icons.Outlined.PushPin,
                contentDescription = if (chat.settings.isPinned) "Unpin" else "Pin",
                tint = Color.White
            )
        },
        background = MaterialTheme.colorScheme.primary,
        onSwipe = onPin
    )
    
    val archiveAction = SwipeAction(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Archive,
                contentDescription = "Archive",
                tint = Color.White
            )
        },
        background = MaterialTheme.colorScheme.secondary,
        onSwipe = onArchive
    )
    
    val deleteAction = SwipeAction(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete",
                tint = Color.White
            )
        },
        background = MaterialTheme.colorScheme.error,
        onSwipe = onDelete
    )
    
    SwipeableActionsBox(
        startActions = listOf(pinAction),
        endActions = listOf(archiveAction, deleteAction),
        modifier = modifier
    ) {
        ChatListItem(
            chat = chat,
            onClick = onClick
        )
    }
}

@Composable
private fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chat.settings.isPinned) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pinned indicator
            if (chat.settings.isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Chat Avatar
            ChatAvatar(
                chatType = chat.type,
                chatName = chat.name
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Chat Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    chat.lastMessage?.let { lastMessage ->
                        Text(
                            text = formatTimestamp(lastMessage.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Last Message Preview
                    chat.lastMessage?.let { lastMessage ->
                        Text(
                            text = getMessagePreview(lastMessage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (chat.unreadCount > 0) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } ?: run {
                        Text(
                            text = "No messages yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Unread Count Badge
                    if (chat.unreadCount > 0) {
                        Badge(
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatAvatar(
    chatType: ChatType,
    chatName: String
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        when (chatType) {
            ChatType.DIRECT -> {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Direct Chat",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            ChatType.GROUP -> {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Group Chat",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun getMessagePreview(message: com.chain.messaging.domain.model.Message): String {
    return when (message.type) {
        MessageType.TEXT -> message.content
        MessageType.IMAGE -> "📷 Image"
        MessageType.VIDEO -> "🎥 Video"
        MessageType.AUDIO -> "🎵 Audio"
        MessageType.DOCUMENT -> "📄 Document"
        MessageType.LOCATION -> "📍 Location"
        MessageType.CONTACT -> "👤 Contact"
        MessageType.POLL -> "📊 Poll"
        MessageType.SYSTEM -> message.content
    }
}

private fun formatTimestamp(timestamp: Date): String {
    val now = Date()
    val diff = now.time - timestamp.time
    
    return when {
        diff < 60 * 1000 -> "now" // Less than 1 minute
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m" // Less than 1 hour
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h" // Less than 1 day
        diff < 7 * 24 * 60 * 60 * 1000 -> { // Less than 1 week
            SimpleDateFormat("EEE", Locale.getDefault()).format(timestamp)
        }
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(timestamp)
    }
}