package com.spacehog.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.spacehog.data.JSONDataHandler
import org.json.JSONArray

class SettingsViewModel : ViewModel() {

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