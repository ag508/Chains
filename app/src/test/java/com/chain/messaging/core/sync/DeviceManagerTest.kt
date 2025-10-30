package com.chain.messaging.core.sync

import android.content.Context
import android.content.SharedPreferences
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.data.local.dao.DeviceDao
import com.chain.messaging.data.local.entity.RegisteredDeviceEntity
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.ecc.Curve
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceManagerTest {
    
    private val context = mockk<Context>()
    private val deviceDao = mockk<DeviceDao>()
    private val keyManager = mockk<KeyManager>()
    private val sharedPreferences = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>()
    
    private lateinit var deviceManager: DeviceManagerImpl
    
    @Before
    fun setup() {
        every { context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs
        
        // Mock KeyManager
        val keyPair = Curve.generateKeyPair()
        val identityKeyPair = IdentityKeyPair(IdentityKey(keyPair.publicKey), keyPair.privateKey)
        every { keyManager.getIdentityKeyPair() } returns identityKeyPair
        
        deviceManager = DeviceManagerImpl(context, deviceDao, keyManager)
    }
    
    @Test
    fun `registerDevice should store device in database`() = runTest {
        // Given
        val deviceInfo = createTestDeviceInfo()
        coEvery { deviceDao.insertDevice(any()) } just Runs
        
        // When
        val result = deviceManager.registerDevice(deviceInfo)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { deviceDao.insertDevice(any()) }
    }
    
    @Test
    fun `getRegisteredDevices should return devices from database`() = runTest {
        // Given
        val deviceEntities = listOf(createTestDeviceEntity())
        coEvery { deviceDao.getAllDevices() } returns deviceEntities
        
        // When
        val devices = deviceManager.getRegisteredDevices()
        
        // Then
        assertEquals(1, devices.size)
        assertEquals("test-device-1", devices[0].deviceInfo.deviceId)
    }
    
    @Test
    fun `removeDevice should delete device from database`() = runTest {
        // Given
        val deviceId = "test-device-1"
        coEvery { deviceDao.deleteDevice(deviceId) } just Runs
        
        // When
        val result = deviceManager.removeDevice(deviceId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { deviceDao.deleteDevice(deviceId) }
    }
    
    @Test
    fun `getCurrentDevice should generate device info for first time`() = runTest {
        // Given
        every { sharedPreferences.getString("device_id", null) } returns null
        every { context.resources } returns mockk {
            every { configuration } returns mockk {
                every { screenLayout } returns 1
            }
        }
        coEvery { deviceDao.insertDevice(any()) } just Runs
        
        // When
        val device = deviceManager.getCurrentDevice()
        
        // Then
        assertTrue(device.isCurrentDevice)
        assertEquals("Android", device.platform)
        coVerify { deviceDao.insertDevice(any()) }
    }
    
    @Test
    fun `isDeviceTrusted should return trust status from database`() = runTest {
        // Given
        val deviceId = "test-device-1"
        val deviceEntity = createTestDeviceEntity().copy(isTrusted = true)
        coEvery { deviceDao.getDeviceById(deviceId) } returns deviceEntity
        
        // When
        val isTrusted = deviceManager.isDeviceTrusted(deviceId)
        
        // Then
        assertTrue(isTrusted)
    }
    
    @Test
    fun `trustDevice should update trust status in database`() = runTest {
        // Given
        val deviceId = "test-device-1"
        coEvery { deviceDao.updateDeviceTrustStatus(deviceId, true) } just Runs
        
        // When
        val result = deviceManager.trustDevice(deviceId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { deviceDao.updateDeviceTrustStatus(deviceId, true) }
    }
    
    @Test
    fun `untrustDevice should update trust status in database`() = runTest {
        // Given
        val deviceId = "test-device-1"
        coEvery { deviceDao.updateDeviceTrustStatus(deviceId, false) } just Runs
        
        // When
        val result = deviceManager.untrustDevice(deviceId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { deviceDao.updateDeviceTrustStatus(deviceId, false) }
    }
    
    @Test
    fun `observeRegisteredDevices should return flow from database`() = runTest {
        // Given
        val deviceEntities = listOf(createTestDeviceEntity())
        every { deviceDao.observeAllDevices() } returns flowOf(deviceEntities)
        
        // When
        val flow = deviceManager.observeRegisteredDevices()
        
        // Then
        flow.collect { devices ->
            assertEquals(1, devices.size)
            assertEquals("test-device-1", devices[0].deviceInfo.deviceId)
        }
    }
    
    @Test
    fun `getDevicesNeedingKeySync should return devices with pending sync`() = runTest {
        // Given
        val deviceEntities = listOf(
            createTestDeviceEntity().copy(syncStatus = "PENDING"),
            createTestDeviceEntity().copy(deviceId = "device-2", syncStatus = "ERROR")
        )
        coEvery { deviceDao.getDevicesNeedingSync() } returns deviceEntities
        
        // When
        val devices = deviceManager.getDevicesNeedingKeySync()
        
        // Then
        assertEquals(2, devices.size)
        assertTrue(devices.all { it.syncStatus == SyncStatus.PENDING || it.syncStatus == SyncStatus.ERROR })
    }
    
    @Test
    fun `updateDeviceInfo should update existing device`() = runTest {
        // Given
        val deviceInfo = createTestDeviceInfo()
        val existingEntity = createTestDeviceEntity()
        coEvery { deviceDao.getDeviceById(deviceInfo.deviceId) } returns existingEntity
        coEvery { deviceDao.updateDevice(any()) } just Runs
        
        // When
        val result = deviceManager.updateDeviceInfo(deviceInfo)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { deviceDao.updateDevice(any()) }
    }
    
    private fun createTestDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = "test-device-1",
            deviceName = "Test Device",
            deviceType = DeviceType.MOBILE,
            platform = "Android",
            platformVersion = "13",
            appVersion = "1.0.0",
            publicKey = "test-public-key",
            lastSeen = LocalDateTime.now(),
            isCurrentDevice = false
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
}