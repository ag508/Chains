package com.chain.messaging.core.group

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

class InviteLinkGeneratorTest {
    
    private lateinit var inviteLinkGenerator: InviteLinkGenerator
    
    @BeforeEach
    fun setup() {
        inviteLinkGenerator = InviteLinkGeneratorImpl()
    }
    
    @Test
    fun `generateInviteLink should create valid link`() {
        // Given
        val groupId = "group123"
        
        // When
        val inviteLink = inviteLinkGenerator.generateInviteLink(groupId)
        
        // Then
        assertTrue(inviteLink.startsWith("https://chain.app/invite/"))
        assertTrue(inviteLink.length > "https://chain.app/invite/".length)
        
        // Should be able to validate the generated link
        val validatedGroupId = inviteLinkGenerator.validateInviteLink(inviteLink)
        assertEquals(groupId, validatedGroupId)
    }
    
    @Test
    fun `generateInviteLink should create unique links for different groups`() {
        // Given
        val groupId1 = "group1"
        val groupId2 = "group2"
        
        // When
        val link1 = inviteLinkGenerator.generateInviteLink(groupId1)
        val link2 = inviteLinkGenerator.generateInviteLink(groupId2)
        
        // Then
        assertNotEquals(link1, link2)
        assertEquals(groupId1, inviteLinkGenerator.validateInviteLink(link1))
        assertEquals(groupId2, inviteLinkGenerator.validateInviteLink(link2))
    }
    
    @Test
    fun `generateInviteLink should replace existing link for same group`() {
        // Given
        val groupId = "group123"
        
        // When
        val firstLink = inviteLinkGenerator.generateInviteLink(groupId)
        val secondLink = inviteLinkGenerator.generateInviteLink(groupId)
        
        // Then
        assertNotEquals(firstLink, secondLink)
        
        // First link should no longer be valid
        assertNull(inviteLinkGenerator.validateInviteLink(firstLink))
        
        // Second link should be valid
        assertEquals(groupId, inviteLinkGenerator.validateInviteLink(secondLink))
    }
    
    @Test
    fun `validateInviteLink should return null for invalid link`() {
        // Given
        val invalidLink = "https://chain.app/invite/invalid"
        
        // When
        val result = inviteLinkGenerator.validateInviteLink(invalidLink)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `validateInviteLink should return null for malformed link`() {
        // Given
        val malformedLink = "not-a-valid-link"
        
        // When
        val result = inviteLinkGenerator.validateInviteLink(malformedLink)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `revokeInviteLink should invalidate existing link`() {
        // Given
        val groupId = "group123"
        val inviteLink = inviteLinkGenerator.generateInviteLink(groupId)
        
        // Verify link is initially valid
        assertEquals(groupId, inviteLinkGenerator.validateInviteLink(inviteLink))
        
        // When
        inviteLinkGenerator.revokeInviteLink(groupId)
        
        // Then
        assertNull(inviteLinkGenerator.validateInviteLink(inviteLink))
    }
    
    @Test
    fun `revokeInviteLink should handle non-existent group gracefully`() {
        // Given
        val nonExistentGroupId = "non-existent-group"
        
        // When & Then (should not throw exception)
        assertDoesNotThrow {
            inviteLinkGenerator.revokeInviteLink(nonExistentGroupId)
        }
    }
    
    @Test
    fun `generated tokens should have sufficient entropy`() {
        // Given
        val groupId = "group123"
        val links = mutableSetOf<String>()
        
        // When - Generate multiple links
        repeat(100) {
            inviteLinkGenerator.revokeInviteLink(groupId) // Clear previous
            val link = inviteLinkGenerator.generateInviteLink(groupId)
            links.add(link)
        }
        
        // Then - All links should be unique (very high probability with good entropy)
        assertEquals(100, links.size)
    }
    
    @Test
    fun `link format should be consistent`() {
        // Given
        val groupId = "group123"
        
        // When
        val link = inviteLinkGenerator.generateInviteLink(groupId)
        
        // Then
        val expectedPrefix = "https://chain.app/invite/"
        assertTrue(link.startsWith(expectedPrefix))
        
        val token = link.removePrefix(expectedPrefix)
        assertEquals(32, token.length) // Expected token length
        assertTrue(token.all { it.isLetterOrDigit() }) // Should only contain alphanumeric characters
    }
}