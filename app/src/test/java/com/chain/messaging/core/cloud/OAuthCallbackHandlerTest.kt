package com.chain.messaging.core.cloud

import android.content.Intent
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class OAuthCallbackHandlerTest {
    
    private lateinit var callbackHandler: OAuthCallbackHandler
    
    @Before
    fun setup() {
        callbackHandler = OAuthCallbackHandler()
    }
    
    @Test
    fun `handleCallback processes successful OAuth callback`() = runTest {
        // Given
        val mockIntent = mockk<Intent>()
        val mockUri = mockk<Uri>()
        
        every { mockIntent.data } returns mockUri
        every { mockUri.scheme } returns "com.chain.messaging"
        every { mockUri.host } returns "oauth"
        every { mockUri.path } returns "/callback"
        every { mockUri.getQueryParameter("code") } returns "auth_code_123"
        every { mockUri.getQueryParameter("error") } returns null
        every { mockUri.getQueryParameter("state") } returns "state_456"
        
        // When
        val handled = callbackHandler.handleCallback(mockIntent)
        
        // Then
        assertTrue(handled)
        
        val callback = callbackHandler.callbackFlow.first()
        assertTrue(callback is OAuthCallback.Success)
        val successCallback = callback as OAuthCallback.Success
        assertEquals("auth_code_123", successCallback.code)
        assertEquals("state_456", successCallback.state)
    }
    
    @Test
    fun `handleCallback processes OAuth error callback`() = runTest {
        // Given
        val mockIntent = mockk<Intent>()
        val mockUri = mockk<Uri>()
        
        every { mockIntent.data } returns mockUri
        every { mockUri.scheme } returns "com.chain.messaging"
        every { mockUri.host } returns "oauth"
        every { mockUri.path } returns "/callback"
        every { mockUri.getQueryParameter("code") } returns null
        every { mockUri.getQueryParameter("error") } returns "access_denied"
        every { mockUri.getQueryParameter("error_description") } returns "User denied access"
        every { mockUri.getQueryParameter("state") } returns "state_456"
        
        // When
        val handled = callbackHandler.handleCallback(mockIntent)
        
        // Then
        assertTrue(handled)
        
        val callback = callbackHandler.callbackFlow.first()
        assertTrue(callback is OAuthCallback.Error)
        val errorCallback = callback as OAuthCallback.Error
        assertEquals("access_denied", errorCallback.error)
        assertEquals("User denied access", errorCallback.description)
    }
    
    @Test
    fun `handleCallback handles malformed callback`() = runTest {
        // Given
        val mockIntent = mockk<Intent>()
        val mockUri = mockk<Uri>()
        
        every { mockIntent.data } returns mockUri
        every { mockUri.scheme } returns "com.chain.messaging"
        every { mockUri.host } returns "oauth"
        every { mockUri.path } returns "/callback"
        every { mockUri.getQueryParameter("code") } returns null
        every { mockUri.getQueryParameter("error") } returns null
        every { mockUri.getQueryParameter("state") } returns "state_456"
        
        // When
        val handled = callbackHandler.handleCallback(mockIntent)
        
        // Then
        assertTrue(handled)
        
        val callback = callbackHandler.callbackFlow.first()
        assertTrue(callback is OAuthCallback.Error)
        val errorCallback = callback as OAuthCallback.Error
        assertEquals("unknown_error", errorCallback.error)
        assertTrue(errorCallback.description.contains("No code or error"))
    }
    
    @Test
    fun `handleCallback ignores non-OAuth intents`() {
        // Given
        val mockIntent = mockk<Intent>()
        val mockUri = mockk<Uri>()
        
        every { mockIntent.data } returns mockUri
        every { mockUri.scheme } returns "https"
        every { mockUri.host } returns "example.com"
        
        // When
        val handled = callbackHandler.handleCallback(mockIntent)
        
        // Then
        assertFalse(handled)
    }
    
    @Test
    fun `handleCallback ignores wrong OAuth path`() {
        // Given
        val mockIntent = mockk<Intent>()
        val mockUri = mockk<Uri>()
        
        every { mockIntent.data } returns mockUri
        every { mockUri.scheme } returns "com.chain.messaging"
        every { mockUri.host } returns "oauth"
        every { mockUri.path } returns "/wrong-path"
        
        // When
        val handled = callbackHandler.handleCallback(mockIntent)
        
        // Then
        assertFalse(handled)
    }
    
    @Test
    fun `handleCallback handles null intent data`() {
        // Given
        val mockIntent = mockk<Intent>()
        every { mockIntent.data } returns null
        
        // When
        val handled = callbackHandler.handleCallback(mockIntent)
        
        // Then
        assertFalse(handled)
    }
}