package com.chain.messaging.presentation.cloud

import com.chain.messaging.core.cloud.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant

class CloudAccountsViewModelTest {
    
    private lateinit var viewModel: CloudAccountsViewModel
    private val mockCloudAuthManager = mockk<CloudAuthManager>()
    
    @Before
    fun setup() {
        clearAllMocks()
        
        // Default mock behaviors
        coEvery { mockCloudAuthManager.getAuthenticatedAccounts() } returns emptyList()
        every { mockCloudAuthManager.observeAuthState() } returns flowOf(emptyMap())
        
        viewModel = CloudAccountsViewModel(mockCloudAuthManager)
    }
    
    @Test
    fun `initial state loads authenticated accounts`() = runTest {
        // Given
        val accounts = listOf(
            CloudAccount(
                service = CloudService.GOOGLE_DRIVE,
                userId = "user1",
                email = "user@gmail.com",
                displayName = "User",
                token = AuthToken("token", "refresh", Instant.now().plusSeconds(3600))
            )
        )
        
        coEvery { mockCloudAuthManager.getAuthenticatedAccounts() } returns accounts
        every { mockCloudAuthManager.observeAuthState() } returns flowOf(
            mapOf(CloudService.GOOGLE_DRIVE to true)
        )
        
        // When
        val newViewModel = CloudAccountsViewModel(mockCloudAuthManager)
        
        // Then
        val state = newViewModel.uiState.value
        assertEquals(1, state.accounts.size)
        assertEquals(CloudService.GOOGLE_DRIVE, state.accounts[0].service)
        assertTrue(state.authenticationStates[CloudService.GOOGLE_DRIVE] == true)
    }
    
    @Test
    fun `authenticateService shows loading state and handles success`() = runTest {
        // Given
        val successToken = AuthToken("token", "refresh", Instant.now().plusSeconds(3600))
        coEvery { mockCloudAuthManager.authenticate(CloudService.GOOGLE_DRIVE) } returns 
            AuthResult.Success(successToken)
        coEvery { mockCloudAuthManager.getAuthenticatedAccounts() } returns emptyList()
        
        // When
        viewModel.authenticateService(CloudService.GOOGLE_DRIVE)
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.authenticatingService) // Should be null after completion
        assertTrue(state.message?.contains("Successfully connected") == true)
        assertNull(state.error)
        
        coVerify { mockCloudAuthManager.authenticate(CloudService.GOOGLE_DRIVE) }
    }
    
    @Test
    fun `authenticateService handles authentication error`() = runTest {
        // Given
        coEvery { mockCloudAuthManager.authenticate(CloudService.GOOGLE_DRIVE) } returns 
            AuthResult.Error("Authentication failed")
        
        // When
        viewModel.authenticateService(CloudService.GOOGLE_DRIVE)
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.authenticatingService)
        assertNull(state.message)
        assertTrue(state.error?.contains("Failed to connect") == true)
    }
    
    @Test
    fun `authenticateService handles authentication cancellation`() = runTest {
        // Given
        coEvery { mockCloudAuthManager.authenticate(CloudService.GOOGLE_DRIVE) } returns 
            AuthResult.Cancelled
        
        // When
        viewModel.authenticateService(CloudService.GOOGLE_DRIVE)
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.authenticatingService)
        assertTrue(state.message?.contains("cancelled") == true)
        assertNull(state.error)
    }
    
    @Test
    fun `signOutFromService handles successful sign out`() = runTest {
        // Given
        coEvery { mockCloudAuthManager.signOut(CloudService.GOOGLE_DRIVE) } returns true
        coEvery { mockCloudAuthManager.getAuthenticatedAccounts() } returns emptyList()
        
        // When
        viewModel.signOutFromService(CloudService.GOOGLE_DRIVE)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.message?.contains("Signed out") == true)
        assertNull(state.error)
        
        coVerify { mockCloudAuthManager.signOut(CloudService.GOOGLE_DRIVE) }
    }
    
    @Test
    fun `signOutFromService handles sign out failure`() = runTest {
        // Given
        coEvery { mockCloudAuthManager.signOut(CloudService.GOOGLE_DRIVE) } returns false
        
        // When
        viewModel.signOutFromService(CloudService.GOOGLE_DRIVE)
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.message)
        assertTrue(state.error?.contains("Failed to sign out") == true)
    }
    
    @Test
    fun `refreshToken handles successful token refresh`() = runTest {
        // Given
        val refreshedToken = AuthToken("new_token", "new_refresh", Instant.now().plusSeconds(3600))
        coEvery { mockCloudAuthManager.refreshToken(CloudService.GOOGLE_DRIVE) } returns 
            AuthResult.Success(refreshedToken)
        coEvery { mockCloudAuthManager.getAuthenticatedAccounts() } returns emptyList()
        
        // When
        viewModel.refreshToken(CloudService.GOOGLE_DRIVE)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.message?.contains("Token refreshed") == true)
        assertNull(state.error)
        
        coVerify { mockCloudAuthManager.refreshToken(CloudService.GOOGLE_DRIVE) }
    }
    
    @Test
    fun `refreshToken handles refresh failure`() = runTest {
        // Given
        coEvery { mockCloudAuthManager.refreshToken(CloudService.GOOGLE_DRIVE) } returns 
            AuthResult.Error("Refresh failed")
        
        // When
        viewModel.refreshToken(CloudService.GOOGLE_DRIVE)
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.message)
        assertTrue(state.error?.contains("Failed to refresh token") == true)
    }
    
    @Test
    fun `clearMessage clears success message`() = runTest {
        // Given - set a message first
        coEvery { mockCloudAuthManager.signOut(CloudService.GOOGLE_DRIVE) } returns true
        coEvery { mockCloudAuthManager.getAuthenticatedAccounts() } returns emptyList()
        viewModel.signOutFromService(CloudService.GOOGLE_DRIVE)
        
        // Verify message is set
        assertTrue(viewModel.uiState.value.message != null)
        
        // When
        viewModel.clearMessage()
        
        // Then
        assertNull(viewModel.uiState.value.message)
    }
    
    @Test
    fun `clearError clears error message`() = runTest {
        // Given - set an error first
        coEvery { mockCloudAuthManager.signOut(CloudService.GOOGLE_DRIVE) } returns false
        viewModel.signOutFromService(CloudService.GOOGLE_DRIVE)
        
        // Verify error is set
        assertTrue(viewModel.uiState.value.error != null)
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `observeAuthState updates authentication states`() = runTest {
        // Given
        val authStates = mapOf(
            CloudService.GOOGLE_DRIVE to true,
            CloudService.ONEDRIVE to false
        )
        every { mockCloudAuthManager.observeAuthState() } returns flowOf(authStates)
        
        // When
        val newViewModel = CloudAccountsViewModel(mockCloudAuthManager)
        
        // Then
        val state = newViewModel.uiState.value
        assertEquals(authStates, state.authenticationStates)
    }
}