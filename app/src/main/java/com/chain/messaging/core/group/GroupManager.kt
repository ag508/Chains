package com.chain.messaging.core.group

import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.GroupChat
import com.chain.messaging.domain.model.GroupPermissions
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing group chat operations including creation, member management,
 * and admin controls.
 */
interface GroupManager {
    
    /**
     * Creates a new group chat with the specified parameters.
     */
    suspend fun createGroup(
        name: String,
        description: String?,
        memberIds: List<String>,
        adminIds: List<String>,
        permissions: GroupPermissions = GroupPermissions()
    ): Result<GroupChat>
    
    /**
     * Updates group information (name, description, permissions).
     */
    suspend fun updateGroupInfo(
        groupId: String,
        name: String? = null,
        description: String? = null,
        permissions: GroupPermissions? = null
    ): Result<GroupChat>
    
    /**
     * Adds members to a group chat.
     */
    suspend fun addMembers(
        groupId: String,
        memberIds: List<String>,
        invitedBy: String
    ): Result<Unit>
    
    /**
     * Removes members from a group chat.
     */
    suspend fun removeMembers(
        groupId: String,
        memberIds: List<String>,
        removedBy: String
    ): Result<Unit>
    
    /**
     * Promotes members to admin status.
     */
    suspend fun promoteToAdmin(
        groupId: String,
        memberIds: List<String>,
        promotedBy: String
    ): Result<Unit>
    
    /**
     * Demotes admins to regular member status.
     */
    suspend fun demoteFromAdmin(
        groupId: String,
        adminIds: List<String>,
        demotedBy: String
    ): Result<Unit>
    
    /**
     * Leaves a group chat.
     */
    suspend fun leaveGroup(
        groupId: String,
        userId: String
    ): Result<Unit>
    
    /**
     * Generates an invite link for the group.
     */
    suspend fun generateInviteLink(
        groupId: String,
        generatedBy: String
    ): Result<String>
    
    /**
     * Revokes the current invite link and optionally generates a new one.
     */
    suspend fun revokeInviteLink(
        groupId: String,
        revokedBy: String,
        generateNew: Boolean = false
    ): Result<String?>
    
    /**
     * Joins a group using an invite link.
     */
    suspend fun joinGroupByInvite(
        inviteLink: String,
        userId: String
    ): Result<GroupChat>
    
    /**
     * Gets group information by ID.
     */
    suspend fun getGroup(groupId: String): Result<GroupChat?>
    
    /**
     * Observes group changes for real-time updates.
     */
    fun observeGroup(groupId: String): Flow<GroupChat?>
    
    /**
     * Validates if a user has permission to perform a specific action.
     */
    suspend fun hasPermission(
        groupId: String,
        userId: String,
        action: GroupAction
    ): Boolean
}

enum class GroupAction {
    ADD_MEMBERS,
    REMOVE_MEMBERS,
    EDIT_GROUP_INFO,
    PROMOTE_ADMIN,
    DEMOTE_ADMIN,
    GENERATE_INVITE_LINK,
    REVOKE_INVITE_LINK
}