package com.spacehog.ui.splash

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
    onSplashFinished: () -> Unit
) {
    // This state will hold our renderer instance
    val renderer = remember { mutableStateOf<SplashScreenRenderer?>(null) }

    // This effect handles setting the window to immersive mode
    ImmersiveModeEffect()

    // This effect observes the lifecycle of the composable
    // and calls the renderer's pause() and resume() methods.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> renderer.value?.resume()
                Lifecycle.Event.ON_PAUSE -> renderer.value?.pause()
                Lifecycle.Event.ON_DESTROY -> renderer.value?.stop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            renderer.value?.stop()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Detect a tap anywhere on the screen
                detectTapGestures(
                    onPress = { renderer.value?.onTouch() }
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
                            renderer.value = SplashScreenRenderer(context, holder, onSplashFinished)
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