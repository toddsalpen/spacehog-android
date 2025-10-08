package com.spacehog.util

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * Manages entering and exiting a "Do Not Disturb" state for immersive gameplay.
 */
class GameFocusManager(private val context: Context) {
    private val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)

    fun isPermissionGranted(): Boolean {
        return notificationManager?.isNotificationPolicyAccessGranted == true
    }

    /**
     * Requests the "Do Not Disturb" permission from the user if not already granted.
     */
    fun requestPermission(launcher: ActivityResultLauncher<Intent>) {
        if (!isPermissionGranted()) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            launcher.launch(intent)
        }
    }

    /**
     * Attempts to enter a "game mode" that silences non-priority notifications.
     * Allows calls and alarms through.
     */
    fun enterGameMode() {
        if (isPermissionGranted()) {
            notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }

    /**
     * Exits "game mode" and restores all notifications.
     */
    fun exitGameMode() {
        if (isPermissionGranted()) {
            notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}