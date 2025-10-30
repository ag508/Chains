package com.chain.messaging.core.notification

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing notification permissions
 * Handles Android 13+ notification permission requests
 */
@Singleton
class NotificationPermissionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Check if notification permission is granted
     */
    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pre-Android 13, notifications are enabled by default
            true
        }
    }
    
    /**
     * Check if we should show rationale for notification permission
     */
    fun shouldShowNotificationRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            false
        }
    }
    
    /**
     * Create permission launcher for requesting notification permission
     */
    fun createPermissionLauncher(
        fragment: Fragment,
        onResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResult(isGranted)
        }
    }
    
    /**
     * Create permission launcher for requesting notification permission from activity
     */
    fun createPermissionLauncher(
        activity: androidx.activity.ComponentActivity,
        onResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResult(isGranted)
        }
    }
    
    /**
     * Request notification permission
     */
    fun requestNotificationPermission(launcher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    /**
     * Open app notification settings
     */
    fun openNotificationSettings(activity: Activity) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        activity.startActivity(intent)
    }
    
    /**
     * Get notification permission status
     */
    fun getNotificationPermissionStatus(): NotificationPermissionStatus {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                NotificationPermissionStatus.NOT_REQUIRED
            }
            
            isNotificationPermissionGranted() -> {
                NotificationPermissionStatus.GRANTED
            }
            
            else -> {
                NotificationPermissionStatus.DENIED
            }
        }
    }
}

/**
 * Notification permission status
 */
enum class NotificationPermissionStatus {
    GRANTED,
    DENIED,
    NOT_REQUIRED
}