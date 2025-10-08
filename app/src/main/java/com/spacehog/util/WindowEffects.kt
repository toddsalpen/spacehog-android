package com.spacehog.util

import android.app.Activity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun ImmersiveModeEffect() {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)

        // Hide both status bar and navigation bar
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Set the behavior for when user swipes to reveal system bars
        // They will show temporarily and then hide again automatically
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Keep screen on throughout the app
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Make the content draw behind system bars (edge-to-edge)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        onDispose {
            // DON'T restore system bars during navigation
            // Only clear the keep screen on flag when the app is truly disposed
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // Only restore system bars when the entire app is closing
            // (This onDispose rarely runs since it's at the app root level)
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }
}