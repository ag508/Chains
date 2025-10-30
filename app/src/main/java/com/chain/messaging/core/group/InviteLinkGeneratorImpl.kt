package com.chain.messaging.core.group

import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of InviteLinkGenerator that manages group invite links.
 */
@Singleton
class InviteLinkGeneratorImpl @Inject constructor() : InviteLinkGenerator {
    
    private val activeLinks = ConcurrentHashMap<String, String>() // link -> groupId
    private val groupLinks = ConcurrentHashMap<String, String>() // groupId -> link
    private val secureRandom = SecureRandom()
    
    companion object {
        private const val LINK_PREFIX = "https://chain.app/invite/"
        private const val LINK_LENGTH = 32
        private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }
    
    override fun generateInviteLink(groupId: String): String {
        // Revoke existing link if any
        revokeInviteLink(groupId)
        
        // Generate new random token
        val token = generateRandomToken()
        val inviteLink = LINK_PREFIX + token
        
        // Store the mapping
        activeLinks[inviteLink] = groupId
        groupLinks[groupId] = inviteLink
        
        return inviteLink
    }
    
    override fun validateInviteLink(inviteLink: String): String? {
        return activeLinks[inviteLink]
    }
    
    override fun revokeInviteLink(groupId: String) {
        val existingLink = groupLinks[groupId]
        if (existingLink != null) {
            activeLinks.remove(existingLink)
            groupLinks.remove(groupId)
        }
    }
    
    private fun generateRandomToken(): String {
        val token = StringBuilder(LINK_LENGTH)
        repeat(LINK_LENGTH) {
            token.append(CHARACTERS[secureRandom.nextInt(CHARACTERS.length)])
        }
        return token.toString()
    }
}