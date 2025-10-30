package com.chain.messaging.core.sync

import android.content.Context
import android.content.SharedPreferences
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.data.local.dao.DeviceDao
import com.chain.messaging.data.local.dao.MessageDao
import com.chain.messaging.data.local.dao.SyncLogDao
import com.chain.messaging.data.local.entity.MessageEntity
import com.chain.messaging.data.local.entity.RegisteredDeviceEntity
import com.chain.messaging.data.local.entity.SyncLogEntity
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDeviceSyncServiceTest {
    
    private val context = mockk<Context>()
    private val deviceManager = mockk<DeviceManager>()
    private val deviceDao = mockk<DeviceDao>()
    private val syncLogDao = mockk<SyncLogDao>()
    private val messageDao = mockk<MessageDao>()
    private val messageRepository = mockk<MessageRepository>()
    private val keyManager = mockk<KeyManager>()
    private val blockchainManager = mockk<BlockchainManager>()
    private val sharedPreferences = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>()
    
    private lateinit var syncService: CrossDeviceSyncServiceImpl
    
    @Before
    fun setup() {
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.getBoolean(any(), any()) } returns true
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } just Runs
        every { sharedPreferences.all } returns emptyMap()
        
        syncService = CrossDeviceSyncServiceImpl(
            context,
            deviceManager,
            deviceDao,
            syncLogDao,
            messageDao,
            messageRepository,
            keyManager,
            blockchainManager
        )
    }
    
    @Test
    fun `initialize should set up sync service`() = runTest {
        // When
        syncService.initialize()
        
        // Then
        assertTrue(syncService.isAutoSyncEnabled())
    }
    
    @Test
    fun `syncMessageHistory should sync messages with trusted devices`() = runTest {
        // Given
        val currentDevice = createTestDeviceInfo()
        val trustedDevices = listOf(createTestDeviceEntity().copy(isTrusted = true))
        val messages = listOf(createTestMessageEntity())
        
        coEvery { deviceManager.getCurrentDevice() } returns currentDevice
        coEvery { deviceDao.getTrustedDevices() } returns trustedDevices
        coEvery { messageDao.getRecentMessages(any()) } returns messages
        coEvery { syncLogDao.insertSyncLog(any()) } just Runs
        coEvery { syncLogDao.updateSyncLog(any()) } just Runs
        coEvery { syncLogDao.getRecentSyncLogs(any()) } returns emptyList()
        every { keyManager.exportKeys() } returns emptyMap()
        
        // When
        val result = syncService.syncMessageHistory()
        
        // Then
        assertTrue(result is SyncResult.Success)
        coVerify { syncLogDao.insertSyncLog(any()) }
        coVerify { syncLogDao.updateSyncLog(any()) }
    }
    
    @Test
    fun `syncEncryptionKeys should sync keys with trusted devices`() = runTest {
        // Given
        val currentDevice = createTestDeviceInfo()
        val trustedDevices = listOf(createTestDeviceEntity().copy(isTrusted = true))
        val keys = mapOf("key1" to "value1", "key2" to "value2")
        
        coEvery { deviceManager.getCurrentDevice() } returns currentDevice
        coEvery { deviceDao.getTrustedDevices() } returns trustedDevices
        every { keyManager.exportKeys() } returns keys
        coEvery { syncLogDao.insertSyncLog(any()) } just Runs
        coEvery { syncLogDao.updateSyncLog(any()) } just Runs
        coEvery { syncLogDao.getRecentSyncLogs(any()) } returns emptyList()
        
        // When
        val result = syncService.syncEncryptionKeys()
        
        // Then
        assertTrue(result is SyncResult.Success)
        coVerify { syncLogDao.insertSyncLog(any()) }
    }
    
    @Test
    fun `sendSyncRequest should send request through blockchain and wait for response`() = runTest {
        // Given
        val deviceId = "device123"
        val request = SyncRequest(
            requestId = "req123",
            fromDeviceId = "currentDevice",
            syncType = SyncType.MESSAGES_ONLY,
            timestamp = LocalDateTime.now()
        )
        
        val targetDevice = createMockDevice(deviceId, "Test Device")
        val currentDevice = createMockDevice("currentDevice", "Current Device")
        
        coEvery { deviceDao.getDeviceById(deviceId) } returns targetDevice
        coEvery { deviceManager.getCurrentDevice() } returns DeviceInfo(
            deviceId = "currentDevice",
            deviceName = "Current Device",
            deviceType = DeviceType.MOBILE,
            platform = "Android",
            platformVersion = "13",
            appVersion = "1.0.0",
            publicKey = "publicKey123"
        )
        coEvery { blockchainManager.sendMessage(any(), any(), any()) } returns "txHash123"
        coEvery { blockchainManager.subscribeToMessages(any()) } returns flowOf()
        coEvery { deviceDao.updateDevice(any()) } just Runs
        
        // When
        val result = syncService.requestSyncFromDevice(deviceId)
        
        // Then
        assertTrue(result is SyncResult.Error) // Should timeout since no response
        coVerify { blockchainManager.sendMessage(deviceId, any(), "SYNC_REQUEST") }
    }

    @Test
    fun `syncUserSettings should sync settings with trusted devices`() = runTest {
        // Given
        val currentDevice = createTestDeviceInfo()
        val trustedDevices = listOf(createTestDeviceEntity().copy(isTrusted = true))
        
        coEvery { deviceManager.getCurrentDevice() } returns currentDevice
        coEvery { deviceDao.getTrustedDevices() } returns trustedDevices
        coEvery { syncLogDao.insertSyncLog(any()) } just Runs
        coEvery { syncLogDao.updateSyncLog(any()) } just Runs
        coEvery { syncLogDao.getRecentSyncLogs(any()) } returns emptyList()
        
        // When
        val result = syncService.syncUserSettings()
        
        // Then
        assertTrue(result is SyncResult.Success)
        coVerify { syncLogDao.insertSyncLog(any()) }
    }
    
    @Test
    fun `performFullSync should sync all data types`() = runTest {
        // Given
        val currentDevice = createTestDeviceInfo()
        val trustedDevices = listOf(createTestDeviceEntity().copy(isTrusted = true))
        val messages = listOf(createTestMessageEntity())
        val keys = mapOf("key1" to "value1")
        
        coEvery { deviceManager.getCurrentDevice() } returns currentDevice
        coEvery { deviceDao.getTrustedDevices() } returns trustedDevices
        coEvery { messageDao.getRecentMessages(any()) } returns messages
        every { keyManager.exportKeys() } returns keys
        coEvery { syncLogDao.insertSyncLog(any()) } just Runs
        coEvery { syncLogDao.updateSyncLog(any()) } just Runs
        coEvery { syncLogDao.getRecentSyncLogs(any()) } returns emptyList()
        
        // When
        val result = syncService.performFullSync()
        
        // Then
        assertTrue(result is SyncResult.Success)
        coVerify(exactly = 4) { syncLogDao.insertSyncLog(any()) } // One for full sync + 3 for sub-syncs
    }
    
    @Test
    fun `requestSyncFromDevice should handle sync request`() = runTest {
        // Given
        val deviceId = "test-device-1"
        val device = createTestDeviceEntity().copy(isTrusted = true)
        
        coEvery { deviceDao.getDeviceById(deviceId) } returns device
        coEvery { deviceManager.getCurrentDevice() } returns createTestDeviceInfo()
        
        // When
        val result = syncService.requestSyncFromDevice(deviceId)
        
        // Then
        // Result depends on implementation of sendSyncRequest which is mocked
        assertTrue(result is SyncResult.Success)
    }
    
    @Test
    fun `handleSyncRequest should process sync request from trusted device`() = runTest {
        // Given
        val request = SyncRequest(
            requestId = "request-1",
            fromDeviceId = "device-1",
            syncType = SyncType.FULL,
            timestamp = LocalDateTime.now()
        )
        val device = createTestDeviceEntity().copy(isTrusted = true)
        
        coEvery { deviceDao.getDeviceById(request.fromDeviceId) } returns device
        coEvery { messageDao.getAllMessages() } returns emptyList()
        every { keyManager.exportKeys() } returns emptyMap()
        
        // When
        val response = syncService.handleSyncRequest(request)
        
        // Then
        assertTrue(response.success)
        assertEquals(request.requestId, response.requestId)
    }
    
    @Test
    fun `handleSyncRequest should reject request from untrusted device`() = runTest {
        // Given
        val request = SyncRequest(
            requestId = "request-1",
            fromDeviceId = "device-1",
            syncType = SyncType.FULL,
            timestamp = LocalDateTime.now()
        )
        val device = createTestDeviceEntity().copy(isTrusted = false)
        
        coEvery { deviceDao.getDeviceById(request.fromDeviceId) } returns device
        
        // When
        val response = syncService.handleSyncRequest(request)
        
        // Then
        assertTrue(!response.success)
        assertEquals("Device not trusted", response.error)
    }
    
    @Test
    fun `getSyncStatus should return current sync status`() = runTest {
        // Given
        coEvery { deviceDao.getTrustedDeviceCount() } returns 2
        coEvery { deviceDao.getPendingSyncCount() } returns 1
        coEvery { syncLogDao.getLastSuccessfulSync(any()) } returns null
        coEvery { syncLogDao.getFailedSyncs() } returns emptyList()
        coEvery { deviceManager.getCurrentDevice() } returns createTestDeviceInfo()
        
        // When
        syncService.initialize()
        
        // Then
        syncService.getSyncStatus().collect { status ->
            assertEquals(2, status.trustedDevices)
            assertEquals(1, status.pendingSyncs)
            assertTrue(status.isEnabled)
        }
    }
    
    @Test
    fun `setAutoSyncEnabled should update auto sync preference`() = runTest {
        // When
        syncService.setAutoSyncEnabled(false)
        
        // Then
        assertTrue(!syncService.isAutoSyncEnabled())
        verify { editor.putBoolean("auto_sync_enabled", false) }
    }
    
    private fun createTestDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = "current-device",
            deviceName = "Current Device",
            deviceType = DeviceType.MOBILE,
            platform = "Android",
            platformVersion = "13",
            appVersion = "1.0.0",
            publicKey = "current-public-key",
            lastSeen = LocalDateTime.now(),
            isCurrentDevice = true
        )
    }
    
    private fun createTestDeviceEntity(): RegisteredDeviceEntity {
        return RegisteredDeviceEntity(
            deviceId = "test-device-1",
            deviceName = "Test Device",
            deviceType = "MOBILE",
            platform = "Android",
            platformVersion = "13",
            appVersion = "1.0.0",
            publicKey = "test-public-key",
            lastSeen = LocalDateTime.now(),
            registeredAt = LocalDateTime.now(),
            isTrusted = false,
            lastSyncAt = null,
            syncStatus = "PENDING",
            isCurrentDevice = false
        )
    }
    
    private fun createTestMessageEntity(): MessageEntity {
        return MessageEntity(
            id = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            content = "Test message",
            type = "TEXT",
            timestamp = LocalDateTime.now(),
            status = "SENT",
            replyToId = null,
            isDisappearing = false,
            expiresAt = null
        )
    }
}