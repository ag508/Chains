package com.chain.messaging.core.cloud

import android.content.Context
import com.chain.messaging.core.security.SecureStorage
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException
import java.time.Instant

class CloudAuthManagerTest {
    
    private lateinit var cloudAuthManager: CloudAuthManagerImpl
    private val mockContext = mockk<Context>()
    private val mockSecureStorage = mockk<SecureStorage>()
    private val mockHttpClient = mockk<OkHttpClient>()
    private val mockCall = mockk<Call>()
    private val json = Json { ignoreUnknownKeys = true }
    
    @Before
    fun setup() {
        clearAllMocks()
        
        cloudAuthManager = CloudAuthManagerImpl(
            context = mockContext,
            secureStorage = mockSecureStorage,
            httpClient = mockHttpClient,
            json = json
        )
        
        // Default mock behaviors
        coEvery { mockSecureStorage.store(any(), any()) } just Runs
        coEvery { mockSecureStorage.get(any()) } returns null
        coEvery { mockSecureStorage.remove(any()) } just Runs
        coEvery { mockSecureStorage.contains(any()) } returns false
        
        every { mockHttpClient.newCall(any()) } returns mockCall
    }
    
    @Test
    fun `getToken returns null when no token stored`() = runTest {
        // Given
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns null
        
        // When
        val result = cloudAuthManager.getToken(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getToken returns stored token when valid`() = runTest {
        // Given
        val token = AuthToken(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresAt = Instant.now().plusSeconds(3600),
            tokenType = "Bearer"
        )
        val tokenJson = json.encodeToString(AuthToken.serializer(), token)
        
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns tokenJson
        
        // When
        val result = cloudAuthManager.getToken(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertNotNull(result)
        assertEquals(token.accessToken, result?.accessToken)
        assertEquals(token.refreshToken, result?.refreshToken)
    }
    
    @Test
    fun `getToken auto-refreshes expired token`() = runTest {
        // Given
        val expiredToken = AuthToken(
            accessToken = "expired_token",
            refreshToken = "refresh_token",
            expiresAt = Instant.now().minusSeconds(3600),
            tokenType = "Bearer"
        )
        val expiredTokenJson = json.encodeToString(AuthToken.serializer(), expiredToken)
        
        val refreshedToken = AuthToken(
            accessToken = "new_access_token",
            refreshToken = "new_refresh_token",
            expiresAt = Instant.now().plusSeconds(3600),
            tokenType = "Bearer"
        )
        
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns expiredTokenJson
        
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
        
        // When
        val result = cloudAuthManager.getToken(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertNotNull(result)
        assertEquals("new_access_token", result?.accessToken)
        
        // Verify token was stored
        coVerify { mockSecureStorage.store("cloud_token_GOOGLE_DRIVE", any()) }
    }
    
    @Test
    fun `refreshToken successfully refreshes valid token`() = runTest {
        // Given
        val currentToken = AuthToken(
            accessToken = "current_token",
            refreshToken = "refresh_token",
            expiresAt = Instant.now().minusSeconds(300),
            tokenType = "Bearer"
        )
        val currentTokenJson = json.encodeToString(AuthToken.serializer(), currentToken)
        
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns currentTokenJson
        
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
        
        // When
        val result = cloudAuthManager.refreshToken(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertTrue(result is AuthResult.Success)
        val successResult = result as AuthResult.Success
        assertEquals("new_access_token", successResult.token.accessToken)
        
        // Verify token was stored
        coVerify { mockSecureStorage.store("cloud_token_GOOGLE_DRIVE", any()) }
    }
    
    @Test
    fun `refreshToken fails when no refresh token available`() = runTest {
        // Given
        val tokenWithoutRefresh = AuthToken(
            accessToken = "access_token",
            refreshToken = null,
            expiresAt = Instant.now().minusSeconds(300),
            tokenType = "Bearer"
        )
        val tokenJson = json.encodeToString(AuthToken.serializer(), tokenWithoutRefresh)
        
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns tokenJson
        
        // When
        val result = cloudAuthManager.refreshToken(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertTrue(result is AuthResult.Error)
        val errorResult = result as AuthResult.Error
        assertTrue(errorResult.message.contains("cannot be refreshed"))
    }
    
    @Test
    fun `refreshToken handles network error`() = runTest {
        // Given
        val currentToken = AuthToken(
            accessToken = "current_token",
            refreshToken = "refresh_token",
            expiresAt = Instant.now().minusSeconds(300),
            tokenType = "Bearer"
        )
        val currentTokenJson = json.encodeToString(AuthToken.serializer(), currentToken)
        
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns currentTokenJson
        every { mockCall.execute() } throws IOException("Network error")
        
        // When
        val result = cloudAuthManager.refreshToken(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertTrue(result is AuthResult.Error)
        val errorResult = result as AuthResult.Error
        assertTrue(errorResult.message.contains("Network error"))
    }
    
    @Test
    fun `signOut removes stored credentials`() = runTest {
        // Given
        val token = AuthToken(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresAt = Instant.now().plusSeconds(3600),
            tokenType = "Bearer"
        )
        val tokenJson = json.encodeToString(AuthToken.serializer(), token)
        
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns tokenJson
        
        // Mock revoke request (optional)
        val mockResponse = mockk<Response>()
        every { mockResponse.isSuccessful } returns true
        every { mockCall.execute() } returns mockResponse
        
        // When
        val result = cloudAuthManager.signOut(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertTrue(result)
        
        // Verify credentials were removed
        coVerify { mockSecureStorage.remove("cloud_token_GOOGLE_DRIVE") }
        coVerify { mockSecureStorage.remove("cloud_account_GOOGLE_DRIVE") }
    }
    
    @Test
    fun `isAuthenticated returns true for valid token`() = runTest {
        // Given
        val validToken = AuthToken(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresAt = Instant.now().plusSeconds(3600),
            tokenType = "Bearer"
        )
        val tokenJson = json.encodeToString(AuthToken.serializer(), validToken)
        
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns tokenJson
        
        // When
        val result = cloudAuthManager.isAuthenticated(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isAuthenticated returns false for expired token`() = runTest {
        // Given
        val expiredToken = AuthToken(
            accessToken = "access_token",
            refreshToken = null,
            expiresAt = Instant.now().minusSeconds(3600),
            tokenType = "Bearer"
        )
        val tokenJson = json.encodeToString(AuthToken.serializer(), expiredToken)
        
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns tokenJson
        
        // When
        val result = cloudAuthManager.isAuthenticated(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isAuthenticated returns false when no token stored`() = runTest {
        // Given
        coEvery { mockSecureStorage.get("cloud_token_GOOGLE_DRIVE") } returns null
        
        // When
        val result = cloudAuthManager.isAuthenticated(CloudService.GOOGLE_DRIVE)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getAuthenticatedAccounts returns all authenticated accounts`() = runTest {
        // Given
        val googleAccount = CloudAccount(
            service = CloudService.GOOGLE_DRIVE,
            userId = "google_user_id",
            email = "user@gmail.com",
            displayName = "Google User",
            token = AuthToken("token", "refresh", Instant.now().plusSeconds(3600))
        )
        
        val onedriveAccount = CloudAccount(
            service = CloudService.ONEDRIVE,
            userId = "onedrive_user_id",
            email = "user@outlook.com",
            displayName = "OneDrive User",
            token = AuthToken("token", "refresh", Instant.now().plusSeconds(3600))
        )
        
        val googleAccountJson = json.encodeToString(CloudAccount.serializer(), googleAccount)
        val onedriveAccountJson = json.encodeToString(CloudAccount.serializer(), onedriveAccount)
        
        coEvery { mockSecureStorage.get("cloud_account_GOOGLE_DRIVE") } returns googleAccountJson
        coEvery { mockSecureStorage.get("cloud_account_ONEDRIVE") } returns onedriveAccountJson
        coEvery { mockSecureStorage.get("cloud_account_ICLOUD") } returns null
        coEvery { mockSecureStorage.get("cloud_account_DROPBOX") } returns null
        
        // When
        val result = cloudAuthManager.getAuthenticatedAccounts()
        
        // Then
        assertEquals(2, result.size)
        assertTrue(result.any { it.service == CloudService.GOOGLE_DRIVE })
        assertTrue(result.any { it.service == CloudService.ONEDRIVE })
    }
    
    @Test
    fun `revokeAllTokens signs out from all services`() = runTest {
        // Given
        val services = CloudService.values()
        
        // When
        cloudAuthManager.revokeAllTokens()
        
        // Then
        services.forEach { service ->
            coVerify { mockSecureStorage.remove("cloud_token_${service.name}") }
            coVerify { mockSecureStorage.remove("cloud_account_${service.name}") }
        }
    }
}