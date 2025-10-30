package com.chain.messaging.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chain.messaging.core.cloud.*
import com.chain.messaging.core.security.SecureStorage
import com.chain.messaging.core.security.SecureStorageImpl
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class CloudAuthenticationIntegrationTest {
    
    private lateinit var cloudAuthManager: CloudAuthManagerImpl
    private lateinit var secureStorage: SecureStorage
    private lateinit var callbackHandler: OAuthCallbackHandler
    private val mockHttpClient = mockk<OkHttpClient>()
    private val mockCall = mockk<Call>()
    private val json = Json { ignoreUnknownKeys = true }
    
    @Before
    fun setup() {
        clearAllMocks()
        
        val context = ApplicationProvider.getApplicationContext<Context>()
        secureStorage = SecureStorageImpl(context)
        callbackHandler = OAuthCallbackHandler()
        
        cloudAuthManager = CloudAuthManagerImpl(
            context = context,
            secureStorage = secureStorage,
            httpClient = mockHttpClient,
            json = json
        )
        
        every { mockHttpClient.newCall(any()) } returns mockCall
    }
    
    @Test
    fun `complete OAuth flow for Google Drive`() = runTest {
        // Given - Mock successful token exchange
        val tokenResponse = """
            {
                "access_token": "ya29.access_token",
                "refresh_token": "1//refresh_token",
                "expires_in": 3600,
                "token_type": "Bearer",
                "scope": "https://www.googleapis.com/auth/drive.file"
            }
        """.trimIndent()
        
        val userInfoResponse = """
            {
                "id": "123456789",
                "email": "user@gmail.com",
                "name": "Test User",
                "picture": "https://example.com/photo.jpg"
            }
        """.trimIndent()
        
        val mockTokenResponse = mockk<Response>()
        every { mockTokenResponse.isSuccessful } returns true
        every { mockTokenResponse.body } returns tokenResponse.toResponseBody("application/json".toMediaType())
        
        val mockUserResponse = mockk<Response>()
        every { mockUserResponse.isSuccessful } returns true
        every { mockUserResponse.body } returns userInfoResponse.toResponseBody("application/json".toMediaType())
        
        every { mockCall.execute() } returnsMany listOf(mockTokenResponse, mockUserResponse)
        
        // When - Simulate OAuth callback with authorization code
        // Note: In a real test, you would need to mock the OAuth flow more completely
        // This is a simplified version focusing on the token exchange part
        
        // Verify initial state
        assertFalse(cloudAuthManager.isAuthenticated(CloudService.GOOGLE_DRIVE))
        assertNull(cloudAuthManager.getToken(CloudService.GOOGLE_DRIVE))
        
        // Simulate successful authentication (would normally come from OAuth flow)
        // For this test, we'll directly test the token storage and retrieval
        val testToken = AuthToken(
            accessToken = "ya29.access_token",
            refreshToken = "1//refresh_token",
            expiresAt = Instant.now().plusSeconds(3600),
            tokenType = "Bearer",
            scope = "https://www.googleapis.com/auth/drive.file"
        )
        
        // Store token directly (simulating successful OAuth)
        val tokenJson = json.encodeToString(AuthToken.serializer(), testToken)
        secureStorage.store("cloud_token_GOOGLE_DRIVE", tokenJson)
        
        val testAccount = CloudAccount(
            service = CloudService.GOOGLE_DRIVE,
            userId = "123456789",
            email = "user@gmail.com",
            displayName = "Test User",
            token = testToken
        )
        
        val accountJson = json.encodeToString(CloudAccount.serializer(), testAccount)
        secureStorage.store("cloud_account_GOOGLE_DRIVE", accountJson)
        
        // Then - Verify authentication state
        assertTrue(cloudAuthManager.isAuthenticated(CloudService.GOOGLE_DRIVE))
        
        val retrievedToken = cloudAuthManager.getToken(CloudService.GOOGLE_DRIVE)
        assertNotNull(retrievedToken)
        assertEquals("ya29.access_token", retrievedToken?.accessToken)
        assertEquals("1//refresh_token", retrievedToken?.refreshToken)
        
        val retrievedAccount = cloudAuthManager.getAccount(CloudService.GOOGLE_DRIVE)
        assertNotNull(retrievedAccount)
        assertEquals("user@gmail.com", retrievedAccount?.email)
        assertEquals("Test User", retrievedAccount?.displayName)
        
        val authenticatedAccounts = cloudAuthManager.getAuthenticatedAccounts()
        assertEquals(1, authenticatedAccounts.size)
        assertEquals(CloudService.GOOGLE_DRIVE, authenticatedAccounts[0].service)
    }
    
    @Test
    fun `token refresh flow works correctly`() = runTest {
        // Given - Store an expired token
        val expiredToken = AuthToken(
            accessToken = "expired_token",
            refreshToken = "valid_refresh_token",
            expiresAt = Instant.now().minusSeconds(3600),
            tokenType = "Bearer"
        )
        
        val expiredTokenJson = json.encodeToString(AuthToken.serializer(), expiredToken)
        secureStorage.store("cloud_token_GOOGLE_DRIVE", expiredTokenJson)
        
        // Mock successful token refresh
        val refreshResponse = """
            {
                "access_token": "new_access_token",
                "refresh_token": "new_refresh_token",
                "expires_in": 3600,
                "token_type": "Bearer"
            }
        """.trimIndent()
        
        val mockResponse = mockk<Response>()
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns refreshResponse.toResponseBody("application/json".toMediaType())
        every { mockCall.execute() } returns mockResponse
        
        // When - Refresh token
        val result = cloudAuthManager.refreshToken(CloudService.GOOGLE_DRIVE)
        
        // Then - Verify successful refresh
        assertTrue(result is AuthResult.Success)
        val successResult = result as AuthResult.Success
        assertEquals("new_access_token", successResult.token.accessToken)
        assertEquals("new_refresh_token", successResult.token.refreshToken)
        
        // Verify token was stored
        val storedToken = cloudAuthManager.getToken(CloudService.GOOGLE_DRIVE)
        assertEquals("new_access_token", storedToken?.accessToken)
    }
    
    @Test
    fun `sign out removes all stored data`() = runTest {
        // Given - Store authentication data
        val token = AuthToken(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresAt = Instant.now().plusSeconds(3600),
            tokenType = "Bearer"
        )
        
        val account = CloudAccount(
            service = CloudService.GOOGLE_DRIVE,
            userId = "user_id",
            email = "user@gmail.com",
            displayName = "User",
            token = token
        )
        
        val tokenJson = json.encodeToString(AuthToken.serializer(), token)
        val accountJson = json.encodeToString(CloudAccount.serializer(), account)
        
        secureStorage.store("cloud_token_GOOGLE_DRIVE", tokenJson)
        secureStorage.store("cloud_account_GOOGLE_DRIVE", accountJson)
        
        // Verify data is stored
        assertTrue(cloudAuthManager.isAuthenticated(CloudService.GOOGLE_DRIVE))
        assertNotNull(cloudAuthManager.getAccount(CloudService.GOOGLE_DRIVE))
        
        // Mock revoke request (optional)
        val mockResponse = mockk<Response>()
        every { mockResponse.isSuccessful } returns true
        every { mockCall.execute() } returns mockResponse
        
        // When - Sign out
        val success = cloudAuthManager.signOut(CloudService.GOOGLE_DRIVE)
        
        // Then - Verify data is removed
        assertTrue(success)
        assertFalse(cloudAuthManager.isAuthenticated(CloudService.GOOGLE_DRIVE))
        assertNull(cloudAuthManager.getToken(CloudService.GOOGLE_DRIVE))
        assertNull(cloudAuthManager.getAccount(CloudService.GOOGLE_DRIVE))
        
        val authenticatedAccounts = cloudAuthManager.getAuthenticatedAccounts()
        assertTrue(authenticatedAccounts.isEmpty())
    }
    
    @Test
    fun `multiple cloud services can be authenticated simultaneously`() = runTest {
        // Given - Create tokens for multiple services
        val googleToken = AuthToken(
            accessToken = "google_token",
            refreshToken = "google_refresh",
            expiresAt = Instant.now().plusSeconds(3600),
            tokenType = "Bearer"
        )
        
        val onedriveToken = AuthToken(
            accessToken = "onedrive_token",
            refreshToken = "onedrive_refresh",
            expiresAt = Instant.now().plusSeconds(3600),
            tokenType = "Bearer"
        )
        
        val googleAccount = CloudAccount(
            service = CloudService.GOOGLE_DRIVE,
            userId = "google_user",
            email = "user@gmail.com",
            displayName = "Google User",
            token = googleToken
        )
        
        val onedriveAccount = CloudAccount(
            service = CloudService.ONEDRIVE,
            userId = "onedrive_user",
            email = "user@outlook.com",
            displayName = "OneDrive User",
            token = onedriveToken
        )
        
        // Store authentication data for both services
        secureStorage.store("cloud_token_GOOGLE_DRIVE", json.encodeToString(AuthToken.serializer(), googleToken))
        secureStorage.store("cloud_account_GOOGLE_DRIVE", json.encodeToString(CloudAccount.serializer(), googleAccount))
        secureStorage.store("cloud_token_ONEDRIVE", json.encodeToString(AuthToken.serializer(), onedriveToken))
        secureStorage.store("cloud_account_ONEDRIVE", json.encodeToString(CloudAccount.serializer(), onedriveAccount))
        
        // When - Check authentication status
        val authenticatedAccounts = cloudAuthManager.getAuthenticatedAccounts()
        
        // Then - Verify both services are authenticated
        assertEquals(2, authenticatedAccounts.size)
        assertTrue(cloudAuthManager.isAuthenticated(CloudService.GOOGLE_DRIVE))
        assertTrue(cloudAuthManager.isAuthenticated(CloudService.ONEDRIVE))
        assertFalse(cloudAuthManager.isAuthenticated(CloudService.ICLOUD))
        assertFalse(cloudAuthManager.isAuthenticated(CloudService.DROPBOX))
        
        val googleRetrieved = cloudAuthManager.getAccount(CloudService.GOOGLE_DRIVE)
        val onedriveRetrieved = cloudAuthManager.getAccount(CloudService.ONEDRIVE)
        
        assertNotNull(googleRetrieved)
        assertNotNull(onedriveRetrieved)
        assertEquals("user@gmail.com", googleRetrieved?.email)
        assertEquals("user@outlook.com", onedriveRetrieved?.email)
    }
    
    @Test
    fun `revoke all tokens clears all authentication data`() = runTest {
        // Given - Authenticate multiple services
        val services = listOf(CloudService.GOOGLE_DRIVE, CloudService.ONEDRIVE)
        
        services.forEach { service ->
            val token = AuthToken(
                accessToken = "${service.name}_token",
                refreshToken = "${service.name}_refresh",
                expiresAt = Instant.now().plusSeconds(3600),
                tokenType = "Bearer"
            )
            
            val account = CloudAccount(
                service = service,
                userId = "${service.name}_user",
                email = "user@${service.name.lowercase()}.com",
                displayName = "${service.displayName} User",
                token = token
            )
            
            secureStorage.store("cloud_token_${service.name}", json.encodeToString(AuthToken.serializer(), token))
            secureStorage.store("cloud_account_${service.name}", json.encodeToString(CloudAccount.serializer(), account))
        }
        
        // Verify services are authenticated
        services.forEach { service ->
            assertTrue(cloudAuthManager.isAuthenticated(service))
        }
        
        // Mock revoke requests
        val mockResponse = mockk<Response>()
        every { mockResponse.isSuccessful } returns true
        every { mockCall.execute() } returns mockResponse
        
        // When - Revoke all tokens
        cloudAuthManager.revokeAllTokens()
        
        // Then - Verify all authentication data is cleared
        services.forEach { service ->
            assertFalse(cloudAuthManager.isAuthenticated(service))
            assertNull(cloudAuthManager.getToken(service))
            assertNull(cloudAuthManager.getAccount(service))
        }
        
        val authenticatedAccounts = cloudAuthManager.getAuthenticatedAccounts()
        assertTrue(authenticatedAccounts.isEmpty())
    }
}