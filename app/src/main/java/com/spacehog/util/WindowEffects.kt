package com.spacehog.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * A Jetpack Compose side-effect that enters immersive mode (hides system bars)
 * for the duration of the composable's lifecycle.
 *
 * This implementation uses the modern, non-deprecated `WindowInsetsControllerCompat` API
 * for Android 11 (API 30) and above, while providing backward compatibility for older versions.
 */
@Composable
fun ImmersiveModeEffect() {
    val view = LocalView.current
    // We can't do anything unless we're in an activity
    val window = (view.context as? Activity)?.window ?: return

    // DisposableEffect is crucial to ensure we restore the system UI when the
    // composable leaves the screen.
    DisposableEffect(Unit) {
        // Get a controller for the window. This is the modern way to interact with insets.
        val controller = WindowCompat.getInsetsController(window, view)

        // --- ENTER IMMERSIVE MODE ---

        // 1. Tell the window that we want to draw behind the system bars.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Hide the system bars (status and navigation).
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // 3. Configure the behavior for when the user swipes from the edge.
        // BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE makes the bars temporarily appear and then hide again.
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // The onDispose block is called when the composable is removed from the screen.
        onDispose {
            // --- EXIT IMMERSIVE MODE ---

            // 1. Restore the default behavior of fitting the layout to the system bars.
            WindowCompat.setDecorFitsSystemWindows(window, true)

            // 2. Show the system bars again.
            controller.show(WindowInsetsCompat.Type.systemBars())

            // 3. Restore the default swipe behavior.
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }
}