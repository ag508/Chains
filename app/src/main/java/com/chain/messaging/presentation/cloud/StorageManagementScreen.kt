package com.chain.messaging.presentation.cloud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.messaging.core.cloud.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: StorageManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Show snackbar for messages and errors
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshStorageInfo() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Quota alerts
            if (uiState.quotaAlerts.isNotEmpty()) {
                item {
                    QuotaAlertsSection(
                        alerts = uiState.quotaAlerts,
                        onClearAlert = viewModel::clearAlert,
                        onClearAllAlerts = viewModel::clearAllAlerts
                    )
                }
            }
            
            // Storage overview
            item {
                StorageOverviewSection(
                    storageInfos = uiState.storageInfos,
                    localStorageInfo = uiState.localStorageInfo
                )
            }
            
            // Quick actions
            item {
                QuickActionsSection(
                    isPerformingCleanup = uiState.isPerformingCleanup,
                    isOptimizing = uiState.isOptimizing,
                    onPerformCleanup = { viewModel.performCleanup() },
                    onOptimizeStorage = viewModel::optimizeStorage
                )
            }
            
            // Cleanup suggestions
            if (uiState.cleanupSuggestions.isNotEmpty()) {
                item {
                    CleanupSuggestionsSection(
                        suggestions = uiState.cleanupSuggestions,
                        onPerformCleanup = viewModel::performCleanup
                    )
                }
            }
            
            // Storage recommendations
            if (uiState.recommendations.isNotEmpty()) {
                item {
                    RecommendationsSection(
                        recommendations = uiState.recommendations
                    )
                }
            }
            
            // Last cleanup result
            uiState.lastCleanupResult?.let { result ->
                item {
                    CleanupResultSection(result = result)
                }
            }
        }
    }
}

@Composable
private fun QuotaAlertsSection(
    alerts: List<QuotaAlert>,
    onClearAlert: (CloudService) -> Unit,
    onClearAllAlerts: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Storage Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                TextButton(onClick = onClearAllAlerts) {
                    Text("Clear All")
                }
            }
            
            alerts.forEach { alert ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.service.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    IconButton(onClick = { onClearAlert(alert.service) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear alert",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageOverviewSection(
    storageInfos: List<StorageInfo>,
    localStorageInfo: LocalStorageInfo?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Storage Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Cloud storage
            storageInfos.forEach { info ->
                StorageInfoItem(
                    title = info.service.displayName,
                    usedSpace = info.usedSpace,
                    totalSpace = info.totalSpace,
                    usagePercentage = info.usagePercentage
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Local storage
            localStorageInfo?.let { info ->
                StorageInfoItem(
                    title = "Local Storage",
                    usedSpace = info.totalSize,
                    totalSpace = info.maxSize,
                    usagePercentage = info.usagePercentage
                )
            }
        }
    }
}

@Composable
private fun StorageInfoItem(
    title: String,
    usedSpace: Long,
    totalSpace: Long,
    usagePercentage: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${formatFileSize(usedSpace)} / ${formatFileSize(totalSpace)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = usagePercentage / 100f,
            modifier = Modifier.fillMaxWidth(),
            color = when {
                usagePercentage >= 95f -> MaterialTheme.colorScheme.error
                usagePercentage >= 80f -> Color(0xFFFF9800) // Orange
                else -> MaterialTheme.colorScheme.primary
            }
        )
        
        Text(
            text = "${usagePercentage.toInt()}% used",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickActionsSection(
    isPerformingCleanup: Boolean,
    isOptimizing: Boolean,
    onPerformCleanup: () -> Unit,
    onOptimizeStorage: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPerformCleanup,
                    enabled = !isPerformingCleanup && !isOptimizing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isPerformingCleanup) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cleaning...")
                    } else {
                        Icon(
                            Icons.Default.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clean Up")
                    }
                }
                
                OutlinedButton(
                    onClick = onOptimizeStorage,
                    enabled = !isPerformingCleanup && !isOptimizing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isOptimizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimizing...")
                    } else {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimize")
                    }
                }
            }
        }
    }
}

@Composable
private fun CleanupSuggestionsSection(
    suggestions: List<CleanupSuggestion>,
    onPerformCleanup: (CleanupType) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Cleanup Suggestions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            suggestions.forEach { suggestion ->
                CleanupSuggestionItem(
                    suggestion = suggestion,
                    onPerformCleanup = { onPerformCleanup(suggestion.type) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CleanupSuggestionItem(
    suggestion: CleanupSuggestion,
    onPerformCleanup: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Can free up ${formatFileSize(suggestion.potentialSpaceSaved)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        TextButton(onClick = onPerformCleanup) {
            Text("Clean")
        }
    }
}

@Composable
private fun RecommendationsSection(
    recommendations: List<StorageRecommendation>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recommendations.forEach { recommendation ->
                RecommendationItem(recommendation = recommendation)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    recommendation: StorageRecommendation
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = when (recommendation.priority) {
                Priority.HIGH -> Icons.Default.Warning
                Priority.MEDIUM -> Icons.Default.Info
                Priority.LOW -> Icons.Default.Lightbulb
            },
            contentDescription = null,
            tint = when (recommendation.priority) {
                Priority.HIGH -> MaterialTheme.colorScheme.error
                Priority.MEDIUM -> Color(0xFFFF9800) // Orange
                Priority.LOW -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = recommendation.message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CleanupResultSection(
    result: CleanupResult
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Last Cleanup Result",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Files deleted: ${result.totalFilesDeleted}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "Space freed: ${formatFileSize(result.totalSpaceFreed)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return "%.1f %s".format(size, units[unitIndex])
}