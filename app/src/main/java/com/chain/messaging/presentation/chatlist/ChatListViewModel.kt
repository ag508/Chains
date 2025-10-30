package com.chain.messaging.presentation.chatlist

import androidx.lifecycle.viewModelScope
import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.usecase.ArchiveChatUseCase
import com.chain.messaging.domain.usecase.DeleteChatUseCase
import com.chain.messaging.domain.usecase.GetChatsUseCase
import com.chain.messaging.domain.usecase.PinChatUseCase
import com.chain.messaging.presentation.base.BaseViewModel
import com.chain.messaging.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Chat List screen with search, filtering, and sorting
 * Implements Requirements: 11.1, 11.3
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getChatsUseCase: GetChatsUseCase,
    private val archiveChatUseCase: ArchiveChatUseCase,
    private val deleteChatUseCase: DeleteChatUseCase,
    private val pinChatUseCase: PinChatUseCase
) : BaseViewModel<ChatListUiState>() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(ChatSortOption.RECENT)
    private val _activeFilters = MutableStateFlow<Set<ChatFilter>>(emptySet())
    
    override val initialState = ChatListUiState()
    
    init {
        loadChats()
        observeFiltersAndSearch()
    }
    
    private fun observeFiltersAndSearch() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _sortOption,
                _activeFilters,
                uiState
            ) { searchQuery, sortOption, activeFilters, state ->
                val filteredAndSortedChats = state.chats
                    .let { chats -> filterChats(chats, searchQuery, activeFilters) }
                    .let { chats -> sortChats(chats, sortOption) }
                
                state.copy(
                    searchQuery = searchQuery,
                    sortOption = sortOption,
                    activeFilters = activeFilters,
                    filteredChats = filteredAndSortedChats
                )
            }.collect { newState ->
                updateState { newState }
            }
        }
    }
    
    fun loadChats() {
        launchSafe {
            updateState { copy(isLoading = true, error = null) }
            
            getChatsUseCase.observeChats().collect { chats ->
                updateState { 
                    copy(
                        isLoading = false,
                        chats = chats,
                        error = null
                    )
                }
            }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    
    fun onSortChanged(sortOption: ChatSortOption) {
        _sortOption.value = sortOption
    }
    
    fun onFilterToggled(filter: ChatFilter) {
        val currentFilters = _activeFilters.value.toMutableSet()
        if (currentFilters.contains(filter)) {
            currentFilters.remove(filter)
        } else {
            currentFilters.add(filter)
        }
        _activeFilters.value = currentFilters
    }
    
    fun onArchiveChat(chatId: String) {
        launchSafe {
            // Find the current chat to determine its archive status
            val currentChat = uiState.value.chats.find { it.id == chatId }
            if (currentChat != null) {
                // Toggle archive status
                val newArchiveStatus = !currentChat.settings.isArchived
                
                archiveChatUseCase.execute(chatId, newArchiveStatus)
                    .onSuccess {
                        // Refresh the chat list to reflect the change
                        loadChats()
                    }
                    .onFailure { error ->
                        handleError(Exception("Failed to archive chat: ${error.message}"))
                    }
            }
        }
    }
    
    fun onDeleteChat(chatId: String) {
        launchSafe {
            deleteChatUseCase.execute(chatId)
                .onSuccess {
                    // Refresh the chat list to reflect the deletion
                    loadChats()
                }
                .onFailure { error ->
                    handleError(Exception("Failed to delete chat: ${error.message}"))
                }
        }
    }
    
    fun onPinChat(chatId: String) {
        launchSafe {
            // Find the current chat to determine its pin status
            val currentChat = uiState.value.chats.find { it.id == chatId }
            if (currentChat != null) {
                // Toggle pin status
                val newPinStatus = !currentChat.settings.isPinned
                
                pinChatUseCase.execute(chatId, newPinStatus)
                    .onSuccess {
                        // Refresh the chat list to reflect the change
                        loadChats()
                    }
                    .onFailure { error ->
                        handleError(Exception("Failed to pin chat: ${error.message}"))
                    }
            }
        }
    }
    
    fun onChatSelected(chatId: String) {
        // Navigation is handled by the UI layer through the onChatClick callback
        // This method can be used for additional logic when a chat is selected
        launchSafe {
            // Mark any unread messages as read when entering the chat
            val selectedChat = uiState.value.chats.find { it.id == chatId }
            if (selectedChat != null && selectedChat.unreadCount > 0) {
                // The actual read status update will be handled by the chat screen
                // This is just a placeholder for future read status management
                loadChats() // Refresh to get updated read status
            }
        }
    }
    
    fun onRefresh() {
        loadChats()
    }
    
    private fun filterChats(
        chats: List<Chat>,
        searchQuery: String,
        activeFilters: Set<ChatFilter>
    ): List<Chat> {
        var filteredChats = chats
        
        // Apply search filter
        if (searchQuery.isNotBlank()) {
            filteredChats = filteredChats.filter { chat ->
                chat.name.contains(searchQuery, ignoreCase = true) ||
                chat.lastMessage?.content?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        // Apply active filters
        activeFilters.forEach { filter ->
            filteredChats = when (filter) {
                ChatFilter.UNREAD -> filteredChats.filter { it.unreadCount > 0 }
                ChatFilter.GROUPS -> filteredChats.filter { it.type == com.chain.messaging.domain.model.ChatType.GROUP }
                ChatFilter.DIRECT -> filteredChats.filter { it.type == com.chain.messaging.domain.model.ChatType.DIRECT }
                ChatFilter.PINNED -> filteredChats.filter { it.settings.isPinned }
                ChatFilter.ARCHIVED -> filteredChats.filter { it.settings.isArchived }
            }
        }
        
        return filteredChats
    }
    
    private fun sortChats(chats: List<Chat>, sortOption: ChatSortOption): List<Chat> {
        // Always prioritize pinned chats at the top, then apply the selected sort option
        return when (sortOption) {
            ChatSortOption.RECENT -> chats.sortedWith(
                compareByDescending<Chat> { it.settings.isPinned }
                    .thenByDescending { it.lastMessage?.timestamp ?: it.updatedAt }
            )
            ChatSortOption.NAME -> chats.sortedWith(
                compareByDescending<Chat> { it.settings.isPinned }
                    .thenBy { it.name }
            )
            ChatSortOption.UNREAD -> chats.sortedWith(
                compareByDescending<Chat> { it.settings.isPinned }
                    .thenByDescending { it.unreadCount > 0 }
                    .thenByDescending { it.unreadCount }
                    .thenByDescending { it.lastMessage?.timestamp ?: it.updatedAt }
            )
        }
    }
    
    override fun handleError(error: Exception) {
        updateState { 
            copy(
                isLoading = false,
                error = error.message ?: "Unknown error occurred"
            )
        }
    }
}

/**
 * UI State for Chat List screen
 */
data class ChatListUiState(
    val isLoading: Boolean = false,
    val chats: List<Chat> = emptyList(),
    val filteredChats: List<Chat> = emptyList(),
    val searchQuery: String = "",
    val sortOption: ChatSortOption = ChatSortOption.RECENT,
    val activeFilters: Set<ChatFilter> = emptySet(),
    val availableFilters: List<ChatFilter> = ChatFilter.values().toList(),
    val error: String? = null
) : UiState

/**
 * Sort options for chat list
 */
enum class ChatSortOption(val displayName: String) {
    RECENT("Recent"),
    NAME("Name"),
    UNREAD("Unread")
}

/**
 * Filter options for chat list
 */
enum class ChatFilter(val displayName: String) {
    UNREAD("Unread"),
    GROUPS("Groups"),
    DIRECT("Direct"),
    PINNED("Pinned"),
    ARCHIVED("Archived")
}