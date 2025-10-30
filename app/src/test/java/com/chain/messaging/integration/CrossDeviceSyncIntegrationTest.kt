package com.chain.messaging.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.core.sync.*
import com.chain.messaging.data.local.ChainDatabase
import com.chain.messaging.data.local.Converters
import com.chain.messaging.data.local.entity.MessageEntity
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.ecc.Curve
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration test for cross-device synchronization functionality
 */
class CrossDeviceSyncIntegrationTest {
    
    private lateinit var database: ChainDatabase
    private lateinit var context: Context
    private lateinit var deviceManager: DeviceManager
    private lateinit var syncService: CrossDeviceSyncService
    private lateinit var keyManager: KeyManager
    private lateinit var messageRepository: MessageRepository
    private lateinit var blockchainManager: BlockchainManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            ChainDatabase::class.java
        )
            .addTypeConverter(Converters())
            .allowMainThreadQueries()
            .build()
        
        // Mock dependencies
        keyManager = mockk()
        messageRepository = mockk()
        blockchainManager = mockk()
        
        // Setup KeyManager mock
        val keyPair = Curve.generateKeyPair()
        val identityKeyPair = IdentityKeyPair(IdentityKey(keyPair.publicKey), keyPair.privateKey)
        every { keyManager.getIdentityKeyPair() } returns identityKeyPair
        every { keyManager.exportKeys() } returns mapOf("test-key" to "test-value")
        every { keyManager.exportRecentKeys(any()) } returns mapOf("recent-key" to "recent-value")
        every { keyManager.importKey(any(), any()) } returns Unit
        
        // Setup MessageRepository mock
        coEvery { messageRepository.saveMessage(any()) } returns Unit
        
        // Create real implementations
        deviceManager = DeviceManagerImpl(context, database.deviceDao(), keyManager)
        syncService = CrossDeviceSyncServiceImpl(
            context,
            deviceManager,
            database.deviceDao(),
            database.syncLogDao(),
            database.messageDao(),
            messageRepository,
            keyManager,
            blockchainManager
        )
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun `should register and manage multiple devices`() = runTest {
        // Given
        val device1 = createTestDevice("device-1", "Phone", DeviceType.MOBILE, true)
        val device2 = createTestDevice("device-2", "Tablet", DeviceType.TABLET, false)
        val device3 = createTestDevice("device-3", "Desktop", DeviceType.DESKTOP, false)
        
        // When - Register devices
        deviceManager.registerDevice(device1)
        deviceManager.registerDevice(device2)
        deviceManager.registerDevice(device3)
        
        // Then - All devices should be registered
        val registeredDevices = deviceManager.getRegisteredDevices()
        assertEquals(3, registeredDevices.size)
        
        // Current device should be trusted by default
        val currentDevice = registeredDevices.find { it.deviceInfo.isCurrentDevice }
        assertTrue(currentDevice?.isTrusted == true)
        
        // Other devices should not be trusted initially
        val otherDevices = registeredDevices.filter { !it.deviceInfo.isCurrentDevice }
        assertTrue(otherDevices.all { !it.isTrusted })
    }
    
    @Test
    fun `should trust device and enable synchronization`() = runTest {
        // Given
        val currentDevice = createTestDevice("current", "Current Phone", DeviceType.MOBILE, true)
        val otherDevice = createTestDevice("other", "Other Phone", DeviceType.MOBILE, false)
        
        deviceManager.registerDevice(currentDevice)
        deviceManager.registerDevice(otherDevice)
        
        // When - Trust the other device
        val trustResult = deviceManager.trustDevice("other")
        
        // Then - Device should be trusted
        assertTrue(trustResult.isSuccess)
        assertTrue(deviceManager.isDeviceTrusted("other"))
        
        // Should appear in devices needing sync
        val devicesNeedingSync = deviceManager.getDevicesNeedingKeySync()
        assertTrue(devicesNeedingSync.any { it.deviceInfo.deviceId == "other" })
    }
    
    @Test
    fun `should perform full synchronization between trusted devices`() = runTest {
        // Given
        val currentDevice = createTestDevice("current", "Current Phone", DeviceType.MOBILE, true)
        val trustedDevice = createTestDevice("trusted", "Trusted Tablet", DeviceType.TABLET, false)
        
        deviceManager.registerDevice(currentDevice)
        deviceManager.registerDevice(trustedDevice)
        deviceManager.trustDevice("trusted")
        
        // Add some test messages
        val testMessage = MessageEntity(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            content = "Test message for sync",
            type = "TEXT",
            timestamp = LocalDateTime.now(),
            status = "SENT",
            replyToId = null,
            isDisappearing = false,
            expiresAt = null
        )
        database.messageDao().insertMessage(testMessage)
        
        // Initialize sync service
        syncService.initialize()
        
        // When - Perform full sync
        val syncResult = syncService.performFullSync()
        
        // Then - Sync should complete successfully
        assertTrue(syncResult is SyncResult.Success || syncResult is SyncResult.PartialSuccess)
        
        // Verify sync logs were created
        val syncLogs = database.syncLogDao().getRecentSyncLogs(10)
        assertTrue(syncLogs.isNotEmpty())
        
        // Verify sync status is updated
        val syncStatus = syncService.getSyncStatus().first()
        assertEquals(1, syncStatus.trustedDevices)
        assertTrue(syncStatus.isEnabled)
    }
    
    @Test
    fun `should handle sync request from trusted device`() = runTest {
        // Given
        val currentDevice = createTestDevice("current", "Current Phone", DeviceType.MOBILE, true)
        val trustedDevice = createTestDevice("trusted", "Trusted Device", DeviceType.DESKTOP, false)
        
        deviceManager.registerDevice(currentDevice)
        deviceManager.registerDevice(trustedDevice)
        deviceManager.trustDevice("trusted")
        
        // Add test data
        val testMessage = MessageEntity(
            id = "msg-sync-1",
            chatId = "chat-sync-1",
            senderId = "user-sync-1",
            content = "Message to sync",
            type = "TEXT",
            timestamp = LocalDateTime.now(),
            status = "SENT",
            replyToId = null,
            isDisappearing = false,
            expiresAt = null
        )
        database.messageDao().insertMessage(testMessage)
        
        syncService.initialize()
        
        // When - Handle sync request
        val syncRequest = SyncRequest(
            requestId = "req-1",
            fromDeviceId = "trusted",
            syncType = SyncType.FULL,
            timestamp = LocalDateTime.now()
        )
        
        val response = syncService.handleSyncRequest(syncRequest)
        
        // Then - Request should be handled successfully
        assertTrue(response.success)
        assertEquals("req-1", response.requestId)
        assertTrue(response.data != null)
    }
    
    @Test
    fun `should reject sync request from untrusted device`() = runTest {
        // Given
        val currentDevice = createTestDevice("current", "Current Phone", DeviceType.MOBILE, true)
        val untrustedDevice = createTestDevice("untrusted", "Untrusted Device", DeviceType.DESKTOP, false)
        
        deviceManager.registerDevice(currentDevice)
        deviceManager.registerDevice(untrustedDevice)
        // Note: Not trusting the device
        
        syncService.initialize()
        
        // When - Handle sync request from untrusted device
        val syncRequest = SyncRequest(
            requestId = "req-untrusted",
            fromDeviceId = "untrusted",
            syncType = SyncType.FULL,
            timestamp = LocalDateTime.now()
        )
        
        val response = syncService.handleSyncRequest(syncRequest)
        
        // Then - Request should be rejected
        assertTrue(!response.success)
        assertEquals("Device not trusted", response.error)
    }
    
    @Test
    fun `should observe device changes and sync status`() = runTest {
        // Given
        syncService.initialize()
        
        // When - Register devices and observe changes
        val device1 = createTestDevice("obs-1", "Observable Device 1", DeviceType.MOBILE, true)
        val device2 = createTestDevice("obs-2", "Observable Device 2", DeviceType.TABLET, false)
        
        deviceManager.registerDevice(device1)
        
        // Then - Should observe the registered device
        val devicesFlow = deviceManager.observeRegisteredDevices()
        devicesFlow.first().let { devices ->
            assertEquals(1, devices.size)
            assertEquals("obs-1", devices[0].deviceInfo.deviceId)
        }
        
        // When - Register another device
        deviceManager.registerDevice(device2)
        
        // Then - Should observe both devices
        devicesFlow.first().let { devices ->
            assertEquals(2, devices.size)
        }
        
        // When - Trust the second device
        deviceManager.trustDevice("obs-2")
        
        // Then - Sync status should reflect trusted device
        val syncStatus = syncService.getSyncStatus().first()
        assertEquals(1, syncStatus.trustedDevices)
    }
    
    @Test
    fun `should handle auto sync enable and disable`() = runTest {
        // Given
        syncService.initialize()
        
        // When - Disable auto sync
        syncService.setAutoSyncEnabled(false)
        
        // Then - Auto sync should be disabled
        assertTrue(!syncService.isAutoSyncEnabled())
        
        val syncStatus = syncService.getSyncStatus().first()
        assertTrue(!syncStatus.isEnabled)
        
        // When - Re-enable auto sync
        syncService.setAutoSyncEnabled(true)
        
        // Then - Auto sync should be enabled
        assertTrue(syncService.isAutoSyncEnabled())
    }
    
    @Test
    fun `should sync different data types independently`() = runTest {
        // Given
        val currentDevice = createTestDevice("current", "Current Device", DeviceType.MOBILE, true)
        val trustedDevice = createTestDevice("trusted", "Trusted Device", DeviceType.TABLET, false)
        
        deviceManager.registerDevice(currentDevice)
        deviceManager.registerDevice(trustedDevice)
        deviceManager.trustDevice("trusted")
        
        syncService.initialize()
        
        // When - Sync messages only
        val messageResult = syncService.syncMessageHistory()
        
        // Then - Message sync should succeed
        assertTrue(messageResult is SyncResult.Success)
        
        // When - Sync keys only
        val keyResult = syncService.syncEncryptionKeys()
        
        // Then - Key sync should succeed
        assertTrue(keyResult is SyncResult.Success)
        
        // When - Sync settings only
        val settingsResult = syncService.syncUserSettings()
        
        // Then - Settings sync should succeed
        assertTrue(settingsResult is SyncResult.Success)
        
        // Verify separate sync logs were created
        val syncLogs = database.syncLogDao().getRecentSyncLogs(10)
        assertTrue(syncLogs.size >= 3) // At least one for each sync type
    }
    
    private fun createTestDevice(
        deviceId: String,
        deviceName: String,
        deviceType: DeviceType,
        isCurrentDevice: Boolean
    ): DeviceInfo {
        return DeviceInfo(
            deviceId = deviceId,
            deviceName = deviceName,
            deviceType = deviceType,
            platform = "Android",
            platformVersion = "13",
            appVersion = "1.0.0",
            publicKey = "public-key-$deviceId",
            lastSeen = LocalDateTime.now(),
            isCurrentDevice = isCurrentDevice
        )
    }
}