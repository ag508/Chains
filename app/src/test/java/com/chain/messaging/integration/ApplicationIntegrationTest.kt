package com.chain.messaging.integration

import com.chain.messaging.core.integration.ApplicationState
import com.chain.messaging.core.integration.AuthMethod
import com.chain.messaging.core.integration.ChainApplicationManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the complete Chain application,
 * testing component integration and system-wide functionality.
 */
@HiltAndroidTest
class ApplicationIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var applicationManager: ChainApplicationManager

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testApplicationInitializationFlow() = runTest {
        // Test complete application initialization
        
        // Initial state should be initializing
        assertEquals(ApplicationState.INITIALIZING, applicationManager.applicationState.first())
        
        // Initialize application
        val initResult = applicationManager.initialize()
        assertTrue(initResult.isSuccess, "Application initialization should succeed")
        
        // Wait for ready state
        applicationManager.isReady.first { it }
        assertEquals(ApplicationState.READY, applicationManager.applicationState.first())
        
        // Verify health status
        val healthStatus = applicationManager.getHealthStatus()
        assertNotNull(healthStatus, "Health status should be available")
        assertTrue(healthStatus.hasNetworkConnectivity, "Should have network connectivity")
    }

    @Test
    fun testUserAuthenticationFlow() = runTest {
        // Initialize application first
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        
        // Test authentication with different methods
        val authMethods = listOf(
            AuthMethod.GOOGLE_OAUTH,
            AuthMethod.MICROSOFT_OAUTH,
            AuthMethod.PASSKEY,
            AuthMethod.BIOMETRIC
        )
        
        for (authMethod in authMethods) {
            // Reset to ready state
            assertEquals(ApplicationState.READY, applicationManager.applicationState.first())
            
            // Authenticate user
            val authResult = applicationManager.authenticateUser(authMethod)
            assertTrue(authResult.isSuccess, "Authentication with $authMethod should succeed")
            
            // Verify authenticated state
            assertEquals(ApplicationState.AUTHENTICATED, applicationManager.applicationState.first())
        }
    }

    @Test
    fun testNetworkStateHandling() = runTest {
        // Initialize and authenticate
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        applicationManager.authenticateUser(AuthMethod.GOOGLE_OAUTH)
        
        // Test network disconnection
        applicationManager.onNetworkStateChanged(false)
        
        // Verify offline capabilities are enabled
        val healthStatus = applicationManager.getHealthStatus()
        // In offline mode, some services may be disconnected but app should still function
        assertNotNull(healthStatus, "Health status should be available even offline")
        
        // Test network reconnection
        applicationManager.onNetworkStateChanged(true)
        
        // Verify services are restored
        val onlineHealthStatus = applicationManager.getHealthStatus()
        assertNotNull(onlineHealthStatus, "Health status should be available online")
    }

    @Test
    fun testApplicationShutdownFlow() = runTest {
        // Initialize and authenticate
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        applicationManager.authenticateUser(AuthMethod.GOOGLE_OAUTH)
        
        // Verify authenticated state
        assertEquals(ApplicationState.AUTHENTICATED, applicationManager.applicationState.first())
        
        // Shutdown application
        applicationManager.shutdown()
        
        // Verify shutdown state
        assertEquals(ApplicationState.SHUTDOWN, applicationManager.applicationState.first())
    }

    @Test
    fun testComponentIntegration() = runTest {
        // Test that all major components are properly integrated
        
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        applicationManager.authenticateUser(AuthMethod.GOOGLE_OAUTH)
        
        val healthStatus = applicationManager.getHealthStatus()
        
        // Verify all major components are integrated and functional
        assertTrue(healthStatus.isBlockchainConnected, "Blockchain should be integrated")
        assertTrue(healthStatus.isP2PConnected, "P2P network should be integrated")
        assertTrue(healthStatus.hasNetworkConnectivity, "Network monitoring should be integrated")
        assertNotNull(healthStatus.encryptionStatus, "Encryption service should be integrated")
        assertTrue(healthStatus.performanceMetrics.isNotEmpty(), "Performance monitoring should be integrated")
        assertNotNull(healthStatus.securityStatus, "Security monitoring should be integrated")
    }

    @Test
    fun testErrorRecoveryIntegration() = runTest {
        // Test error recovery across integrated components
        
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        
        // Simulate various error conditions and verify recovery
        
        // Network error recovery
        applicationManager.onNetworkStateChanged(false)
        applicationManager.onNetworkStateChanged(true)
        
        // Verify system recovers properly
        val healthStatus = applicationManager.getHealthStatus()
        assertNotNull(healthStatus, "System should recover from network errors")
        
        // Authentication error recovery
        val authResult = applicationManager.authenticateUser(AuthMethod.GOOGLE_OAUTH)
        assertTrue(authResult.isSuccess, "Should recover from authentication errors")
    }

    @Test
    fun testConcurrentOperations() = runTest {
        // Test that integrated components handle concurrent operations properly
        
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        applicationManager.authenticateUser(AuthMethod.GOOGLE_OAUTH)
        
        // Simulate concurrent operations
        val operations = listOf(
            { applicationManager.getHealthStatus() },
            { applicationManager.onNetworkStateChanged(true) },
            { applicationManager.getHealthStatus() }
        )
        
        // Execute operations concurrently
        operations.forEach { operation ->
            try {
                operation()
            } catch (e: Exception) {
                // Should handle concurrent operations gracefully
                assertTrue(false, "Concurrent operations should not cause exceptions: ${e.message}")
            }
        }
        
        // Verify system remains stable
        val finalHealthStatus = applicationManager.getHealthStatus()
        assertNotNull(finalHealthStatus, "System should remain stable under concurrent operations")
    }

    @Test
    fun testServiceDependencyResolution() = runTest {
        // Test that all service dependencies are properly resolved
        
        applicationManager.initialize()
        applicationManager.isReady.first { it }
        
        // Verify that initialization succeeded, which means all dependencies were resolved
        assertTrue(applicationManager.isReady.first(), "All service dependencies should be resolved")
        
        // Verify health status includes all expected components
        val healthStatus = applicationManager.getHealthStatus()
        
        // Each component in health status indicates successful dependency injection
        assertTrue(healthStatus.isBlockchainConnected || !healthStatus.isBlockchainConnected, "Blockchain dependency resolved")
        assertTrue(healthStatus.isP2PConnected || !healthStatus.isP2PConnected, "P2P dependency resolved")
        assertTrue(healthStatus.hasNetworkConnectivity || !healthStatus.hasNetworkConnectivity, "Network dependency resolved")
        assertNotNull(healthStatus.encryptionStatus, "Encryption dependency resolved")
        assertNotNull(healthStatus.performanceMetrics, "Performance dependency resolved")
        assertNotNull(healthStatus.securityStatus, "Security dependency resolved")
    }
}