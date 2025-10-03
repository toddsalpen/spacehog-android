package com.spacehog.ui.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.spacehog.data.JSONDataHandler
import com.spacehog.ui.common.StarfieldViewModel
import com.spacehog.util.GameFocusManager
import org.json.JSONArray

class SettingsViewModel : StarfieldViewModel() {

    fun handleFocusModeToggle(
        context: Context,
        isEnabled: Boolean,
        permissionLauncher: ActivityResultLauncher<Intent> // Launcher to request permission
    ) {
        val gameFocusManager = GameFocusManager(context)
        if (isEnabled) {
            // If the user is trying to TURN ON focus mode...
            if (!gameFocusManager.isPermissionGranted()) {
                // ...but we don't have permission, request it.
                gameFocusManager.requestPermission(permissionLauncher)
            } else {
                // We already have permission, so the toggle is now ON.
                // (No further action needed, the state is saved by the system permission)
            }
        } else {
            // If the user is turning it OFF, they are revoking permission via system settings.
            // For now, we just guide them there.
            if (gameFocusManager.isPermissionGranted()) {
                // Optionally, you can guide the user to the settings page to disable it.
                // For simplicity, often just letting them know it's a system setting is enough.
                gameFocusManager.requestPermission(permissionLauncher) // This takes them to the right screen
            }
        }
    }

    // A helper to get the current status for the UI
    fun isFocusModeEnabled(context: Context): Boolean {
        return GameFocusManager(context).isPermissionGranted()
    }

    fun onControlStyleSelected(context: Context, styleIndex: Int) {
        val jdh = JSONDataHandler(context)
        val controlChoices = arrayOf(
            "Standard - Touch To Move and Fire",
            "Tilt - Tilt To Move and Touch to Fire"
        )
        val settings = JSONArray()
        when (styleIndex) {
            0 -> settings.put(false) // Standard
            1 -> settings.put(true)  // Tilt
        }
        jdh.writeToFile(settings, "control_settings.txt")
        Toast.makeText(context, "Selected: ${controlChoices[styleIndex]}", Toast.LENGTH_SHORT).show()
    }

    fun onResetScores(context: Context) {
        val jdh = JSONDataHandler(context)
        val message = if (jdh.deleteFile("score_board.txt")) {
            "Scoreboard Reset"
        } else {
            "Sorry, something broke. Scoreboard was not reset."
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}