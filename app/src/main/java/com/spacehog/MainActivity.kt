package com.spacehog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.spacehog.logic.StarfieldManager
import com.spacehog.navigation.AppNavigation
import com.spacehog.ui.theme.SpaceHogTheme
import com.spacehog.util.GameFocusManager
import com.spacehog.util.ImmersiveModeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val starfieldManager = StarfieldManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CRITICAL: Enable edge-to-edge display FIRST
        enableEdgeToEdge()

        // Start the starfield animation loop
        lifecycleScope.launch {
            while (true) {
                starfieldManager.tick()
                delay(16) // Update at ~60fps
            }
        }

        setContent {
            SpaceHogTheme {
                // Apply immersive mode at the app level - THIS IS THE KEY CHANGE
                ImmersiveModeEffect()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val gameFocusManager = remember { GameFocusManager(this@MainActivity) }

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        // Initialize starfield with screen dimensions
                        LaunchedEffect(Unit) {
                            starfieldManager.initialize(
                                constraints.maxWidth.toFloat(),
                                constraints.maxHeight.toFloat()
                            )
                        }

                        // Navigate through the app
                        AppNavigation(
                            gameFocusManager = gameFocusManager,
                            starfieldManager = starfieldManager
                        )
                    }
                }
            }
        }
    }
}