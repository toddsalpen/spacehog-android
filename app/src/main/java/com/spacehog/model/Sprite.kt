package com.spacehog.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.withRotation
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

/**
 * A high-performance, stateful game object for rendering and animating sprites from a sprite sheet.
 *
 * This class is designed for a standard game loop (`update` then `draw`).
 * - State changes (like advancing animation frames) happen *only* in the `update` method.
 * - The `draw` method is "dumb" and only renders the current state to the canvas.
 * - Frame creation from the master sheet is an expensive operation and should only be
 *   done during a loading phase, not during active gameplay.
 */
open class Sprite(
    var masterBitmap: Bitmap,
    width: Float,
    height: Float,
    x: Float,
    y: Float
) : GameObject(width, height, x, y) {

    /** Defines the current playback state of the sprite's animation. */
    enum class AnimationState {
        PLAYING, PAUSED, FINISHED
    }

    // --- Animation Properties ---
    var frameDelayMs: Long = 33L // Default to ~30 FPS (1000ms / 30)
    var loops: Boolean = false
    var state: AnimationState = AnimationState.PAUSED
        private set

    private val frames = mutableListOf<Bitmap>()
    private var currentFrameIndex = 0
    private var frameTimer = 0L

    init {
        // A Sprite should be drawable immediately after creation.
        // This creates a single default frame from the entire master bitmap.
        // This is the crucial step we were missing.
        addFrameFromMaster(0, 0, masterBitmap.width, masterBitmap.height)
    }
    /**
     * Updates the animation state based on the elapsed time.
     * This should be called once per game loop.
     *
     * @param deltaTimeMs The time in milliseconds since the last update.
     */
    open fun update(deltaTimeMs: Long) {
        if (state != AnimationState.PLAYING) return

        frameTimer += deltaTimeMs
        if (frameTimer >= frameDelayMs) {
            frameTimer -= frameDelayMs
            currentFrameIndex++ // Just let the index grow indefinitely

            // We only check for the end of the animation if it's NOT looping.
            if (!loops && currentFrameIndex >= frames.size) {
                state = AnimationState.FINISHED
            }
        }
    }

    // --- Drawing Methods ---

    // This draw method is perfect. No changes needed.
    open fun draw(canvas: Canvas, paint: Paint) {
        if (frames.isEmpty()) return

        val safeIndex = if (state == AnimationState.FINISHED) {
            frames.size - 1
        } else {
            currentFrameIndex % frames.size
        }

        // It's still a good defensive practice for a Sprite to ensure it's opaque.
        paint.alpha = 255
        canvas.drawBitmap(frames[safeIndex], x, y, paint)
    }

    /** Draws the sprite's current frame, rotating it around its center. */
    open fun draw(canvas: Canvas, paint: Paint, rotationDegrees: Float) {
        if (frames.isEmpty()) return

        val safeIndex = if (state == AnimationState.FINISHED) {
            frames.size - 1
        } else {
            currentFrameIndex % frames.size
        }

        paint.alpha = 255
        // Use the KTX extension function for safe, clean save/rotate/restore.
        canvas.withRotation(
            degrees = rotationDegrees,
            pivotX = x + width / 2f,
            pivotY = y + height / 2f
        ) {
            drawBitmap(frames[safeIndex], x, y, paint)
        }
    }

    fun clearFrames() {
        frames.clear()
    }

    // --- Animation Control ---

    fun play() {
        if (frames.isNotEmpty()) {
            state = AnimationState.PLAYING
        }
    }

    fun pause() {
        state = AnimationState.PAUSED
    }

    fun resetAnimation() {
        currentFrameIndex = 0
        frameTimer = 0L
        state = if (frames.isNotEmpty()) AnimationState.PAUSED else AnimationState.FINISHED
    }

    // --- Frame Creation ---

    /**
     * Creates a single animation frame from a section of the master bitmap.
     * WARNING: This is a memory-intensive operation. Only call during loading.
     */
    fun addFrameFromMaster(xPos: Int, yPos: Int, frameWidth: Int, frameHeight: Int) {
        frames.add(createFrameFromSheet(xPos, yPos, frameWidth, frameHeight))
    }

    /**
     * Creates multiple animation frames from a horizontal strip on the master bitmap.
     * WARNING: This is a memory-intensive operation. Only call during loading.
     */
    fun addFramesFromMaster(startFrameX: Int, yPos: Int, frameWidth: Int, frameHeight: Int, frameCount: Int) {
        for (i in 0 until frameCount) {
            addFrameFromMaster(startFrameX + i, yPos, frameWidth, frameHeight)
        }
    }

    /** The core logic for cutting a single frame from the sprite sheet and scaling it. */
    private fun createFrameFromSheet(xPos: Int, yPos: Int, frameWidth: Int, frameHeight: Int): Bitmap {
        // Define the source rectangle on the master sheet (in pixels)
        val srcRect = Rect(
            xPos * frameWidth,
            yPos * frameHeight,
            (xPos * frameWidth) + frameWidth,
            (yPos * frameHeight) + frameHeight
        )

        // Create a new bitmap for this frame
        val frameBitmap = createBitmap(frameWidth, frameHeight)
        val canvas = Canvas(frameBitmap)

        // Define the destination rectangle (the entire new bitmap)
        val dstRect = Rect(0, 0, frameWidth, frameHeight)

        // Copy the section from the master sheet to the new frame bitmap
        canvas.drawBitmap(masterBitmap, srcRect, dstRect, null)

        // Return a final bitmap scaled to the GameObject's dimensions
        return frameBitmap.scale(width.toInt(), height.toInt(), false)
    }
}