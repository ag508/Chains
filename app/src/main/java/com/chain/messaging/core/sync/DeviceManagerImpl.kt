package com.chain.messaging.core.sync

import android.content.Context
import android.os.Build
import com.chain.messaging.BuildConfig
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.data.local.dao.DeviceDao
import com.chain.messaging.data.local.entity.RegisteredDeviceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceManagerImpl @Inject constructor(
    private val context: Context,
    private val deviceDao: DeviceDao,
    private val keyManager: KeyManager
) : DeviceManager {
    
    private var currentDeviceInfo: DeviceInfo? = null
    
    override suspend fun registerDevice(deviceInfo: DeviceInfo): Result<Unit> {
        return try {
            val registeredDevice = RegisteredDevice(
                deviceInfo = deviceInfo,
                registeredAt = LocalDateTime.now(),
                isTrusted = deviceInfo.isCurrentDevice, // Trust current device by default
                syncStatus = if (deviceInfo.isCurrentDevice) SyncStatus.SYNCED else SyncStatus.PENDING
            )
            
            deviceDao.insertDevice(registeredDevice.toEntity())
            
            if (deviceInfo.isCurrentDevice) {
                currentDeviceInfo = deviceInfo
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRegisteredDevices(): List<RegisteredDevice> {
        return try {
            deviceDao.getAllDevices().map { it.toRegisteredDevice() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun removeDevice(deviceId: String): Result<Unit> {
        return try {
            deviceDao.deleteDevice(deviceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentDevice(): DeviceInfo {
        return currentDeviceInfo ?: generateCurrentDeviceInfo().also {
            currentDeviceInfo = it
            // Auto-register current device
            registerDevice(it)
        }
    }
    
    override suspend fun updateDeviceInfo(deviceInfo: DeviceInfo): Result<Unit> {
        return try {
            val existingDevice = deviceDao.getDeviceById(deviceInfo.deviceId)
            if (existingDevice != null) {
                val updatedDevice = existingDevice.toRegisteredDevice().copy(
                    deviceInfo = deviceInfo
                )
                deviceDao.updateDevice(updatedDevice.toEntity())
            }
            
            if (deviceInfo.isCurrentDevice) {
                currentDeviceInfo = deviceInfo
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isDeviceTrusted(deviceId: String): Boolean {
        return try {
            deviceDao.getDeviceById(deviceId)?.isTrusted ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun trustDevice(deviceId: String): Result<Unit> {
        return try {
            deviceDao.updateDeviceTrustStatus(deviceId, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun untrustDevice(deviceId: String): Result<Unit> {
        return try {
            deviceDao.updateDeviceTrustStatus(deviceId, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeRegisteredDevices(): Flow<List<RegisteredDevice>> {
        return deviceDao.observeAllDevices().map { entities ->
            entities.map { it.toRegisteredDevice() }
        }
    }
    
    override suspend fun getDevicesNeedingKeySync(): List<RegisteredDevice> {
        return try {
            deviceDao.getDevicesNeedingSync().map { it.toRegisteredDevice() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun generateCurrentDeviceInfo(): DeviceInfo {
        val deviceId = getOrCreateDeviceId()
        val publicKey = keyManager.getIdentityKeyPair().publicKey.serialize().toString(Charsets.UTF_8)
        
        return DeviceInfo(
            deviceId = deviceId,
            deviceName = getDeviceName(),
            deviceType = getDeviceType(),
            platform = "Android",
            platformVersion = Build.VERSION.RELEASE,
            appVersion = BuildConfig.VERSION_NAME,
            publicKey = publicKey,
            lastSeen = LocalDateTime.now(),
            isCurrentDevice = true
        )
    }
    
    private fun getOrCreateDeviceId(): String {
        val prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", null)
        
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        
        return deviceId
    }
    
    private fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
    
    private fun getDeviceType(): DeviceType {
        return when {
            isTablet() -> DeviceType.TABLET
            else -> DeviceType.MOBILE
        }
    }
    
    private fun isTablet(): Boolean {
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
        return screenLayout >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }
}

// Extension functions for entity conversion
private fun RegisteredDevice.toEntity(): RegisteredDeviceEntity {
    return RegisteredDeviceEntity(
        deviceId = deviceInfo.deviceId,
        deviceName = deviceInfo.deviceName,
        deviceType = deviceInfo.deviceType.name,
        platform = deviceInfo.platform,
        platformVersion = deviceInfo.platformVersion,
        appVersion = deviceInfo.appVersion,
        publicKey = deviceInfo.publicKey,
        lastSeen = deviceInfo.lastSeen,
        registeredAt = registeredAt,
        isTrusted = isTrusted,
        lastSyncAt = lastSyncAt,
        syncStatus = syncStatus.name,
        isCurrentDevice = deviceInfo.isCurrentDevice
    )
}

private fun RegisteredDeviceEntity.toRegisteredDevice(): RegisteredDevice {
    return RegisteredDevice(
        deviceInfo = DeviceInfo(
            deviceId = deviceId,
            deviceName = deviceName,
            deviceType = enumValueOf(deviceType),
            platform = platform,
            platformVersion = platformVersion,
            appVersion = appVersion,
            publicKey = publicKey,
            lastSeen = lastSeen,
            isCurrentDevice = isCurrentDevice
        ),
        registeredAt = registeredAt,
        isTrusted = isTrusted,
        lastSyncAt = lastSyncAt,
        syncStatus = enumValueOf(syncStatus)
    )
}