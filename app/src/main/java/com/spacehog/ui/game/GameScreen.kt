package com.spacehog.ui.game

import android.graphics.PixelFormat
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.spacehog.logic.StarfieldManager
import com.spacehog.util.GameFocusManager
import com.spacehog.util.ImmersiveModeEffect

@Composable
fun GameScreen(
    navController: NavController,
    gameFocusManager: GameFocusManager,
    starfieldManager: StarfieldManager
) {
    // Enable immersive mode for the game screen
    ImmersiveModeEffect()

    val renderer = remember { mutableStateOf<GameRenderer?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var isGameOver by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> renderer.value?.resume()
                Lifecycle.Event.ON_PAUSE -> renderer.value?.pause()
                else -> {}
            }
        }

        if (gameFocusManager.isPermissionGranted()) {
            gameFocusManager.enterGameMode()
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            gameFocusManager.exitGameMode()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets(0, 0, 0, 0))
            .pointerInput(Unit) {
                awaitEachGesture {
                    while (true) {
                        val event = awaitPointerEvent()
                        val isPressed = event.changes.any { it.pressed }
                        if (isGameOver) {
                            return@awaitEachGesture
                        }
                        if (isPressed) {
                            val touchX = event.changes.first().position.x
                            renderer.value?.onPlayerInput(touchX, isFiring = true)
                        } else {
                            renderer.value?.onPlayerInput(null, isFiring = false)
                            return@awaitEachGesture
                        }
                    }
                }
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                SurfaceView(context).apply {
                    setZOrderMediaOverlay(true)
                    holder.setFormat(PixelFormat.TRANSLUCENT)

                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            val newRenderer = GameRenderer(context, holder, starfieldManager) {
                                isGameOver = true
                            }
                            renderer.value = newRenderer
                            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                newRenderer.resume()
                            }
                        }
                        override fun surfaceChanged(holder: SurfaceHolder, f: Int, w: Int, h: Int) {}
                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            renderer.value?.pause()
                            renderer.value = null
                        }
                    })
                }
            }
        )

        if (isGameOver) {
            GameOverOverlay(
                onRestart = {
                    renderer.value?.restartGame()
                    isGameOver = false
                },
                onExit = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun GameOverOverlay(onRestart: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Over",
            color = Color.White,
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Restart") }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Exit to Menu") }
    }
}