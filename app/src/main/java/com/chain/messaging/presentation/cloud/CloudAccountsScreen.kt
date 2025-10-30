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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.messaging.core.cloud.CloudAccount
import com.chain.messaging.core.cloud.CloudService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudAccountsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CloudAccountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Show snackbar for messages and errors
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            // Show snackbar
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Show error snackbar
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloud Storage Accounts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            item {
                Text(
                    text = "Connect your cloud storage accounts to share files securely",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            items(CloudService.values()) { service ->
                CloudServiceCard(
                    service = service,
                    account = uiState.accounts.find { it.service == service },
                    isAuthenticated = uiState.authenticationStates[service] == true,
                    isAuthenticating = uiState.authenticatingService == service,
                    onConnect = { viewModel.authenticateService(service) },
                    onDisconnect = { viewModel.signOutFromService(service) },
                    onRefresh = { viewModel.refreshToken(service) }
                )
            }
        }
    }
}

@Composable
private fun CloudServiceCard(
    service: CloudService,
    account: CloudAccount?,
    isAuthenticated: Boolean,
    isAuthenticating: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getServiceIcon(service),
                        contentDescription = service.displayName,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = service.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (account != null) {
                            Text(
                                text = account.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                when {
                    isAuthenticating -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    isAuthenticated -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Connected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = "Not connected",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            if (account != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Connected as ${account.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRefresh,
                        enabled = !isAuthenticating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh")
                    }
                    
                    OutlinedButton(
                        onClick = onDisconnect,
                        enabled = !isAuthenticating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Disconnect")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onConnect,
                    enabled = !isAuthenticating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connecting...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect ${service.displayName}")
                    }
                }
            }
        }
    }
}

private fun getServiceIcon(service: CloudService): ImageVector {
    return when (service) {
        CloudService.GOOGLE_DRIVE -> Icons.Default.CloudQueue
        CloudService.ONEDRIVE -> Icons.Default.Cloud
        CloudService.ICLOUD -> Icons.Default.CloudDone
        CloudService.DROPBOX -> Icons.Default.CloudUpload
    }
}