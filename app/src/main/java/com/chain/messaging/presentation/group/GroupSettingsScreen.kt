package com.chain.messaging.presentation.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSettingsScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    viewModel: GroupSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(groupId) {
        viewModel.loadGroup(groupId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group Info Section
                item {
                    GroupInfoSection(
                        groupName = uiState.groupName,
                        groupDescription = uiState.groupDescription,
                        memberCount = uiState.members.size,
                        canEdit = uiState.canEditGroupInfo,
                        onEditInfo = { viewModel.showEditInfoDialog() }
                    )
                }
                
                // Admin Controls Section
                if (uiState.isCurrentUserAdmin) {
                    item {
                        AdminControlsSection(
                            onAddMembers = { viewModel.showAddMembersDialog() },
                            onManageInviteLink = { viewModel.showInviteLinkDialog() }
                        )
                    }
                }
                
                // Members Section
                item {
                    Text(
                        text = "Members (${uiState.members.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                items(uiState.members) { member ->
                    MemberItem(
                        member = member,
                        isCurrentUserAdmin = uiState.isCurrentUserAdmin,
                        onPromoteToAdmin = { viewModel.promoteToAdmin(member.id) },
                        onDemoteFromAdmin = { viewModel.demoteFromAdmin(member.id) },
                        onRemoveMember = { viewModel.removeMember(member.id) }
                    )
                }
                
                // Leave Group Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.leaveGroup() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Leave Group")
                    }
                }
            }
        }
        
        // Error Snackbar
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar
            }
        }
    }
    
    // Dialogs
    if (uiState.showEditInfoDialog) {
        EditGroupInfoDialog(
            currentName = uiState.groupName,
            currentDescription = uiState.groupDescription,
            onDismiss = { viewModel.hideEditInfoDialog() },
            onSave = { name, description ->
                viewModel.updateGroupInfo(name, description)
            }
        )
    }
    
    if (uiState.showInviteLinkDialog) {
        InviteLinkDialog(
            currentLink = uiState.inviteLink,
            onDismiss = { viewModel.hideInviteLinkDialog() },
            onGenerateLink = { viewModel.generateInviteLink() },
            onRevokeLink = { viewModel.revokeInviteLink() }
        )
    }
}

@Composable
private fun GroupInfoSection(
    groupName: String,
    groupDescription: String?,
    memberCount: Int,
    canEdit: Boolean,
    onEditInfo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
                    text = groupName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (canEdit) {
                    IconButton(onClick = onEditInfo) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Group Info")
                    }
                }
            }
            
            if (!groupDescription.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = groupDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$memberCount members",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminControlsSection(
    onAddMembers: () -> Unit,
    onManageInviteLink: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Admin Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddMembers,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Members")
                }
                
                OutlinedButton(
                    onClick = onManageInviteLink,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invite Link")
                }
            }
        }
    }
}

@Composable
private fun MemberItem(
    member: GroupMember,
    isCurrentUserAdmin: Boolean,
    onPromoteToAdmin: () -> Unit,
    onDemoteFromAdmin: () -> Unit,
    onRemoveMember: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (member.isAdmin) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Admin") }
                        )
                    }
                }
                
                if (member.status.isNotEmpty()) {
                    Text(
                        text = member.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isCurrentUserAdmin && !member.isCurrentUser) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (member.isAdmin) {
                            DropdownMenuItem(
                                text = { Text("Remove Admin") },
                                onClick = {
                                    onDemoteFromAdmin()
                                    showMenu = false
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Make Admin") },
                                onClick = {
                                    onPromoteToAdmin()
                                    showMenu = false
                                }
                            )
                        }
                        
                        DropdownMenuItem(
                            text = { Text("Remove from Group") },
                            onClick = {
                                onRemoveMember()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

