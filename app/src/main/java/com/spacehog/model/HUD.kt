package com.spacehog.ui.game

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.spacehog.data.AssetLibrary
import com.spacehog.data.GameAsset
import com.spacehog.logic.DebugData
import com.spacehog.model.LevelState
import com.spacehog.model.PowerUpType
import kotlin.math.max

class HUD(
    private val screenHeight: Float,
    private val screenWidth: Float,
    private val assetLibrary: AssetLibrary
) {
    // --- Paints (The "Brushes" for our UI) ---

    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    private val livesTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    private val hpTextPaint = Paint().apply {
        color = Color.BLACK // Black for good contrast on green/red
        textSize = 40f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.RIGHT // Aligned to the right
    }

    private val healthBarPaint = Paint().apply { color = Color.GREEN }
    private val healthBarBackgroundPaint = Paint().apply { color = Color.RED }

    private val messagePaint = Paint().apply {
        color = Color.WHITE
        textSize = 100f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val debugPaint = Paint().apply {
        color = Color.CYAN
        textSize = 32f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textAlign = Paint.Align.LEFT
    }

    // --- Bitmaps (Pre-fetched assets for performance) ---
    private val playerShipIcon: Bitmap = assetLibrary.getBitmap(GameAsset.PLAYER_SHIP)

    // --- State Properties (The data to be drawn) ---
    private var score = 0
    private var playerHp = 0
    private var playerMaxHp = 1
    private var playerLives = 0
    private var powerUpQueue = listOf<PowerUpType>()
    private var currentLevelState: LevelState = LevelState.RUNNING
    private var currentLevelNumber: Int = 0
    private var debugData: DebugData = DebugData()
    var isDebugMode: Boolean = true // Set to false to hide debug info in final game

    /**
     * Updates the HUD's state with the latest data from the game world.
     * This is called once per frame by the GameRenderer.
     */
    fun update(
        newScore: Int,
        newPowerUpQueue: List<PowerUpType>,
        newPlayerHp: Int,
        newPlayerMaxHp: Int,
        newLevelState: LevelState,
        newLevelNumber: Int,
        newDebugData: DebugData
    ) {
        this.score = newScore
        this.powerUpQueue = newPowerUpQueue
        this.playerHp = newPlayerHp
        this.playerMaxHp = newPlayerMaxHp
        this.currentLevelState = newLevelState
        this.currentLevelNumber = newLevelNumber
        this.debugData = newDebugData
        this.playerLives = newDebugData.playerLives // Get lives from debug data for consistency
    }

    /**
     * Draws the entire HUD onto the canvas.
     * This is called once per frame by the GameRenderer.
     */
    /**
     * The main draw entry point. It calls all the individual component draw methods.
     * This makes the drawing order explicit and easy to change.
     */
    fun draw(canvas: Canvas) {
        // Draw in logical order (bottom layers first)
        drawScoreCounter(canvas)
        drawLivesCounter(canvas)
        drawHealthBar(canvas)
        drawPowerUpQueue(canvas)
        drawLevelMessages(canvas)

        if (isDebugMode) {
            drawDebugInfo(canvas)
        }
    }

    // --- NEW HELPER METHODS FOR EACH COMPONENT ---

    private fun drawScoreCounter(canvas: Canvas) {
        val scoreText = String.format("%010d", score)
        val xPos = 50f
        val yPos = 80f
        canvas.drawText(scoreText, xPos, yPos, scorePaint)
    }

    private fun drawLivesCounter(canvas: Canvas) {
        val scoreY = 80f // Anchor below the score
        val livesTopY = scoreY + 30f
        val iconSize = 50f
        val textPadding = 15f
        val iconX = 50f

        val iconRect = RectF(iconX, livesTopY, iconX + iconSize, livesTopY + iconSize)
        canvas.drawBitmap(playerShipIcon, null, iconRect, null)

        val livesText = "x $playerLives"
        val textY = livesTopY + (iconSize / 2f) + (livesTextPaint.textSize / 3f)
        canvas.drawText(livesText, iconRect.right + textPadding, textY, livesTextPaint)
    }

    private fun drawHealthBar(canvas: Canvas) {
        // --- Layout Constants ---
        val barHeight = scorePaint.textSize
        val cornerRadius = barHeight / 2f
        val topMargin = (80f - barHeight / 2f) + (scorePaint.descent() + scorePaint.ascent()) / 2f
        val barMargin = 20f

        val totalBarWidth = screenWidth * 0.66f // Let's make it 50% of the screen width
        val barRight = screenWidth - barMargin
        val barLeft = barRight - totalBarWidth

        // --- Health Percentage Calculation ---
        val healthPercentage = if (playerMaxHp > 0) max(0f, playerHp.toFloat() / playerMaxHp.toFloat()) else 0f
        val currentHealthWidth = totalBarWidth * healthPercentage

        // Draw the red background first with rounded corners
        canvas.drawRoundRect(barLeft, topMargin, barRight, topMargin + barHeight, cornerRadius, cornerRadius, healthBarBackgroundPaint)

        // Draw the green foreground, depleting from RIGHT to LEFT
        if (currentHealthWidth > 0) {
            val greenBarLeft = barRight - currentHealthWidth
            canvas.drawRoundRect(greenBarLeft, topMargin, barRight, topMargin + barHeight, cornerRadius, cornerRadius, healthBarPaint)
        }

        // Draw the HP Text inside the bar
        val hpText = "$playerHp HP"
        val hpTextX = barRight - barMargin
        val hpTextY = topMargin + (barHeight / 2f) - (hpTextPaint.descent() + hpTextPaint.ascent()) / 2f
        canvas.drawText(hpText, hpTextX, hpTextY, hpTextPaint)
    }

    private fun drawPowerUpQueue(canvas: Canvas) {
        val iconSize = 80f
        val padding = 20f
        var startX = padding

        for (powerUp in powerUpQueue) {
            val iconBitmap = assetLibrary.getBitmap(powerUp.hudAsset)
            val iconRect = RectF(startX, screenHeight - iconSize - padding, startX + iconSize, screenHeight - padding)
            canvas.drawBitmap(iconBitmap, null, iconRect, null)
            startX += iconSize + padding
        }
    }

    private fun drawLevelMessages(canvas: Canvas) {
        when (currentLevelState) {
            LevelState.INTERMISSION -> {
                val message = "Level $currentLevelNumber Complete!"
                canvas.drawText(message, screenWidth / 2f, screenHeight / 2f, messagePaint)
            }
            LevelState.CAMPAIGN_COMPLETE -> {
                canvas.drawText("YOU WIN!", screenWidth / 2f, screenHeight / 2f, messagePaint)
            }
            else -> { /* Do nothing in RUNNING state */ }
        }
    }

    private fun drawDebugInfo(canvas: Canvas) {
        val startY = 300f
        val lineHeight = 40f

        canvas.drawText("--- DEBUG INFO ---", 50f, startY, debugPaint)
        canvas.drawText("Level: ${debugData.levelNumber} [${debugData.levelState}]", 50f, startY + lineHeight * 1, debugPaint)
        canvas.drawText("Enemies Spawned: ${debugData.totalEnemiesSpawned}", 50f, startY + lineHeight * 2, debugPaint)
        canvas.drawText("Enemies Defeated: ${debugData.enemiesDefeatedInLevel}", 50f, startY + lineHeight * 3, debugPaint)
        canvas.drawText("Active Enemies: ${debugData.activeEnemiesOnScreen}", 50f, startY + lineHeight * 4, debugPaint)
        canvas.drawText("Player HP: ${debugData.playerHP}", 50f, startY + lineHeight * 5, debugPaint)
        canvas.drawText("Player Lives: ${debugData.playerLives}", 50f, startY + lineHeight * 6, debugPaint)
    }
}