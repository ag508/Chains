package com.chain.messaging.core.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.core.crypto.KeyManager
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthenticationServiceTest {

    private lateinit var context: Context
    private lateinit var keyManager: KeyManager
    private lateinit var userIdentityManager: UserIdentityManager
    private lateinit var oAuthManager: OAuthManager
    private lateinit var passkeyManager: PasskeyManager
    private lateinit var authenticationService: AuthenticationService

    @Before
    fun setup() = runTest {
        context = ApplicationProvider.getApplicationContext()
        
        keyManager = KeyManager(context)
        userIdentityManager = UserIdentityManager(context)
        oAuthManager = OAuthManager(context)
        passkeyManager = PasskeyManager(context)
        
        authenticationService = AuthenticationService(
            context = context,
            keyManager = keyManager,
            userIdentityManager = userIdentityManager,
            oAuthManager = oAuthManager,
            passkeyManager = passkeyManager
        )
        
        // Initialize key manager
        keyManager.initialize()
    }

    @After
    fun tearDown() = runTest {
        // Clean up test data
        keyManager.clearAllKeys()
        userIdentityManager.clearAllUsers()
        oAuthManager.clearTokens()
        passkeyManager.clearAllPasskeys()
    }

    @Test
    fun `authenticateWithGoogle should succeed with valid OAuth`() = runTest {
        // Given
        val mockActivity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).create().get()
        
        // When
        val result = authenticationService.authenticateWithGoogle(mockActivity)

        // Then
        assertTrue("Google authentication should succeed", result.isSuccess)
        
        val authResult = result.getOrThrow()
        assertTrue("Authentication should be successful", authResult.isSuccess)
        assertNotNull("User identity should not be null", authResult.userIdentity)
        assertEquals("Auth method should be Google OAuth", 
            AuthMethod.GOOGLE_OAUTH, authResult.authMethod)
        
        // Verify user is stored
        val currentUser = authenticationService.getCurrentUser()
        assertNotNull("Current user should be set", currentUser)
        assertEquals("Current user should match authenticated user", 
            authResult.userIdentity?.userId, currentUser?.userId)
    }

    @Test
    fun `authenticateWithMicrosoft should succeed with valid OAuth`() = runTest {
        // Given
        val mockActivity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).create().get()
        
        // When
        val result = authenticationService.authenticateWithMicrosoft(mockActivity)

        // Then
        assertTrue("Microsoft authentication should succeed", result.isSuccess)
        
        val authResult = result.getOrThrow()
        assertTrue("Authentication should be successful", authResult.isSuccess)
        assertNotNull("User identity should not be null", authResult.userIdentity)
        assertEquals("Auth method should be Microsoft OAuth", 
            AuthMethod.MICROSOFT_OAUTH, authResult.authMethod)
        
        // Verify user is stored
        val currentUser = authenticationService.getCurrentUser()
        assertNotNull("Current user should be set", currentUser)
        assertEquals("Current user should match authenticated user", 
            authResult.userIdentity?.userId, currentUser?.userId)
    }

    @Test
    fun `registerUser should create new user identity`() = runTest {
        // Given
        val oAuthData = OAuthData(
            providerId = "google",
            providerUserId = "test_user_123",
            email = "test@example.com",
            name = "Test User",
            profilePictureUrl = "https://example.com/profile.jpg",
            accessToken = "test_access_token",
            refreshToken = "test_refresh_token",
            expiresAt = System.currentTimeMillis() + 3600000
        )

        // When
        val result = authenticationService.registerUser(oAuthData)

        // Then
        assertTrue("User registration should succeed", result.isSuccess)
        
        val userIdentity = result.getOrThrow()
        assertNotNull("User identity should not be null", userIdentity)
        assertEquals("Email should match", oAuthData.email, userIdentity.email)
        assertEquals("Display name should match", oAuthData.name, userIdentity.displayName)
        assertEquals("Profile picture URL should match", 
            oAuthData.profilePictureUrl, userIdentity.profilePictureUrl)
        assertNotNull("Blockchain public key should be generated", userIdentity.blockchainPublicKey)
        assertNotNull("Signal identity key should be set", userIdentity.signalIdentityKey)
    }

    @Test
    fun `setupPasskey should succeed for registered user`() = runTest {
        // Given - register a user first
        val mockActivity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).create().get()
        val oAuthData = OAuthData(
            providerId = "google",
            providerUserId = "test_user_123",
            email = "test@example.com",
            name = "Test User",
            accessToken = "test_access_token",
            expiresAt = System.currentTimeMillis() + 3600000
        )
        val userIdentity = authenticationService.registerUser(oAuthData).getOrThrow()

        // When
        val result = authenticationService.setupPasskey(mockActivity, userIdentity)

        // Then
        assertTrue("Passkey setup should succeed", result.isSuccess)
        
        // Verify passkey was created
        val hasPasskey = passkeyManager.hasPasskey(userIdentity.username)
        assertTrue("User should have passkey after setup", hasPasskey)
    }

    @Test
    fun `authenticateWithPasskey should succeed with existing passkey`() = runTest {
        // Given - register user and setup passkey
        val mockActivity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).create().get()
        val oAuthData = OAuthData(
            providerId = "google",
            providerUserId = "test_user_123",
            email = "test@example.com",
            name = "Test User",
            accessToken = "test_access_token",
            expiresAt = System.currentTimeMillis() + 3600000
        )
        val userIdentity = authenticationService.registerUser(oAuthData).getOrThrow()
        authenticationService.setupPasskey(mockActivity, userIdentity)

        // When
        val result = authenticationService.authenticateWithPasskey(mockActivity, userIdentity.username)

        // Then
        assertTrue("Passkey authentication should succeed", result.isSuccess)
        
        val authResult = result.getOrThrow()
        assertTrue("Authentication should be successful", authResult.isSuccess)
        assertNotNull("User identity should not be null", authResult.userIdentity)
        assertEquals("Auth method should be passkey", 
            AuthMethod.PASSKEY, authResult.authMethod)
        assertEquals("User ID should match", 
            userIdentity.userId, authResult.userIdentity?.userId)
    }

    @Test
    fun `authenticateWithPasskey should fail for non-existent user`() = runTest {
        // Given - no user registered
        val mockActivity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).create().get()
        val nonExistentUsername = "nonexistent@example.com"

        // When
        val result = authenticationService.authenticateWithPasskey(mockActivity, nonExistentUsername)

        // Then
        assertTrue("Passkey authentication should fail for non-existent user", result.isFailure)
        assertTrue("Should be AuthenticationException", 
            result.exceptionOrNull() is AuthenticationException)
    }

    @Test
    fun `isBiometricAuthenticationAvailable should return correct status`() {
        // When
        val isAvailable = authenticationService.isBiometricAuthenticationAvailable()

        // Then
        // In test environment, biometric authentication is typically not available
        assertFalse("Biometric authentication should not be available in test environment", isAvailable)
    }

    @Test
    fun `isPasskeyAuthenticationAvailable should return correct status`() = runTest {
        // When
        val isAvailable = authenticationService.isPasskeyAuthenticationAvailable()

        // Then
        // Should be available on API 24+ (which our test uses API 28)
        assertTrue("Passkey authentication should be available", isAvailable)
    }

    @Test
    fun `logout should clear current user`() = runTest {
        // Given - authenticate user first
        val mockActivity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).create().get()
        authenticationService.authenticateWithGoogle(mockActivity)
        
        // Verify user is authenticated
        assertTrue("User should be authenticated before logout", 
            authenticationService.isAuthenticated())
        assertNotNull("Current user should exist before logout", 
            authenticationService.getCurrentUser())

        // When
        val result = authenticationService.logout()

        // Then
        assertTrue("Logout should succeed", result.isSuccess)
        assertFalse("User should not be authenticated after logout", 
            authenticationService.isAuthenticated())
        assertNull("Current user should be null after logout", 
            authenticationService.getCurrentUser())
    }

    @Test
    fun `getCurrentUser should return null when not authenticated`() = runTest {
        // Given - no authentication

        // When
        val currentUser = authenticationService.getCurrentUser()

        // Then
        assertNull("Current user should be null when not authenticated", currentUser)
    }

    @Test
    fun `isAuthenticated should return false when not authenticated`() = runTest {
        // Given - no authentication

        // When
        val isAuthenticated = authenticationService.isAuthenticated()

        // Then
        assertFalse("Should not be authenticated when no user is logged in", isAuthenticated)
    }

    @Test
    fun `isAuthenticated should return true when authenticated`() = runTest {
        // Given - authenticate user
        val mockActivity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).create().get()
        authenticationService.authenticateWithGoogle(mockActivity)

        // When
        val isAuthenticated = authenticationService.isAuthenticated()

        // Then
        assertTrue("Should be authenticated after successful login", isAuthenticated)
    }

    @Test
    fun `multiple authentication methods should work for same user`() = runTest {
        // Given - register user with Google OAuth
        val mockActivity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).create().get()
        val googleResult = authenticationService.authenticateWithGoogle(mockActivity)
        assertTrue("Google authentication should succeed", googleResult.isSuccess)
        
        val userIdentity = googleResult.getOrThrow().userIdentity!!
        
        // Setup passkey for the same user
        val passkeySetupResult = authenticationService.setupPasskey(mockActivity, userIdentity)
        assertTrue("Passkey setup should succeed", passkeySetupResult.isSuccess)

        // When - authenticate with passkey
        val passkeyResult = authenticationService.authenticateWithPasskey(mockActivity, userIdentity.username)

        // Then
        assertTrue("Passkey authentication should succeed", passkeyResult.isSuccess)
        
        val passkeyAuthResult = passkeyResult.getOrThrow()
        assertEquals("User ID should be the same", 
            userIdentity.userId, passkeyAuthResult.userIdentity?.userId)
        assertEquals("Auth method should be passkey", 
            AuthMethod.PASSKEY, passkeyAuthResult.authMethod)
    }

    @Test
    fun `user identity should persist across service instances`() = runTest {
        // Given - authenticate user
        val mockActivity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).create().get()
        val authResult = authenticationService.authenticateWithGoogle(mockActivity)
        val originalUserId = authResult.getOrThrow().userIdentity?.userId

        // When - create new service instance
        val newAuthService = AuthenticationService(
            context = context,
            keyManager = keyManager,
            userIdentityManager = userIdentityManager,
            oAuthManager = oAuthManager,
            passkeyManager = passkeyManager
        )
        
        val currentUser = newAuthService.getCurrentUser()

        // Then
        assertNotNull("Current user should persist across instances", currentUser)
        assertEquals("User ID should match", originalUserId, currentUser?.userId)
    }
}