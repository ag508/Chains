package com.chain.messaging.core.security

/**
 * Interface for secure storage of sensitive data
 */
interface SecureStorage {
    
    /**
     * Store a value securely
     */
    suspend fun store(key: String, value: String)
    
    /**
     * Retrieve a value from secure storage
     */
    suspend fun get(key: String): String?
    
    /**
     * Remove a value from secure storage
     */
    suspend fun remove(key: String)
    
    /**
     * Check if a key exists in secure storage
     */
    suspend fun contains(key: String): Boolean
    
    /**
     * Clear all stored values
     */
    suspend fun clear()
}