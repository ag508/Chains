package com.chain.messaging.integration

import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatSettings
import com.chain.messaging.domain.model.ChatType
import com.chain.messaging.domain.repository.ChatRepository
import com.chain.messaging.domain.usecase.GetChatsUseCase
import com.chain.messaging.domain.usecase.PinChatUseCase
import com.chain.messaging.presentation.chatlist.ChatListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class ChatPinIntegrationTest {
    
    private lateinit var chatRepository: ChatRepository
    private lateinit var getChatsUseCase: GetChatsUseCase
    private lateinit var pinChatUseCase: PinChatUseCase
    private lateinit var viewModel: ChatListViewModel
    
    @Before
    fun setUp() {
        chatRepository = mockk()
        getChatsUseCase = mockk()
        pinChatUseCase = PinChatUseCase(chatRepository)
        
        // Mock other use cases that are required by ChatListViewModel
        val archiveChatUseCase = mockk<com.chain.messaging.domain.usecase.ArchiveChatUseCase>()
        val deleteChatUseCase = mockk<com.chain.messaging.domain.usecase.DeleteChatUseCase>()
        
        viewModel = ChatListViewModel(
            getChatsUseCase = getChatsUseCase,
            archiveChatUseCase = archiveChatUseCase,
            deleteChatUseCase = deleteChatUseCase,
            pinChatUseCase = pinChatUseCase
        )
    }
    
    @Test
    fun `pin chat integration test - should pin unpinned chat`() = runTest {
        // Given
        val chatId = "chat123"
        val unpinnedChat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            settings = ChatSettings(isPinned = false),
            createdAt = Date(),
            updatedAt = Date()
        )
        val pinnedChat = unpinnedChat.copy(
            settings = unpinnedChat.settings.copy(isPinned = true)
        )
        
        // Mock the repository calls
        coEvery { chatRepository.getChatById(chatId) } returns unpinnedChat
        coEvery { chatRepository.updateChatSettings(chatId, any()) } returns Result.success(Unit)
        coEvery { getChatsUseCase.observeChats() } returns flowOf(listOf(pinnedChat))
        
        // When
        viewModel.onPinChat(chatId)
        
        // Then
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { 
            chatRepository.updateChatSettings(
                chatId, 
                ChatSettings(isPinned = true)
            ) 
        }
    }
    
    @Test
    fun `pin chat integration test - should unpin pinned chat`() = runTest {
        // Given
        val chatId = "chat123"
        val pinnedChat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            settings = ChatSettings(isPinned = true),
            createdAt = Date(),
            updatedAt = Date()
        )
        val unpinnedChat = pinnedChat.copy(
            settings = pinnedChat.settings.copy(isPinned = false)
        )
        
        // Mock the repository calls
        coEvery { chatRepository.getChatById(chatId) } returns pinnedChat
        coEvery { chatRepository.updateChatSettings(chatId, any()) } returns Result.success(Unit)
        coEvery { getChatsUseCase.observeChats() } returns flowOf(listOf(unpinnedChat))
        
        // When
        viewModel.onPinChat(chatId)
        
        // Then
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { 
            chatRepository.updateChatSettings(
                chatId, 
                ChatSettings(isPinned = false)
            ) 
        }
    }
    
    @Test
    fun `pin chat integration test - should handle chat not found`() = runTest {
        // Given
        val chatId = "nonexistent"
        
        // Mock the repository calls
        coEvery { chatRepository.getChatById(chatId) } returns null
        coEvery { getChatsUseCase.observeChats() } returns flowOf(emptyList())
        
        // When
        viewModel.onPinChat(chatId)
        
        // Then
        coVerify { chatRepository.getChatById(chatId) }
        coVerify(exactly = 0) { chatRepository.updateChatSettings(any(), any()) }
    }
}