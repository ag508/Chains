package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.RegisteredDeviceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for device management
 */
@Dao
interface DeviceDao {
    
    @Query("SELECT * FROM registered_devices ORDER BY lastSeen DESC")
    suspend fun getAllDevices(): List<RegisteredDeviceEntity>
    
    @Query("SELECT * FROM registered_devices ORDER BY lastSeen DESC")
    fun observeAllDevices(): Flow<List<RegisteredDeviceEntity>>
    
    @Query("SELECT * FROM registered_devices WHERE deviceId = :deviceId")
    suspend fun getDeviceById(deviceId: String): RegisteredDeviceEntity?
    
    @Query("SELECT * FROM registered_devices WHERE isTrusted = 1")
    suspend fun getTrustedDevices(): List<RegisteredDeviceEntity>
    
    @Query("SELECT * FROM registered_devices WHERE syncStatus = 'PENDING' OR syncStatus = 'ERROR'")
    suspend fun getDevicesNeedingSync(): List<RegisteredDeviceEntity>
    
    @Query("SELECT * FROM registered_devices WHERE isCurrentDevice = 1 LIMIT 1")
    suspend fun getCurrentDevice(): RegisteredDeviceEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: RegisteredDeviceEntity)
    
    @Update
    suspend fun updateDevice(device: RegisteredDeviceEntity)
    
    @Query("UPDATE registered_devices SET isTrusted = :trusted WHERE deviceId = :deviceId")
    suspend fun updateDeviceTrustStatus(deviceId: String, trusted: Boolean)
    
    @Query("UPDATE registered_devices SET syncStatus = :status, lastSyncAt = :syncTime WHERE deviceId = :deviceId")
    suspend fun updateSyncStatus(deviceId: String, status: String, syncTime: LocalDateTime)
    
    @Query("UPDATE registered_devices SET lastSeen = :lastSeen WHERE deviceId = :deviceId")
    suspend fun updateLastSeen(deviceId: String, lastSeen: LocalDateTime)
    
    @Query("DELETE FROM registered_devices WHERE deviceId = :deviceId")
    suspend fun deleteDevice(deviceId: String)
    
    @Query("DELETE FROM registered_devices WHERE isTrusted = 0 AND lastSeen < :cutoffTime")
    suspend fun deleteUntrustedOldDevices(cutoffTime: LocalDateTime)
    
    @Query("SELECT COUNT(*) FROM registered_devices WHERE isTrusted = 1")
    suspend fun getTrustedDeviceCount(): Int
    
    @Query("SELECT COUNT(*) FROM registered_devices WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncCount(): Int
}