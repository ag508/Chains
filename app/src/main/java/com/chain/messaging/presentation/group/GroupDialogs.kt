package com.chain.messaging.presentation.group

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun EditGroupInfoDialog(
    currentName: String,
    currentDescription: String?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var description by remember { mutableStateOf(currentDescription ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Group Info") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InviteLinkDialog(
    currentLink: String?,
    onDismiss: () -> Unit,
    onGenerateLink: () -> Unit,
    onRevokeLink: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite Link") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentLink != null) {
                    Text("Share this link to invite people to the group:")
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentLink,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentLink))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Copy Link")
                        }
                        
                        OutlinedButton(
                            onClick = onRevokeLink,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Revoke Link")
                        }
                    }
                } else {
                    Text("No active invite link. Generate one to invite people to the group.")
                    
                    Button(
                        onClick = onGenerateLink,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Invite Link")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}