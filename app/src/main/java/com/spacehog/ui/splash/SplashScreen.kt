package com.spacehog.ui.splash

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.spacehog.util.ImmersiveModeEffect

@Composable
fun SplashScreen(
    isReadyToProceed: Boolean,
    onFinish: () -> Unit
) {

    // This state prevents onFinish from being called multiple times.
    val hasFinished = remember { mutableStateOf(false) }

    // This effect triggers navigation automatically if permission is granted later.
    LaunchedEffect(isReadyToProceed) {
        if (isReadyToProceed && !hasFinished.value) {
            hasFinished.value = true
            onFinish()
        }
    }

    // This state will hold our renderer instance
    val renderer = remember { mutableStateOf<SplashScreenRenderer?>(null) }

    // This effect handles setting the window to immersive mode
    ImmersiveModeEffect()

    DisposableEffect(Unit) {
        onDispose {
            renderer.value?.stopAndRelease()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // The tap gesture can also trigger the finish, but only once.
                        if (isReadyToProceed && !hasFinished.value) {
                            hasFinished.value = true
                            onFinish()
                        }
                    }
                )
            }
    ) {
        // The AndroidView composable is the bridge to the old View system
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                // Create the SurfaceView
                SurfaceView(context).apply {
                    // Create and attach our renderer when the surface is ready
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            // The renderer just does its animation now. The composable decides when to leave.
                            renderer.value = SplashScreenRenderer(context, holder)
                            renderer.value?.resume()
                        }
                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            renderer.value?.pause()
                        }
                    })
                }
            }
        )
    }
}