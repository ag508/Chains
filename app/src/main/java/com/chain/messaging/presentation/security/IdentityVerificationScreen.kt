package com.chain.messaging.presentation.security

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.messaging.domain.model.SecurityAlert as CoreSecurityAlert
import com.chain.messaging.domain.model.SecurityRecommendation as CoreSecurityRecommendation
import com.chain.messaging.domain.model.ScanResult
import com.chain.messaging.domain.model.VerificationState
import com.chain.messaging.domain.model.SecurityAlert as DomainSecurityAlert
import com.chain.messaging.domain.model.SecurityRecommendation as DomainSecurityRecommendation
import com.chain.messaging.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityVerificationScreen(
    user: User,
    onNavigateBack: () -> Unit,
    viewModel: IdentityVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val safetyNumber by viewModel.safetyNumber.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("QR Code", "Safety Number", "Security")
    
    LaunchedEffect(user) {
        viewModel.generateQRCode(user)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Identity Verification") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> QRCodeTab(
                user = user,
                qrCodeBitmap = qrCodeBitmap,
                scanResult = scanResult,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onScanQRCode = viewModel::scanQRCode,
                onClearScanResult = viewModel::clearScanResult,
                onClearError = viewModel::clearError
            )
            1 -> SafetyNumberTab(
                safetyNumber = safetyNumber,
                isLoading = uiState.isLoading,
                error = uiState.error,
                verificationSuccess = uiState.verificationSuccess,
                onGenerateSafetyNumber = { userId, identityKey ->
                    viewModel.generateSafetyNumber(userId, identityKey)
                },
                onVerifySafetyNumber = { userId, enteredNumber, identityKey ->
                    viewModel.verifySafetyNumber(userId, enteredNumber, identityKey)
                },
                onClearError = viewModel::clearError,
                onClearVerificationSuccess = viewModel::clearVerificationSuccess
            )
            2 -> SecurityTab(
                securityAlerts = uiState.securityAlerts,
                securityRecommendations = uiState.securityRecommendations,
                verificationStates = uiState.verificationStates,
                onDismissAlert = viewModel::dismissSecurityAlert,
                onClearAllAlerts = viewModel::clearAllSecurityAlerts
            )
            else -> {
                // Default case - show QR Code tab
                QRCodeTab(
                    user = user,
                    qrCodeBitmap = qrCodeBitmap,
                    scanResult = scanResult,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onScanQRCode = viewModel::scanQRCode,
                    onClearScanResult = viewModel::clearScanResult,
                    onClearError = viewModel::clearError
                )
            }
        }
    }
}

@Composable
private fun QRCodeTab(
    user: User,
    qrCodeBitmap: Bitmap?,
    scanResult: ScanResult?,
    isLoading: Boolean,
    error: String?,
    onScanQRCode: (Bitmap) -> Unit,
    onClearScanResult: () -> Unit,
    onClearError: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Verification QR Code",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (qrCodeBitmap != null) {
                        Image(
                            bitmap = qrCodeBitmap.asImageBitmap(),
                            contentDescription = "Verification QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Show this QR code to ${user.displayName} to verify your identity",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Scan QR Code",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Scan the QR code shown by the person you want to verify",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { /* Open camera scanner */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan QR Code")
                    }
                }
            }
        }
        
        // Show scan result
        scanResult?.let { result ->
            item {
                ScanResultCard(
                    result = result,
                    onDismiss = onClearScanResult
                )
            }
        }
        
        // Show error
        error?.let { errorMessage ->
            item {
                ErrorCard(
                    error = errorMessage,
                    onDismiss = onClearError
                )
            }
        }
    }
}

@Composable
private fun SafetyNumberTab(
    safetyNumber: String?,
    isLoading: Boolean,
    error: String?,
    verificationSuccess: Boolean?,
    onGenerateSafetyNumber: (String, org.signal.libsignal.protocol.IdentityKey) -> Unit,
    onVerifySafetyNumber: (String, String, org.signal.libsignal.protocol.IdentityKey) -> Unit,
    onClearError: () -> Unit,
    onClearVerificationSuccess: () -> Unit
) {
    var enteredSafetyNumber by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Safety Number",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Compare this safety number with the one shown on the other person's device. If they match, your communication is secure.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (safetyNumber != null) {
                        Text(
                            text = safetyNumber,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Button(
                            onClick = { /* Generate safety number */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Safety Number")
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Manual Verification",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Enter the safety number from the other person's device:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = enteredSafetyNumber,
                        onValueChange = { enteredSafetyNumber = it },
                        label = { Text("Safety Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { /* Verify entered safety number */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enteredSafetyNumber.isNotBlank()
                    ) {
                        Text("Verify Safety Number")
                    }
                }
            }
        }
        
        // Show verification success
        verificationSuccess?.let { success ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (success) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (success) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = if (success) {
                                "Safety numbers match! Your communication is secure."
                            } else {
                                "Safety numbers don't match. Please verify again."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(onClick = onClearVerificationSuccess) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }
        }
        
        // Show error
        error?.let { errorMessage ->
            item {
                ErrorCard(
                    error = errorMessage,
                    onDismiss = onClearError
                )
            }
        }
    }
}

@Composable
private fun SecurityTab(
    securityAlerts: List<DomainSecurityAlert>,
    securityRecommendations: List<DomainSecurityRecommendation>,
    verificationStates: Map<String, VerificationState>,
    onDismissAlert: (String) -> Unit,
    onClearAllAlerts: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security Overview
        item {
            SecurityOverviewCard(
                verificationStates = verificationStates,
                alertCount = securityAlerts.size
            )
        }
        
        // Security Recommendations
        if (securityRecommendations.isNotEmpty()) {
            item {
                Text(
                    text = "Security Recommendations",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            items(securityRecommendations) { recommendation ->
                IdentitySecurityRecommendationCard(recommendation = recommendation)
            }
        }
        
        // Security Alerts
        if (securityAlerts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Security Alerts",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    TextButton(onClick = onClearAllAlerts) {
                        Text("Clear All")
                    }
                }
            }
            
            items(securityAlerts) { alert ->
                IdentitySecurityAlertCard(
                    alert = alert,
                    onDismiss = { onDismissAlert(alert.id) }
                )
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "No Security Alerts",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        Text(
                            text = "Your account is secure",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanResultCard(
    result: ScanResult,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is ScanResult.Success -> MaterialTheme.colorScheme.primaryContainer
                is ScanResult.KeyMismatch -> MaterialTheme.colorScheme.errorContainer
                is ScanResult.Error -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (result) {
                    is ScanResult.Success -> Icons.Default.CheckCircle
                    is ScanResult.KeyMismatch -> Icons.Default.Warning
                    is ScanResult.Error -> Icons.Default.Error
                },
                contentDescription = null,
                tint = when (result) {
                    is ScanResult.Success -> MaterialTheme.colorScheme.primary
                    is ScanResult.KeyMismatch -> MaterialTheme.colorScheme.error
                    is ScanResult.Error -> MaterialTheme.colorScheme.error
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (result) {
                        is ScanResult.Success -> "Verification Successful"
                        is ScanResult.KeyMismatch -> "Key Mismatch Warning"
                        is ScanResult.Error -> "Verification Failed"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = when (result) {
                        is ScanResult.Success -> "Successfully verified ${result.displayName}"
                        is ScanResult.KeyMismatch -> "Identity keys don't match for ${result.displayName}"
                        is ScanResult.Error -> result.message
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}

@Composable
private fun SecurityOverviewCard(
    verificationStates: Map<String, VerificationState>,
    alertCount: Int
) {
    val verifiedCount = verificationStates.values.count { it.isVerified }
    val totalCount = verificationStates.size
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Security Overview",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SecurityMetric(
                    title = "Verified Contacts",
                    value = "$verifiedCount/$totalCount",
                    icon = Icons.Default.VerifiedUser
                )
                
                SecurityMetric(
                    title = "Security Alerts",
                    value = alertCount.toString(),
                    icon = Icons.Default.Warning
                )
            }
        }
    }
}

@Composable
private fun SecurityMetric(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun IdentitySecurityRecommendationCard(
    recommendation: DomainSecurityRecommendation
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = when (recommendation) {
                    is DomainSecurityRecommendation.VerifyContacts -> 
                        "Verify ${recommendation.count} unverified contacts"
                    is DomainSecurityRecommendation.ReviewSecurityAlerts -> 
                        "Review ${recommendation.count} security alerts"
                    is DomainSecurityRecommendation.ReviewKeyChanges -> 
                        "Review ${recommendation.count} key changes"
                    is DomainSecurityRecommendation.VerifyIdentities -> 
                        "Verify ${recommendation.count} identities"
                    is DomainSecurityRecommendation.ReviewSuspiciousActivity -> 
                        "Review ${recommendation.count} suspicious activities"
                    DomainSecurityRecommendation.UpdateKeys -> 
                        "Consider updating your encryption keys"
                    DomainSecurityRecommendation.EnableTwoFactor -> 
                        "Enable two-factor authentication"
                    DomainSecurityRecommendation.IncreaseSecurityMeasures -> 
                        "Consider increasing security measures"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun IdentitySecurityAlertCard(
    alert: DomainSecurityAlert,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (alert) {
                        is DomainSecurityAlert.IdentityKeyChanged -> "Identity Key Changed"
                        is DomainSecurityAlert.KeyMismatch -> "Key Mismatch Detected"
                        is DomainSecurityAlert.SuspiciousActivity -> "Suspicious Activity"
                        is DomainSecurityAlert.PolicyViolation -> "Policy Violation"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = when (alert) {
                        is DomainSecurityAlert.IdentityKeyChanged -> 
                            "${alert.displayName}'s identity key has changed"
                        is DomainSecurityAlert.KeyMismatch -> 
                            "Key mismatch detected for ${alert.displayName}"
                        is DomainSecurityAlert.SuspiciousActivity -> 
                            "${alert.activityType}: ${alert.details}"
                        is DomainSecurityAlert.PolicyViolation -> 
                            "${alert.policyType}: ${alert.description}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}