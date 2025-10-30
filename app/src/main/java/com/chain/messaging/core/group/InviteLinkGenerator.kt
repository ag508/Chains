package com.chain.messaging.core.group

/**
 * Interface for generating and managing group invite links.
 */
interface InviteLinkGenerator {
    
    /**
     * Generates a new invite link for the specified group.
     */
    fun generateInviteLink(groupId: String): String
    
    /**
     * Validates an invite link and returns the group ID if valid.
     */
    fun validateInviteLink(inviteLink: String): String?
    
    /**
     * Revokes an existing invite link for the group.
     */
    fun revokeInviteLink(groupId: String)
}