package com.spacehog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.spacehog.navigation.AppNavigation
import com.spacehog.ui.theme.SpaceHogTheme
import com.spacehog.util.GameFocusManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpaceHogTheme {
                // The GameFocusManager is still created here and passed down,
                // so other screens can use it.
                val gameFocusManager = remember { GameFocusManager(this) }

                // All the lifecycle effects and permission launchers are now gone.
                // The app will proceed directly to the splash screen.
                AppNavigation(gameFocusManager = gameFocusManager)
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