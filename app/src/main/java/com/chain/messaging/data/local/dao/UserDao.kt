package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user operations
 */
@Dao
interface UserDao {
    
    /**
     * Get user by ID
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    /**
     * Get multiple users by IDs
     */
    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<String>): List<UserEntity>
    
    /**
     * Get user as Flow for reactive updates
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: String): Flow<UserEntity?>
    
    /**
     * Get user as Flow for reactive updates
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: String): Flow<UserEntity?>
    
    /**
     * Get all users
     */
    @Query("SELECT * FROM users ORDER BY displayName ASC")
    suspend fun getAllUsers(): List<UserEntity>
    
    /**
     * Get all users as Flow
     */
    @Query("SELECT * FROM users ORDER BY displayName ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>
    
    /**
     * Insert user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    /**
     * Insert or update user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)
    
    /**
     * Update user
     */
    @Update
    suspend fun updateUser(user: UserEntity)
    
    /**
     * Insert or update multiple users
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsers(users: List<UserEntity>)
    
    /**
     * Update user display name
     */
    @Query("UPDATE users SET displayName = :displayName, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateDisplayName(userId: String, displayName: String, updatedAt: Long)
    
    /**
     * Update user avatar
     */
    @Query("UPDATE users SET avatar = :avatar, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateAvatar(userId: String, avatar: String?, updatedAt: Long)
    
    /**
     * Update user status
     */
    @Query("UPDATE users SET status = :status, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateStatus(userId: String, status: String, updatedAt: Long)
    
    /**
     * Update user last seen
     */
    @Query("UPDATE users SET lastSeen = :lastSeen, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateLastSeen(userId: String, lastSeen: Long, updatedAt: Long)
    
    /**
     * Delete user
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
    
    /**
     * Search users by display name
     */
    @Query("SELECT * FROM users WHERE displayName LIKE '%' || :query || '%' ORDER BY displayName ASC")
    suspend fun searchUsers(query: String): List<UserEntity>
    
    /**
     * Check if user exists
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE id = :userId")
    suspend fun userExists(userId: String): Boolean
}