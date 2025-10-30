package com.chain.messaging.presentation.security

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
import com.chain.messaging.domain.model.SecurityLevel
import com.chain.messaging.domain.model.SecuritySeverity
import com.chain.messaging.domain.model.SecurityAlert
import com.chain.messaging.domain.model.SecurityStatus
import com.chain.messaging.domain.model.SecurityMetrics
import com.chain.messaging.domain.model.SecurityRecommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityMonitoringScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecurityMonitoringViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.startSecurityMonitoring()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "Security Monitoring",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { viewModel.refreshData() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Security Status Card
                item {
                    SecurityStatusCard(
                        status = uiState.securityStatus,
                        isMonitoringActive = uiState.isMonitoringActive
                    )
                }
                
                // Security Metrics Card
                item {
                    uiState.metrics?.let { metrics ->
                        SecurityMetricsCard(metrics = metrics)
                    }
                }
                
                // Active Alerts Section
                if (alerts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Security Alerts",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(alerts.filter { !it.isAcknowledged }) { alert ->
                        SecurityAlertCard(
                            alert = alert,
                            onAcknowledge = { viewModel.acknowledgeAlert(alert.id) }
                        )
                    }
                }
                
                // Recommendations Section
                if (uiState.recommendations.isNotEmpty()) {
                    item {
                        Text(
                            text = "Security Recommendations",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(uiState.recommendations) { recommendation ->
                        SecurityRecommendationCard(recommendation = recommendation)
                    }
                }
            }
        }
        
        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar or handle error
                viewModel.clearError()
            }
        }
    }
}

@Composable
fun SecurityStatusCard(
    status: SecurityStatus?,
    isMonitoringActive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status?.level) {
                SecurityLevel.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                SecurityLevel.DANGER -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                SecurityLevel.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                SecurityLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                SecurityLevel.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                SecurityLevel.LOW -> MaterialTheme.colorScheme.surfaceVariant
                SecurityLevel.SECURE -> MaterialTheme.colorScheme.primaryContainer
                null -> MaterialTheme.colorScheme.surfaceVariant
            }
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
                    text = "Security Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = when (status?.level) {
                        SecurityLevel.CRITICAL -> Icons.Default.Warning
                        SecurityLevel.DANGER -> Icons.Default.Warning
                        SecurityLevel.HIGH -> Icons.Default.Warning
                        SecurityLevel.WARNING -> Icons.Default.Info
                        SecurityLevel.MEDIUM -> Icons.Default.Info
                        SecurityLevel.LOW -> Icons.Default.CheckCircle
                        SecurityLevel.SECURE -> Icons.Default.CheckCircle
                        null -> Icons.Default.Help
                    },
                    contentDescription = null,
                    tint = when (status?.level) {
                        SecurityLevel.CRITICAL -> MaterialTheme.colorScheme.error
                        SecurityLevel.DANGER -> MaterialTheme.colorScheme.error
                        SecurityLevel.HIGH -> MaterialTheme.colorScheme.error
                        SecurityLevel.WARNING -> MaterialTheme.colorScheme.tertiary
                        SecurityLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
                        SecurityLevel.LOW -> MaterialTheme.colorScheme.primary
                        SecurityLevel.SECURE -> MaterialTheme.colorScheme.primary
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            status?.let {
                Text(
                    text = "Level: ${it.level.name}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Active Threats: ${it.activeThreats}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Recommendations: ${it.recommendations}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Last Scan: ${it.lastScanTime}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isMonitoringActive) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = if (isMonitoringActive) Color.Green else Color.Red
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isMonitoringActive) "Monitoring Active" else "Monitoring Inactive",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SecurityMetricsCard(metrics: SecurityMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Security Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem("Security Score", "${metrics.securityScore}/100")
                MetricItem("Total Events", metrics.totalEvents.toString())
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem("Last 24h", metrics.eventsLast24Hours.toString())
                MetricItem("Critical Alerts", metrics.criticalAlertsActive.toString())
            }
            
            metrics.lastBreachAttempt?.let { lastBreach ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last Breach Attempt: $lastBreach",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SecurityAlertCard(
    alert: SecurityAlert,
    onAcknowledge: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                SecuritySeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                SecuritySeverity.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                SecuritySeverity.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
                SecuritySeverity.LOW -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = alert.severity.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (alert.severity) {
                            SecuritySeverity.CRITICAL -> MaterialTheme.colorScheme.error
                            SecuritySeverity.HIGH -> MaterialTheme.colorScheme.error
                            SecuritySeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
                            SecuritySeverity.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Text(
                    text = alert.timestamp.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (alert.recommendedActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recommended Actions:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                alert.recommendedActions.forEach { action ->
                    Text(
                        text = "• $action",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onAcknowledge) {
                    Text("Acknowledge")
                }
            }
        }
    }
}

@Composable
fun SecurityRecommendationCard(recommendation: SecurityRecommendation) {
    val (title, description, category) = when (recommendation) {
        is SecurityRecommendation.VerifyContacts -> 
            Triple("Verify Contacts", "Verify ${recommendation.count} unverified contacts", "IDENTITY")
        is SecurityRecommendation.ReviewSecurityAlerts -> 
            Triple("Review Security Alerts", "Review ${recommendation.count} security alerts", "SECURITY")
        is SecurityRecommendation.ReviewKeyChanges -> 
            Triple("Review Key Changes", "Review ${recommendation.count} key changes", "KEY_MANAGEMENT")
        is SecurityRecommendation.VerifyIdentities -> 
            Triple("Verify Identities", "Verify ${recommendation.count} identities", "IDENTITY")
        is SecurityRecommendation.ReviewSuspiciousActivity -> 
            Triple("Review Suspicious Activity", "Review ${recommendation.count} suspicious activities", "SECURITY")
        SecurityRecommendation.UpdateKeys -> 
            Triple("Update Keys", "Consider updating your encryption keys", "KEY_MANAGEMENT")
        SecurityRecommendation.EnableTwoFactor -> 
            Triple("Enable Two-Factor Authentication", "Enable two-factor authentication for better security", "AUTHENTICATION")
        SecurityRecommendation.IncreaseSecurityMeasures -> 
            Triple("Increase Security Measures", "Consider increasing security measures", "SECURITY")
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = "5 min", // Default estimated time
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}