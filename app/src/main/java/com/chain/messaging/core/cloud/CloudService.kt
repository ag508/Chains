package com.chain.messaging.core.cloud

import kotlinx.serialization.Serializable

/**
 * Enum representing supported cloud storage services
 */
@Serializable
enum class CloudService(
    val displayName: String,
    val clientId: String,
    val scopes: List<String>
) {
    GOOGLE_DRIVE(
        displayName = "Google Drive",
        clientId = "your-google-client-id",
        scopes = listOf(
            "https://www.googleapis.com/auth/drive.file",
            "https://www.googleapis.com/auth/userinfo.profile"
        )
    ),
    ONEDRIVE(
        displayName = "OneDrive",
        clientId = "your-onedrive-client-id",
        scopes = listOf(
            "https://graph.microsoft.com/Files.ReadWrite",
            "https://graph.microsoft.com/User.Read"
        )
    ),
    ICLOUD(
        displayName = "iCloud",
        clientId = "your-icloud-client-id",
        scopes = listOf(
            "https://www.icloud.com/documents"
        )
    ),
    DROPBOX(
        displayName = "Dropbox",
        clientId = "your-dropbox-client-id",
        scopes = listOf(
            "files.content.write",
            "files.content.read"
        )
    )
}