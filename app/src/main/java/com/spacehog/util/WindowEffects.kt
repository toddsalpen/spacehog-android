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
    val window = (view.context as? Activity)?.window ?: return

    DisposableEffect(Unit) {
        if (view.isFocused) {
            val controller = WindowCompat.getInsetsController(window, view)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            onDispose {
                // When focus is lost or composable is disposed, show bars again.
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        } else {
            onDispose {} // Do nothing if not focused
        }
    }
}