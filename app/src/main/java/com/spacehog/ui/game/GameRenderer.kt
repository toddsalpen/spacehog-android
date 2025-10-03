package com.spacehog.ui.game // Use your package name

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import com.spacehog.logic.DebugData
import com.spacehog.logic.GameWorld
import com.spacehog.logic.Scaler
import com.spacehog.model.LevelState

class GameRenderer(
    private val context: Context,
    private val holder: SurfaceHolder,
    val onGameOver: () -> Unit
) : Runnable {
    // --- Core Threading and Loop ---
    private var thread: Thread? = null
    @Volatile private var isRunning = false
    private val targetFPS = 60
    private val targetTimeMillis = (1000 / targetFPS).toLong()

    // --- NEW STRUCTURE ---
    // These will be initialized on the background thread
    private var gameWorld: GameWorld? = null
    private var hud: HUD? = null
    private val paint = Paint()
    // This will be updated by the GameScreen composable
    @Volatile private var playerTouchX: Float? = null
    @Volatile private var isPlayerFiring = false // <-- NEW property

    init {
        Log.d("GameRenderer", "Renderer instance created (lightweight init).")
    }

    private fun setupGame() {
        Log.d("GameRenderer", "Setting up game assets on background thread...")
        try {
            // 1. The GameRenderer is the first to know the screen size, so it creates the Scaler.
            val scaler = Scaler(
                holder.surfaceFrame.width().toFloat(),
                holder.surfaceFrame.height().toFloat()
            )

            // 2. It then passes this single Scaler instance to both the GameWorld and the HUD.
            gameWorld = GameWorld(context, holder, scaler, onGameOver = onGameOver)
            hud = HUD(
                holder.surfaceFrame.height().toFloat(),
                holder.surfaceFrame.width().toFloat(),
                gameWorld!!.assetLibrary, // Use the asset library from the game world
                scaler = scaler
            )
            Log.d("GameRenderer", "Game setup complete.")
        } catch (e: IllegalStateException) {
            Log.e("GameRenderer", "Asset loading failed: ${e.message}")
            isRunning = false
        }
    }

    // This method will be called from the UI's "Restart" button
    fun restartGame() {
        // We create a brand new GameWorld instance, which resets everything:
        // score, player lives, level manager, etc.
        // It's already on the background thread, but let's be safe.
        thread?.let {
            if (it.isAlive) {
                // In a more complex game, we'd use a message queue,
                // but for now, we can just re-run setup.
                setupGame()
            }
        }
    }

    /**
     * This is the main game loop, now corrected to use GameWorld and HUD.
     */
    override fun run() {
        setupGame()
        var lastTime = System.currentTimeMillis()
        var waitTime: Long

        while (isRunning) {
            if (!holder.surface.isValid) continue

            val startTime = System.currentTimeMillis()
            val deltaTime = startTime - lastTime
            lastTime = startTime

            val canvas = holder.lockCanvas()
            try {
                synchronized(holder) {
                    if (gameWorld != null && hud != null) {
                        update(deltaTime)
                        draw(canvas)
                    }
                }
            } catch (e: Exception) {
                Log.e("GameRenderer", "Exception during game loop", e)
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            val loopTime = System.currentTimeMillis() - startTime
            waitTime = targetTimeMillis - loopTime

            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime)
                } catch (e: InterruptedException) {
                    isRunning = false
                }
            }
        }
    }

    // The bridge from the UI now has more information
    fun onPlayerInput(touchX: Float?, isFiring: Boolean) {
        this.playerTouchX = touchX
        this.isPlayerFiring = isFiring
    }

    private fun update(deltaTimeMs: Long) {

        // Get all the data from the world
        val currentScore = gameWorld?.score ?: 0
        val currentPowerUpQueue = gameWorld?.getPowerUpQueue() ?: emptyList()
        val currentPlayerHp = gameWorld?.playerShip?.hp ?: 0
        val currentPlayerMaxHp = gameWorld?.playerShip?.maxHp ?: 1
        val currentLevelState = gameWorld?.levelManager?.state ?: LevelState.RUNNING
        val currentLevelNumber = gameWorld?.levelManager?.currentLevelNumber ?: 0
        val currentDebugData = gameWorld?.getDebugData() ?: DebugData()

        // Delegate the new, richer input state to the GameWorld
        gameWorld?.update(deltaTimeMs, playerTouchX, isPlayerFiring)

        hud?.update(
            currentScore,
            currentPowerUpQueue,
            currentPlayerHp,
            currentPlayerMaxHp,
            currentLevelState,
            currentLevelNumber,
            currentDebugData)
    }

    private fun draw(canvas: Canvas) {
        // Get fresh, non-null references for this frame
        val currentWorld = gameWorld ?: return
        val currentHud = hud ?: return

        canvas.drawColor(Color.BLACK)

        paint.color = Color.WHITE
        for (star in currentWorld.starfieldManager.starStates) {
            paint.alpha = (star.alpha * 255).toInt()
            canvas.drawPoint(star.x, star.y, paint)
        }

        // Reset paint state to a safe default after custom drawing
        paint.alpha = 255
        paint.color = Color.WHITE

        // 3. Render all other game world objects
        // These objects have their own 'draw' methods that know how to render themselves.
        currentWorld.enemyManager.drawAll(canvas, paint)
        currentWorld.playerShip.draw(canvas, paint)
        currentWorld.effectManager.drawAll(canvas, paint)

        // --- RENDER THE HUD ---
        // The HUD is drawn last so it is on top of everything.
        currentHud.draw(canvas)
    }

    fun resume() {
        if (thread == null || !thread!!.isAlive) {
            isRunning = true
            thread = Thread(this)
            thread?.start()
            Log.d("GameRenderer", "Thread resumed.")
        }
    }

    fun pause() {
        Log.d("GameRenderer", "Attempting to pause thread.")
        isRunning = false
        try {
            thread?.join(200)
        } catch (e: InterruptedException) {
            // Do nothing
        }
        thread = null
    }

    // This is called from GameScreen's onDoubleTap
    fun onActivatePowerUp() {
        // Pass the command to the game world's player ship
        gameWorld?.playerShip?.activateNextPowerUp()
    }
}