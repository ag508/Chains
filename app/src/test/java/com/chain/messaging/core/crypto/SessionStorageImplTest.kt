package com.chain.messaging.core.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.state.SessionRecord

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SessionStorageImplTest {

    private lateinit var context: Context
    private lateinit var sessionStorage: SessionStorageImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sessionStorage = SessionStorageImpl(context)
    }

    @After
    fun tearDown() {
        // Clean up test data by clearing all sessions
        sessionStorage.deleteAllSessions("testuser")
    }

    @Test
    fun `loadSession should return fresh session for new address`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)

        // When
        val session = sessionStorage.loadSession(address)

        // Then
        assertNotNull("Session should not be null", session)
        assertFalse("Fresh session should not have current session", 
            session.hasCurrentSession())
    }

    @Test
    fun `storeSession and loadSession should work correctly`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val originalSession = SessionRecord()
        
        // Simulate session establishment (this would normally be done by Signal Protocol)
        // For testing, we'll just store and retrieve the session

        // When
        sessionStorage.storeSession(address, originalSession)
        val loadedSession = sessionStorage.loadSession(address)

        // Then
        assertNotNull("Loaded session should not be null", loadedSession)
        // Note: We can't directly compare SessionRecord objects, but we can verify
        // that the session was stored and retrieved without error
    }

    @Test
    fun `containsSession should return correct status`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val session = SessionRecord()

        // Initially should not contain session
        assertFalse("Should not contain session initially", 
            sessionStorage.containsSession(address))

        // When
        sessionStorage.storeSession(address, session)

        // Then
        assertTrue("Should contain session after storing", 
            sessionStorage.containsSession(address))
    }

    @Test
    fun `deleteSession should remove session`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val session = SessionRecord()
        sessionStorage.storeSession(address, session)

        // Verify session exists
        assertTrue("Session should exist before deletion", 
            sessionStorage.containsSession(address))

        // When
        sessionStorage.deleteSession(address)

        // Then
        assertFalse("Session should not exist after deletion", 
            sessionStorage.containsSession(address))
    }

    @Test
    fun `getSubDeviceSessions should return correct device IDs`() {
        // Given
        val username = "testuser"
        val deviceIds = listOf(1, 2, 3)
        
        // Store sessions for multiple devices
        deviceIds.forEach { deviceId ->
            val address = SignalProtocolAddress(username, deviceId)
            val session = SessionRecord()
            sessionStorage.storeSession(address, session)
        }

        // When
        val subDeviceSessions = sessionStorage.getSubDeviceSessions(username)

        // Then
        assertEquals("Should return correct number of device sessions", 
            deviceIds.size, subDeviceSessions.size)
        
        deviceIds.forEach { deviceId ->
            assertTrue("Should contain device ID $deviceId", 
                subDeviceSessions.contains(deviceId))
        }
    }

    @Test
    fun `deleteAllSessions should remove all sessions for user`() {
        // Given
        val username = "testuser"
        val deviceIds = listOf(1, 2, 3)
        
        // Store sessions for multiple devices
        deviceIds.forEach { deviceId ->
            val address = SignalProtocolAddress(username, deviceId)
            val session = SessionRecord()
            sessionStorage.storeSession(address, session)
        }

        // Verify sessions exist
        deviceIds.forEach { deviceId ->
            val address = SignalProtocolAddress(username, deviceId)
            assertTrue("Session should exist for device $deviceId", 
                sessionStorage.containsSession(address))
        }

        // When
        sessionStorage.deleteAllSessions(username)

        // Then
        deviceIds.forEach { deviceId ->
            val address = SignalProtocolAddress(username, deviceId)
            assertFalse("Session should not exist for device $deviceId after deletion", 
                sessionStorage.containsSession(address))
        }
        
        assertTrue("Should have no sub-device sessions", 
            sessionStorage.getSubDeviceSessions(username).isEmpty())
    }

    @Test
    fun `sessions should be isolated between different users`() {
        // Given
        val user1 = "user1"
        val user2 = "user2"
        val deviceId = 1
        
        val address1 = SignalProtocolAddress(user1, deviceId)
        val address2 = SignalProtocolAddress(user2, deviceId)
        
        val session1 = SessionRecord()
        val session2 = SessionRecord()

        // When
        sessionStorage.storeSession(address1, session1)
        sessionStorage.storeSession(address2, session2)

        // Then
        assertTrue("User1 should have session", sessionStorage.containsSession(address1))
        assertTrue("User2 should have session", sessionStorage.containsSession(address2))

        // When deleting user1 sessions
        sessionStorage.deleteAllSessions(user1)

        // Then
        assertFalse("User1 should not have session after deletion", 
            sessionStorage.containsSession(address1))
        assertTrue("User2 should still have session", 
            sessionStorage.containsSession(address2))
    }
}