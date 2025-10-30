package com.chain.messaging.data.repository

import com.chain.messaging.data.local.dao.UserDao
import com.chain.messaging.data.local.dao.UserSettingsDao
import com.chain.messaging.data.local.entity.toDomain
import com.chain.messaging.data.local.entity.toEntity
import com.chain.messaging.domain.model.User
import com.chain.messaging.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userSettingsDao: UserSettingsDao
) : UserRepository {
    
    private var currentUserId: String? = null
    
    override suspend fun getCurrentUser(): User? {
        return try {
            currentUserId?.let { userId ->
                if (userId.isBlank()) {
                    null
                } else {
                    userDao.getUserById(userId)?.toDomain()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getUserById(userId: String): User? {
        return try {
            if (userId.isBlank()) {
                return null
            }
            userDao.getUserById(userId)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getUsersByIds(userIds: List<String>): List<User> {
        return try {
            if (userIds.isEmpty()) {
                return emptyList()
            }
            val validIds = userIds.filter { it.isNotBlank() }
            if (validIds.isEmpty()) {
                return emptyList()
            }
            userDao.getUsersByIds(validIds).mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getAllUsers(): List<User> {
        return try {
            userDao.getAllUsers().mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun searchUsers(query: String): List<User> {
        return try {
            if (query.isBlank()) {
                return emptyList()
            }
            userDao.searchUsers(query).mapNotNull { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    null // Skip invalid entities
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            if (user.id.isBlank()) {
                return Result.failure(IllegalArgumentException("User ID cannot be blank"))
            }
            if (user.displayName.isBlank()) {
                return Result.failure(IllegalArgumentException("Display name cannot be blank"))
            }
            if (user.publicKey.isBlank()) {
                return Result.failure(IllegalArgumentException("Public key cannot be blank"))
            }
            
            userDao.updateUser(user.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeCurrentUser(): Flow<User?> {
        return if (currentUserId != null) {
            userDao.observeUserById(currentUserId!!).map { entity ->
                try {
                    entity?.toDomain()
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            kotlinx.coroutines.flow.flowOf(null)
        }
    }
    
    override fun observeUserById(userId: String): Flow<User?> {
        return if (userId.isBlank()) {
            kotlinx.coroutines.flow.flowOf(null)
        } else {
            userDao.observeUserById(userId).map { entity ->
                try {
                    entity?.toDomain()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    
    override suspend fun insertUser(user: User): Result<Unit> {
        return try {
            if (user.id.isBlank()) {
                return Result.failure(IllegalArgumentException("User ID cannot be blank"))
            }
            if (user.displayName.isBlank()) {
                return Result.failure(IllegalArgumentException("Display name cannot be blank"))
            }
            if (user.publicKey.isBlank()) {
                return Result.failure(IllegalArgumentException("Public key cannot be blank"))
            }
            
            userDao.insertUser(user.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveUser(user: User): Result<Unit> {
        return try {
            if (user.id.isBlank()) {
                return Result.failure(IllegalArgumentException("User ID cannot be blank"))
            }
            if (user.displayName.isBlank()) {
                return Result.failure(IllegalArgumentException("Display name cannot be blank"))
            }
            if (user.publicKey.isBlank()) {
                return Result.failure(IllegalArgumentException("Public key cannot be blank"))
            }
            
            userDao.insertUser(user.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Set the current user ID (used during authentication)
     */
    fun setCurrentUserId(userId: String) {
        if (userId.isNotBlank()) {
            currentUserId = userId
        }
    }
    
    /**
     * Clear the current user ID (used during logout)
     */
    fun clearCurrentUserId() {
        currentUserId = null
    }
}