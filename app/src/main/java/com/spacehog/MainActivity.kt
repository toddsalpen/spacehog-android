package com.spacehog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.spacehog.navigation.AppNavigation
import com.spacehog.ui.theme.SpaceHogTheme
import com.spacehog.util.GameFocusManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpaceHogTheme {
                val gameFocusManager = remember { GameFocusManager(this) }

                // This state will now control when the splash screen can navigate.
                var isReadyToProceed by remember { mutableStateOf(gameFocusManager.isPermissionGranted()) }

                // A flag to prevent asking for permission more than once per app launch.
                var permissionAlreadyRequested by remember { mutableStateOf(false) }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    // When we return from the settings screen, we re-check the permission
                    // and update our state.
                    isReadyToProceed = gameFocusManager.isPermissionGranted()
                }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            val permissionGranted = gameFocusManager.isPermissionGranted()
                            isReadyToProceed = permissionGranted

                            // Only request permission if we haven't already asked in this session
                            // AND permission is not granted.
                            if (!permissionGranted && !permissionAlreadyRequested) {
                                permissionAlreadyRequested = true // Mark that we've asked
                                gameFocusManager.requestPermission(notificationPermissionLauncher)
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Pass the readiness state down to the navigation graph
                AppNavigation(
                    gameFocusManager = gameFocusManager,
                    isReadyToProceed = isReadyToProceed
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