package com.chain.messaging.data.local.storage

import com.chain.messaging.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Helper class for managing message pagination
 */
class MessagePagination @Inject constructor(
    private val messageStorageService: MessageStorageService
) {
    
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
    
    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()
    
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load messages with pagination
     */
    suspend fun loadMessages(
        chatId: String,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Result<List<Message>> {
        _loadingState.value = true
        _error.value = null
        
        val result = messageStorageService.getMessages(
            chatId = chatId,
            limit = minOf(pageSize, MAX_PAGE_SIZE),
            offset = offset
        )
        
        result.fold(
            onSuccess = { messages ->
                _hasMorePages.value = messages.size == pageSize
                _loadingState.value = false
            },
            onFailure = { exception ->
                _error.value = exception.message
                _loadingState.value = false
            }
        )
        
        return result
    }
    
    /**
     * Load next page of messages
     */
    suspend fun loadNextPage(
        chatId: String,
        currentMessages: List<Message>,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): Result<List<Message>> {
        if (!_hasMorePages.value || _loadingState.value) {
            return Result.success(emptyList())
        }
        
        return loadMessages(chatId, pageSize, currentMessages.size)
    }
    
    /**
     * Reset pagination state
     */
    fun reset() {
        _loadingState.value = false
        _hasMorePages.value = true
        _error.value = null
    }
    
    /**
     * Create a paginated message loader
     */
    fun createPaginatedLoader(
        chatId: String,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): PaginatedMessageLoader {
        return PaginatedMessageLoader(chatId, pageSize, this)
    }
}

/**
 * Paginated message loader for a specific chat
 */
class PaginatedMessageLoader(
    private val chatId: String,
    private val pageSize: Int,
    private val pagination: MessagePagination
) {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private var currentOffset = 0
    
    /**
     * Load initial messages
     */
    suspend fun loadInitial(): Result<Unit> {
        pagination.reset()
        currentOffset = 0
        
        val result = pagination.loadMessages(chatId, pageSize, currentOffset)
        result.fold(
            onSuccess = { newMessages ->
                _messages.value = newMessages
                currentOffset = newMessages.size
            },
            onFailure = { /* Error handled by pagination */ }
        )
        
        return result.map { }
    }
    
    /**
     * Load more messages (next page)
     */
    suspend fun loadMore(): Result<Unit> {
        if (!pagination.hasMorePages.value || pagination.loadingState.value) {
            return Result.success(Unit)
        }
        
        val result = pagination.loadMessages(chatId, pageSize, currentOffset)
        result.fold(
            onSuccess = { newMessages ->
                if (newMessages.isNotEmpty()) {
                    _messages.value = _messages.value + newMessages
                    currentOffset += newMessages.size
                }
            },
            onFailure = { /* Error handled by pagination */ }
        )
        
        return result.map { }
    }
    
    /**
     * Refresh messages (reload from beginning)
     */
    suspend fun refresh(): Result<Unit> {
        return loadInitial()
    }
    
    /**
     * Add new message to the beginning of the list
     */
    fun addMessage(message: Message) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(0, message)
        _messages.value = currentMessages
        currentOffset++
    }
    
    /**
     * Update existing message
     */
    fun updateMessage(updatedMessage: Message) {
        val currentMessages = _messages.value.toMutableList()
        val index = currentMessages.indexOfFirst { it.id == updatedMessage.id }
        if (index != -1) {
            currentMessages[index] = updatedMessage
            _messages.value = currentMessages
        }
    }
    
    /**
     * Remove message
     */
    fun removeMessage(messageId: String) {
        val currentMessages = _messages.value.toMutableList()
        val removed = currentMessages.removeAll { it.id == messageId }
        if (removed) {
            _messages.value = currentMessages
            currentOffset--
        }
    }
    
    /**
     * Get loading state
     */
    val isLoading: StateFlow<Boolean> = pagination.loadingState
    
    /**
     * Get has more pages state
     */
    val hasMorePages: StateFlow<Boolean> = pagination.hasMorePages
    
    /**
     * Get error state
     */
    val error: StateFlow<String?> = pagination.error
}