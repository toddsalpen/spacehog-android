package com.spacehog.ui.game

import android.app.Activity
import android.graphics.PixelFormat
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.spacehog.util.GameFocusManager
import com.spacehog.util.ImmersiveModeEffect

// NEW: The screen now accepts a NavController to handle the "Exit" button.
@Composable
fun GameScreen(
    navController: NavController,
    gameFocusManager: GameFocusManager // <-- Accept the new parameter
) {

    // 1. Activate our Immersive Mode effect, which hides the system bars.
    ImmersiveModeEffect()

    // 2. This effect manages entering and exiting our "game focus" mode.
    DisposableEffect(Unit) {
        // When the GameScreen becomes active, enter game mode.
        gameFocusManager.enterGameMode()

        // When the GameScreen is left (onDispose), exit game mode and restore notifications.
        onDispose {
            gameFocusManager.exitGameMode()
        }
    }

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
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    while (true) {
                        val event = awaitPointerEvent()
                        val isPressed = event.changes.any { it.pressed }

                        // Don't allow input if the game is over.
                        if(isGameOver) {
                            // By returning here, we stop processing touch for this gesture
                            // which effectively 'disables' input.
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
                            // --- START OF THE FIX ---
                            // We create the GameRenderer and pass it a lambda function.
                            // This function will be the 'onGameOver' callback.
                            val newRenderer = GameRenderer(context, holder) {
                                // This block of code will execute on the Main thread
                                // when the GameWorld tells the renderer the game is over.
                                isGameOver = true
                            }
                            // --- END OF THE FIX ---
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

        // --- START OF NEW UI LOGIC ---
        // This overlay will only appear when the `isGameOver` state becomes true.
        if (isGameOver) {
            GameOverOverlay(
                onRestart = {
                    // Tell the renderer to restart the game
                    renderer.value?.restartGame()
                    // Hide the overlay
                    isGameOver = false
                },
                onExit = {
                    // Use the NavController to go back to the menu
                    navController.popBackStack()
                }
            )
        }
        // --- END OF NEW UI LOGIC ---
    }
}


@Composable
fun GameOverOverlay(onRestart: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)) // Semi-transparent overlay
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
        ) {
            Text("Restart")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exit to Menu")
        }
    }
}