package com.chain.messaging.presentation.sync

import com.chain.messaging.core.sync.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceManagementViewModelTest {
    
    private val deviceManager = mockk<DeviceManager>()
    private val crossDeviceSyncService = mockk<CrossDeviceSyncService>()
    
    private lateinit var viewModel: DeviceManagementViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mocks
        coEvery { crossDeviceSyncService.initialize() } just Runs
        every { deviceManager.observeRegisteredDevices() } returns flowOf(emptyList())
        every { crossDeviceSyncService.getSyncStatus() } returns flowOf(createTestSyncStatus())
        every { crossDeviceSyncService.getSyncProgress() } returns flowOf(createTestSyncProgress())
        coEvery { deviceManager.getRegisteredDevices() } returns emptyList()
        
        viewModel = DeviceManagementViewModel(deviceManager, crossDeviceSyncService)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `should initialize with loading state`() = runTest {
        // Then
        val initialState = viewModel.uiState.value
        assertTrue(initialState.isLoading)
        assertEquals(0, initialState.otherDevices.size)
    }
    
    @Test
    fun `should load devices on initialization`() = runTest {
        // Given
        val currentDevice = createTestRegisteredDevice("current", true)
        val otherDevice = createTestRegisteredDevice("other", false)
        val devices = listOf(currentDevice, otherDevice)
        
        every { deviceManager.observeRegisteredDevices() } returns flowOf(devices)
        
        // When
        viewModel = DeviceManagementViewModel(deviceManager, crossDeviceSyncService)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(currentDevice, state.currentDevice)
        assertEquals(1, state.otherDevices.size)
        assertEquals(otherDevice, state.otherDevices[0])
    }
    
    @Test
    fun `refreshDevices should update device list`() = runTest {
        // Given
        val devices = listOf(
            createTestRegisteredDevice("device1", true),
            createTestRegisteredDevice("device2", false)
        )
        coEvery { deviceManager.getRegisteredDevices() } returns devices
        
        // When
        viewModel.refreshDevices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.otherDevices.size)
        assertEquals("device2", state.otherDevices[0].deviceInfo.deviceId)
    }
    
    @Test
    fun `trustDevice should trust device and trigger sync`() = runTest {
        // Given
        val deviceId = "test-device"
        coEvery { deviceManager.trustDevice(deviceId) } returns Result.success(Unit)
        coEvery { crossDeviceSyncService.requestSyncFromDevice(deviceId) } returns SyncResult.Success("Synced")
        
        // When
        viewModel.trustDevice(deviceId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { deviceManager.trustDevice(deviceId) }
        coVerify { crossDeviceSyncService.requestSyncFromDevice(deviceId) }
        assertEquals(null, viewModel.uiState.value.error)
    }
    
    @Test
    fun `trustDevice should handle failure`() = runTest {
        // Given
        val deviceId = "test-device"
        val error = Exception("Trust failed")
        coEvery { deviceManager.trustDevice(deviceId) } returns Result.failure(error)
        
        // When
        viewModel.trustDevice(deviceId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("Failed to trust device") == true)
    }
    
    @Test
    fun `removeDevice should remove device from list`() = runTest {
        // Given
        val deviceId = "test-device"
        coEvery { deviceManager.removeDevice(deviceId) } returns Result.success(Unit)
        
        // When
        viewModel.removeDevice(deviceId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { deviceManager.removeDevice(deviceId) }
        assertEquals(null, viewModel.uiState.value.error)
    }
    
    @Test
    fun `removeDevice should handle failure`() = runTest {
        // Given
        val deviceId = "test-device"
        val error = Exception("Remove failed")
        coEvery { deviceManager.removeDevice(deviceId) } returns Result.failure(error)
        
        // When
        viewModel.removeDevice(deviceId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("Failed to remove device") == true)
    }
    
    @Test
    fun `performFullSync should show sync progress`() = runTest {
        // Given
        coEvery { crossDeviceSyncService.performFullSync() } returns SyncResult.Success("Full sync completed")
        
        // When
        viewModel.performFullSync()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(viewModel.uiState.value.showSyncProgress)
        coVerify { crossDeviceSyncService.performFullSync() }
    }
    
    @Test
    fun `performFullSync should handle sync error`() = runTest {
        // Given
        coEvery { crossDeviceSyncService.performFullSync() } returns SyncResult.Error("Sync failed")
        
        // When
        viewModel.performFullSync()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("Sync failed") == true)
    }
    
    @Test
    fun `performFullSync should handle partial success`() = runTest {
        // Given
        coEvery { crossDeviceSyncService.performFullSync() } returns SyncResult.PartialSuccess(
            "Partial sync", 
            listOf("Error 1", "Error 2")
        )
        
        // When
        viewModel.performFullSync()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("Sync completed with errors") == true)
    }
    
    @Test
    fun `toggleAutoSync should change sync enabled state`() = runTest {
        // Given
        val initialStatus = createTestSyncStatus(isEnabled = true)
        every { crossDeviceSyncService.getSyncStatus() } returns flowOf(initialStatus)
        coEvery { crossDeviceSyncService.setAutoSyncEnabled(false) } just Runs
        
        // When
        viewModel.toggleAutoSync()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { crossDeviceSyncService.setAutoSyncEnabled(false) }
    }
    
    @Test
    fun `toggleAutoSync should handle failure`() = runTest {
        // Given
        coEvery { crossDeviceSyncService.setAutoSyncEnabled(any()) } throws Exception("Toggle failed")
        
        // When
        viewModel.toggleAutoSync()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("Failed to toggle auto sync") == true)
    }
    
    @Test
    fun `dismissSyncProgress should hide sync progress dialog`() = runTest {
        // Given
        viewModel.performFullSync() // Show progress first
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showSyncProgress)
        
        // When
        viewModel.dismissSyncProgress()
        
        // Then
        assertFalse(viewModel.uiState.value.showSyncProgress)
    }
    
    @Test
    fun `clearError should remove error message`() = runTest {
        // Given - Set an error state
        coEvery { deviceManager.trustDevice(any()) } returns Result.failure(Exception("Test error"))
        viewModel.trustDevice("test")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.error != null)
        
        // When
        viewModel.clearError()
        
        // Then
        assertEquals(null, viewModel.uiState.value.error)
    }
    
    @Test
    fun `should observe sync progress changes`() = runTest {
        // Given
        val progressFlow = flowOf(
            SyncProgress(SyncPhase.SYNCING_MESSAGES, 0.5f, "Syncing messages"),
            SyncProgress(SyncPhase.COMPLETED, 1.0f, "Sync completed")
        )
        every { crossDeviceSyncService.getSyncProgress() } returns progressFlow
        
        // When
        viewModel = DeviceManagementViewModel(deviceManager, crossDeviceSyncService)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(SyncPhase.COMPLETED, state.syncProgress.phase)
        assertEquals(1.0f, state.syncProgress.progress)
    }
    
    private fun createTestRegisteredDevice(deviceId: String, isCurrentDevice: Boolean): RegisteredDevice {
        return RegisteredDevice(
            deviceInfo = DeviceInfo(
                deviceId = deviceId,
                deviceName = "Test Device $deviceId",
                deviceType = DeviceType.MOBILE,
                platform = "Android",
                platformVersion = "13",
                appVersion = "1.0.0",
                publicKey = "public-key-$deviceId",
                lastSeen = LocalDateTime.now(),
                isCurrentDevice = isCurrentDevice
            ),
            registeredAt = LocalDateTime.now(),
            isTrusted = isCurrentDevice,
            lastSyncAt = if (isCurrentDevice) LocalDateTime.now() else null,
            syncStatus = if (isCurrentDevice) SyncStatus.SYNCED else SyncStatus.PENDING
        )
    }
    
    private fun createTestSyncStatus(isEnabled: Boolean = true): CrossDeviceSyncStatus {
        return CrossDeviceSyncStatus(
            isEnabled = isEnabled,
            lastSyncTime = LocalDateTime.now(),
            connectedDevices = 2,
            trustedDevices = 1,
            pendingSyncs = 0,
            syncErrors = emptyList()
        )
    }
    
    private fun createTestSyncProgress(): SyncProgress {
        return SyncProgress(
            phase = SyncPhase.INITIALIZING,
            progress = 0f,
            currentOperation = "Initializing"
        )
    }
}