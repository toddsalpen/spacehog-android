package com.spacehog.model

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

/**
 * Manages the creation, updating, and drawing of a parallax star field effect.
 *
 * This class is optimized for high-performance rendering loops by avoiding
 * any object allocations in its `update` and `draw` methods.
 */
class StarField(width: Float, height: Float, x: Float, y: Float) : GameObject(width, height, x, y) {

    // Use a more specific, idiomatic Kotlin type.
    private val stars: MutableList<Star> = mutableListOf()
    private val scaledWidth: Float
    private val scaledHeight: Float

    // Constants are cleaner in a companion object.
    companion object {
        private const val STARS_PER_LAYER = 50
        private const val LAYER_COUNT = 3
        private const val MAX_STAR_SPEED = 25
    }

    init {
        // Initializer block for constructor logic.
        val starWidth = 20f
        val starHeight = 20f
        val scale = 0.1f
        val unitWidth = width / starWidth
        val unitHeight = height / starHeight
        scaledWidth = unitWidth * scale
        scaledHeight = unitHeight * scale
    }

    /**
     * Populates the star field with a new set of randomly placed stars.
     */
    fun generateNewStars() {
        stars.clear() // Ensure we start with a fresh list
        // 'repeat' is a more expressive way to write a simple for-loop.
        repeat(LAYER_COUNT) { layer ->
            repeat(STARS_PER_LAYER) {
                stars.add(
                    Star(
                        width = scaledWidth,
                        height = scaledHeight,
                        // Place stars randomly within the screen bounds
                        x = Random.nextInt(width.toInt()).toFloat(),
                        y = Random.nextInt(height.toInt()).toFloat(),
                        vx = 0f,
                        // Speed is based on the layer for a parallax effect
                        vy = Random.nextInt(1, MAX_STAR_SPEED).toFloat() * (layer + 1) * 0.5f,
                        layer = layer
                    )
                )
            }
        }
    }

    /**
     * Updates the position of every star for the next frame.
     * This method is highly optimized to avoid generating garbage.
     */
    fun update() {
        // A single loop is much more efficient.
        for (star in stars) {
            star.update()
            // Check if the star has moved off-screen or become inactive.
            if (star.y > height || !star.active) {
                // Instead of adding to a list, we reset the star immediately.
                // This is the key performance improvement.
                star.resetStar(width, height)
            }
        }
    }

    /**
     * Draws every star onto the provided canvas.
     */
    fun draw(p: Paint, c: Canvas) {
        p.color = Color.GRAY
        for (star in stars) {
            star.draw(p, c)
        }
    }
}