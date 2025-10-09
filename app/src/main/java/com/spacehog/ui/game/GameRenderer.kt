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
import com.spacehog.logic.StarfieldManager
import com.spacehog.model.LevelState

class GameRenderer(
    private val context: Context,
    private val holder: SurfaceHolder,
    val starfieldManager: StarfieldManager,
    val onGameOver: () -> Unit
) : Runnable {
    private var thread: Thread? = null
    @Volatile private var isRunning = false
    private val targetFPS = 60
    private val targetTimeMillis = (1000 / targetFPS).toLong()
    private var gameWorld: GameWorld? = null
    private var hud: HUD? = null
    private val paint = Paint()
    @Volatile var safeAreaTop: Float = 0f
    @Volatile private var playerTouchX: Float? = null
    @Volatile private var isPlayerFiring = false

    init {
    }

    private fun setupGame() {
        Log.d("GameRenderer", "Setting up game assets...")
        try {
            val scaler = Scaler(
                holder.surfaceFrame.width().toFloat(),
                holder.surfaceFrame.height().toFloat()
            )
            gameWorld = GameWorld(context, holder, scaler, starfieldManager, onGameOver = onGameOver)
            hud = HUD(
                holder.surfaceFrame.height().toFloat(),
                holder.surfaceFrame.width().toFloat(),
                gameWorld!!.assetLibrary,
                scaler = scaler
            )
            Log.d("GameRenderer", "Game setup complete.")
        } catch (e: IllegalStateException) {
            Log.e("GameRenderer", "Asset loading failed: ${e.message}")
            isRunning = false
        }
    }
    fun restartGame() {
        thread?.let {
            if (it.isAlive) {
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

    fun onPlayerInput(touchX: Float?, isFiring: Boolean) {
        this.playerTouchX = touchX
        this.isPlayerFiring = isFiring
    }

    private fun update(deltaTimeMs: Long) {

        val currentScore = gameWorld?.score ?: 0
        val currentPowerUpQueue = gameWorld?.getPowerUpQueue() ?: emptyList()
        val currentPlayerHp = gameWorld?.playerShip?.hp ?: 0
        val currentPlayerMaxHp = gameWorld?.playerShip?.maxHp ?: 1
        val currentLevelState = gameWorld?.levelManager?.state ?: LevelState.RUNNING
        val currentLevelNumber = gameWorld?.levelManager?.currentLevelNumber ?: 0
        val currentDebugData = gameWorld?.getDebugData() ?: DebugData()

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
        val currentWorld = gameWorld ?: return
        val currentHud = hud ?: return

        canvas.drawColor(Color.BLACK)

        paint.color = Color.WHITE
        for (star in currentWorld.starfieldManager.getStarStates()) {
            paint.alpha = (star.alpha * 255).toInt()
            canvas.drawCircle(star.x, star.y, 2f, paint)
        }

        paint.alpha = 255
        paint.color = Color.WHITE

        currentWorld.enemyManager.drawAll(canvas, paint)
        currentWorld.powerUpManager.drawAll(canvas, paint)
        currentWorld.playerShip.draw(canvas, paint)
        currentWorld.effectManager.drawAll(canvas, paint)

        currentHud.draw(canvas, safeAreaTop)
    }

    fun resume() {
        if (thread == null || !thread!!.isAlive) {
            isRunning = true
            thread = Thread(this)
            thread?.start()
        }
    }

    fun pause() {
        isRunning = false
        try {
            thread?.join(200)
        } catch (e: InterruptedException) {
        }
        thread = null
    }

    fun onActivatePowerUp() {
        gameWorld?.playerShip?.activateNextPowerUp()
    }
}