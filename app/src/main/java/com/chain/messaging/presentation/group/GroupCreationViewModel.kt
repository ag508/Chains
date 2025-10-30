package com.chain.messaging.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.group.GroupManager
import com.chain.messaging.domain.model.GroupPermissions
import com.chain.messaging.domain.model.isOnline
import com.chain.messaging.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupCreationViewModel @Inject constructor(
    private val groupManager: GroupManager,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GroupCreationUiState())
    val uiState: StateFlow<GroupCreationUiState> = _uiState.asStateFlow()
    
    init {
        loadContacts()
    }
    
    fun updateGroupName(name: String) {
        _uiState.value = _uiState.value.copy(
            groupName = name,
            groupNameError = if (name.isBlank()) "Group name is required" else null
        )
    }
    
    fun updateGroupDescription(description: String) {
        _uiState.value = _uiState.value.copy(groupDescription = description)
    }
    
    fun addMember(memberId: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedMembers = currentState.selectedMembers + memberId
        )
    }
    
    fun removeMember(memberId: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedMembers = currentState.selectedMembers - memberId
        )
    }
    
    fun createGroup() {
        val currentState = _uiState.value
        
        if (currentState.groupName.isBlank()) {
            _uiState.value = currentState.copy(
                groupNameError = "Group name is required"
            )
            return
        }
        
        if (currentState.selectedMembers.isEmpty()) {
            _uiState.value = currentState.copy(
                error = "Please select at least one member"
            )
            return
        }
        
        _uiState.value = currentState.copy(
            isCreatingGroup = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                // Add current user as admin
                val currentUserId = getCurrentUserId()
                val memberIds = (currentState.selectedMembers + currentUserId).toList()
                val adminIds = listOf(currentUserId)
                
                val result = groupManager.createGroup(
                    name = currentState.groupName,
                    description = currentState.groupDescription.takeIf { it.isNotBlank() },
                    memberIds = memberIds,
                    adminIds = adminIds,
                    permissions = GroupPermissions()
                )
                
                result.fold(
                    onSuccess = { groupChat ->
                        _uiState.value = currentState.copy(
                            isCreatingGroup = false,
                            createdGroupId = groupChat.chat.id
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = currentState.copy(
                            isCreatingGroup = false,
                            error = error.message ?: "Failed to create group"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isCreatingGroup = false,
                    error = e.message ?: "Failed to create group"
                )
            }
        }
    }
    
    private fun loadContacts() {
        _uiState.value = _uiState.value.copy(isLoadingContacts = true)
        
        viewModelScope.launch {
            try {
                val users = userRepository.getAllUsers()
                val currentUserId = getCurrentUserId()
                
                val contacts = users
                    .filter { it.id != currentUserId }
                    .map { user ->
                        ContactInfo(
                            id = user.id,
                            name = user.displayName,
                            status = if (user.isOnline) "Online" else "Last seen recently"
                        )
                    }
                
                _uiState.value = _uiState.value.copy(
                    isLoadingContacts = false,
                    availableContacts = contacts
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingContacts = false,
                    error = "Failed to load contacts"
                )
            }
        }
    }
    
    private suspend fun getCurrentUserId(): String {
        // This should get the current authenticated user ID
        // For now, returning a placeholder
        return "current_user_id"
    }
}

data class GroupCreationUiState(
    val groupName: String = "",
    val groupDescription: String = "",
    val groupNameError: String? = null,
    val selectedMembers: Set<String> = emptySet(),
    val availableContacts: List<ContactInfo> = emptyList(),
    val isLoadingContacts: Boolean = false,
    val isCreatingGroup: Boolean = false,
    val error: String? = null,
    val createdGroupId: String? = null
) {
    val canCreateGroup: Boolean
        get() = groupName.isNotBlank() && selectedMembers.isNotEmpty() && !isCreatingGroup
}

data class ContactInfo(
    val id: String,
    val name: String,
    val status: String
)