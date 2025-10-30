package com.chain.messaging.core.config

import com.chain.messaging.BuildConfig

/**
 * Application configuration constants
 */
object AppConfig {
    
    // Debug configuration
    val DEBUG = BuildConfig.DEBUG
    
    // Database configuration
    const val DATABASE_NAME = "chain_database"
    const val DATABASE_VERSION = 1
    
    // Encryption configuration
    const val ENCRYPTED_PREFS_NAME = "chain_encrypted_prefs"
    const val DB_PASSPHRASE_KEY = "db_passphrase"
    
    // Network configuration (for future blockchain integration)
    const val DEFAULT_BLOCKCHAIN_NODE_URL = "wss://chain-node.example.com"
    const val MESSAGE_PRUNING_HOURS = 48L
    const val BLOCK_INTERVAL_SECONDS = 10L
    
    // App metadata
    const val APP_VERSION = "1.0.0"
    const val MIN_ANDROID_VERSION = 26
    const val TARGET_ANDROID_VERSION = 34
    
    // Feature flags (for gradual rollout)
    const val ENABLE_BLOCKCHAIN_INTEGRATION = false
    const val ENABLE_P2P_NETWORKING = false
    const val ENABLE_WEBRTC_CALLS = false
    const val ENABLE_CLOUD_STORAGE = false
}