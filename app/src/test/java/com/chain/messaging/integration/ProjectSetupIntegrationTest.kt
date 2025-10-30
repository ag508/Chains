package com.chain.messaging.integration

import com.chain.messaging.core.config.AppConfig
import com.chain.messaging.core.util.Logger
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration test to verify project setup is complete and working
 */
class ProjectSetupIntegrationTest {
    
    @Test
    fun appConfig_hasCorrectValues() {
        assertEquals("chain_database", AppConfig.DATABASE_NAME)
        assertEquals(1, AppConfig.DATABASE_VERSION)
        assertEquals("chain_encrypted_prefs", AppConfig.ENCRYPTED_PREFS_NAME)
        assertEquals("1.0.0", AppConfig.APP_VERSION)
        assertEquals(24, AppConfig.MIN_ANDROID_VERSION)
        assertEquals(34, AppConfig.TARGET_ANDROID_VERSION)
    }
    
    @Test
    fun logger_canLogMessages() {
        // This test verifies that the Logger utility works without throwing exceptions
        Logger.d("Debug message")
        Logger.i("Info message")
        Logger.w("Warning message")
        Logger.e("Error message")
        Logger.v("Verbose message")
        
        // If we reach here, logging is working
        assertTrue(true)
    }
    
    @Test
    fun featureFlags_areSetCorrectly() {
        // Verify that feature flags are initially disabled for gradual rollout
        assertFalse("Blockchain integration should be disabled initially", AppConfig.ENABLE_BLOCKCHAIN_INTEGRATION)
        assertFalse("P2P networking should be disabled initially", AppConfig.ENABLE_P2P_NETWORKING)
        assertFalse("WebRTC calls should be disabled initially", AppConfig.ENABLE_WEBRTC_CALLS)
        assertFalse("Cloud storage should be disabled initially", AppConfig.ENABLE_CLOUD_STORAGE)
    }
    
    @Test
    fun networkConfig_hasReasonableDefaults() {
        assertEquals(48L, AppConfig.MESSAGE_PRUNING_HOURS)
        assertEquals(10L, AppConfig.BLOCK_INTERVAL_SECONDS)
        assertNotNull(AppConfig.DEFAULT_BLOCKCHAIN_NODE_URL)
        assertTrue(AppConfig.DEFAULT_BLOCKCHAIN_NODE_URL.startsWith("wss://"))
    }
}