package com.chain.messaging.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.group.GroupManager
import com.chain.messaging.domain.model.isOnline
import com.chain.messaging.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupSettingsViewModel @Inject constructor(
    private val groupManager: GroupManager,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GroupSettingsUiState())
    val uiState: StateFlow<GroupSettingsUiState> = _uiState.asStateFlow()
    
    private var currentGroupId: String? = null
    
    fun loadGroup(groupId: String) {
        currentGroupId = groupId
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val result = groupManager.getGroup(groupId)
                result.fold(
                    onSuccess = { groupChat ->
                        if (groupChat != null) {
                            val currentUserId = getCurrentUserId()
                            val members = loadMembers(groupChat.chat.participants, groupChat.chat.admins, currentUserId)
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                groupName = groupChat.chat.name,
                                groupDescription = groupChat.description,
                                members = members,
                                isCurrentUserAdmin = groupChat.chat.admins.contains(currentUserId),
                                canEditGroupInfo = groupChat.chat.admins.contains(currentUserId)
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Group not found"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load group"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load group"
                )
            }
        }
    }
    
    fun showEditInfoDialog() {
        _uiState.value = _uiState.value.copy(showEditInfoDialog = true)
    }
    
    fun hideEditInfoDialog() {
        _uiState.value = _uiState.value.copy(showEditInfoDialog = false)
    }
    
    fun updateGroupInfo(name: String, description: String) {
        val groupId = currentGroupId ?: return
        
        viewModelScope.launch {
            try {
                val result = groupManager.updateGroupInfo(
                    groupId = groupId,
                    name = name,
                    description = description.takeIf { it.isNotBlank() }
                )
                
                result.fold(
                    onSuccess = { groupChat ->
                        _uiState.value = _uiState.value.copy(
                            groupName = groupChat.chat.name,
                            groupDescription = groupChat.description,
                            showEditInfoDialog = false
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to update group info"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update group info"
                )
            }
        }
    }
    
    fun promoteToAdmin(memberId: String) {
        val groupId = currentGroupId ?: return
        
        viewModelScope.launch {
            try {
                val result = groupManager.promoteToAdmin(
                    groupId = groupId,
                    memberIds = listOf(memberId),
                    promotedBy = getCurrentUserId()
                )
                
                result.fold(
                    onSuccess = {
                        loadGroup(groupId) // Refresh the group data
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to promote member"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to promote member"
                )
            }
        }
    }
    
    fun demoteFromAdmin(memberId: String) {
        val groupId = currentGroupId ?: return
        
        viewModelScope.launch {
            try {
                val result = groupManager.demoteFromAdmin(
                    groupId = groupId,
                    adminIds = listOf(memberId),
                    demotedBy = getCurrentUserId()
                )
                
                result.fold(
                    onSuccess = {
                        loadGroup(groupId) // Refresh the group data
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to demote admin"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to demote admin"
                )
            }
        }
    }
    
    fun removeMember(memberId: String) {
        val groupId = currentGroupId ?: return
        
        viewModelScope.launch {
            try {
                val result = groupManager.removeMembers(
                    groupId = groupId,
                    memberIds = listOf(memberId),
                    removedBy = getCurrentUserId()
                )
                
                result.fold(
                    onSuccess = {
                        loadGroup(groupId) // Refresh the group data
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to remove member"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to remove member"
                )
            }
        }
    }
    
    fun showAddMembersDialog() {
        _uiState.value = _uiState.value.copy(showAddMembersDialog = true)
    }
    
    fun hideAddMembersDialog() {
        _uiState.value = _uiState.value.copy(showAddMembersDialog = false)
    }
    
    fun showInviteLinkDialog() {
        _uiState.value = _uiState.value.copy(showInviteLinkDialog = true)
    }
    
    fun hideInviteLinkDialog() {
        _uiState.value = _uiState.value.copy(showInviteLinkDialog = false)
    }
    
    fun generateInviteLink() {
        val groupId = currentGroupId ?: return
        
        viewModelScope.launch {
            try {
                val result = groupManager.generateInviteLink(
                    groupId = groupId,
                    generatedBy = getCurrentUserId()
                )
                
                result.fold(
                    onSuccess = { inviteLink ->
                        _uiState.value = _uiState.value.copy(
                            inviteLink = inviteLink
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to generate invite link"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to generate invite link"
                )
            }
        }
    }
    
    fun revokeInviteLink() {
        val groupId = currentGroupId ?: return
        
        viewModelScope.launch {
            try {
                val result = groupManager.revokeInviteLink(
                    groupId = groupId,
                    revokedBy = getCurrentUserId(),
                    generateNew = false
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            inviteLink = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to revoke invite link"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to revoke invite link"
                )
            }
        }
    }
    
    fun leaveGroup() {
        val groupId = currentGroupId ?: return
        
        viewModelScope.launch {
            try {
                val result = groupManager.leaveGroup(
                    groupId = groupId,
                    userId = getCurrentUserId()
                )
                
                result.fold(
                    onSuccess = {
                        // Navigate back or show success message
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to leave group"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to leave group"
                )
            }
        }
    }
    
    private suspend fun loadMembers(
        participantIds: List<String>,
        adminIds: List<String>,
        currentUserId: String
    ): List<GroupMember> {
        return try {
            val users = userRepository.getUsersByIds(participantIds)
            users.map { user ->
                GroupMember(
                    id = user.id,
                    name = user.displayName,
                    status = if (user.isOnline) "Online" else "Last seen recently",
                    isAdmin = adminIds.contains(user.id),
                    isCurrentUser = user.id == currentUserId
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun getCurrentUserId(): String {
        // This should get the current authenticated user ID
        // For now, returning a placeholder
        return "current_user_id"
    }
}

data class GroupSettingsUiState(
    val isLoading: Boolean = false,
    val groupName: String = "",
    val groupDescription: String? = null,
    val members: List<GroupMember> = emptyList(),
    val isCurrentUserAdmin: Boolean = false,
    val canEditGroupInfo: Boolean = false,
    val inviteLink: String? = null,
    val showEditInfoDialog: Boolean = false,
    val showAddMembersDialog: Boolean = false,
    val showInviteLinkDialog: Boolean = false,
    val error: String? = null
)

data class GroupMember(
    val id: String,
    val name: String,
    val status: String,
    val isAdmin: Boolean,
    val isCurrentUser: Boolean
)