package com.chain.messaging.presentation.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.messaging.core.sync.DeviceType
import com.chain.messaging.core.sync.RegisteredDevice
import com.chain.messaging.core.sync.SyncStatus
import java.time.format.DateTimeFormatter

/**
 * Screen for managing registered devices and cross-device synchronization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeviceManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshDevices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sync status card
            item {
                SyncStatusCard(
                    syncStatus = uiState.syncStatus,
                    onSyncNow = { viewModel.performFullSync() },
                    onToggleAutoSync = { viewModel.toggleAutoSync() }
                )
            }
            
            // Current device
            uiState.currentDevice?.let { device ->
                item {
                    Text(
                        text = "Current Device",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    DeviceCard(
                        device = device,
                        isCurrentDevice = true,
                        onTrustDevice = { },
                        onRemoveDevice = { }
                    )
                }
            }
            
            // Other devices
            if (uiState.otherDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "Other Devices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(uiState.otherDevices) { device ->
                    DeviceCard(
                        device = device,
                        isCurrentDevice = false,
                        onTrustDevice = { viewModel.trustDevice(device.deviceInfo.deviceId) },
                        onRemoveDevice = { viewModel.removeDevice(device.deviceInfo.deviceId) }
                    )
                }
            }
            
            // Add device instructions
            item {
                AddDeviceInstructions()
            }
        }
    }
    
    // Show sync progress dialog
    if (uiState.showSyncProgress) {
        SyncProgressDialog(
            syncProgress = uiState.syncProgress,
            onDismiss = { viewModel.dismissSyncProgress() },
            isDismissible = uiState.syncProgress.phase.let { 
                it == com.chain.messaging.core.sync.SyncPhase.COMPLETED || 
                it == com.chain.messaging.core.sync.SyncPhase.ERROR 
            }
        )
    }
}

@Composable
private fun SyncStatusCard(
    syncStatus: com.chain.messaging.core.sync.CrossDeviceSyncStatus,
    onSyncNow: () -> Unit,
    onToggleAutoSync: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Synchronization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Switch(
                    checked = syncStatus.isEnabled,
                    onCheckedChange = { onToggleAutoSync() }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Trusted Devices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${syncStatus.trustedDevices}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "Pending Syncs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${syncStatus.pendingSyncs}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            syncStatus.lastSyncTime?.let { lastSync ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last sync: ${lastSync.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (syncStatus.syncErrors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${syncStatus.syncErrors.size} sync error(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSyncNow,
                modifier = Modifier.fillMaxWidth(),
                enabled = syncStatus.isEnabled
            ) {
                Icon(Icons.Default.Sync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync Now")
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: RegisteredDevice,
    isCurrentDevice: Boolean,
    onTrustDevice: () -> Unit,
    onRemoveDevice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getDeviceIcon(device.deviceInfo.deviceType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = device.deviceInfo.deviceName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = "${device.deviceInfo.platform} ${device.deviceInfo.platformVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (isCurrentDevice) {
                    AssistChip(
                        onClick = { },
                        label = { Text("This Device") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = getSyncStatusColor(device.syncStatus),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = getSyncStatusText(device.syncStatus),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "Last Seen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = device.deviceInfo.lastSeen.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            if (!isCurrentDevice) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!device.isTrusted) {
                        OutlinedButton(
                            onClick = onTrustDevice,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trust")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onRemoveDevice,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddDeviceInstructions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Adding New Devices",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "To add a new device, sign in to Chain on that device using the same account. " +
                        "New devices will appear here and need to be trusted before synchronization begins.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getDeviceIcon(deviceType: DeviceType) = when (deviceType) {
    DeviceType.MOBILE -> Icons.Default.PhoneAndroid
    DeviceType.TABLET -> Icons.Default.Tablet
    DeviceType.DESKTOP -> Icons.Default.Computer
    DeviceType.WEB -> Icons.Default.Language
    DeviceType.UNKNOWN -> Icons.Default.DeviceUnknown
}

@Composable
private fun getSyncStatusColor(status: SyncStatus) = when (status) {
    SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
    SyncStatus.SYNCING -> MaterialTheme.colorScheme.tertiary
    SyncStatus.PENDING -> MaterialTheme.colorScheme.outline
    SyncStatus.ERROR -> MaterialTheme.colorScheme.error
    SyncStatus.OFFLINE -> MaterialTheme.colorScheme.outlineVariant
}

private fun getSyncStatusText(status: SyncStatus) = when (status) {
    SyncStatus.SYNCED -> "Synced"
    SyncStatus.SYNCING -> "Syncing"
    SyncStatus.PENDING -> "Pending"
    SyncStatus.ERROR -> "Error"
    SyncStatus.OFFLINE -> "Offline"
}