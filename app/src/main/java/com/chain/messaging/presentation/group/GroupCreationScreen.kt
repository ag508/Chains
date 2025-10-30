package com.chain.messaging.presentation.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreationScreen(
    onNavigateBack: () -> Unit,
    onGroupCreated: (String) -> Unit,
    viewModel: GroupCreationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.createdGroupId) {
        uiState.createdGroupId?.let { groupId ->
            onGroupCreated(groupId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Group") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.createGroup() },
                        enabled = uiState.canCreateGroup
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Create Group")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group Name Input
            OutlinedTextField(
                value = uiState.groupName,
                onValueChange = viewModel::updateGroupName,
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.groupNameError != null,
                supportingText = uiState.groupNameError?.let { { Text(it) } }
            )
            
            // Group Description Input
            OutlinedTextField(
                value = uiState.groupDescription,
                onValueChange = viewModel::updateGroupDescription,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            
            // Member Selection
            Text(
                text = "Add Members",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (uiState.isLoadingContacts) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.availableContacts) { contact ->
                        ContactSelectionItem(
                            contact = contact,
                            isSelected = uiState.selectedMembers.contains(contact.id),
                            onSelectionChanged = { isSelected ->
                                if (isSelected) {
                                    viewModel.addMember(contact.id)
                                } else {
                                    viewModel.removeMember(contact.id)
                                }
                            }
                        )
                    }
                }
            }
            
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            if (uiState.isCreatingGroup) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ContactSelectionItem(
    contact: ContactInfo,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectionChanged
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (contact.status.isNotEmpty()) {
                Text(
                    text = contact.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

