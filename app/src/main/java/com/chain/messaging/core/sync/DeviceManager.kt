package com.chain.messaging.core.sync

import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing user devices and cross-device synchronization
 */
interface DeviceManager {
    /**
     * Register this device with the user's account
     */
    suspend fun registerDevice(deviceInfo: DeviceInfo): Result<Unit>
    
    /**
     * Get all registered devices for the current user
     */
    suspend fun getRegisteredDevices(): List<RegisteredDevice>
    
    /**
     * Remove a device from the user's account
     */
    suspend fun removeDevice(deviceId: String): Result<Unit>
    
    /**
     * Get the current device information
     */
    suspend fun getCurrentDevice(): DeviceInfo
    
    /**
     * Update device information (last seen, name, etc.)
     */
    suspend fun updateDeviceInfo(deviceInfo: DeviceInfo): Result<Unit>
    
    /**
     * Check if a device is trusted
     */
    suspend fun isDeviceTrusted(deviceId: String): Boolean
    
    /**
     * Trust a device for synchronization
     */
    suspend fun trustDevice(deviceId: String): Result<Unit>
    
    /**
     * Untrust a device
     */
    suspend fun untrustDevice(deviceId: String): Result<Unit>
    
    /**
     * Observe changes to registered devices
     */
    fun observeRegisteredDevices(): Flow<List<RegisteredDevice>>
    
    /**
     * Get devices that need key synchronization
     */
    suspend fun getDevicesNeedingKeySync(): List<RegisteredDevice>
}