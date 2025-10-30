package com.chain.messaging.domain.repository

import com.chain.messaging.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user-related operations.
 * This defines the contract for user data access in the domain layer.
 */
interface UserRepository {
    
    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): User?
    
    /**
     * Get multiple users by their IDs
     */
    suspend fun getUsersByIds(userIds: List<String>): List<User>
    
    /**
     * Get all users
     */
    suspend fun getAllUsers(): List<User>
    
    /**
     * Search users by display name
     */
    suspend fun searchUsers(query: String): List<User>
    
    /**
     * Update user profile
     */
    suspend fun updateUser(user: User): Result<Unit>
    
    /**
     * Observe current user changes
     */
    fun observeCurrentUser(): Flow<User?>
    
    /**
     * Observe user by ID for reactive updates
     */
    fun observeUserById(userId: String): Flow<User?>
    
    /**
     * Insert user to local database
     */
    suspend fun insertUser(user: User): Result<Unit>
    
    /**
     * Save user to local database
     */
    suspend fun saveUser(user: User): Result<Unit>
}