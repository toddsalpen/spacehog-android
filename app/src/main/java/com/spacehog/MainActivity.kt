package com.spacehog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.spacehog.logic.StarfieldManager
import com.spacehog.navigation.AppNavigation
import com.spacehog.ui.theme.SpaceHogTheme
import com.spacehog.util.GameFocusManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val starfieldManager = StarfieldManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            while (true) {
                starfieldManager.tick()
                delay(16) // Update at ~60fps
            }
        }
        enableEdgeToEdge()
        setContent {
            SpaceHogTheme {
                // The GameFocusManager is still created here and passed down,
                // so other screens can use it.
                val gameFocusManager = remember { GameFocusManager(this) }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    // This will only run a single time when the app starts
                    // and screen dimensions are first known.
                    LaunchedEffect(Unit) {
                        starfieldManager.initialize(
                            constraints.maxWidth.toFloat(),
                            constraints.maxHeight.toFloat()
                        )
                    }
                }
                // All the lifecycle effects and permission launchers are now gone.
                // The app will proceed directly to the splash screen.
                AppNavigation(
                    gameFocusManager = gameFocusManager,
                    starfieldManager = starfieldManager // <-- Pass it down
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    SpaceHogTheme {
//        AppNavigation()
//    }
//}