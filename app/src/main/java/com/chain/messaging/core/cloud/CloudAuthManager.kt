package com.chain.messaging.core.cloud

import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing cloud service authentication
 */
interface CloudAuthManager {
    
    /**
     * Initialize authentication service
     */
    suspend fun initializeService(service: CloudService)
    
    /**
     * Authenticate with a cloud service using OAuth
     */
    suspend fun authenticate(service: CloudService): AuthResult
    
    /**
     * Refresh an expired token
     */
    suspend fun refreshToken(service: CloudService): AuthResult
    
    /**
     * Sign out from a cloud service
     */
    suspend fun signOut(service: CloudService): Boolean
    
    /**
     * Get the current authentication token for a service
     */
    suspend fun getToken(service: CloudService): AuthToken?
    
    /**
     * Get account information for a service
     */
    suspend fun getAccount(service: CloudService): CloudAccount?
    
    /**
     * Get all authenticated accounts
     */
    suspend fun getAuthenticatedAccounts(): List<CloudAccount>
    
    /**
     * Check if a service is authenticated
     */
    suspend fun isAuthenticated(service: CloudService): Boolean
    
    /**
     * Observe authentication state changes
     */
    fun observeAuthState(): Flow<Map<CloudService, Boolean>>
    
    /**
     * Revoke all tokens and clear stored credentials
     */
    suspend fun revokeAllTokens()
}